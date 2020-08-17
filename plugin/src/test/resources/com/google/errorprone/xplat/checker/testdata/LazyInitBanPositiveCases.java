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


public class LazyInitBanPositiveCases {

  private final int x;
  private String y;

  private String z = "Hi";

  LazyInitBanPositiveCases() {
    x = 1;
  }

  // BUG: Diagnostic contains: An error prone lazy init pattern has been detected.
  public String lazyInit() {
    if (y == null) {
      y = new String();
    }
    return y;
  }

  // BUG: Diagnostic contains: An error prone lazy init pattern has been detected.
  public String lazyInit2() {
    String local = "Local";
    if (y == null) {
      y = new String();
    }
    return y;
  }


}