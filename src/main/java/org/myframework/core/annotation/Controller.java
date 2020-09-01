package org.myframework.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
//通过反射获取到信息，所以需要运行中
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
}
