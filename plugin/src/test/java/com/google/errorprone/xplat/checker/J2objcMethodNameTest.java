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


import com.google.j2objc.annotations.ObjectiveCName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link MyCustomCheck}.
 */
@RunWith(JUnit4.class)
public class J2objcMethodNameTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setup() {
    compilationHelper = CompilationTestHelper.newInstance(J2objcMethodName.class, getClass());
  }

  @Test
  public void positiveCases() {
    compilationHelper.addSourceFile("J2objcMethodNamePositiveCases.java")
//        .addSourceLines("package-info.java",
//            "@ObjectiveCName(\"XPT\")",
//            "package com.google.errorprone.xplat.checker.testdata;",
//            "import com.google.j2objc.annotations.ObjectiveCName;")
        .doTest();
  }

  @Test
  public void negativeCases() {
    compilationHelper.addSourceFile("J2objcMethodNameNegativeCases.java").doTest();
  }

  @Test
  public void refactorLongMethod() {
    BugCheckerRefactoringTestHelper.newInstance(new J2objcMethodName(), getClass())
        .addInputLines("Test.java",
            "import java.util.HashMap;",
            "import java.util.Set;",
            "class Test {",
            "  private HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x,\n"
                + "      HashMap<Object, Set<String>> y,\n"
                + "      HashMap<Object, Set<String>> z, HashMap<Object, Set<String>> q,\n"
                + "      HashMap<Object, Set<String>> r,\n"
                + "      HashMap<Object, Set<String>> s, HashMap<Object, Set<String>> t,\n"
                + "      HashMap<Object, Set<String>> u, HashMap<Object, Set<String>> v) {\n",
            "    return x;",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import com.google.j2objc.annotations.ObjectiveCName;",
            "import java.util.HashMap;",
            "import java.util.Set;",
            "class Test {",
            "  @ObjectiveCName(\"hello\")",
            "  private HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x,\n"
                + "      HashMap<Object, Set<String>> y,\n"
                + "      HashMap<Object, Set<String>> z, HashMap<Object, Set<String>> q,\n"
                + "      HashMap<Object, Set<String>> r,\n"
                + "      HashMap<Object, Set<String>> s, HashMap<Object, Set<String>> t,\n"
                + "      HashMap<Object, Set<String>> u, HashMap<Object, Set<String>> v) {\n",
            "    return x;",
            "  }",
            "}")
        .doTest();
  }

}
