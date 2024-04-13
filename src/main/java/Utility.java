import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Utility {

    public static long countLine(String fileName) {
        long lines =  0;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while (reader.readLine() != null) lines++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }


    static void deleteFilesInDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFilesInDirectory(file.getAbsolutePath());
                    }
                    file.delete();
                }
            }
        }
    }


    public static boolean isEmpty(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return false; // Return false if the path does not exist or is not a directory
        }
        String[] files = directory.list();
        return files == null || files.length == 0;
    }
}

