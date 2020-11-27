package top.candysky.demo.proxy.cglib;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 代码生成库 Code Generation Library
 * 不要求被代理类实现接口
 * CGLIB内部主要封装了ASM Java字节码操作框架
 * ASM使用了一个分析器
 *
 * 从本质上说，CGLIB动态生成子类以覆盖非final的方法，绑定钩子回调自定义拦截器
 *
 * 主要干活的是MethodInterceptor，继承Callback接口，用来拦截方法调用
 *
 * Enhancer用来创建动态代理对象
 */
public class AlipayMethodInterceptor implements MethodInterceptor {
    /**
     * @param methodProxy 是CGLIB生成的用来代替Method的动态代理Method方法
     */
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        beforePay();
        // 不用像InvocationHandler一样，还需要传入被代理类实例，来调用被代理类实例的方法
        Object result = methodProxy.invokeSuper(o, objects);
        afterPay();
        return result;
    }

    private void afterPay() {
        System.out.println("支付给商家");
    }

    private void beforePay() {
        System.out.println("从银行取款");
    }
}
