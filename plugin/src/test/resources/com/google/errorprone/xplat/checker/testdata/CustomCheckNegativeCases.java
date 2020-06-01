package com.google.errorprone.xplat.checker.testdata;

import java.util.Arrays;

public class CustomCheckNegativeCases {

  public void printfErrWithFormat(String[] args) {
    System.err.printf("Hello: %s\n", Arrays.toString(args));

  }

  public void printfOutWithFormat(String[] args) {
    System.out.printf("Hello: %s\n", Arrays.toString(args));

  }
}
