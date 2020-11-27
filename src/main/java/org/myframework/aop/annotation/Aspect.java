package org.myframework.aop.annotation;

import java.lang.annotation.*;

// 作用在类上
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    /**
     * 定义一个属性定义Aspect的目标，对应的类型是注解类型的
     * 当前被Aspect标签标记的横切逻辑，是会织入到被属性值里的注解标签标记的那些类里
     * 比如，当value为controller，表示会将Aspect里的横切逻辑，会织入到被@Controller标记的类里
     */
    Class<? extends Annotation> value();
}
