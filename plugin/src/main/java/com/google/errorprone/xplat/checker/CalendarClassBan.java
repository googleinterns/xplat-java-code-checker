package com.google.errorprone.xplat.checker;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

/**
 * Checks for usage of java.util.Calendar in Variables, Methods and Parameters. Can be overridden
 * with the @AllowLegacyTime annotation.
 */
@BugPattern(
    name = "JodaTimeClassBan",
    summary = "java.util.Calendar ban - override with @AllowLegacyTime.",
    explanation =
        "The usage of java.util.Calendar is banned from cross platform development due"
            + " to incompatibilities. If one is sure that they must use it, the "
            + "@AllowLegacyTime annotation will allowed to override the error.",
    severity = ERROR)
public class CalendarClassBan extends BugChecker implements MethodTreeMatcher, VariableTreeMatcher {

  private static final String CALENDAR_CLASS = "java.util.Calendar";

  private static final Matcher<MethodTree> METHOD_MATCHER =
      Matchers.allOf(
          Matchers.methodReturns(Matchers.isSameType(CALENDAR_CLASS)),
          Matchers.not(Matchers.hasAnnotation(AllowLegacyTime.class.getCanonicalName()))
      );

  private static final Matcher<VariableTree> VAR_MATCHER =
      Matchers.allOf(
          Matchers.isSameType(CALENDAR_CLASS),
          Matchers.not(Matchers.hasAnnotation(AllowLegacyTime.class.getCanonicalName()))
      );

  private Description message(Tree tree) {
    return buildDescription(tree)
        .setMessage(
            String.format("%s is banned for cross platform development due to incompatibilities."
                    + " If you must use it, please use the @AllowLegacyTime annotation.",
                CALENDAR_CLASS))
        .build();
  }


  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (METHOD_MATCHER.matches(tree, state)) {
      return message(tree);
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (VAR_MATCHER.matches(tree, state)) {
      return message(tree);
    }
    return Description.NO_MATCH;
  }
}
