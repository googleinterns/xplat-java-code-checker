package com.google.errorprone.xplat.checker;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Type;

/**
 * Checks for usage of legacy time classes in Variables, Methods and Parameters. Can be overridden
 * with the @AllowLegacyTime annotation.
 */
@BugPattern(
    name = "LegacyTimeBan",
    summary = "Java legacy time class ban - override with @AllowLegacyTime.",
    explanation =
        "The usage of several legacy time classes are banned from cross platform development due"
            + " to incompatibilities. If one is sure that they must use one of them, the"
            + " @AllowLegacyTime annotation will override the error.",
    severity = ERROR)
public class LegacyTimeBan extends BugChecker implements MethodTreeMatcher, VariableTreeMatcher {

  private static final ImmutableSet<String> BANNED_CLASSES =
      ImmutableSet.of("java.util.Calendar", "java.util.Calendar.Builder", "java.util.Date",
          "java.util.GregorianCalendar", "java.util.TimeZone", "java.util.SimpleTimeZone");

  private static final Matcher<MethodTree> METHOD_MATCHER =
      Matchers.allOf(
          Matchers.anyOf(
              BANNED_CLASSES.stream()
                  .map(
                      className ->
                          Matchers.methodReturns(Matchers.isSameType(className)))
                  .collect(toImmutableList())),
          Matchers.not(Matchers.hasAnnotation(AllowLegacyTime.class.getCanonicalName()))
      );

  private static final Matcher<VariableTree> VAR_MATCHER =
      Matchers.allOf(
          Matchers.anyOf(
              BANNED_CLASSES.stream()
                  .map(Matchers::isSameType)
                  .collect(toImmutableList())),
          Matchers.not(Matchers.hasAnnotation(AllowLegacyTime.class.getCanonicalName()))
      );

  private Description message(Tree tree, String className) {
    return buildDescription(tree)
        .setMessage(
            String.format("%s is banned for cross platform development due to incompatibilities."
                    + " If you must use it, please use the @AllowLegacyTime annotation.",
                className))
        .build();
  }


  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (METHOD_MATCHER.matches(tree, state)) {
      return message(tree, ASTHelpers.getSymbol(tree).getReturnType().toString());
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    Type varType = ASTHelpers.getType(tree);

    if (VAR_MATCHER.matches(tree, state)) {
      if (varType != null) {
        return message(tree, varType.toString());
      }
    }
    return Description.NO_MATCH;
  }
}
