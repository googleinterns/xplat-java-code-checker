package com.google.errorprone.xplat.checker;

import com.google.common.base.Predicates;
import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link MyCustomCheck}.
 */
@RunWith(JUnit4.class)
public class JodaTimeClassBanTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setup() {
    compilationHelper = CompilationTestHelper.newInstance(JodaTimeClassBan.class, getClass());
  }

  @Test
  public void customCheckPositiveCases() {
    compilationHelper.addSourceFile("JodaTimeClassBanPositiveCases.java").doTest();
  }

  @Test
  public void customCheckNegativeCases() {
    compilationHelper.addSourceFile("JodaTimeClassBanNegativeCases.java").doTest();
  }

}

