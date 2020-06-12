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

  // LocalDateTime -> DateTime with DateTimeZone
  @Test
  public void refactorLocalDateTime() {
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
            "return new DateTime(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfYear(),"
                + " ldt.getHourOfDay(),"
                + " ldt.getMinuteOfHour(), ldt.getSecondOfMinute(), ldt.getMillisOfSecond(),"
                + " DateTimeZone.forID(\"America / New_York\"))",
            "    .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();
  }

  // LocalDateTime -> DateTime without DateTimeZone
  @Test
  public void refactorLocalDateTime2() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDateTime;",
            "class Test {",
            "  private DateTime badLocalDateTimeUse() {",
            "    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);",
            "    return ldt.toDateTime()",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDateTime;",
            "class Test {",
            "  private DateTime badLocalDateTimeUse() {",
            "    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);",
            "    return new DateTime(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfYear(),"
                + " ldt.getHourOfDay(),"
                + " ldt.getMinuteOfHour(), ldt.getSecondOfMinute(), ldt.getMillisOfSecond(),"
                + " DateTimeZone.getDefault())",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();
  }

  // LocalTime -> DateTime with DateTimeZone
  @Test
  public void refactorLocalTime() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalTimeUse() {",
            "    LocalTime lt = new LocalTime(8, 0, 0, 0);",
            "    return lt.toDateTimeToday(DateTimeZone.forID(\"America / New_York\"))",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalTimeUse() {",
            "    LocalTime lt = new LocalTime(8, 0, 0, 0);",
            "    return new DateTime().now(DateTimeZone.forID(\"America / New_York\"))"
                + ".withTime(lt)",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();
  }

  // LocalTime -> DateTime without DateTimeZone
  @Test
  public void refactorLocalTime2() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalTimeUse() {",
            "    LocalTime lt = new LocalTime(8, 0, 0, 0);",
            "    return lt.toDateTimeToday()",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalTimeUse() {",
            "    LocalTime lt = new LocalTime(8, 0, 0, 0);",
            "    return new DateTime().now(DateTimeZone.getDefault()).withTime(lt)",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();
  }

  // Testing to make sure import statement gets added
  @Test
  public void refactorAddImport() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalTimeUse() {",
            "    LocalTime lt = new LocalTime(8, 0, 0, 0);",
            "    return lt.toDateTimeToday();",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalTimeUse() {",
            "    LocalTime lt = new LocalTime(8, 0, 0, 0);",
            "    return new DateTime().now(DateTimeZone.getDefault()).withTime(lt);",
            "  }",
            "}")
        .doTest();
  }

  // LocalDate -> DateTime with DateTimeZone
  @Test
  public void refactorLocalDate() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    LocalTime now = LocalTime.now();",
            "    return ld.toDateTime(now, DateTimeZone.forID(\"America / New_York\"))",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    LocalTime now = LocalTime.now();",
            "    return new DateTime(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfYear(),"
                + " now.getHourOfDay(), now.getMinuteOfHour(),"
                + " now.getSecondOfMinute(), now.getMillisOfSecond(),"
                + " DateTimeZone.forID(\"America / New_York\"))",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();
  }

  // LocalDate -> DateTime without DateTimeZone
  @Test
  public void refactorLocalDate2() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    LocalTime now = LocalTime.now();",
            "    return ld.toDateTime(now)",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "import org.joda.time.LocalTime;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    LocalTime now = LocalTime.now();",
            "    return new DateTime(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfYear(),"
                + " now.getHourOfDay(), now.getMinuteOfHour(),"
                + " now.getSecondOfMinute(), now.getMillisOfSecond(), DateTimeZone.getDefault())",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();

  }

  // LocalDate -> DateTime with DateTimeZone - toDateTimeAtStartOfDay
  @Test
  public void refactorLocalDate3() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    return ld.toDateTimeAtStartOfDay()",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    return ld.toDateTimeAtStartOfDay(DateTimeZone.getDefault())",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();

  }

  // LocalDate -> DateTime with DateTimeZone - toDateTimeAtCurrentTime
  @Test
  public void refactorLocalDate4() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    return ld.toDateTimeAtCurrentTime(DateTimeZone.forID(\"America / New_York\"))",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    return new DateTime().now(DateTimeZone.forID(\"America / New_York\")).withDate(ld)",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();
  }

  // LocalDate -> DateTime without DateTimeZone - toDateTimeAtCurrentTime
  @Test
  public void refactorLocalDate5() {
    BugCheckerRefactoringTestHelper.newInstance(new JodaTimeLocal(), getClass())
        .addInputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    return ld.toDateTimeAtCurrentTime()",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .addOutputLines("Test.java",
            "import org.joda.time.DateTime;",
            "import org.joda.time.DateTimeZone;",
            "import org.joda.time.LocalDate;",
            "class Test {",
            "  private DateTime badLocalDateUse() {",
            "    LocalDate ld = new LocalDate(2020, 6, 2);",
            "    return new DateTime().now(DateTimeZone.getDefault()).withDate(ld)",
            "      .toDateTime(DateTimeZone.forID(\"America / Los_Angeles\"));",
            "  }",
            "}")
        .doTest();
  }

}

