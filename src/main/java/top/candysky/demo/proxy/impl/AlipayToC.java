package top.candysky.demo.proxy.impl;

import top.candysky.demo.proxy.ToCPayment;

public class AlipayToC implements ToCPayment {

    /**
     * AlipayToC作为TOCPayment的代理类，里面应该有TOCPayment的成员变量
     * 该成员变量通过构造函数引入
     */
    ToCPayment toCPayment;
    public AlipayToC(ToCPayment toCPayment) {
        this.toCPayment = toCPayment;
    }

    @Override
    public void pay() {
        beforePay();
        toCPayment.pay();
        afterPay();
    }

    private void afterPay() {
        System.out.println("支付给商家");
    }

    private void beforePay() {
        System.out.println("从银行取款");
    }
}
