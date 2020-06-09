package com.google.errorprone.xplat.checker;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.common.collect.ImmutableSet;
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

  private static final ImmutableSet<String> CLASS_NAMES =
      ImmutableSet
          .of("org.joda.time.LocalDateTime", "org.joda.time.LocalDate", "org.joda.time.LocalTime");


  private static final Matcher<ExpressionTree> CONSTRUCTOR_MATCHER =
      Matchers.allOf(
          Matchers.anyOf(
              CLASS_NAMES.stream()
                  .map(
                      typeName ->
                          Matchers.constructor()
                              .forClass(typeName)
                              .withParameters("org.joda.time.DateTimeZone"))
                  .collect(toImmutableList())),
          // Allow usage by JodaTime itself
          Matchers.not(Matchers.packageStartsWith("org.joda.time")));

  private static final Matcher<ExpressionTree> METHOD_MATCHER =
      Matchers.allOf(
          Matchers.anyOf(
              CLASS_NAMES.stream()
                  .map(
                      className ->
                          Matchers.instanceMethod()
                              .onExactClass(className)
                              .named("toDateTime"))
                  .collect(toImmutableList())),
          // Allow usage by JodaTime itself
          Matchers.not(Matchers.packageStartsWith("org.joda.time")));


  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (METHOD_MATCHER.matches(tree, state)) {

      ExpressionTree recv = ASTHelpers.getReceiver(tree);

      String recvSource = state.getSourceForNode(recv);

      return buildDescription(tree)
          .setMessage(
              String.format("The use of %s is banned from cross platform development due to"
                  + " incompatibilities.", ASTHelpers.getSymbol(tree)))
          .addFix(
              SuggestedFix.replace(
                  state.getEndPosition(ASTHelpers.getReceiver(tree)) - state.getSourceForNode(recv)
                      .length(),
                  state.getEndPosition(tree),
                  String.format("new DateTime(%s.getYear(), %s.getMonthOfYear(),"
                          + " %s.getDayOfYear(), %s.getHourOfDay(),"
                          + " %s.getMinuteOfHour(), %s.getSecondOfMinute(), %s.getMillisOfSecond(),"
                          + " %s)", recvSource, recvSource, recvSource, recvSource, recvSource,
                      recvSource, recvSource, tree.getArguments())))
          .build();
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    if (CONSTRUCTOR_MATCHER.matches(tree, state)) {
      return buildDescription(tree)
          .setMessage(
              String.format(
                  "The use of %s is banned from cross platform development due to"
                      + " incompatibilities. Please use a different constructor.",
                  ASTHelpers.getSymbol(tree)))
          .build();
    }
    return Description.NO_MATCH;
  }
}
