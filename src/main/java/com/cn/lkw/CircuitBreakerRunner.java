package com.cn.lkw;/**
 * Created by liukangwei on 2020/9/2.
 */

/**
 * @author kangwei.liu
 * @create 2020-09-02 下午9:50
 **/
public interface CircuitBreakerRunner {

    void run();

    void fallback();

}
