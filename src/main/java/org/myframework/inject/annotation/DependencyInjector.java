package org.myframework.inject.annotation;

import lombok.extern.slf4j.Slf4j;
import org.myframework.core.BeanContainer;
import org.myframework.util.ClassUtil;
import org.myframework.util.ValidationUtil;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 提供依赖注入的服务
 */
@Slf4j
public class DependencyInjector {

    /**
     * Bean容器
     */
    private BeanContainer beanContainer;
    public DependencyInjector() {
        beanContainer = BeanContainer.getInstance();
    }

    /**
     * 执行IoC
     */
    public void doIoC() {
        //1.遍历Bean容器中所有的Class对象
        if (ValidationUtil.isEmpty(beanContainer.getClasses())) {
            log.warn("empty classSet in BeanContainer");
            return;
        }
        for (Class<?> clazz : beanContainer.getClasses()) {
            //2.遍历Class对象的所有成员对象
            Field[] fields = clazz.getDeclaredFields();
            //3.找出被Autowired标记的成员变量
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    //获取到autowired实例，从而获取到autowired中设定的值
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String autowiredValue = autowired.value();
                    //4.获取这些成员变量的类型
                    Class<?> fieldClass = field.getType();
                    //5.获取这些成员变量类型在容器中对应的实例
                    Object fieldValue = getFieldInstance(fieldClass, autowiredValue);
                    if (fieldValue == null) {
                        throw new RuntimeException("unable to inject relevant type, target fieldClass is : " + fieldClass.getName() + "autowiredValue is : " + autowiredValue);
                    } else {
                        //6.通过反射将对应的成员变量实例注入到成员变量所在类的实例中
                        Object bean = beanContainer.getBean(clazz);
                        ClassUtil.setFiled(field, bean, fieldValue, true);
                    }
                }
            }
        }


    }

    /**
     * 
     * @param fieldClass
     * @return
     */
    private Object getFieldInstance(Class<?> fieldClass, String autowiredValue) {
        //获取成员变量的实例
        Object field = beanContainer.getBean(fieldClass);
        if (field != null) {
            //如果field不为null，那么说明成员变量的类型不是接口
            return field;
        } else {
            //否则的话，为接口
            Class<?> implementClass = getImplementClass(fieldClass, autowiredValue);
            if (implementClass != null) {
                return beanContainer.getBean(implementClass);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取接口的实现类
     * @param fieldClass
     * @return
     */
    private Class<?> getImplementClass(Class<?> fieldClass, String autowiredValue) {
        Set<Class<?>> classSet = beanContainer.getClassesBySuper(fieldClass);
        if (!ValidationUtil.isEmpty(classSet)) {
            //同一个接口多个实现类怎么办？ Qualifier注解说明使用哪一个实现类
            if (ValidationUtil.isEmpty(autowiredValue)) {
                //没有告诉实现类
                if (classSet.size() == 1) {
                    return classSet.iterator().next();
                } else {
                    //用户没有指定其中哪一个实现类，抛出异常
                    throw new RuntimeException("multiple implemented classes for " + fieldClass.getName() + "please set @Autowired's value to pick one");
                }
            } else {
                for (Class<?> clazz : classSet) {
                    if (autowiredValue.equals(clazz.getSimpleName())) {
                        return clazz;
                    }
                }
            }
        }
        return null;
    }

}
