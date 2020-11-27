package top.candysky.demo.proxy.jdkProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * invocationHandler是统一管理横切逻辑的类
 * 真正的代理类是由Proxy生成的
 */
public class JdkDynamicProxyUtil {
    public static <T> T newProxyInstance(T targetObject, InvocationHandler invocationHandler) {
        ClassLoader classLoader = targetObject.getClass().getClassLoader();
        Class<?>[] interfaces = targetObject.getClass().getInterfaces();
        return (T) Proxy.newProxyInstance(classLoader,interfaces,invocationHandler);
    }
}
