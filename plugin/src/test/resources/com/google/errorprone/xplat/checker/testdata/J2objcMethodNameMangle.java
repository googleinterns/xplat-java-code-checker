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

public class J2objcMethodNameMangle {

  // BUG: Diagnostic contains: ComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_hello
  public static HashMap<Object, Set<String>> hello() {
    return new HashMap<Object, Set<String>>();
  }

  // BUG: Diagnostic contains: ComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_helloWithJavaUtilHashMap_
  public static HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x) {
    return x;
  }

  // BUG: Diagnostic contains: ComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_helloWithJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_withJavaUtilHashMap_
  public static HashMap<Object, Set<String>> hello(HashMap<Object, Set<String>> x,
      HashMap<Object, Set<String>> y,
      HashMap<Object, Set<String>> z, HashMap<Object, Set<String>> q,
      HashMap<Object, Set<String>> r,
      HashMap<Object, Set<String>> s, HashMap<Object, Set<String>> t,
      HashMap<Object, Set<String>> u, HashMap<Object, Set<String>> v) {
    return x;
  }

  @ObjectiveCName("renamedMethod")
  // BUG: Diagnostic contains: ComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_renamedMethod
  public static void hi() {
    return;
  }

  // BUG: Diagnostic contains: ComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_classParamsWithMiddleclass_withComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_Generics_
  public static Test classParams(Test test, Generics generic) {
    return test;
  }

  @ObjectiveCName("Middleclass")
  public static class Test {

    // BUG: Diagnostic contains: Middleclass_hello2WithJavaUtilCalendar_Builder_
    public static Calendar.Builder hello2(Calendar.Builder x) {
      return x;
    }

    // BUG: Diagnostic contains: Middleclass_lotsOfLocalClassesWithMiddleclass_Test2_withComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_withDoubleNestedClass_
    public static Test2 lotsOfLocalClasses(Test2 test2, J2objcMethodNameMangle nameMangle,
        Test3 test3) {
      return test2;
    }

    public static class Test2 {

      // BUG: Diagnostic contains: Middleclass_Test2_testception
      public static int testception() {
        return 1;
      }
    }

    @ObjectiveCName("DoubleNestedClass")
    public static class Test3 {

      // BUG: Diagnostic contains: DoubleNestedClass_testAgain
      public static int testAgain() {
        return 1;
      }

      @ObjectiveCName("nestedRenamedMethod")
      // BUG: Diagnostic contains: DoubleNestedClass_nestedRenamedMethod
      public static void rename() {

      }

      // BUG: Diagnostic contains: DoubleNestedClass_classArgsWithMiddleclass_withComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_
      public static Test classArgs(Test test, J2objcMethodNameMangle nameMangle) {
        return test;
      }
    }

    // BUG: Diagnostic contains: Middleclass_lotsOfLocalClassesBottomWithMiddleclass_Test2_withComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_withDoubleNestedClass_
    public static Test2 lotsOfLocalClassesBottom(Test2 test2, J2objcMethodNameMangle nameMangle,
        Test3 test3) {
      return test2;
    }

  }

  public static class Generics {

    // BUG: Diagnostic contains: ComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_Generics_genericTestWithId_
    public static <T> T genericTest(T x) {
      return x;
    }

    // BUG: Diagnostic contains: ComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_Generics_genericTestWithId_withId_
    public static <E> E genericTest(E x, E y) {
      return y;
    }

    // BUG: Diagnostic contains: ComGoogleErrorproneXplatCheckerTestdataJ2objcMethodNameMangle_Generics_genericTestWithId_withId_withId_
    public static <T, E> T genericTest(E x, T y, T z) {
      return z;
    }
  }
}