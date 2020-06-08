package com.google.errorprone.xplat.checker.testdata;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;


public class JodaTimeObjectParamPositiveCases {

  public static void main(String[] args) {
    Object ob = null;

    // testing constructor with object param (not boxed long)
    // BUG: Diagnostic contains: Use of constructors that have java.lang.Object
    DateTime time = new DateTime(ob);

    LocalDateTime time2 = new LocalDateTime();

    // testing method with object param (not boxed long)
    // BUG: Diagnostic contains: Use of methods that have java.lang.Object
    time2.equals(ob);

    // testing method with object param (not boxed long)
    // BUG: Diagnostic contains: Use of methods that have java.lang.Object
    time2.equals(null);

    // testing method with object param (not boxed long)
    // BUG: Diagnostic contains: Use of methods that have java.lang.Object
    time2.equals(new Double(2.0));

    // testing method with object param (not boxed long)
    // BUG: Diagnostic contains: Use of methods that have java.lang.Object
    time2.equals(new String("Hello"));

  }
}