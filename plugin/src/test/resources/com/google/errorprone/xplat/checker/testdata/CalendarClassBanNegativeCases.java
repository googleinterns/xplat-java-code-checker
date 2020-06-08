package com.google.errorprone.xplat.checker.testdata;

import com.google.errorprone.xplat.checker.AllowLegacyTime;

import java.util.Calendar;

public class CalendarClassBanNegativeCases {

  // field
  @AllowLegacyTime
  private Calendar cal;

  // constructor param
  public CalendarClassBanNegativeCases(@AllowLegacyTime Calendar cal) {
    this.cal = cal;
  }

  // method
  @AllowLegacyTime
  public Calendar returnCalendar() {
    return Calendar.getInstance();
  }

  // method param
  public void calParam(@AllowLegacyTime Calendar cal) {
    // local var
    @AllowLegacyTime
    Calendar test = Calendar.getInstance();
  }


}