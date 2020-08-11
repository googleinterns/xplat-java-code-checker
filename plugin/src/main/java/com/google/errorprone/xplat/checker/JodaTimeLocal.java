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
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import java.util.List;

/**
 * Bans the use of some Joda-Time constructors and methods that convert to a DateTime or use a
 * DateTimeZone. A fix is suggested that is cross platform compatible.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "JodaTimeLocal",
    summary = "Bans the usage of timezoned toDateTime methods in some Joda-Time classes.",
    explanation =
        "The usage of timezoned LocalDateTime, LocalDate and LocalTime Joda-Time constructors"
            + " and toDateTime methods are banned from cross platform development"
            + " due to incompatibilities. A fix using a new DateTime is suggested.",
    severity = ERROR)
public class JodaTimeLocal extends BugChecker implements MethodInvocationTreeMatcher,
    NewClassTreeMatcher {

  private static final ImmutableMap<String, ImmutableList<String>> DISALLOWED_CLASS_METHOD_MAP =
      ImmutableMap.of("org.joda.time.LocalDateTime", ImmutableList.of("toDateTime"),

          "org.joda.time.LocalDate",
          ImmutableList.of("toDateTime", "toDateTimeAtCurrentTime", "toDateTimeAtStartOfDay"),

          "org.joda.time.LocalTime", ImmutableList.of("toDateTimeToday"));


  private static final Matcher<ExpressionTree> CONSTRUCTOR_MATCHER =
      Matchers.anyOf(
          Matchers.anyOf(
              DISALLOWED_CLASS_METHOD_MAP.keySet().stream()
                  .map(
                      typeName ->
                          Matchers.constructor()
                              .forClass(typeName)
                              .withParameters("org.joda.time.DateTimeZone"))
                  .collect(toImmutableList())),
          Matchers.constructor()
              .forClass("org.joda.time.LocalDateTime")
              .withParameters("long", "org.joda.time.DateTimeZone"),
          Matchers.constructor()
              .forClass("org.joda.time.LocalDateTime")
              .withParameters("java.lang.Object", "org.joda.time.DateTimeZone"));

  private static Matcher<ExpressionTree> methodMatcher(String key) {
    return Matchers.anyOf(
        DISALLOWED_CLASS_METHOD_MAP.get(key).stream()
            .map(
                methodName ->
                    Matchers.instanceMethod().onExactClass(key).named(methodName))
            .collect(toImmutableList()));
  }

  private static final Matcher<ExpressionTree> LOCAL_DATE_TIME_METHOD_MATCHER =
      methodMatcher("org.joda.time.LocalDateTime");

  private static final Matcher<ExpressionTree> LOCAL_TIME_METHOD_MATCHER =
      methodMatcher("org.joda.time.LocalTime");

  private static final Matcher<ExpressionTree> LOCAL_DATE_METHOD_MATCHER =
      methodMatcher("org.joda.time.LocalDate");

  private Description.Builder message(Tree tree, String arg) {
    return buildDescription(tree)
        .setMessage(
            String.format("The use of %s is banned from cross platform development due to"
                + " incompatibilities.", arg));
  }

  private Description messageFix(Tree tree, String arg, int start, int end,
      String replacement) {
    return message(tree, arg)
        .addFix(
            SuggestedFix.builder()
                .addImport("org.joda.time.DateTimeZone")
                .replace(
                    start,
                    end,
                    replacement)
                .build())
        .build();
  }


  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    ExpressionTree recv = ASTHelpers.getReceiver(tree);
    if (recv == null) {
      return Description.NO_MATCH;
    }

    String recvSrc = state.getSourceForNode(recv);
    Symbol symbol = ASTHelpers.getSymbol(tree);
    List<? extends ExpressionTree> arguments = tree.getArguments();

    if (symbol == null || recvSrc == null || recv == null) {
      return Description.NO_MATCH;
    }

    String argument;
    if (arguments.isEmpty()) {
      argument = "DateTimeZone.getDefault()";
    } else {
      argument = state.getSourceForNode(arguments.get(0));
    }

    if (LOCAL_DATE_TIME_METHOD_MATCHER.matches(tree, state)) {

      return messageFix(tree, symbol.toString(), ((JCTree) recv).getStartPosition(),
          state.getEndPosition(tree),
          String.format("new DateTime(%s.getYear(), %<s.getMonthOfYear(),"
                  + " %<s.getDayOfYear(), %<s.getHourOfDay(),"
                  + " %<s.getMinuteOfHour(), %<s.getSecondOfMinute(),"
                  + " %<s.getMillisOfSecond(), %s)",
              recvSrc, argument));

    } else if (LOCAL_TIME_METHOD_MATCHER.matches(tree, state)) {

      return messageFix(tree, symbol.toString(), ((JCTree) recv).getStartPosition(),
          state.getEndPosition(tree), String.format("new DateTime().toDateTime(%s).withTime(%s)",
              argument, recvSrc));

    } else if (LOCAL_DATE_METHOD_MATCHER.matches(tree, state)) {

      if (symbol.name.toString().equals("toDateTime")) {
        String zone;

        if (arguments.size() == 1) {
          zone = "DateTimeZone.getDefault()";
        } else {
          zone = state.getSourceForNode(arguments.get(1));
        }

        return messageFix(tree, symbol.toString(), ((JCTree) recv).getStartPosition(),
            state.getEndPosition(tree),
            String.format("new DateTime(%s.getYear(), %<s.getMonthOfYear(),"
                    + " %<s.getDayOfYear(), %s.getHourOfDay(),"
                    + " %<s.getMinuteOfHour(), %<s.getSecondOfMinute(),"
                    + " %<s.getMillisOfSecond(), %s)",
                recvSrc, argument, zone));

      } else if (symbol.name.toString().equals("toDateTimeAtStartOfDay") && arguments.isEmpty()) {

        return messageFix(tree, symbol.toString(), state.getEndPosition(recv),
            state.getEndPosition(tree), ".toDateTimeAtStartOfDay(DateTimeZone.getDefault())");

      } else if (symbol.name.toString().equals("toDateTimeAtCurrentTime")) {

        return messageFix(tree, symbol.toString(), ((JCTree) recv).getStartPosition(),
            state.getEndPosition(tree), String.format("new DateTime().toDateTime(%s).withDate(%s)",
                argument, recvSrc));
      }

    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    Symbol symbol = ASTHelpers.getSymbol(tree);

    if (CONSTRUCTOR_MATCHER.matches(tree, state) && symbol != null) {
      return message(tree, symbol.toString()).build();
    }
    return Description.NO_MATCH;
  }
}
