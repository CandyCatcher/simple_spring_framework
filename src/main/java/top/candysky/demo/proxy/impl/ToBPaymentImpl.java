package top.candysky.demo.proxy.impl;

import top.candysky.demo.proxy.ToBPayment;

public class ToBPaymentImpl implements ToBPayment {
    @Override
    public void pay() {
        System.out.println("以公司的名义进行支付");
    }
}
