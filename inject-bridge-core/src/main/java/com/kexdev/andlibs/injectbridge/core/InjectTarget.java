package com.kexdev.andlibs.injectbridge.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
public @interface InjectTarget {
    String[] name() default {}; // default value is method name
    String model() default InjectConfig.MODEL_REPLACE;
}
