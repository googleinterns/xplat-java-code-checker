package com.google.errorprone.xplat.checker.testdata;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;


public class JodaTimeObjectParamNegativeCases {


  public static void main(String[] args) {

    // constructor with object param, but using boxed long
    DateTime time = new DateTime(new Long(1));

    // constructor with non-object param
    LocalDateTime time2 = new LocalDateTime(2);

    // method with object parameter but using boxed long
    time2.equals(new Long(1));

    // method with non-object param
    time.plusYears(1);


  }
}