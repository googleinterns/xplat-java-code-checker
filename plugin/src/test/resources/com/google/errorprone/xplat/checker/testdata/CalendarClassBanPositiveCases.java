package com.google.errorprone.xplat.checker.testdata;

import com.google.errorprone.xplat.checker.AllowLegacyTime;

// xBUG: Diagnostic contains:
import java.util.Calendar;


public class CalendarClassBanPositiveCases {

  public static void main(String[] args) {
    // BUG: Diagnostic contains: java.util.Calendar is banned for cross platform development
    Calendar cal = Calendar.getInstance();
  }


}