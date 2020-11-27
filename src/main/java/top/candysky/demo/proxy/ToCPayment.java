package top.candysky.demo.proxy;

/**
 * 用户使用支付宝支付
 * 中间的从银行取钱的过程是不关心的
 * 支付宝实现这些过程
 */
public interface ToCPayment {
    void pay();
}
