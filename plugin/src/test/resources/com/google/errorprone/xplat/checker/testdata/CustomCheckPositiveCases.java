// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
