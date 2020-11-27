package org.myframework.aop.aspect;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AspectInfo {
    private int orderIndex;
    private DefaultAspect aspectObject;
}
