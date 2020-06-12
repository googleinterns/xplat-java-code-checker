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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AssignmentTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.beans.Expression;
import java.util.concurrent.ConcurrentHashMap;

@BugPattern(
    name = "UnnecessaryConcurrentHashMap",
    summary = "Suggests the use of synchronizedMap instead of ConcurrentHashMap",
    explanation =
        "ConcurrentHashMap is not well suited for use on Android devices. For this reason,"
            + " synchronizedMap is suggested to be used in its place for better compatibility.",
    severity = WARNING)
public class UnnecessaryConcurrentHashMap extends BugChecker implements NewClassTreeMatcher,
    VariableTreeMatcher {

  private static final Matcher<Tree> MATCHER =
      Matchers.isSameType("java.util.concurrent.ConcurrentHashMap");

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    if (MATCHER.matches(tree, state)) {
      System.out.println(ASTHelpers.getType(tree));
      System.out.println();
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (MATCHER.matches(tree, state)) {
      System.out.println(ASTHelpers.getType(tree).getTypeArguments());
      System.out.println(state.getSourceForNode(tree));
      System.out.println();
    }
    return Description.NO_MATCH;
  }
}
