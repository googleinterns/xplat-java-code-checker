package com.google.errorprone.xplat.checker.testdata;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;


public class JodaTimeObjectParamNegativeCases {

  Object test;

  JodaTimeObjectParamNegativeCases(Object test) {
    this.test = test;
  }

  public static void main(String[] args) {

    // constructor with object param, but using boxed long
    DateTime time = new DateTime(new Long(1));

    // constructor with non-object param
    LocalDateTime time2 = new LocalDateTime(2);

    // constructor that isn't a joda time class
    JodaTimeObjectParamNegativeCases test1 = new JodaTimeObjectParamNegativeCases(null);
    
    // method (not being checked)
    time2.equals(new Long(1));

    // method (not being checked)
    time.plusYears(1);


  }
}