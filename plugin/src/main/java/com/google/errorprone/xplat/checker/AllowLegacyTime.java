package com.google.errorprone.xplat.checker;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;

import java.lang.annotation.Target;

@Target({CONSTRUCTOR, METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
public @interface AllowLegacyTime {

}