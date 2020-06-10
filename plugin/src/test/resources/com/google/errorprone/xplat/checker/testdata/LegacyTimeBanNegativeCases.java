package com.google.errorprone.xplat.checker.testdata;

import com.google.errorprone.xplat.checker.AllowLegacyTime;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.SimpleTimeZone;

public class LegacyTimeBanNegativeCases {

  // fields
  @AllowLegacyTime
  private Calendar cal;
  @AllowLegacyTime
  private Date date;
  @AllowLegacyTime
  private GregorianCalendar calG;
  @AllowLegacyTime
  private TimeZone timeZone;
  @AllowLegacyTime
  private SimpleTimeZone simpleTimeZone;

  // constructor param
  public LegacyTimeBanNegativeCases(@AllowLegacyTime Calendar cal, @AllowLegacyTime Date date) {
    this.cal = cal;
  }

  // method
  @AllowLegacyTime
  public Calendar returnCalendar() {
    return Calendar.getInstance();
  }

  // method
  @AllowLegacyTime
  public TimeZone returnTimeZone() {
    return TimeZone.getDefault();
  }

  // method param
  public void calParam(@AllowLegacyTime Calendar cal) {
    // local var
    @AllowLegacyTime
    Calendar test = Calendar.getInstance();
  }

  // self-documenting annotation
  public void legacy() {
    @AllowLegacyTime("Required by legacy API")
    Calendar local = returnCalendar();
  }
}