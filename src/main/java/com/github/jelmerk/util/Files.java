package com.github.jelmerk.util;

import java.io.*;

/**
 * @author Jelmer Kuperus
 */
public class Files {

    private static final int BUFFER_SIZE = 4096;

    private Files() {
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
