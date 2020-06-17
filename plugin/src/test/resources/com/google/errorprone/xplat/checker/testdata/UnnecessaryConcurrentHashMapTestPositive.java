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
    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();

    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    ConcurrentHashMap<Object, Long> map2 = new ConcurrentHashMap<>();

    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    ConcurrentHashMap<String, Integer> map3;

    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    map3 = new ConcurrentHashMap<String, Integer>();

    ConcurrentMap<Object, String> map4;

    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    ConcurrentMap<String, Integer> map5 = new ConcurrentHashMap<>();

    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    Map<String, Integer> map6 = new ConcurrentHashMap<>();

    // BUG: Diagnostic contains: ConcurrentHashMap is not advised
    map4 = new ConcurrentHashMap<>();
  }
}