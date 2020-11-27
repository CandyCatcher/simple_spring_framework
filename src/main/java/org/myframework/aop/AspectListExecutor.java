package org.myframework.aop;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.myframework.aop.aspect.AspectInfo;
import org.myframework.util.ValidationUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 往被代理的类中添加横切逻辑
 */
public class AspectListExecutor implements MethodInterceptor {

    //被代理的类
    Class<?> targetClass;

    /**
     * AspectList 由于我们需要在程序运行过程中通过反射获取被@Aspect和@Order标签标记的实现类的列表
     * 并且实现类列表的执行顺序需要在这里去确定好的，所以还需要接受实现类实现的顺序
     */
    private List<AspectInfo> sortedAspectInfoList;

    public AspectListExecutor( Class<?> targetClass, List<AspectInfo> aspectInfoList) {
        this.targetClass = targetClass;
        /*
         * 既然aspectInfo中已经有order的信息了，那么就直接在传入aspectInfoList时，就按优先级进行排序
         */
        this.sortedAspectInfoList = sortAspectInfoList(aspectInfoList);
    }

    /**
     * 按照order的值进行升序排序，确保order值小的Aspect先被织入
     */
    private List<AspectInfo> sortAspectInfoList(List<AspectInfo> aspectInfoList) {
        // 方法参数中有一个比较器
        Collections.sort(aspectInfoList, new Comparator<AspectInfo>() {
            @Override
            public int compare(AspectInfo o1, AspectInfo o2) {
                // 0表示两者相等
                return o1.getOrderIndex() - o2.getOrderIndex();
            }
        });
        return aspectInfoList;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object returnValue = null;
        if (ValidationUtil.isEmpty(sortedAspectInfoList)) {
            return null;
        }
        /*
        1.按照order的顺序升序执行完所有Aspect的before方法
        2.执行被代理类的方法
        3.如果被代理方法正常返回，则按照order的顺序降序执行完所有Aspect的afterReturning方法
        4.如果被代理方法抛出异常，则按照order的顺序降序执行完所有Aspect的afterThrowing方法
         */
        invokeBeforeAdvices(method, objects);
        try {
            returnValue =  methodProxy.invokeSuper(o, objects);
            returnValue = invokeAfterReturningAdvices(method, objects, returnValue);
        }  catch (Exception e) {
            invokeAfterThrowingAdvices(method, objects, e);
        }
        return returnValue;
    }

    private void invokeAfterThrowingAdvices(Method method, Object[] args, Exception e) throws Throwable {
        for (int i = sortedAspectInfoList.size() - 1; i >= 0; i--) {
            sortedAspectInfoList.get(i).getAspectObject().afterThrowing(targetClass, method, args, e);
        }
    }

    private Object invokeAfterReturningAdvices(Method method, Object[] args, Object returnValue) throws Throwable {
        Object result = null;
        for (int i = sortedAspectInfoList.size() - 1; i >= 0; i--) {
            result = sortedAspectInfoList.get(i).getAspectObject().afterReturning(targetClass, method, args, returnValue);
        }
        return result;
    }

    private void invokeBeforeAdvices(Method method, Object[] args) throws Throwable {
        for (AspectInfo aspectInfo : sortedAspectInfoList) {
            aspectInfo.getAspectObject().before(targetClass, method, args);
        }
    }
}
