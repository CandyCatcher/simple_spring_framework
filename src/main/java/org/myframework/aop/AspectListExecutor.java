package org.myframework.aop;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.myframework.aop.aspect.AspectInfo;
import org.myframework.util.ValidationUtil;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 针对每个被代理的对象进行方法的拦截，每个对象可能有多个Aspect
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
        // 这里对sortedAspectInfoList进行精筛
        collectAccurateMatchedAspectList(method);
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

    /**
     * 传入被代理方法的实例进行精筛，被代理方法实例是否真的匹配expression表达式。如果匹配就保留，否则就删除
     */
    private void collectAccurateMatchedAspectList(Method method) {
        if (ValidationUtil.isEmpty(sortedAspectInfoList)) {
            return;
        }
        /*
         在遍历过程中可能需要删除元素，如果还是使用foreach方法进行遍历，当移除sortedAspectInfoList中的元素时，会报出
         java.util.ConcurrentModificationException的异常，这是因为foreach中用到的iterator是工作在一个独立的线程中的，
         并且有一个mutex锁，iterator在被创建之后，会建立一个指向原来对象的单链索引表，当原来的对象的数量发生变化时，索引表的内容不会发生变化，
         当索引指针向后寻找的时候就找不到要迭代的对象了。接下来会按照一个fail-fast的原则，使得iterator马上抛出异常

         但是可以使用iterator本身的remove方法删除对象，因为当remove时，会维护前面说的索引表，所以使用迭代器的方式进行遍历
         */
        sortedAspectInfoList.removeIf(aspectInfo -> !aspectInfo.getPointcutLocator().accurateMatches(method));
        //Iterator<AspectInfo> it = sortedAspectInfoList.iterator();
        //while (it.hasNext()){
        //    AspectInfo aspectInfo = it.next();
        //    if(!aspectInfo.getPointcutLocator().accurateMatches(method)){
        //        it.remove();
        //    }
        //}
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
