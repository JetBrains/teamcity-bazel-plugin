package com.example.myproject;

import org.junit.*;
import java.io.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for correct dependency retrieval with maven rules.
 */
public class TestApp {

    @Test
    public void testCompare() throws Exception {
        App app = new App();
        assertEquals("should return 0 when both numbers are equal", 0, app.compare(1, 1));
    }

    @Test
    public void testFlaky() throws Exception {
        File tempFile = getTempFile();
        if (tempFile.exists()) {
            System.out.println(tempFile + " exists");
            tempFile.delete();
        } else {
            System.out.println(tempFile + " does not exist");
            tempFile.createNewFile();
            assertEquals("flaky 2 test", 0, 1);
        }
    }

    private static File getTempFile() throws Exception {
        File tempDir;
        if (System.getProperty("os.name").startsWith("Windows")) {
            tempDir = new File(System.getenv("TEMP"));
        } else {
            tempDir = new File("/tmp");
        }

        System.out.println("tempDir: " + tempDir);
        File tempFile = new File(tempDir, "testFlaky_" + System.getenv("test_id")).getCanonicalFile();
        System.out.println("File: " + tempFile);
        return tempFile;
    }
}
