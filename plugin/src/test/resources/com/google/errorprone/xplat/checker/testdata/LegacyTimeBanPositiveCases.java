package com.google.errorprone.xplat.checker.testdata;

import com.google.errorprone.xplat.checker.AllowLegacyTime;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.SimpleTimeZone;

public class LegacyTimeBanPositiveCases {

  // tests fields
  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  private Calendar cal;
  // BUG: Diagnostic contains: java.util.Date is banned for cross platform development
  private Date date;
  // BUG: Diagnostic contains: java.util.GregorianCalendar is banned for cross platform development
  private GregorianCalendar calG;
  // BUG: Diagnostic contains: java.util.TimeZone is banned for cross platform development
  private TimeZone timeZone;
  // BUG: Diagnostic contains: java.util.SimpleTimeZone is banned for cross platform development
  private SimpleTimeZone simpleTimeZone;

  // tests constructor param
  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public LegacyTimeBanPositiveCases(Calendar cal) {
    this.cal = cal;
  }

  // tests method
  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public Calendar returnCalendar() {
    return Calendar.getInstance();
  }

  // tests method
  // BUG: Diagnostic contains: java.util.TimeZone is banned for cross platform development
  public TimeZone returnTimeZone() {
    return TimeZone.getDefault();
  }

  // tests method param
  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public void calParam(Calendar cal) {
    // tests local var
    // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
    Calendar test = Calendar.getInstance();
  }

}