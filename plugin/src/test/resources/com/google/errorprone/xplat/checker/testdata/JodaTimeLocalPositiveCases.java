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
    // BUG: Diagnostic contains: The use of toDateTime()
    return ldt.toDateTime()
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private DateTime badLocalTimeUse() {
    LocalTime lt = new LocalTime(8, 0, 0, 0);
    // BUG: Diagnostic contains: The use of toDateTimeToday(org.joda.time.DateTimeZone)
    return lt.toDateTimeToday(DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private DateTime badLocalTimeUse2() {
    LocalTime lt = new LocalTime(8, 0, 0, 0);
    // BUG: Diagnostic contains: The use of toDateTimeToday()
    return lt.toDateTimeToday()
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private DateTime badLocalDateUse() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    LocalTime now = LocalTime.now();
    // BUG: Diagnostic contains: The use of toDateTime(org.joda.time.LocalTime,org.joda.time.DateTimeZone)
    return ld.toDateTime(now, DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private DateTime badLocalDateUse2() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    LocalTime now = LocalTime.now();
    // BUG: Diagnostic contains: The use of toDateTime(org.joda.time.LocalTime)
    return ld.toDateTime(now)
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private DateTime badLocalDateUse3() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    // BUG: Diagnostic contains: The use of toDateTimeAtStartOfDay()
    return ld.toDateTimeAtStartOfDay()
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private DateTime badLocalDateUse4() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    // BUG: Diagnostic contains: The use of toDateTimeAtCurrentTime(org.joda.time.DateTimeZone)
    return ld.toDateTimeAtCurrentTime(DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private DateTime badLocalDateUse5() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    // BUG: Diagnostic contains: The use of toDateTimeAtCurrentTime()
    return ld.toDateTimeAtCurrentTime()
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  private void consTest() {
    // BUG: Diagnostic contains: The use of LocalDateTime(org.joda.time.DateTimeZone)
    LocalDateTime test1 = new LocalDateTime(DateTimeZone.forID("America/New_York"));

    // BUG: Diagnostic contains: The use of LocalDateTime(long,org.joda.time.DateTimeZone)
    LocalDateTime test2 = new LocalDateTime(1L, DateTimeZone.forID("America/New_York"));

    // BUG: Diagnostic contains: The use of LocalDateTime(java.lang.Object,org.joda.time.DateTimeZone)
    LocalDateTime test3 = new LocalDateTime(null, DateTimeZone.forID("America/New_York"));
  }
}