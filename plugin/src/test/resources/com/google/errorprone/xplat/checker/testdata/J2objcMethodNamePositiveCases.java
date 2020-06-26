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

import com.google.j2objc.annotations.ObjectiveCName;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.Calendar.Builder;

//@ObjectiveCName("Hmm")
public class J2objcMethodNamePositiveCases {

  private int test;

  public J2objcMethodNamePositiveCases(int x) {
    this.test = x;
  }

  public HashMap<Object, Set<String>> hello() {
    return new HashMap<Object, Set<String>>();
  }

  public static HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x) {
    return x;
  }

  @ObjectiveCName("Fixed")
  private HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x,
      HashMap<Object, Set<String>> y,
      HashMap<Object, Set<String>> z, HashMap<Object, Set<String>> q,
      HashMap<Object, Set<String>> r,
      HashMap<Object, Set<String>> s, HashMap<Object, Set<String>> t,
      HashMap<Object, Set<String>> u, HashMap<Object, Set<String>> v) {
    return x;
  }

  private static void hi() {
    return;
  }

  @ObjectiveCName("Middleclass")
  private class Test {

    public void hello1(Calendar.Builder x) {
      return;
    }

    private Calendar.Builder hello2(Calendar.Builder x) {
      return x;
    }

    //@ObjectiveCName("ChangedName")
    private class Test2 {

      private int testception() {
        return 1;
      }
    }
  }

  private class Generics {

    private <T> T genericTest(T x) {
      return x;
    }


    private <E> E genericTest(E x, E y) {
      return y;
    }

    private <T, E> T genericTest(E x, T y, T z) {
      return z;
    }
  }
}
