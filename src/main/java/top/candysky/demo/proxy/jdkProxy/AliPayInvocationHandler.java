package top.candysky.demo.proxy.jdkProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * ClassLoader
 * 类是通过类加载器进行加载进内存的
 * 1.通过带有包名的类来获取对应class文件的二进制字节流
 * 在第一部和第二步中，还有这一步：
 * 2.根据读取的字节流，将代表的静态存储结构转化为运行时数据结构
 * 3.生成一个代表该类的class对象，作为方发区该类的数据访问入口
 *
 * 我们需要关注的点是字节流，既然字节流能定义类的行为
 * 考虑根据一定规则去改动或者生成新的字节流，将切面逻辑织入其中，使其动态生成成为天生带有的切面逻辑的类
 * 1.行之有效的方案就是取代被代理类的动态代理机制
 * 2.根据接口或者目标类，计算出代理类的字节码并加载到JVM中
 *
 * 静态代理：代理类需要在编译前进行实现，编译完成后代理类是一个具体的class文件。在运行前要进行编译，并且代理类是有具体的类的
 * 动态代理：程序运行时动态生成类的字节码，并加载到JVM中，没有一个具体的class文件
 *
 * 动态代理只要求【被代理的类】实现接口
 * 不要求【代理对象】去实现接口，所以可以复用代理对象的逻辑
 */
public class AliPayInvocationHandler implements InvocationHandler {

    /**
     * 目标类，被代理对象
     */
    private Object targetObject;

    public AliPayInvocationHandler(Object targetObject) {
        this.targetObject = targetObject;
    }

    /**
     * 封装横切逻辑
     * InvocationHandler和Aspect注解标签类似，实现了InvocationHandler接口的类才具有Aspect,即具有代理功能
     * 相关的切面逻辑就存在于InvocationHandler的实现类中
     * 当一个动态代理对象调用一个方法的时候，这个方法的调用就会被转发到实现了InvocationHandler接口的类实现的invoke方法中
     * @param proxy 真实的代理对象 不像静态代理方法，需要显式地调用代理类的代理方法。
     *              对于动态代理方法，当调用织入横切逻辑的方法时，显式地调用的是被代理类的方法，给用户的感觉是调用自身的方法
     *              用户想获取真实的代理类的情况，所以这是设计第一个参数的原因
     * @param method 需要调用的目标对象的方法
     * @param args 运行方法的参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        beforePay();
        Object result = method.invoke(targetObject,args);
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
