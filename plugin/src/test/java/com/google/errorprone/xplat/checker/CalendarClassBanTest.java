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
public class CalendarClassBanTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setup() {
    compilationHelper = CompilationTestHelper.newInstance(CalendarClassBan.class, getClass());
  }

  @Test
  public void customCheckPositiveCases() {
    compilationHelper.addSourceFile("CalendarClassBanPositiveCases.java").doTest();
  }

  @Test
  public void customCheckNegativeCases() {
    compilationHelper.addSourceFile("CalendarClassBanNegativeCases.java").doTest();
  }

}