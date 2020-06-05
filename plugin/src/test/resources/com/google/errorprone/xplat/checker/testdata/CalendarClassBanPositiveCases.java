package com.google.errorprone.xplat.checker.testdata;

import com.google.errorprone.xplat.checker.AllowLegacyTime;

import java.util.Calendar;

public class CalendarClassBanPositiveCases {

  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  private Calendar cal;

  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public CalendarClassBanPositiveCases(Calendar cal) {
    this.cal = cal;
  }

  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public Calendar returnCalendar() {
    return Calendar.getInstance();
  }

  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public void calParam(Calendar cal) {
    // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
    Calendar test = Calendar.getInstance();
  }

}