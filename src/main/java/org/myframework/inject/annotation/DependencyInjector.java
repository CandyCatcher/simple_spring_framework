package org.myframework.inject.annotation;

import lombok.extern.slf4j.Slf4j;
import org.myframework.core.BeanContainer;
import org.myframework.util.ClassUtil;
import org.myframework.util.ValidationUtil;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 创建spring框架的思路是什么？
 * 给contriller、service、dao的实现类的成员变量、类等添加注解标签，通过一个class将这些类管理起来
 * 框架具备的基本功能：
 * 1.解析配置——解析语言的功能，这里使用注解
 * 2.定位与注册对象——解析完配置文件之后，需要通过配置信息去定位到目标对象
 *                 比如说解析完配置之后，就知道去哪里寻找确定的service、controller、dao等
 *                 定位就涉及到了标记，而注解就是一个很好的标记方式
 * 3.注入对象——在用户需要用的时候，将对象实例精确无误的返回给用户
 * 4.提供通用的工具类
 *
 * 核心：IoC容器
 *
 * IoC容器实现的思路：
 * 创建注解--标记目标对象--定位标记，提取标记对象--实现控制反转，创建出容器，并将对象的信息存储到容器中。在这里容器中直接存储的是类名和类的实例
 * （在spring IoC中，为了实现更精细的控制，存储的是类的class对象、注解、xml的配置信息）
 * --使用IoC容器实现依赖注入
 *
 * 实现过程：
 * 1.在core下创建annotation的包，并创建注解
 * 2.创建注解后，需要提取出对象，提取对象首先需要做的是定位到对象。
 *   那么怎么定位到对象呢？
 *   实现思路：
 *   肯定是通过遍历来查找，那要在哪一个范围内进行遍历呢？范围的确定肯定不是框架的范畴，而是框架的使用者进行确定的。需要使用者根据业务项目确定范围，
 *   也就是遍历业务项目这个范围。
 *   1. 指定范围，获取范围内所有的类
 *   2. 遍历所有的类，获取被注解标记的类并加载到容器中
 * 3.实现容器的依赖注入
 *   实现思路：
 *   1. 定义相关的注解标签，使用标签标注以后需要容器注入bean实例的地方
 *   2. 实现创建被注解标记的成员变量实例，并将其注入到成员变量里
 *   3. 使用注解
 *
 * 在这里只实现成员变量的注入
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
     * 1.遍历Bean容器中所有的Class对象
     * 2.遍历Class对象的所有成员对象
     * 3.找出被Autowired标记的成员变量
     * 4.获取这些成员变量的类型
     * 5.获取这些成员变量类型在容器中对应的实例
     * 6.通过反射将对应的成员变量实例注入到成员变量所在类的实例中
     */
    public void doIoC() {
        //1.遍历Bean容器中所有的Class对象
        if (ValidationUtil.isEmpty(beanContainer.getClasses())) {
            log.warn("empty classSet in BeanContainer");
            return;
        }

        //foreach之前最好要判空

        for (Class<?> clazz : beanContainer.getClasses()) {
            //2.遍历Class对象的所有成员对象
            // TODO Filed是？？？
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
                        // 知道类名就可以直接注入了
                        ClassUtil.setFiled(field, bean, fieldValue, true);
                    }
                }
            }
        }


    }

    /**
     * 根据class对象在BeanContainer中获取到实例
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
            //否则的话，有可能为接口，通过接口获取到实现类
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
            // TODO Qualifier
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
        return null;
    }

}
