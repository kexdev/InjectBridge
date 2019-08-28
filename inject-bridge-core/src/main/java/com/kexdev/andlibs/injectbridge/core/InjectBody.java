package com.kexdev.andlibs.injectbridge.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface InjectBody {
    String target();
    int priority() default 0;
}
