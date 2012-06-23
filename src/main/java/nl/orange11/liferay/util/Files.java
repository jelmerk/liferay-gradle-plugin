package nl.orange11.liferay.util;

import java.io.*;

/**
 * @author Jelmer Kuperus
 */
public class Files {

    private static final int BUFFER_SIZE = 4096;
    private static final int TEMP_DIR_ATTEMPTS = 10000;

    private Files() {
    }

    public static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within "
                + TEMP_DIR_ATTEMPTS + " attempts (tried "
                + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }

    public static void deleteRecursively(File file) {

        if (file.isDirectory()) {
            File[] nestedFiles = file.listFiles();
            for (File nestedFile : nestedFiles) {
                deleteRecursively(nestedFile);
            }
        }

        if (!file.delete()) {
            throw new IllegalStateException("Failed to delete file " + file);
        }
    }


    public static void copy(File from, File to) throws IOException {
        if (from == null) {
            throw new IllegalArgumentException("From cannot be null.");
        }

        if (to == null) {
            throw new IllegalArgumentException("To cannot be null.");
        }

        InputStream in = new FileInputStream(from);
        OutputStream out = new FileOutputStream(to);

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
            try {
                out.close();
            } catch (IOException ex) {
            }
        }
    }
}
