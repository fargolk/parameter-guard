import configuration.AgentConfig;
import configuration.ConfigLoader;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class DecompileJar {
    private boolean isDecompiled = false;

    public synchronized void decompileJar() {
        // Simulate decompiling a JAR file
        System.out.println("Decompiling JAR file...");
        AgentConfig config = ConfigLoader.getConfig();
        System.out.println("Checkpoint #00000 -> Running Decompiler");
        String outputDirectory = config.getPath();
        Utility.deleteFilesInDirectory(outputDirectory);

        String jarPath = config.getJar();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar","-Xmx2g", "C:\\Users\\DELL\\Desktop\\Uni\\Spring-2024\\Thesis\\argument-guard\\vineflower-1.9.3.jar", "-dgs=1", jarPath, outputDirectory);
            Process process = processBuilder.start();
            // Wait for the process to complete
            boolean exitCode = process.waitFor(5, TimeUnit.SECONDS );
                System.out.println("... Wait done ...:" + exitCode);
                if (exitCode) {
                    System.out.println("Decompilation completed successfully.");
                } else {
                    System.err.println("Decompilation failed with exit code: " + false);
                }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        isDecompiled = true;
        notify(); // Notify waiting threads that decompilation is complete
    }

    public synchronized void waitForDecompilation() {
        while (!isDecompiled) {
            try {
                wait(); // Wait until decompilation is complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Decompilation complete. Proceeding with next steps...");
    }
}
