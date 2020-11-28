package org.myframework.aop;

import org.myframework.aop.annotation.Aspect;
import org.myframework.aop.annotation.Order;
import org.myframework.aop.annotation.PointcutLocator;
import org.myframework.aop.aspect.AspectInfo;
import org.myframework.aop.aspect.DefaultAspect;
import org.myframework.core.BeanContainer;
import org.myframework.util.ValidationUtil;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.*;

/**
 * springAOP1.0是spring自研的，使用起来不是很方便，需要实现各种各样的接口，并继承指定的类
 * springAOP2.0集成了AspectJ，复用AspectJ的语法树，即它对Aspect和advice的定位功能，默认没有用ajc编译工具
 *
 * AspectJ框架提供了完整的AOP解决方案，是AOP的Java实现版本
 * 1.定义了切面语法以及切面语法的解析机制
 * 2.提供了强大的织入工具
 *
 * AspectJ框架的织入时机：静态织入和LTW
 * 1.编译时织入：利用ajc，讲切面逻辑织入到类里生成class文件
 * 2.编译后织入：利用ajc，修改javac编译出来的class文件
 * 3.类加载时期织入：利用Java agent，在类加载的时候织入切面逻辑（修改了类的字节码）
 *
 * 将横切逻辑织入到被代理的对象以生成动态代理对象
 */
public class AspectWeaver {
    /*
    需要在容器中筛选出两种类型的bean，一种是被代理的bean，一种是被Aspect签标记的bean
     */
    private BeanContainer beanContainer;

    public AspectWeaver() {
        /*
        因为容器绝对是单例的，在这里获取到容器的实例
         */
        this.beanContainer = BeanContainer.getInstance();
    }

    public void doAop() {
        /*
        1.获取所有的切面类
        获取切面类后去获取切面类里面@Aspect标签的属性值，才能知道切面类是服务哪些被代理的bean的
        同时还需要获取到order值以便于后序进行排序
         */
        Set<Class<?>> aspectSet = beanContainer.getClassesByAnnotation(Aspect.class);

        /*-------------------------------------------之前的-----------------------------------------------------------*/

        ///*
        // 2.将切面类按照不同的织入目标进行分类
        // 因为被Aspect标记的类，里面的属性值可能是不同的，不同则意味着注入的目标类是不同的
        // 这样就能保存对于不同的注解，它们的对应的类了
        // */
        //Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap = new HashMap<>();
        //if (ValidationUtil.isEmpty(aspectSet)) {
        //    return;
        //}
        //for (Class<?> aspectClass : aspectSet) {
        //    if (verifyAspect(aspectClass)) {
        //        // 执行Aspect的分类逻辑
        //        categorizeAspect(categorizedMap, aspectClass);
        //    } else {
        //        throw new RuntimeException("@Aspect and @Order must be added to the Aspect class," +
        //                " and Aspect class must extend from DefaultAspect");
        //    }
        //}
        ///*
        // 按照不同的织入目标分别去按序织入Aspect的逻辑
        // */
        //if (ValidationUtil.isEmpty(categorizedMap)) {
        //    return;
        //}
        //for (Class<? extends Annotation> category : categorizedMap.keySet()) {
        //    weaveByCategory(category, categorizedMap.get(category));
        //}

        /*-------------------------------------------AspectJ的-----------------------------------------------------------*/

        /*
        先前是按照Aspect标签的属性值进行分类的，但是对于pointcut表达式来讲，这样的分类没有意义
        因为不同的pointcut表达式可能会获取到的是不同的目标
        比如"execution(* com.imooc.controller.frontend..*.*(..))"以及within(com.imooc.controller.frontend.*)
        都是表达对frontend包中所有的类进行织入操作
        又因为PointcutLocator是和Aspect一一对应的，
        可以为被Aspect标签标记的类创建一个专门的PointcutLocator去按照Aspect里的expression去匹配目标类和目标方法
        所以就不需要对Aspect进行分类了
         */
        if (ValidationUtil.isEmpty(aspectSet)) {
            return;
        }
        //    2.拼接AspectInfoList 拼接@Aspect对应的order值和pointcutLocator
        List<AspectInfo> aspectInfoList = packAspectInfoList(aspectSet);
        //    3.遍历容器里的类
        Set<Class<?>> classSet = beanContainer.getClasses();
        for (Class<?> targetClass: classSet) {
            // 排除自身
            if (targetClass.isAnnotationPresent(Aspect.class)) {
                continue;
            }
            /*
            4.粗筛符合条件的Aspect
            就是看一下pointcutLocator里面的pointcut表达式expression和当前类targetClass是否匹配
            如果匹配，就这个类装进新的列表中
             */
            List<AspectInfo> roughMatchedAspectList  = collectRoughMatchedAspectListForSpecificClass(aspectInfoList, targetClass);
            // 5.尝试进行Aspect的织入
            wrapIfNecessary(roughMatchedAspectList, targetClass);
        }
    }

    private void wrapIfNecessary(List<AspectInfo> roughMatchedAspectList, Class<?> targetClass) {
        if(ValidationUtil.isEmpty(roughMatchedAspectList)){
            return;
        }
        //创建出动态代理对象
        AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass, roughMatchedAspectList);
        Object proxyBean = ProxyCreator.createProxy(targetClass, aspectListExecutor);
        beanContainer.addBean(targetClass, proxyBean);
    }

    private List<AspectInfo> collectRoughMatchedAspectListForSpecificClass(List<AspectInfo> aspectInfoList, Class<?> targetClass) {
        List<AspectInfo> roughMatchedAspectList = new ArrayList<>();
        for(AspectInfo aspectInfo : aspectInfoList){
            //粗筛
            if(aspectInfo.getPointcutLocator().roughMatches(targetClass)){
                roughMatchedAspectList.add(aspectInfo);
            }
        }
        return roughMatchedAspectList;
    }

    private List<AspectInfo> packAspectInfoList(Set<Class<?>> aspectSet) {
        List<AspectInfo> aspectInfoList = new ArrayList<>();
        for(Class<?> aspectClass : aspectSet){
            if (verifyAspect(aspectClass)){
                Order orderTag = aspectClass.getAnnotation(Order.class);
                Aspect aspectTag = aspectClass.getAnnotation(Aspect.class);
                DefaultAspect defaultAspect = (DefaultAspect) beanContainer.getBean(aspectClass);
                //初始化表达式定位器
                PointcutLocator pointcutLocator = new PointcutLocator(aspectTag.pointcut());
                AspectInfo aspectInfo = new AspectInfo(orderTag.value(), defaultAspect, pointcutLocator);
                aspectInfoList.add(aspectInfo);
            } else {
                //不遵守规范则直接抛出异常
                throw new RuntimeException("@Aspect and @Order must be added to the Aspect class, and Aspect class must extend from DefaultAspect");
            }
        }
        return aspectInfoList;
    }

    ///**
    // * @param category 被织入的目标
    // * @param aspectInfos 织入的信息
    // */
    //private void weaveByCategory(Class<? extends Annotation> category, List<AspectInfo> aspectInfos) {
    //    /*
    //    1.获取被代理类的集合
    //    被标签标记的类
    //    */
    //    Set<Class<?>> classSet = beanContainer.getClassesByAnnotation(category);
    //    if (ValidationUtil.isEmpty(classSet)) {
    //        return;
    //    }
    //    /*
    //    2.遍历被代理的类，分别为每个被代理类生成动态代理实例
    //     */
    //    for (Class<?> targetClass : classSet) {
    //        // 创建动态代理对象
    //        AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass, aspectInfos);
    //        Object proxyBean = ProxyCreator.createProxy(targetClass, aspectListExecutor);
    //        /*
    //        3.将动态代理对象实例添加到容器里，取代未被代理前的类实例
    //        */
    //        beanContainer.addBean(targetClass,proxyBean);
    //    }
    //}
    //
    //private void categorizeAspect(Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap, Class<?> aspectClass) {
    //    // 获取order标签实例
    //    Order orderTag = aspectClass.getAnnotation(Order.class);
    //    Aspect aspectTag = aspectClass.getAnnotation(Aspect.class);
    //    // 获取Aspect实例，
    //    DefaultAspect aspect = (DefaultAspect) beanContainer.getBean(aspectClass);
    //    AspectInfo aspectInfo = new AspectInfo(orderTag.value(), aspect);
    //    if (!categorizedMap.containsKey(aspectTag.value())) {
    //        // 如果织入的joinpoint是第一次出现，那么以该joinpoint为key，以新创建的List<AspectInfo>为value
    //        List<AspectInfo> aspectInfoList = new ArrayList<>();
    //        aspectInfoList.add(aspectInfo);
    //        categorizedMap.put(aspectTag.value(), aspectInfoList);
    //    } else {
    //        List<AspectInfo> aspectInfoList = categorizedMap.get(aspectTag.value());
    //        aspectInfoList.add(aspectInfo);
    //    }
    //}

    /**
     * 框架中一定要遵循给Aspect类添加@Aspect（通过这个标签指明向哪些类织入）
     * 和@Order标签（通过这个标签知道Aspect的顺序）的规范，同时必须继承自DefaultAspect.class
     * 此外，@Aspect的值不能是自身（陷入死循环）
     */
    //private boolean verifyAspect(Class<?> aspectClass) {
    //    return aspectClass.isAnnotationPresent(Aspect.class) &&
    //            aspectClass.isAnnotationPresent(Order.class) &&
    //            // 标签是DefaultAspect class的子类
    //            DefaultAspect.class.isAssignableFrom(aspectClass) &&
    //            aspectClass.getAnnotation(Aspect.class).value() != Aspect.class;
    //}
    private boolean verifyAspect(Class<?> aspectClass) {
        return aspectClass.isAnnotationPresent(Aspect.class) &&
                aspectClass.isAnnotationPresent(Order.class) &&
                // 标签是DefaultAspect class的子类
                DefaultAspect.class.isAssignableFrom(aspectClass);
    }

}
