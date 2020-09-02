package com.cn.lkw;
/**
 * Created by liukangwei on 2020/9/2.
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 断路器策略
 *
 * @author kangwei.liu
 * @create 2020-09-02 下午9:37
 **/
public class CircuitBreakerStrategy {


    private CircuitBreakerStrategy() {

    }

    private static Map<String, CircuitBreakerStrategy> circuitBreakerStrategyMap = new ConcurrentHashMap<>();
    private static Object lock = new Object();


    public static CircuitBreakerStrategy build(String name, Integer errorMax, Integer limitMax,
                                               Long recoverTime,
                                               CircuitBreakerRunner circuitBreakerRunner) throws Exception {

        if (name == null) {
            throw new Exception("策略名称不能为空！");
        }

        if (errorMax == null || errorMax <= 0) {
            throw new Exception("最大错误次数必须大于0");
        }

        if (limitMax == null || limitMax <= 0) {
            throw new Exception("最大执行次数必须大于0");
        }

        if (errorMax > limitMax) {
            throw new Exception("最大错误次数不能超过最大执行次数！");
        }

        CircuitBreakerStrategy circuitBreakerStrategy = circuitBreakerStrategyMap.get(name);
        if (circuitBreakerStrategy == null) {
            synchronized (lock) {
                if (circuitBreakerStrategy == null) {
                    circuitBreakerStrategy = new CircuitBreakerStrategy(name, errorMax, limitMax, recoverTime, circuitBreakerRunner);
                    circuitBreakerStrategyMap.put(name,circuitBreakerStrategy);
                }
            }
        }
        return circuitBreakerStrategy;
    }


    private String name; // 策略名称
    private Integer errorMax; // 最大错误次数
    private Integer limitMax; // 最大执行次数
    private Long recoverTime; // 恢复时间 0：永远不恢复
    private CircuitBreakerRunner circuitBreakerRunner;

    private AtomicInteger errorMaxCurr = new AtomicInteger(0); // 当前失败次数
    private AtomicInteger limitMaxCurr = new AtomicInteger(0); // 当前执行次数
    private AtomicLong openTimeCurr = new AtomicLong(0); // 开关打开时间，0代表关闭


    public Long getOpenTimeCurr() {
        return openTimeCurr.get();
    }

    public Integer getErrorMaxCurr() {
        return errorMaxCurr.get();
    }

    public Integer getLimitMaxCurr() {
        return limitMaxCurr.get();
    }

    public Integer addErrorMaxCurr() {
        return errorMaxCurr.incrementAndGet();
    }

    public Integer addLimitMaxCurr() {
        return limitMaxCurr.incrementAndGet();
    }


    /**
     * 检查当前失败次数是否触发阀值
     *
     * @return
     */
    public boolean checkErrorLimit() {
        return getErrorMaxCurr() >= errorMax && getLimitMaxCurr() >= limitMax;

    }

    /**
     * 开关是否:未超时，超时需要恢复
     *
     * @param oldOpenTimeCurr
     * @return
     */
    public boolean isNotTimeOut(Long oldOpenTimeCurr) {
        // recoverTime == 0L 永不超时，开关开启时间 + 超时时间 > 当前时间
        return recoverTime == 0L || (oldOpenTimeCurr + recoverTime > System.currentTimeMillis());
    }


    /**
     * 开关是否打开
     *
     * @param oldOpenTimeCurr
     * @return
     */
    public boolean isOpen(Long oldOpenTimeCurr) {
        return oldOpenTimeCurr == 0L ? false : true;
    }


    /**
     * 打开开关
     */
    public void open() {
        openTimeCurr.compareAndSet(0L, System.currentTimeMillis());
    }


    /**
     * 关闭开关
     *
     * @param oldOpenTimeCurr
     */
    public void close(Long oldOpenTimeCurr) {
        if (openTimeCurr.compareAndSet(oldOpenTimeCurr, 0L)) {
            errorMaxCurr.set(0);
            limitMaxCurr.set(0);
        }
    }


    public CircuitBreakerStrategy(String name, Integer errorMax, Integer limitMax, Long recoverTime, CircuitBreakerRunner circuitBreakerRunner) {
        this.name = name;
        this.errorMax = errorMax;
        this.limitMax = limitMax;
        this.recoverTime = recoverTime;
        this.circuitBreakerRunner = circuitBreakerRunner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getErrorMax() {
        return errorMax;
    }

    public void setErrorMax(Integer errorMax) {
        this.errorMax = errorMax;
    }

    public Integer getLimitMax() {
        return limitMax;
    }

    public void setLimitMax(Integer limitMax) {
        this.limitMax = limitMax;
    }

    public Long getRecoverTime() {
        return recoverTime;
    }

    public void setRecoverTime(Long recoverTime) {
        this.recoverTime = recoverTime;
    }

    public CircuitBreakerRunner getCircuitBreakerRunner() {
        return circuitBreakerRunner;
    }

    public void setCircuitBreakerRunner(CircuitBreakerRunner circuitBreakerRunner) {
        this.circuitBreakerRunner = circuitBreakerRunner;
    }
}
