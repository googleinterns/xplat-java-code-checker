package com.google.errorprone.xplat.checker.testdata;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class JodaTimeLocalNegativeCases {


  // Create an 8 AM ET then convert it to PT.
  private DateTime goodLocalDateTimeUse() {
    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);
    return new DateTime(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfYear(), ldt.getHourOfDay(),
        ldt.getMinuteOfHour(), ldt.getSecondOfMinute(), ldt.getMillisOfSecond(),
        DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));

  }

  private void test() {
    LocalDateTime test1 = new LocalDateTime(1L, DateTimeZone.forID("America/New_York"));
  }

}