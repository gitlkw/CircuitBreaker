package com.cn.lkw;
/**
 * Created by liukangwei on 2020/9/2.
 */

import java.util.Random;

/**
 * @author kangwei.liu
 * @create 2020-09-02 下午9:52
 **/
public class TestAction {


    public static void main(String[] args) throws Exception {

        final Random r = new Random(1000);

        final CircuitBreakerStrategy circuitBreakerStrategy = CircuitBreakerStrategy.build("test", 1, 5, 10000L, new CircuitBreakerRunner() {

            @Override
            public void run() {
                int ran = r.nextInt(5);
                // System.out.println("=========run=========");

                System.out.println(ran/0);
            }

            @Override
            public void fallback() {
                System.out.println("=========fallback=========");
            }
        });


        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (; ; ) {
                        try {
                            CircuitBreakerManager.runCircuitBreaker(circuitBreakerStrategy);
                            Thread.sleep(1000L);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }


        Thread.sleep(Integer.MAX_VALUE);

    }
}
