package org.myframework.aop.aspect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.myframework.aop.annotation.PointcutLocator;

@AllArgsConstructor
@Getter
public class AspectInfo {
    private int orderIndex;
    private DefaultAspect aspectObject;
    private PointcutLocator pointcutLocator;
}
