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

import com.google.common.primitives.Ints;

/**
 * This application compares two numbers, using the Ints.compare
 * method from Guava.
 */
public class App {

    public static int compare(int a, int b) {
        return Ints.compare(a, b);
    }

    public static void main(String... args) throws Exception {
        App app = new App();
        System.out.println("Success: " + app.compare(2, 1));
    }

}
