package com.cn.lkw;
/**
 * Created by liukangwei on 2020/9/2.
 */

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


    public static void runCircuitBreaker(CircuitBreakerStrategy circuitBreakerStrategy) {
        String name = circuitBreakerStrategy.getName();
        CircuitBreakerRunner runner = circuitBreakerStrategy.getCircuitBreakerRunner();
        for (; ; ) {
            final Long oldOpenTimeCurr = circuitBreakerStrategy.getOpenTimeCurr();
            if (circuitBreakerStrategy.isOpen(oldOpenTimeCurr)) { // 开关处于打开状态
                if (circuitBreakerStrategy.isTimeOut(oldOpenTimeCurr)) { // 开关如果没超时，执行fallback()
                    try {
                        runner.fallback();
                    } catch (Exception e) {
                        System.out.println(name + ":fallback error" + e.getMessage());
                    }
                    break;
                } else {// 开关超时，尝试关闭开关
                    circuitBreakerStrategy.close(oldOpenTimeCurr);
                }
            } else { // 未打开，执行run()
                try {
                    runner.run();
                } catch (Exception e) {
                    circuitBreakerStrategy.addErrorMaxCurr(); // 记录失败次数
                    System.out.println(name + ":run error" + e.getMessage());
                } finally {
                    circuitBreakerStrategy.addLimitMaxCurr(); // 记录总次数
                }
                if (circuitBreakerStrategy.checkErrorLimit()) { // 如果失败次数触发阀值
                    circuitBreakerStrategy.open(); // 打开开关
                }
                break;
            }

        }
    }


}
