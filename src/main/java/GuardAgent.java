import configuration.AgentConfig;
import configuration.ConfigLoader;
import javassist.*;
import spoon.Launcher;
import spoon.reflect.declaration.CtType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


public class GuardAgent {

    public static void decompileJar(String jarPath, String outputDirectory) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "C:\\Users\\DELL\\Desktop\\Uni\\Spring-2024\\Thesis\\argument-guard\\vineflower-1.9.3.jar", "-dgs=1", jarPath, outputDirectory);
            Process process = processBuilder.start();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(new StreamGobbler(process.getInputStream(), System.out::println));
            executor.submit(new StreamGobbler(process.getErrorStream(), System.err::println));
            process.waitFor();

            executor.shutdown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        AgentConfig config = ConfigLoader.getConfig();
        System.out.println("Checkpoint #00000 -> Running Decompiler");
        String jarPath = config.getJar();
        String outputDirectory = config.getPath();
        if(config.isDecompile()) {
            Utility.deleteFilesInDirectory(outputDirectory);
            decompileJar(jarPath, outputDirectory);
        }
        System.out.println("Checkpoint #00000 -> Running Transformer");
        Launcher launcher = new Launcher();
        String sourcePath = config.getPath();
        launcher.addInputResource(sourcePath);
        System.out.println(sourcePath);

        launcher.buildModel();
        List<String> classNames = new java.util.ArrayList<>();
        boolean codeInserted = false;

        for (CtType<?> type : launcher.getFactory().Type().getAll()) {
            System.out.println(type.getQualifiedName());
            classNames.add(type.getQualifiedName());
        }
        System.out.println(classNames.size());
        long numberOfPatterns = Utility.countLine(config.getExcludes());

        for (String className : classNames) {
            codeInserted = false;
            if (className.startsWith(config.getPkg()) && !className.contains("Test")) {
                System.out.println(className);
                try {
                    ClassPool cp = ClassPool.getDefault();
                    CtClass cc = cp.get(className);
                    if (!cc.isInterface() && !cc.getURL().getPath().contains("test")) {
                        // List all methods
                        CtMethod[] methods = cc.getDeclaredMethods();
                        CtField arrayField = new CtField(cp.get("java.lang.String[]"), "linesArray", cc);
                        arrayField.setModifiers(Modifier.PUBLIC | Modifier.STATIC); // Set modifiers to public and static
                        cc.addField(arrayField);
                        cc.debugWriteFile();

                        CtField longField = new CtField(cp.get("long"), "lastUpdated", cc);
                        longField.setModifiers(Modifier.PUBLIC | Modifier.STATIC); // Set modifiers to public and static
                        cc.addField(longField);
                        cc.debugWriteFile();


                        for (CtMethod method : methods) {
                            java.util.List<Integer> stringIndexes = new java.util.ArrayList<>();
                            CtClass[] paramTypes = method.getParameterTypes();
                            for (int i = 0; i < paramTypes.length; i++) {
                                if (paramTypes[i].getName().equals("java.lang.String") || !(paramTypes[i] instanceof CtPrimitiveType)) {
                                    System.out.println(className);
                                    System.out.println(">>>>>>>>>>>>>>>>Method name:" + method.getName());
                                    System.out.println("Found string parameter at index: " + i);
                                    stringIndexes.add(i);
                                }
                            }
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("{boolean drop = false;");
                            stringBuilder.append("boolean removeDetectedPattern = " + config.getRemoveDetectedPattern() + ";");
                            stringBuilder.append("String path = \"" + config.getExcludes() + "\";");
                            stringBuilder.append("System.out.println(path);");
                            stringBuilder.append("try {");
                            stringBuilder.append("java.io.File file = new java.io.File(path);");
                            stringBuilder.append("long lastModified = file.lastModified();");
                            stringBuilder.append("if(lastModified > lastUpdated){");
                            stringBuilder.append("linesArray = new java.lang.String[" + numberOfPatterns + "];");
                            stringBuilder.append("System.out.println(\">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Opening File:\" + Long.toString(lastModified) +\"-\"+  Long.toString(lastUpdated));");
                            stringBuilder.append("java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(path));");
                            stringBuilder.append("for (int i = 0; i < linesArray.length; i++) {");
                            stringBuilder.append("linesArray[i] = br.readLine();");
                            stringBuilder.append("}");
                            stringBuilder.append("br.close();");
                            stringBuilder.append("lastUpdated = System.currentTimeMillis();");
                            stringBuilder.append("}");
                            stringBuilder.append("for(int i = 0; i < $args.length; i++) {");
                            stringBuilder.append("if($args[i] instanceof String || !$args[i].getClass().isPrimitive()){");
                            stringBuilder.append("System.out.println(linesArray.length + \"len\");");
                            stringBuilder.append("for(int j = 0; j < (linesArray.length - 1) && !drop; j++) {");
                            stringBuilder.append("if (String.valueOf($args[i].toString()).contains(linesArray[j])){");
                            stringBuilder.append("if (removeDetectedPattern){");
                            stringBuilder.append("Object[] copiedArgs =  java.util.Arrays.copyOf($args, $args.length);");
                            stringBuilder.append("copiedArgs[i] = String.valueOf($args[i]).replace(linesArray[j],\"\");");
                            stringBuilder.append("$args =  java.util.Arrays.copyOf(copiedArgs, copiedArgs.length);");
                            stringBuilder.append("System.out.println(\"Removveddddd:\" + String.valueOf($args[i]));}");
                            stringBuilder.append("else { drop = true; System.out.println(\"Drrrrrropped\");}");
                            stringBuilder.append("}");
                            stringBuilder.append("}");
                            stringBuilder.append("}");
                            stringBuilder.append("}");
                            stringBuilder.append("} catch (java.io.IOException e) { System.out.println(\"Exception ...\"); e.printStackTrace(); } ");
                            stringBuilder.append("if(drop) throw new RuntimeException(\"A malicious value detected\");");
                            stringBuilder.append("}");

                            if (!stringIndexes.isEmpty()) {
                                System.out.println("Code : " + stringBuilder);
                                System.out.println("Code Being inserted");
                                method.insertBefore(stringBuilder.toString());
                                System.out.println("Before writing ........");
                                cc.debugWriteFile();
                                codeInserted = true;
                            } else {
                                System.out.println("No String argument found");
                            }
                        }
                        if (codeInserted) {
                            cc.writeFile();
                            cc.toClass();
                        }
                    }
                    System.out.println("Checkpoint #00002");
                } catch (Exception e) {
                    System.out.println("Exception #00001: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }


    static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }
}
