package top.candysky.demo.proxy;

import net.sf.cglib.proxy.MethodInterceptor;
import top.candysky.demo.proxy.cglib.AlipayMethodInterceptor;
import top.candysky.demo.proxy.cglib.CglibUtil;
import top.candysky.demo.proxy.impl.*;
import top.candysky.demo.proxy.jdkProxy.AliPayInvocationHandler;
import top.candysky.demo.proxy.jdkProxy.JdkDynamicProxyUtil;

import java.lang.reflect.InvocationHandler;

public class ProxyDemo {
    public static void main(String[] args) {
        // 静态代理 代理对象在编译的时候就已经实现
        //ToCPayment toCPaymentProxy = new AlipayToC(new ToCPaymentImpl());
        //toCPaymentProxy.pay();
        // 即使横切逻辑一样的，对应的目标对象是不一样的，都需要实现不一样的代理对象
        //ToBPayment toBPaymentProxy = new AlipayToB(new ToBPaymentImpl());
        //toBPaymentProxy.pay();

        /*
        前面只需要四行代码实现需求，使用了动态代理之后反而更加麻烦
        这怎么解释？
        1.通过动态代理，代理逻辑能够进行复用了，两个不同的目标类使用的是同一个Aspect
        2.静态代理显式地调用代理类的方法实现对目标方法的包装
          动态代理只需要使用newProxyInstance返回的方法就能实现对目标对象的包装
        3.可以利用注解进行封装，封装通用的逻辑对目标对象进行封装，在容器启动之前提前处理好目标对象
         */
        ToCPayment toCPayment = new ToCPaymentImpl();
        InvocationHandler CInvocationHandler = new AliPayInvocationHandler(toCPayment);
        ToCPayment toCProxy = JdkDynamicProxyUtil.newProxyInstance(toCPayment, CInvocationHandler);
        toCProxy.pay();
        ToBPayment toBPayment = new ToBPaymentImpl();
        InvocationHandler BInvocationHandler = new AliPayInvocationHandler(toBPayment);
        ToBPayment toBProxy = JdkDynamicProxyUtil.newProxyInstance(toBPayment, BInvocationHandler);
        toBProxy.pay();

        /*
        CGLIB支持不实现接口的代理的

        JDK动态代理和CGLIB
        JDK动态代理的优势：
        1.JDK原生，在JVM里运行较为可靠
        2.平滑支持JDK版本的升级
        CGLIB的优势：
        被代理对象无需实现接口，能实现代理类的无侵入

        SpringAOP的底层机制
        1.CGLIB和JDK动态代理共存
        2.默认策略：Bean实现了接口则用JDK，否则使用CGLIB
         */
        CommonPayment commonPayment = new CommonPayment();
        MethodInterceptor alipayMethodInterceptor = new AlipayMethodInterceptor();
        CommonPayment proxy = CglibUtil.createProxy(commonPayment, alipayMethodInterceptor);
        proxy.pay();
    }
}
