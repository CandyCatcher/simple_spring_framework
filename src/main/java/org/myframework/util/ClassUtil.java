package org.myframework.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ClassUtil {

    public static final String FILE_PROTOCOL = "file";

    /**
     * 根据传入的包名，获取该包下类的集合
     *
     * 1.获取到类的加载器
     *      要使用类加载器，是为了解决实际路径的问题，如果只是根据包名，比如top.candy是不能直接定位到实际的文件夹路径的
     *      通过用户传入的信息，获取项目发布的实际路径
     * 2.通过类加载器获取到加载的资源信息
     * 3.依据不同的资源类型，采用不同的方式获取资源的集合
     *
     * @param packageName 包名
     * @return 返回存储class类型的Set
     */

    public static Set<Class<?>> extractPackageClass(String packageName) {
        // 1.获取类的加载器
        ClassLoader classLoader = getClassLoader();
        // 2.通过类加载器获取到加载的资源URL
        // getResource需要的名称是反斜杠'/'隔开的名称
        URL url = classLoader.getResource(packageName.replace(".", "/"));
        if (url == null) {
            // log4j记录信息
            log.warn("unable to retrieve anything from package:" + packageName);
            return null;
        }
        // 3.依据不同的资源类型，采用不同的方式获取资源的集合
        Set<Class<?>> classSet = null;
        // 通过getprotocol过滤出文件类型资源协议是不是file，也可能是jar、war等
        if (url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
            classSet = new HashSet<Class<?>>();
            // 获取到package所在的实际路径
            // getPath用来获取url实例的路径，该路径是绝对路径
            File packageDirectory = new File(url.getPath());
            // 去page所在的实际路径下面提取class文件，结合用户传入的packageName，生成class对象实例，然后将实例放到classSet中
            extractClassFile(classSet, packageDirectory, packageName);
        }

        //TODO 这里可以加入针对其他类型资源的处理

        return classSet;

    }

    /**
     * 递归获取目标package里面的所有class文件（包括子package里的class文件）
     * @param classSet 装载目标类的集合
     * @param packageDirectory 文件或目录
     * @param packageName 包名
     */
    private static void extractClassFile(Set<Class<?>> classSet, File packageDirectory, String packageName) {
        // 这里是中断判断
        // 如果当前的是一个目录，就继续，如果是一个文件就结束
        if (!packageDirectory.isDirectory()) {
            return ;
        }

        //如果是一个文件夹，则调用其listFiles方法获取文件夹下的文件或文件夹
        //同时，listFiles方法可以过滤出我们感兴趣的实例
        File[] files = packageDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    //true的筛选出来
                    return true;
                } else {
                    //获取文件的绝对值路径
                    String fileAbsolutePath = file.getAbsolutePath();
                    if (fileAbsolutePath.endsWith(".class")) {
                        //若是class文件，则直接加载
                        //将class文件转成class对象
                        addToClassSet(fileAbsolutePath);
                    }
                }
                // 文件处理完之后，不需要保存，直接过滤掉即可了
                // 这里就是非目录文件直接过滤掉
                return false;
            }

            //根据class文件的绝对路径名，获取并生成class对象，并放在classSet中
            private void addToClassSet(String fileAbsolutePath) {
                //1.从class文件的绝对值路径中提取出包含package的类名
                //比如实际的路径是：
                // /home/candyboy/IdeaProjects/my_springframework/src/main/java/top/candysky/entity/dto/MainPageInfoDTO.class
                //需要转变为：
                // top.candysky.entity.dto.MainPageInfoDTO

                //File.separator可根据不同的操作系统判断是‘/’还是‘\’
                fileAbsolutePath = fileAbsolutePath.replace(File.separator, ".");
                String className = fileAbsolutePath.substring(fileAbsolutePath.indexOf(packageName));
                // 从头开始，截止到class前的那一个'.'
                className = className.substring(0, className.lastIndexOf("."));
                //2.通过反射机制获取对应的class对象并加入到classSet中
                Class<?> targetClass = loadClass(className);
                classSet.add(targetClass);
            }
        });

        // 这里是递归操作
        // foreach要做null指针异常判断
        if (files != null) {
            // 如果过滤后的文件夹中还有文件夹的话，进行递归判断
            for (File f : files) {
                extractClassFile(classSet, f, packageName);
            }
        }
    }

    /**
     * 获取class对象
     *
     * @param className class全名=package+类名
     * @return Class
     */
    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("load class error:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 实例化class
     * @param clazz Class
     * @param accessible 是否支持创建出私有class对象的实例
     * @param <T> class的类型
     * @return 类的实例化
     */
    public static <T> T newInstance(Class<?> clazz, boolean accessible) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(accessible);
            return (T) constructor.newInstance();
        } catch (Exception e) {
            log.error("newInstance error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取classLoader 的实例,通过线程实例获取到classLoader
     * @return 当前classLoader
     */
    public static ClassLoader getClassLoader() {
       return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 设置类的属性值
     * @param filed 成员变量
     * @param target 类
     * @param value 成员变量的值
     * @param accessible 是否允许设置私有属性
     */
    public static void setFiled(Field filed, Object target, Object value, Boolean accessible ) {
        //决定是否支持反射操作私有的成员变量
        filed.setAccessible(accessible);
        try {
            filed.set(target,value);
        } catch (IllegalAccessException e) {
            log.error("setField error", e);
            throw new RuntimeException(e);
        }
    }
}
