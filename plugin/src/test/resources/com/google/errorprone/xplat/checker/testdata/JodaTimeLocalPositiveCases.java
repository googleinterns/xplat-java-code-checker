package com.google.errorprone.xplat.checker.testdata;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class JodaTimeLocalPositiveCases {


  private DateTime badLocalDateTimeUse() {
    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);
    // BUG: Diagnostic contains: The use of toDateTime(org.joda.time.DateTimeZone)
    return ldt.toDateTime(DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private void test() {
    // BUG: Diagnostic contains: The use of LocalDateTime(org.joda.time.DateTimeZone)
    LocalDateTime test1 = new LocalDateTime(DateTimeZone.forID("America/New_York"));

  }
}