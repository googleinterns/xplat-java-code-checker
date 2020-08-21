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
  private volatile String y;
  @LazyInit
  private String z;

  private String zz;

  private String a = "hello";

  // syncronized with volatile
  public synchronized String lazyInitValid() {
    if (y == null) {
      y = new String();
    }
    return y;
  }

  // syncronized with @LazyInit
  public synchronized String lazyInitValid1() {
    if (z == null) {
      z = new String();
    }
    return z;
  }

  //syncronized inside
  public String lazyInitValid2() {
    synchronized (this) {
      if (y == null) {
        y = new String();
      }
      return y;
    }
  }

  //syncronized inside
  public String lazyInitValid3() {
    synchronized (this) {
      if (z == null) {
        z = new String();
      }
      return z;
    }
  }

  // non-sync version
  public String lazyInitValid4() {
    String local = y;
    if (local == null) {
      y = local = new String();
    }
    return local;
  }

  // non-sync version
  public String lazyInitValid5() {
    String local = z;
    if (local == null) {
      z = local = new String();
    }
    return local;
  }
  
  // constructor - not checked
  LazyInitBanNegativeCases() {
    if (y == null) {
      y = new String();
    }
    x = 1;
  }

  // local var - not a lazy init
  public String notLazyInit() {
    String y = null;
    if (y == null) {
      y = new String();
    }
    return y;
  }
}