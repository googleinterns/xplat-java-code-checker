// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.errorprone.xplat.checker;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link UnnecessaryConcurrentHashMap}.
 */
@RunWith(JUnit4.class)
public class UnnecessaryConcurrentHashMapTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setup() {
    compilationHelper = CompilationTestHelper
        .newInstance(UnnecessaryConcurrentHashMap.class, getClass());
  }

  @Test
  public void positiveCases() {
    compilationHelper.addSourceFile("UnnecessaryConcurrentHashMapTestPositive.java").doTest();
  }

  @Test
  public void negativeCases() {
    compilationHelper.addSourceFile("UnnecessaryConcurrentHashMapTestNegative.java").doTest();
  }

  @Test
  public void refactorSameLine() {
    BugCheckerRefactoringTestHelper.newInstance(new UnnecessaryConcurrentHashMap(), getClass())
        .addInputLines("Test.java",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import java.util.Collections;",
            "import java.util.HashMap;",
            "import java.util.Map;",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void refactorSameLineMap() {
    BugCheckerRefactoringTestHelper.newInstance(new UnnecessaryConcurrentHashMap(), getClass())
        .addInputLines("Test.java",
            "import java.util.concurrent.ConcurrentHashMap;",
            "import java.util.Map;",
            "class Test {",
            "  private void test() {",
            "    Map<String, Integer> map = new ConcurrentHashMap<>();",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import java.util.Collections;",
            "import java.util.HashMap;",
            "import java.util.Map;",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void refactorSameLineConcurrentMap() {
    BugCheckerRefactoringTestHelper.newInstance(new UnnecessaryConcurrentHashMap(), getClass())
        .addInputLines("Test.java",
            "import java.util.concurrent.ConcurrentMap;",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    ConcurrentMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import java.util.Collections;",
            "import java.util.HashMap;",
            "import java.util.Map;",
            "import java.util.concurrent.ConcurrentHashMap;",
            "import java.util.concurrent.ConcurrentMap;",
            "class Test {",
            "  private void test() {",
            "    Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void refactorDeclaration() {
    BugCheckerRefactoringTestHelper.newInstance(new UnnecessaryConcurrentHashMap(), getClass())
        .addInputLines("Test.java",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    ConcurrentHashMap<String, Integer> map;",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import java.util.Map;",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    Map<String, Integer> map;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void refactor2Lines() {
    BugCheckerRefactoringTestHelper.newInstance(new UnnecessaryConcurrentHashMap(), getClass())
        .addInputLines("Test.java",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    ConcurrentHashMap<String, Integer> map;",
            "    int x = 1;",
            "    map = new ConcurrentHashMap<>();",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import java.util.Collections;",
            "import java.util.HashMap;",
            "import java.util.Map;",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    Map<String, Integer> map;",
            "    int x = 1;",
            "    map = Collections.synchronizedMap(new HashMap<>());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void refactorIncompatibleInterface() {
    BugCheckerRefactoringTestHelper.newInstance(new UnnecessaryConcurrentHashMap(), getClass())
        .addInputLines("Test.java",
            "import java.util.concurrent.ConcurrentMap;",
            "import java.util.concurrent.ConcurrentHashMap;",
            "class Test {",
            "  private void test() {",
            "    ConcurrentMap<String, Integer> map;",
            "    int x = 1;",
            "    map = new ConcurrentHashMap<>();",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import java.util.Collections;",
            "import java.util.HashMap;",
            "import java.util.Map;",
            "import java.util.concurrent.ConcurrentHashMap;",
            "import java.util.concurrent.ConcurrentMap;",
            "class Test {",
            "  private void test() {",
            "    Map<String, Integer> map;",
            "    int x = 1;",
            "    map = Collections.synchronizedMap(new HashMap<>());",
            "  }",
            "}")
        .doTest();
  }

}