/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
