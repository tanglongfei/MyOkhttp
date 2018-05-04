package com.pineteree.myokhttp.net;

import com.pineteree.myokhttp.net.chain.CallServiceInterceptor;
import com.pineteree.myokhttp.net.chain.ConnectionInterceptor;
import com.pineteree.myokhttp.net.chain.HeaderInterceptor;
import com.pineteree.myokhttp.net.chain.Interceptor;
import com.pineteree.myokhttp.net.chain.InterceptorChain;
import com.pineteree.myokhttp.net.chain.RetryInterceptor;
import com.pineteree.myokhttp.net.databean.HttpClient;
import com.pineteree.myokhttp.net.databean.Request;
import com.pineteree.myokhttp.net.databean.Response;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018/5/2.
 */

public class Call {
    //
    Request request;
    HttpClient client;

    //标识 是否执行过
    boolean executed;
    //是否取消
    private boolean canceled;

    public boolean isCanceled() {
        return canceled;
    }

    public Call(Request request, HttpClient client) {
        this.request = request;
        this.client = client;
    }

    public Request request() {
        return request;
    }

    public HttpClient client() {
        return client;
    }

    public void enqueue(Callback callback) {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("已经执行过了");
            }
            executed = true;
        }
        //把任务交给调度器调度
        client.dispatcher().enqueue(new AsyncCall(callback));
    }

    public void cancel() {
        canceled = true;
    }

    /**
     * 执行网络请求的线程
     */
    class AsyncCall implements Runnable {

        Callback callback;

        public AsyncCall(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            //信号是否回调过
            boolean signalledCallbacked = false;
            try {
                Response response = getResponse();
                if (canceled) {
                    //取消
                    callback.onFailure(Call.this, new IOException("Canceled"));
                } else {
                    signalledCallbacked = true;
                    callback.onResponse(Call.this, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!signalledCallbacked) {
                    callback.onFailure(Call.this, e);
                }
            } finally {
                //将这个任务从调度器移除 不然调度器中的队列只会越来越多 不会减少
                client.dispatcher().finished(this);

            }


        }

        public String host() {
            return request.url().getHost();
        }

    }

    /**
     * 真正执行的地方
     *
     * @return
     * @throws Exception
     */
    private Response getResponse() throws Exception {
        //拦截器责任链 (责任链模式 补全Response)
        ArrayList<Interceptor> interceptors = new ArrayList<>();
        //重试拦截器
        interceptors.add(new RetryInterceptor());
        //请求头拦截器
        interceptors.add(new HeaderInterceptor());
        //连接拦截器
        interceptors.add(new ConnectionInterceptor());
        //通信拦截器
        interceptors.add(new CallServiceInterceptor());
        InterceptorChain chain = new InterceptorChain(interceptors, 0, this,null);
        return chain.process();
    }

}
