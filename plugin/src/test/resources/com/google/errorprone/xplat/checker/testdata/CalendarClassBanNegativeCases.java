package com.google.errorprone.xplat.checker.testdata;

import com.google.errorprone.xplat.checker.AllowLegacyTime;

import java.util.Calendar;

@AllowLegacyTime
public class CalendarClassBanNegativeCases {

  public static void main(String[] args) {
    Calendar cal = Calendar.getInstance();

  }


}