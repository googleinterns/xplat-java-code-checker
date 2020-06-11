package com.google.errorprone.xplat.checker;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Description.Builder;
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

@BugPattern(
    name = "JodaTimeLocal",
    summary = "Bans the usage of timezoned toDateTime in some Joda-Time classes.",
    explanation =
        "The usage of timezoned LocalDateTime, LocalDate and LocalTime Joda-Time constructors"
            + "/toDateTime methods are banned from cross platform development"
            + " due to incompatibilities. A fix using a new DateTime is suggested.",
    severity = ERROR)
public class JodaTimeLocal extends BugChecker implements MethodInvocationTreeMatcher,
    NewClassTreeMatcher {

  private static final ImmutableMap<String, ImmutableList<String>> CLASS_NAMES =
      ImmutableMap.of("org.joda.time.LocalDateTime", ImmutableList.of("toDateTime"),

          "org.joda.time.LocalDate",
          ImmutableList.of("toDateTime", "toDateTimeAtCurrentTime", "toDateTimeAtStartOfDay"),

          "org.joda.time.LocalTime", ImmutableList.of("toDateTimeToday"));


  private static final Matcher<ExpressionTree> CONSTRUCTOR_MATCHER =
      Matchers.anyOf(
          Matchers.anyOf(
              CLASS_NAMES.keySet().stream()
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

  private Matcher<ExpressionTree> methodMatcher(String key) {
    return Matchers.anyOf(
        CLASS_NAMES.get(key).stream()
            .map(
                methodName ->
                    Matchers.instanceMethod().onExactClass(key).named(methodName))
            .collect(toImmutableList()));
  }

  private Description.Builder message(Tree tree, String arg) {
    return buildDescription(tree)
        .setMessage(
            String.format("The use of %s is banned from cross platform development due to"
                + " incompatibilities.", arg));
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    ExpressionTree recv = ASTHelpers.getReceiver(tree);
    String recvSrc = state.getSourceForNode(recv);
    Symbol symbol = ASTHelpers.getSymbol(tree);
    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (symbol != null && recvSrc != null && recv != null) {
      if (methodMatcher("org.joda.time.LocalDateTime").matches(tree, state)) {

        String argument;
        if (arguments.isEmpty()) {
          argument = "DateTimeZone.getDefault()";
        } else {
          argument = arguments.toString();
        }

        return message(tree, symbol.toString())
            .addFix(
                SuggestedFix.builder()
                    .addImport("org.joda.time.DateTimeZone")
                    .replace(
                        ((JCTree) recv).getStartPosition(),
                        state.getEndPosition(tree),
                        String.format("new DateTime(%s.getYear(), %<s.getMonthOfYear(),"
                                + " %<s.getDayOfYear(), %<s.getHourOfDay(),"
                                + " %<s.getMinuteOfHour(), %<s.getSecondOfMinute(), "
                                + " %<s.getMillisOfSecond(), %s)",
                            recvSrc, argument)).build())
            .build();
      } else if (methodMatcher("org.joda.time.LocalTime").matches(tree, state)) {
        String argument;
        if (arguments.isEmpty()) {
          argument = "DateTimeZone.getDefault()";
        } else {
          argument = arguments.toString();
        }

        return message(tree, symbol.toString())
            .addFix(
                SuggestedFix.builder()
                    .addImport("org.joda.time.DateTimeZone")
                    .replace(
                        ((JCTree) recv).getStartPosition(),
                        state.getEndPosition(tree),
                        String.format("new DateTime().now(%s).withTime(%s.getHourOfDay(),"
                                + " %<s.getMinuteOfHour(), %<s.getSecondOfMinute(), "
                                + " %<s.getMillisOfSecond())",
                            argument, recvSrc)).build())
            .build();
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
