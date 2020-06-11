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

  private DateTime badLocalDateTimeUse2() {
    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);
    // xBUG: Diagnostic contains: The use of toDateTime()
    return ldt.toDateTime()
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

//  private DateTime badLocalTimeUse() {
//    LocalTime lt = new LocalTime(8, 0, 0, 0);
//    // xBUG: Diagnostic contains: The use of toDateTime(org.joda.time.DateTimeZone)
//    return lt.toDateTime(DateTimeZone.forID("America/New_York"))
//        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
//  }

  private void consTest() {
    // BUG: Diagnostic contains: The use of LocalDateTime(org.joda.time.DateTimeZone)
    LocalDateTime test1 = new LocalDateTime(DateTimeZone.forID("America/New_York"));

    // BUG: Diagnostic contains: The use of LocalDateTime(long,org.joda.time.DateTimeZone)
    LocalDateTime test2 = new LocalDateTime(1L, DateTimeZone.forID("America/New_York"));

    // BUG: Diagnostic contains: The use of LocalDateTime(java.lang.Object,org.joda.time.DateTimeZone)
    LocalDateTime test3 = new LocalDateTime(null, DateTimeZone.forID("America/New_York"));
  }
}