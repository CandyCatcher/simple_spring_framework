package top.candysky.demo.proxy.impl;

import top.candysky.demo.proxy.ToCPayment;

public class ToCPaymentImpl implements ToCPayment {
    @Override
    public void pay() {
        System.out.println("以用户的名义进行支付");
    }
}
