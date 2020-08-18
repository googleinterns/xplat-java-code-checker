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

  private String y;
  private volatile String z;

  // BUG: Diagnostic contains: An error prone lazy init pattern has been detected.
  public String lazyInit() {
    if (y == null) {
      y = new String();
    }
    return y;
  }


  // BUG: Diagnostic contains: An error prone lazy init pattern has been detected.
  public String lazyInit1() {
    synchronized (this) {
      if (y == null) {
        y = new String();
      }
      return y;
    }
  }

  // BUG: Diagnostic contains: An error prone lazy init pattern has been detected.
  public synchronized String lazyInit2() {
    if (y == null) {
      y = new String();
    }
    return y;
  }

  // BUG: Diagnostic contains: An error prone lazy init pattern has been detected.
  public String lazyInit3() {
    String local = y;
    if (local == null) {
      y = local = new String();
    }
    return y;
  }

  public String lazyInit4() {
    String local = z;
    if (local == null) {
      // BUG: Diagnostic contains: Please swap the order of local and z in this assignment.
      local = z = new String();
    }
    // BUG: Diagnostic contains: Please return the local variable instead of the field.
    return z;
  }

  public String lazyInit5() {
    String local = z;
    if (local == null) {
      // BUG: Diagnostic contains: Please swap the order of local and z in this assignment.
      local = z = new String();
    }
    return local;
  }

  public String lazyInit6() {
    String local = z;
    if (local == null) {
      z = local = new String();
    }

    // BUG: Diagnostic contains: Please return the local variable instead of the field.
    return z;
  }

}