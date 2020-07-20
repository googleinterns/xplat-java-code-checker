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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.Chronology;
import org.joda.time.DateMidnight;
import org.joda.time.tz.FixedDateTimeZone;


public class XplatBansPositiveCases {

  public void mutableDate() {
    // test constructor from banned class
    // BUG: Diagnostic contains: Use of org.joda.time.MutableDateTime
    MutableDateTime dateTime = new MutableDateTime();

    // test local var from banned class
    // BUG: Diagnostic contains: Use of org.joda.time.MutableDateTime
    MutableDateTime time;

    // test method call from banned class
    // BUG: Diagnostic contains: Use of org.joda.time.MutableDateTime
    dateTime.add(2);

    // test method call on banned instantiated class
    // BUG: Diagnostic contains: Use of org.joda.time.MutableDateTime
    System.out.println(dateTime.toString());

  }

  public void chrono() {
    DateTime dt = new DateTime();

    // test constructor from banned package
    // BUG: Diagnostic contains: Use of org.joda.time.chrono
    BuddhistChronology chr = BuddhistChronology.getInstance();

    // test method from banned package
    // BUG: Diagnostic contains: Use of org.joda.time.chrono
    chr.toString();

    // test local var from banned package
    // BUG: Diagnostic contains: Use of org.joda.time.chrono
    BuddhistChronology ch2;

    // test method that returns banned type
    // BUG: Diagnostic contains: Use of withChronology(org.joda.time.Chronology)
    DateTime dtBuddhist = dt.withChronology(BuddhistChronology.getInstance());

    System.out.println(dtBuddhist.getYear());
  }

  public void tzTime() {
    // test constructor from banned package
    // BUG: Diagnostic contains: Use of org.joda.time.tz
    FixedDateTimeZone time = new FixedDateTimeZone("1", "1", 1, 1);

    // test method from banned package
    // BUG: Diagnostic contains: Use of org.joda.time.tz
    time.getStandardOffset(1);
  }

  // test method that returns banned class
  // BUG: Diagnostic contains: Use of org.joda.time.DateMidnight
  private DateMidnight doNotUseDateMidnight() {

    // test method from allowed class that returns banned type
    // BUG: Diagnostic contains: Use of toDateMidnight() is not allowed, as org.joda.time.DateMidnight
    return goodLocalDateTimeUse().toDateMidnight();
  }

  private void implicitBannedClassUse() {
    // test method from allowed class that returns banned type
    // BUG: Diagnostic contains: Use of toYearMonthDay() is not allowed, as org.joda.time.YearMonthDay
    goodLocalDateTimeUse().toYearMonthDay();
  }

  // test method declaration that has a parameter with a banned type
  // BUG: Diagnostic contains: Use of org.joda.time.Chronology
  private DateTime bannedParameterTypes(String s, Chronology c) {
    System.out.println(s);

    // tests a method that has an invalid paramater
    // BUG: Diagnostic contains: Use of toDateTime(org.joda.time.Chronology)
    return goodLocalDateTimeUse().toDateTime(BuddhistChronology.getInstance());
  }

  private LocalDateTime badConstructor() {
    // tests the use of a constructor that has arguments that are banned
    // BUG: Diagnostic contains: Use of this constructor (LocalDateTime(long,org.joda.time.Chronology))
    return new LocalDateTime(2, BuddhistChronology.getInstance());
  }

  // Create an 8 AM ET then convert it to PT. Bad case.
  private DateTime badLocalDateTimeUse() {
    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);
    return ldt.toDateTime(DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));
  }

  // Create an 8 AM ET then convert it to PT.
  private DateTime goodLocalDateTimeUse() {
    LocalDateTime ldt = new LocalDateTime(2020, 6, 2, 8, 0, 0, 0);
    return new DateTime(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfYear(), ldt.getHourOfDay(),
        ldt.getMinuteOfHour(), ldt.getSecondOfMinute(), ldt.getMillisOfSecond(),
        DateTimeZone.forID("America/New_York"))
        .toDateTime(DateTimeZone.forID("America/Los_Angeles"));

  }

  private class Illegal {

    // BUG: Diagnostic contains: Use of org.joda.time.Chronology
    private Chronology chrono;

    // tests a created method that has a parameter with a banned type
    // BUG: Diagnostic contains: Use of org.joda.time.Chronology
    public Illegal(Chronology chrono) {
      this.chrono = chrono;
    }

    // tests a created method that returns a banned type
    // BUG: Diagnostic contains: Use of org.joda.time.Chronology
    private Chronology returnChrono() {
      return this.chrono;
    }
  }

  private class Illegal2 {

    // tests a banned field from a banned package
    // BUG: Diagnostic contains: Use of org.joda.time.tz
    private FixedDateTimeZone time;

    // tests a created method that has a parameter with a banned type from a package
    // BUG: Diagnostic contains: Use of org.joda.time.tz
    public Illegal2(FixedDateTimeZone time) {
      this.time = time;
    }

    // tests a created method that returns a banned type from a banned package
    // BUG: Diagnostic contains: Use of org.joda.time.tz
    private FixedDateTimeZone returnChrono() {
      return this.time;
    }
  }


  public static void main(String[] args) {
    XplatBansPositiveCases m = new XplatBansPositiveCases();

    // tests a banned class
    // BUG: Diagnostic contains: Use of org.joda.time.DateMidnight
    DateMidnight dm = m.doNotUseDateMidnight();

    System.err.println(m.badLocalDateTimeUse());
  }
}
