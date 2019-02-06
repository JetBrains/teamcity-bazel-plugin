package com.example.myproject;

import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for correct dependency retrieval with maven rules.
 */
public class TestApp {

    @Test
    public void testSuccess() throws Exception {
        App app = new App();
        assertEquals("should return 0 when both numbers are equal", 0, app.compare(1, 1));
    }
}
