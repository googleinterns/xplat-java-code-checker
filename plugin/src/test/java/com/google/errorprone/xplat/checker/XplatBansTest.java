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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.CompilationTestHelper;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link XplatBans}.
 */
@RunWith(JUnit4.class)
public class XplatBansTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setup() {
    compilationHelper = CompilationTestHelper.newInstance(XplatBans.class, getClass());
  }

  @Test
  public void positiveCases() {
    compilationHelper.addSourceFile("XplatBansPositiveCases.java").doTest();
  }

  @Test
  public void negativeCases() {
    compilationHelper.addSourceFile("XplatBansNegativeCases.java").doTest();
  }

  @Test
  public void positiveCustomCases() {
    File file = new File(
        "src/test/resources/com/google/errorprone/xplat/checker/testdata/XplatCustomBansTest.json");
    String path = file.getAbsolutePath();

    compilationHelper.addSourceFile("XplatBansCustomPositiveCases.java")
        .setArgs(ImmutableList
            .of(String.format("-XepOpt:XplatBans:JSON=%s", path)))
        .doTest();
  }

  @Test
  public void negativeCustomCases() {
    File file = new File(
        "src/test/resources/com/google/errorprone/xplat/checker/testdata/XplatCustomBansTest.json");
    String path = file.getAbsolutePath();

    compilationHelper.addSourceFile("XplatBansCustomNegativeCases.java")
        .setArgs(ImmutableList
            .of(String.format("-XepOpt:XplatBans:JSON=%s", path)))
        .doTest();
  }

}