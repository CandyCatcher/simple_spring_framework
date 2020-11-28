package org.myframework.aop.annotation;

import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * 解析Aspect表达式并定位被织入的目标
 * 不再使用固定的标签去筛选被代理的类
 */
public class PointcutLocator {
    /*
    pointcut解析器，直接给它赋值AspectJ的所有表达式，以便支持对众多表达式的解析

    pointParser是需要被创建出来的，并且需要装配上相关的语法树才能识别Aspect标签属性里面的pointcut表达式，所以调用这个方法
    这样就能解析这些语法树了
     */
    private PointcutParser pointcutParser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(
            // AspectJ所有的语法树
            PointcutParser.getAllSupportedPointcutPrimitives()
    );

    /*
    表达式解析器
    是PointParser根据表达式解析出来的产物，用来判断某个类或者方法是否匹配pointcut表达式
     */
    private PointcutExpression pointcutExpression;

    public PointcutLocator(String expression) {
        // pointcutParser解析expression的到对应的pointcutExpression
        /*
        经过这样的处理之后，就能针对@Aspect标签标记的类，获取标签里面的pointcut属性值
        并将相关的属性值传入到PointcutLocator的构造函数中，生成一个和本expression相对应的pointcutExpression
        也就是说PointcutLocator是和Aspect一一对应的
        既然是一一对应的，又有了pointcutExpression实例，我们就能去判断被代理的类或者方法是否满足和该PointcutLocator对应的Aspect对应的
        pointcut条件了
         */
        this.pointcutExpression = pointcutParser.parsePointcutExpression(expression);
    }

    /**
     * 判断传入的Class对象是否是Aspect的目标代理类，即匹配Pointcut表达式（初筛）
     * @param targrtClass 目标类
     * @return 是否匹配
     */
    public boolean roughMatches(Class<?> targrtClass) {
        /*
        couldMatchJoinPointsInType比较坑，只能校验within
        不能校验 (execution(精确到某个类除外), call, get, set)，面对无法校验的表达式，会直接返回true
         */
        return pointcutExpression.couldMatchJoinPointsInType(targrtClass);
    }

    /**
     * 判断传入的Method对象是否是Aspect的目标代理方法，即匹配Pointcut表达式(精筛)
     */
    public boolean accurateMatches(Method method){

        ShadowMatch shadowMatch = pointcutExpression.matchesMethodExecution(method);
        /*
        alwaysMatches方法用来判断是否完全匹配，还有其他匹配程度的方法
         */
        if(shadowMatch.alwaysMatches()){
            return true;
        } else{
            return false;
        }
    }
}
