package com.google.errorprone.xplat.checker.testdata;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;


public class JodaTimeObjectParamPositiveCases {

  public static void main(String[] args) {
    Object ob = null;

    // testing constructor with object param (not boxed long)
    // BUG: Diagnostic contains: DateTime(java.lang.Object) is a banned constructor
    DateTime time = new DateTime(ob);

    // BUG: Diagnostic contains: LocalDateTime(java.lang.Object) is a banned constructor
    LocalDateTime time2 = new LocalDateTime(ob);

  }
}