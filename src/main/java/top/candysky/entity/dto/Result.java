package top.candysky.entity.dto;

import lombok.Data;

/**
 * 在service使用泛型类作为方法的返回值，这样做便于在controller层中调用service层中的数据时，便于进行统一的处理。
 * 由于返回值也是实体类，并且是一个传输数据的类，所以将其放置在dto类下面
 * dto：data transfer object
 * @param <T>
 */
@Data
public class Result<T> {
    //本次请求结果的状态码,200表示成功
    private int code;
    //本次请求结果的详情
    private String msg;
    //本次请求返回的结果集
    private T data;
}
