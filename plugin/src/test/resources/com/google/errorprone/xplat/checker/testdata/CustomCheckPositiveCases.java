package com.google.errorprone.xplat.checker.testdata;

import java.util.Arrays;

public class CustomCheckPositiveCases {


  public void printWithFormatErr(String[] args) {
    // BUG: Diagnostic contains: String formatting inside print method.
    System.err.print(String.format("Hello: %s\n", Arrays.toString(args)));
  }


  public void printWithFormatOut(String[] args) {
    // BUG: Diagnostic contains: String formatting inside print method.
    System.out.print(String.format("Hello: %s\n", Arrays.toString(args)));
  }

  public void SometimesPrintWithFormatErr(boolean random, String[] args) {
    if(random) {
      // BUG: Diagnostic contains: String formatting inside print method.
      System.err.print(String.format("Hello: %s\n", Arrays.toString(args)));
    }
  }
}
