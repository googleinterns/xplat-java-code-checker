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

import com.google.errorprone.xplat.checker.XplatBanSuppression;
import java.io.CharArrayReader;
import java.util.HashSet;
import java.util.Random;
import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;

public class XplatBansCustomNegativeCases {

  public void test() {

    Random rand = new Random();
    rand.doubles();
    rand.longs();
    rand.nextInt();

    HashSet<String> set = new HashSet<>();
    set.add("1");
    set.clear();

  }

  public void annotationTest() {

    @XplatBanSuppression("test")
    HashMap<String, Integer> map = new HashMap<>();

    @XplatBanSuppression
    Map<String, String> map1 = new HashMap<>();

    @XplatBanSuppression
    HashMap<String, Integer> map2;

    @XplatBanSuppression("test1")
    CharArrayReader read = new CharArrayReader("read" .toCharArray());

    HashSet<String> set = new HashSet<>();

    @XplatBanSuppression
    boolean bool = set.contains("Test");
  }

  @XplatBanSuppression
  public void supress() {
    HashSet<String> set = new HashSet<>();

    set.add("test");

    set.contains("test");

    set.remove("test");

    Random rand = new Random();

    rand.ints();

    rand.ints(1L);
  }

}
