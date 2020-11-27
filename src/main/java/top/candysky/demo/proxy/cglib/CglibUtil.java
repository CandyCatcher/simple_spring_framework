package top.candysky.demo.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

public class CglibUtil {
    public static <T>T createProxy(T targetObjext, MethodInterceptor methodInterceptor) {
        return (T) Enhancer.create(targetObjext.getClass(), methodInterceptor);
    }
}
