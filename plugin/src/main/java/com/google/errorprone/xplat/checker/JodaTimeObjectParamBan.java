package com.google.errorprone.xplat.checker;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;

/**
 * Bans the usage of Joda-Time methods and constructors that have an Object parameter. If the
 * parameter that is passed in is a boxed long, it is permitted.
 */
@BugPattern(
    name = "JodaTimeObjectParamBan",
    summary = "Bans the usage of Joda-Time methods and constructors that have an Object parameter.",
    explanation =
        "The usage of Joda-Time methods and constructors that have an Object parameter are"
            + " banned from cross platform development due to the dangers of passing null as the "
            + "parameter. If the parameter is a boxed long, it is allowed to be used.",
    severity = ERROR)
public class JodaTimeObjectParamBan extends BugChecker implements MethodInvocationTreeMatcher,
    NewClassTreeMatcher {

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return null;
  }

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    return null;
  }
}
