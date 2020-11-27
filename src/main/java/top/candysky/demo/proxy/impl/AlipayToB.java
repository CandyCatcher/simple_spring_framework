package top.candysky.demo.proxy.impl;

import top.candysky.demo.proxy.ToBPayment;

public class AlipayToB implements ToBPayment {

    ToBPayment toBPayment;

    public AlipayToB(ToBPayment toBPayment) {
        this.toBPayment = toBPayment;
    }

    @Override
    public void pay() {
        beforePay();
        toBPayment.pay();
        afterPay();
    }

    /**
     * 即使这样的逻辑是相同的，但是也不能复用
     */
    private void afterPay() {
        System.out.println("支付给商家");
    }

    private void beforePay() {
        System.out.println("从银行取款");
    }
}
