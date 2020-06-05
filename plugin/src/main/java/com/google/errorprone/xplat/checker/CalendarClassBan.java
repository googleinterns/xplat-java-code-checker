package com.google.errorprone.xplat.checker;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.Arrays;


@BugPattern(
    name = "JodaTimeClassBan",
    summary = "java.util.Calendar ban - override with @AllowLegacyTime",
    explanation =
        "The usage of java.util.Calendar is banned from cross platform development due"
            + " to incompatibilities. If one is sure that they must use it, the "
            + "@AllowLegacyTime annotation will allowed to override the error.",
    severity = ERROR)
public class CalendarClassBan extends BugChecker implements MethodInvocationTreeMatcher,
    ClassTreeMatcher {


  private static final String CALENDAR_CLASS = "java.util.Calendar";


  private static final Matcher<ClassTree> HAS_ALLOWLEGACYTIME =
      Matchers.hasAnnotation(AllowLegacyTime.class.getCanonicalName());


  private static final Matcher<ExpressionTree> METHOD_MATCHER =
      Matchers.anyOf(
          Matchers.instanceMethod().onExactClass(CALENDAR_CLASS),
          Matchers.staticMethod().onClass(CALENDAR_CLASS)
      );

  private boolean annotated = false;

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {

    System.out
        .println(ASTHelpers.getSymbol(tree));

    if (METHOD_MATCHER.matches(tree, state) && !this.annotated) {
      return buildDescription(tree)
          .setMessage(
              String.format("%s is banned for cross platform development due to incompatibilities."
                      + " If you must use it, please use the @AllowLegacyTime annotation.",
                  CALENDAR_CLASS))
          .build();
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (HAS_ALLOWLEGACYTIME.matches(tree, state)) {
      System.out.println("Found!");
      this.annotated = true;
    }
    return Description.NO_MATCH;
  }
}

