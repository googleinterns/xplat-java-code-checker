package com.google.errorprone.xplat.checker.testdata;

import com.google.errorprone.xplat.checker.AllowLegacyTime;

import java.util.Calendar;

public class CalendarClassBanNegativeCases {

  @AllowLegacyTime
  private Calendar cal;

  public CalendarClassBanNegativeCases(@AllowLegacyTime Calendar cal) {
    this.cal = cal;
  }

  @AllowLegacyTime
  public Calendar returnCalendar() {
    return Calendar.getInstance();
  }

  public void calParam(@AllowLegacyTime Calendar cal) {
    @AllowLegacyTime
    Calendar test = Calendar.getInstance();
  }


}