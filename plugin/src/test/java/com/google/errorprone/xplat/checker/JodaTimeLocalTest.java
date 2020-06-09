package com.google.errorprone.xplat.checker;

import com.google.errorprone.CompilationTestHelper;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link MyCustomCheck}.
 */
@RunWith(JUnit4.class)
public class JodaTimeLocalTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setup() {
    compilationHelper = CompilationTestHelper.newInstance(JodaTimeLocal.class, getClass());
  }

  @Test
  public void customCheckPositiveCases() {
    compilationHelper.addSourceFile("JodaTimeLocalPositiveCases.java").doTest();
  }

  @Test
  public void customCheckNegativeCases() {
    compilationHelper.addSourceFile("JodaTimeLocalNegativeCases.java").doTest();
  }

}

