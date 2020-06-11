package com.google.errorprone.xplat.checker;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
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
  public void positiveCases() {
    compilationHelper.addSourceFile("JodaTimeLocalPositiveCases.java").doTest();
  }

  @Test
  public void negativeCases() {
    compilationHelper.addSourceFile("JodaTimeLocalNegativeCases.java").doTest();
  }

  @Test
  public void refactor() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDateTime;",
            "class Test {",
            "  private DateTime badLocalDateTimeUse() {",
            "  LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);",
            "  return ldt.toDateTime(DateTimeZone.forID(\"America / New_York\"))",
            "    .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDateTime;",
            "class Test {",
            "  private DateTime badLocalDateTimeUse() {",
            "  LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);",
            "return new DateTime(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfYear(), ldt.getHourOfDay(),"
                + " ldt.getMinuteOfHour(), ldt.getSecondOfMinute(), ldt.getMillisOfSecond(),"
                + " DateTimeZone.forID(\"America / New_York\"))",
            "    .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();
  }

}

