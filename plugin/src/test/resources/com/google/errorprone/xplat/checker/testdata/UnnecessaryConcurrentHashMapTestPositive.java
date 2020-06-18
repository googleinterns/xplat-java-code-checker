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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UnnecessaryConcurrentHashMapTestPositive {

  public void test() {
    // Tests declaration and new class on same line
    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();

    // Tests declaration and new class on same line with implicit type vars
    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    ConcurrentHashMap<Object, Long> map2 = new ConcurrentHashMap<>();

    // Tests declaration alone
    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    ConcurrentHashMap<String, Integer> map3;

    // Tests new class alone
    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    map3 = new ConcurrentHashMap<String, Integer>();

    // Tests special case - incompatable interface with variable that is instantiated later
    // BUG: Diagnostic contains: This variable is declared with an interface
    ConcurrentMap<Object, String> map4;

    // Tests declaration with invalid interface on same line as new class
    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    ConcurrentMap<String, Integer> map5 = new ConcurrentHashMap<>();

    // Tests declaration with valid interface on same line as new class
    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    Map<String, Integer> map6 = new ConcurrentHashMap<>();

    // Tests special case - incompatable interface with variable that is instantiated later
    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    map4 = new ConcurrentHashMap<>();

    if (true) {
      // Tests special case - variable declaration not in same scope
      // BUG: Diagnostic contains: ConcurrentHashMap is not advised
      map4 = new ConcurrentHashMap<>();
    }
  }
}