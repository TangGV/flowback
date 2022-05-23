package cn.flowback.core.asynch;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.*;

/**
 * 异步线程池统一管理
 *
 * @author Tang
 */
public class AsynchPoll {

    private static ExecutorService executorService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

    /**
     * 定时执行统计线程
     */
    private static final ScheduledExecutorService statisticsScheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            new DefaultThreadFactory("work-timing-"));


    public static void doWork(Runnable runnable) {
        executorService.submit(runnable);
    }

    /**
     * 周期执行线程池
     * @param command
     * @param initialDelay
     * @param period
     * @param unit
     */
    public static void scheduleAtFixedRateWork(Runnable command,
                                               long initialDelay,
                                               long period,
                                               TimeUnit unit) {
        statisticsScheduledExecutorService.scheduleAtFixedRate(command,initialDelay,period,unit);
    }



}
