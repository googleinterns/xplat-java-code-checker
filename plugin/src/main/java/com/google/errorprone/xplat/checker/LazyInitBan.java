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
package com.google.errorprone.xplat.checker;


import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.VariableTree;

@AutoService(BugChecker.class)
@BugPattern(
    name = "LazyInitBan",
    summary = "Lazy Init is an error prone pattern on mobile.",
    explanation =
        "TBD",
    severity = ERROR)
public class LazyInitBan extends BugChecker implements VariableTreeMatcher {


  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {

    return Description.NO_MATCH;
  }

}
