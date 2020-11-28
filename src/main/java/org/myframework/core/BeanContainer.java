package org.myframework.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myframework.aop.annotation.Aspect;
import org.myframework.core.annotation.Component;
import org.myframework.core.annotation.Controller;
import org.myframework.core.annotation.Repository;
import org.myframework.core.annotation.Service;
import org.myframework.util.ClassUtil;
import org.myframework.util.ValidationUtil;

import javax.print.attribute.standard.JobKOctets;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器的组成：
 * 1. 既然是一个容器，那么它就能保存Class对象及其实例
 * 2. 容器需要根据配置获取并过滤目标对象————也就是注解标记的对象
 * 3. 需要对外提供容器载体的操作方式，便于客户端结合自己的需求操作载体中的对象
 *
 * 容器的实现
 *
 * 保存对象的载体
 * 加载容器
 * 对外提供容器的操作方式
 *
 */
@Slf4j
//私有的无参构造器
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanContainer {

    /**
     * 存放所有被配置标记的目标对象的Map
     * 以键值对保存目标class对象及其实例
     *
     * 功能一：容器使用一个Map来存储对象和对象实例
     */
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();


    /**
     * 功能二：1.实现容器的加载
     *
     * 实现思路：
     * 1. 怎么获取配置，配置的获取与管理：
     *
     * 2. 获取指定范围内的class对象：
     *    复用ClassUtil的方法即可
     * 3. 依据配置提取Class对象，连同实例一并存入到Map当中
     *
     * 加载bean的注解列表，以标记出被这几个注解标记的类，被标记的就要被管理起来了
     * 这里使用一个list用来存储注解
     */
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATION = Arrays.asList(Component.class, Controller.class,
            Repository.class, Service.class, Aspect.class);

    /**
     * 使用枚举创建单例
     * 获取bean容器实例
     *
     * 能够抵御反射和序列化入侵
     * @return BeanContainer
     */
    public static BeanContainer getInstance() {
        return ContainerHolder.HOLDER.instance;
    }

    private enum ContainerHolder {
        // instance是HOLDER的属性
        HOLDER;
        private BeanContainer instance;
        // 构造函数，默认是私有的
        ContainerHolder() {
            instance = new BeanContainer();
        }
    }

    /**
     * 容器没有被加载过bean
     */
    private boolean loaded = false;

    /**
     *
     * 判断容器bean是否被加载过
     * @return 是否被加载
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 功能二：2.依据配置提取Class对象，连同其实例一并存入容器当中
     *
     * 给一个范围值
     * 扫描加载package所有的Bean
     * @param packageName 包名
     */
    public synchronized void loadBeans(String packageName) {

        //判断bean容器是否被加载过
        if (isLoaded()) {
            log.warn("BeanContainer has been loaded");
            return;
        }

        Set<Class<?>> classSet = ClassUtil.extractPackageClass(packageName);

        if (ValidationUtil.isEmpty(classSet)) {
            log.warn("extract nothing from packageName" + packageName);
            return;
        }

        /*
        根据注解类型过滤出目标
         */
        for (Class<?> clazz : classSet) {
            for (Class<? extends Annotation> annotation : BEAN_ANNOTATION) {
                // 如果类上面标记了定义的注解
                // TODO：isAnnotationPresent方法？
                if (clazz.isAnnotationPresent(annotation)) {
                    //将目标类本身作为键，目标类的实例作为值，放入beanMap中
                    beanMap.put(clazz,  ClassUtil.newInstance(clazz, true));
                }
            }
        }

        loaded = true;
    }

    /**
     * 功能三：容器的操作：增删改查
     *
     * 添加一个bean对象
     * @param clazz Class对象
     * @param bean Bean对象
     * @return put操作会返回添加的Bean实例，没有则返回null
     */
    public Object addBean(Class<?> clazz, Object bean) {
        return beanMap.put(clazz, bean);
    }

    /**
     * 移除一个被容器管理的对象
     * @param clazz Class对象
     * @return 返回删除的Bean实例，没有则返回null
     */
    public Object removeBean(Class<?> clazz) {
        return beanMap.remove(clazz);
    }

    /**
     * 根据Class对象获取Bean实例
     * @param clazz Class对象
     * @return Bean实例
     */
    public Object getBean(Class<?> clazz) {
        return beanMap.get(clazz);
    }

    /**
     * 获取容器管理的所有class对象集合
     * @return Class对象
     */
    public Set<Class<?>> getClasses() {
        return beanMap.keySet();
    }

    /**
     * 获取所有的bean集合
     * @return Bean集合
     */
    public Set<Object> getBeans() {
        return new HashSet<>(beanMap.values());
    }

    /**
     * Bean实例数量
     * @return 数量
     */
    public int size() {
        return beanMap.size();
    }

    /**
     * 根据注解筛选出bean的Class集合
     * @param annotation 注解
     * @return Class集合
     */
    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        //1.获取beanMap的所有class对象
        Set<Class<?>> keySet = getClasses();
        if (ValidationUtil.isEmpty(keySet)) {
            log.warn("nothing in beanMap");
            return null;
        }
        //2.通过注解筛选被注解标注过的class对象，并添加到classSet中
        Set<Class<?>> classSet = new HashSet<>();
        for (Class<?> clazz : keySet) {
            //判断类是否有相关的注解标记
            if (clazz.isAnnotationPresent(annotation)) {
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0 ? classSet : null;
    }

    /**
     * 通过接口或者父类获取实现类或者子类的Class集合，不包括其本身
     *
     * @param interfaceOrClass 接口Class或者父类Class
     * @return Class集合
     */
    public Set<Class<?>> getClassesBySuper(Class<?> interfaceOrClass) {
        //1.获取beanMap的所有class对象
        Set<Class<?>> keySet = getClasses();
        if (ValidationUtil.isEmpty(keySet)) {
            log.warn("nothing in beanMap");
            return null;
        }
        //2.判断keySet里面元素是否是传入的接口或者类的子类，如果是，就将其添加到classSet中
        Set<Class<?>> classSet = new HashSet<>();
        for (Class<?> clazz : keySet) {
            //判断keySet里的元素是否是传入的接口或者类的子类
            //因为isAssignableFrom方法本身和本身也认为true的，所以需要添加一个验证
            if (clazz.isAssignableFrom(clazz) && !clazz.equals(interfaceOrClass)) {
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0? classSet : null;
    }
}
