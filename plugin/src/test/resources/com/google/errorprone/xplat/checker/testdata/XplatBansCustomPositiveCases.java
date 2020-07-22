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

import java.util.HashSet;
import java.util.HashMap;
import java.io.CharArrayReader;
import java.util.Random;

public class XplatBansCustomPositiveCases {


  public void test() {

    // Tests custom class ban
    // BUG: Diagnostic contains: Use of java.util.HashMap has been banned due to a test being preformed.
    HashMap<String, Integer> map = new HashMap<>();

    // Tests custom class ban
    // BUG: Diagnostic contains: Use of java.io has been banned due to side effects frightening Haskell users.
    CharArrayReader read = new CharArrayReader("read" .toCharArray());

  }

  public void customMethodBanTests() {

    HashSet<String> set = new HashSet<>();

    set.add("test");

    // Tests custom method ban
    // BUG: Diagnostic contains: Use of contains() is not allowed, as java.util.HashSet has been banned due to sets being a surprise.
    set.contains("test");

    // Tests custom method ban
    // BUG: Diagnostic contains: Use of remove() is not allowed, as java.util.HashSet has been banned due to sets belonging to someone else.
    set.remove("test");

    Random rand = new Random();

    // Tests custom method ban with 2 different parameters
    // BUG: Diagnostic contains: Use of ints() is not allowed, as java.util.Random has been banned due to other number types being cooler.
    rand.ints();

    // Tests custom method ban with 2 different parameters
    // BUG: Diagnostic contains: Use of ints() is not allowed, as java.util.Random has been banned due to other number types being cooler.
    rand.ints(1L);

  }


}
