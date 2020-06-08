package com.google.errorprone.xplat.checker.testdata;

import com.google.errorprone.xplat.checker.AllowLegacyTime;

import java.util.Calendar;

public class CalendarClassBanPositiveCases {

  // tests field
  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  private Calendar cal;

  // tests constructor param
  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public CalendarClassBanPositiveCases(Calendar cal) {
    this.cal = cal;
  }

  // tests method
  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public Calendar returnCalendar() {
    return Calendar.getInstance();
  }

  // tests method param
  // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
  public void calParam(Calendar cal) {
    // tests local var
    // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
    Calendar test = Calendar.getInstance();
  }

}