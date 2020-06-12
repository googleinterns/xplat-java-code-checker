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

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;


public class JodaTimeObjectParamPositiveCases {

  public static void main(String[] args) {
    Object ob = null;

    // testing constructor with object param (not boxed long)
    // BUG: Diagnostic contains: DateTime(java.lang.Object) is a banned constructor
    DateTime time = new DateTime(ob);

    // BUG: Diagnostic contains: LocalDateTime(java.lang.Object) is a banned constructor
    LocalDateTime time2 = new LocalDateTime(ob);

  }
}