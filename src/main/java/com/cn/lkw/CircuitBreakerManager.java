package com.cn.lkw;
/**
 * Created by liukangwei on 2020/9/2.
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kangwei.liu
 * @create 2020-09-02 下午10:09
 **/
public class CircuitBreakerManager {

    private CircuitBreakerManager() {

    }


    private static class CircuitBreakerManagerHolder {
        private static final CircuitBreakerManager INSTANCE = new CircuitBreakerManager();

    }


    public static final CircuitBreakerManager getInstance() {
        return CircuitBreakerManagerHolder.INSTANCE;
    }


    private Map<String, CircuitBreakerStrategy> circuitBreakerStrategyMap = new ConcurrentHashMap<>();
    private Object lock = new Object();


    public static void runCircuitBreaker(CircuitBreakerStrategy circuitBreakerStrategy) {
        String name = circuitBreakerStrategy.getName();
        CircuitBreakerRunner runner = circuitBreakerStrategy.getCircuitBreakerRunner();
        for (; ; ) {
            final Long oldOpenTimeCurr = circuitBreakerStrategy.getOpenTimeCurr();
            if (circuitBreakerStrategy.isOpen(oldOpenTimeCurr)) {
                if (circuitBreakerStrategy.isTimeOut(oldOpenTimeCurr)) {
                    try {
                        runner.fallback();
                    } catch (Exception e) {
                        System.out.println(name + ":fallback error" + e.getMessage());
                    }
                    break;
                } else {
                    circuitBreakerStrategy.close(oldOpenTimeCurr);
                }
            } else {
                try {
                    runner.run();
                } catch (Exception e) {
                    circuitBreakerStrategy.addErrorMaxCurr();
                    System.out.println(name + ":fallback run" + e.getMessage());
                } finally {
                    circuitBreakerStrategy.addLimitMaxCurr();
                }
                if (circuitBreakerStrategy.checkErrorLimit()) {
                    circuitBreakerStrategy.open();
                }
                break;
            }

        }
    }


}
