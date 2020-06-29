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

public class J2objcMethodNameManglePackage {


  // BUG: Diagnostic contains:  XPTJ2objcMethodNameManglePackage_hello
  private static HashMap<Object, Set<String>> hello() {
    return new HashMap<Object, Set<String>>();
  }

  // BUG: Diagnostic contains: XPTJ2objcMethodNameManglePackage_helloWithJavaUtilHashMap_
  public static HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x) {
    return x;
  }

  // BUG: Diagnostic contains: XPTJ2objcMethodNameManglePackage_helloWithJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_
  private HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x,
      HashMap<Object, Set<String>> y,
      HashMap<Object, Set<String>> z, HashMap<Object, Set<String>> q,
      HashMap<Object, Set<String>> r,
      HashMap<Object, Set<String>> s, HashMap<Object, Set<String>> t,
      HashMap<Object, Set<String>> u, HashMap<Object, Set<String>> v) {
    return x;
  }

  public HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x,
      HashMap<Object, Set<String>> y) {
    return new HashMap<Object, Set<String>>();
  }


  @ObjectiveCName("renamedMethod")
  // BUG: Diagnostic contains: XPTJ2objcMethodNameManglePackage_renamedMethod
  private static void hi() {
    return;
  }

  @ObjectiveCName("Middleclass")
  private class Test {

    public void hello1(Calendar.Builder x) {
      return;
    }

    // BUG: Diagnostic contains: Middleclass_hello2WithJavaUtilCalendar_Builder_
    private Calendar.Builder hello2(Calendar.Builder x) {
      return x;
    }

    private class Test2 {

      // BUG: Diagnostic contains: Middleclass_Test2_testception
      private int testception() {
        return 1;
      }
    }

    @ObjectiveCName("DoubleNestedClass")
    private class Test3 {

      // BUG: Diagnostic contains: DoubleNestedClass_testAgain
      private int testAgain() {
        return 1;
      }

      @ObjectiveCName("nestedRenamedMethod")
      // BUG: Diagnostic contains: DoubleNestedClass_nestedRenamedMethod
      private void rename() {

      }
    }
  }

  private class Generics {

    // BUG: Diagnostic contains: XPTJ2objcMethodNameManglePackage_Generics_genericTestWithId_
    private <T> T genericTest(T x) {
      return x;
    }

    // BUG: Diagnostic contains: XPTJ2objcMethodNameManglePackage_Generics_genericTestWithId_withId_
    private <E> E genericTest(E x, E y) {
      return y;
    }

    // BUG: Diagnostic contains: XPTJ2objcMethodNameManglePackage_Generics_genericTestWithId_withId_withId_
    private <T, E> T genericTest(E x, T y, T z) {
      return z;
    }
  }
}