package com.pineteree.myokhttp.net;

import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 调度器
 * Created by Administrator on 2018/5/2.
 */

public class Dispatcher {

    //同时进行的最大请求数
    private int maxRequests = 64;
    //同时请求的相同的Host数
    private int maxRequestPreHost = 5;

    //等待执行队列
    private Deque<Call.AsyncCall> readyAysncCalls = new ArrayDeque<>();
    //正在执行队列
    private Deque<Call.AsyncCall> runningAysncCalls = new ArrayDeque<>();

    //线程池
    private ExecutorService executorService;

    public synchronized ExecutorService executorService() {
        //线程生成工厂 创建线程 指定线程名字
        if (null == executorService) {

            ThreadFactory threadFactory = new ThreadFactory() {

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r, "Http Client");
                }
            };
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), threadFactory);
        }
        return executorService;
    }

    public Dispatcher() {
        this(64, 5);
    }

    public Dispatcher(int maxRequests, int maxRequestPreHost) {
        this.maxRequests = maxRequests;
        this.maxRequestPreHost = maxRequestPreHost;
    }

    /**
     * 使用调度器进行任务调度
     *
     * @param call
     */
    public void enqueue(Call.AsyncCall call) {
        //不能超过最大请求数和相同host的请求数
        //满足条件意味着可以马上开始任务
        if (runningAysncCalls.size() < maxRequests && runningCallsForHost(call) <
                maxRequestPreHost) {
            //如果满足 说明可以执行 加入正在执行列表
            runningAysncCalls.add(call);
            //添加到线程池
            executorService().execute(call);
        } else {
            readyAysncCalls.add(call);
        }
    }


    private int runningCallsForHost(Call.AsyncCall call) {
        int result = 0;
        for (Call.AsyncCall c : runningAysncCalls) {
            if (c.host().equals(call.host())) {
                result++;
            }
        }
        return result;
    }

    /**
     * 表示一个请求成功
     * 将其从runningAsync移除
     * 并且检查ready是否可以执行
     *
     * @param call
     */
    public void finished(Call.AsyncCall call) {
        synchronized (this) {
            //将其从runningAsync移除
            runningAysncCalls.remove(call);
            //检查ready是否可以执行
            checkReady();
        }
    }

    private void checkReady() {
        //达到了同时请求最大数
        if (readyAysncCalls.size() >= maxRequests) {
            return;
        }
        //没有执行的任务
        if (readyAysncCalls.isEmpty()) {
            return;
        }
        Iterator<Call.AsyncCall> iterator = readyAysncCalls.iterator();
        while (iterator.hasNext()) {
            //获得一个等待执行的任务
            Call.AsyncCall asyncCall = iterator.next();
            //如果获得的等待执行的任务 执行后 小于host相同最大允许数
            if (runningCallsForHost(asyncCall) < maxRequestPreHost) {
                //可以执行
                iterator.remove();
                runningAysncCalls.add(asyncCall);
                executorService.execute(asyncCall);
            }
            //如果正在执行的任务达到了最大
            if (runningAysncCalls.size()>maxRequests){
                return;
            }
        }
    }
}
