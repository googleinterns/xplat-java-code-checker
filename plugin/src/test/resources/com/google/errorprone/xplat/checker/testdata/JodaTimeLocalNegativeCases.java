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

public class JodaTimeLocalNegativeCases {


  // LocalDateTime -> DateTime with DateTimeZone
  private DateTime goodLocalDateTimeUse() {
    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);
    return new DateTime(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfYear(), ldt.getHourOfDay(),
        ldt.getMinuteOfHour(), ldt.getSecondOfMinute(), ldt.getMillisOfSecond(),
        DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));

  }

  // LocalDateTime -> DateTime without DateTimeZone
  private DateTime goodLocalDateTimeUse2() {
    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);
    return new DateTime(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfYear(), ldt.getHourOfDay(),
        ldt.getMinuteOfHour(), ldt.getSecondOfMinute(), ldt.getMillisOfSecond(),
        DateTimeZone.getDefault())
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));

  }

  // LocalTime -> DateTime with DateTimeZone
  private DateTime goodLocalTimeUse() {
    LocalTime lt = new LocalTime(8, 0, 0, 0);
    return new DateTime().now(DateTimeZone.forID("America/New_York"))
        .withTime(lt)
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  // LocalTime -> DateTime without DateTimeZone
  private DateTime goodLocalTimeUse2() {
    LocalTime lt = new LocalTime(8, 0, 0, 0);
    return new DateTime().now(DateTimeZone.getDefault())
        .withTime(lt)
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  // LocalDate -> DateTime with DateTimeZone
  private DateTime goodLocalDateUse() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    LocalTime now = LocalTime.now();
    return new DateTime(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfYear(), now.getHourOfDay(),
        now.getMinuteOfHour(), now.getSecondOfMinute(), now.getMillisOfSecond(),
        DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  // LocalDate -> DateTime without DateTimeZone
  private DateTime goodLocalDateUse2() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    LocalTime now = LocalTime.now();
    return new DateTime(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfYear(), now.getHourOfDay(),
        now.getMinuteOfHour(), now.getSecondOfMinute(), now.getMillisOfSecond(),
        DateTimeZone.getDefault())
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  // LocalDate -> DateTime with DateTimeZone - toDateTimeAtStartOfDay
  private DateTime goodLocalDateUse3() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    return ld.toDateTimeAtStartOfDay(DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  // LocalDate -> DateTime with DateTimeZone - toDateTimeAtCurrentTime
  private DateTime goodLocalDateUse4() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    return new DateTime().now(DateTimeZone.forID("America/New_York"))
        .withDate(ld)
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  // LocalDate -> DateTime without DateTimeZone - toDateTimeAtCurrentTime
  private DateTime goodLocalDateUse5() {
    LocalDate ld = new LocalDate(2020, 6, 2);
    return new DateTime().now(DateTimeZone.getDefault())
        .withDate(ld)
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  // random constructor test
  private void consTest() {
    LocalDateTime test1 = new LocalDateTime(1L);
  }


}