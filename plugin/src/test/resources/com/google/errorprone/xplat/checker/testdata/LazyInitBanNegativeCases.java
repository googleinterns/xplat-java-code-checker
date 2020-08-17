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


import com.google.errorprone.annotations.concurrent.LazyInit;

public class LazyInitBanNegativeCases {

  private final int x;
  private String y;
  @LazyInit
  private String z;
  private String a = "hello";

  LazyInitBanNegativeCases() {
    x = 1;
  }

  //Uses @LazyInit
  public String lazyInitValid() {
    if (z == null) {
      z = new String();
    }
    return z;
  }

  //syncronized
  public synchronized String lazyInitValid2() {
    if (y == null) {
      y = new String();
    }
    return y;
  }

  //syncronized inside
  public String lazyInitValid3() {
    synchronized (this) {
      if (y == null) {
        y = new String();
      }
      return y;
    }
  }


  //local var
  public String notLazyInit() {
    String y = null;
    if (y == null) {
      y = new String();
    }
    return y;
  }
}