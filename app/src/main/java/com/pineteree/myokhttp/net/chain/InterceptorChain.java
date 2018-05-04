package com.pineteree.myokhttp.net.chain;


import com.pineteree.myokhttp.net.Call;
import com.pineteree.myokhttp.net.HttpConnection;
import com.pineteree.myokhttp.net.databean.Response;

import java.io.IOException;
import java.util.List;

/**
 * 拦截责任链
 * Created by Administrator on 2018/4/27.
 */

public class InterceptorChain {


    List<Interceptor> interceptors;
    int index;
    Call call;
    HttpConnection connection;

    public InterceptorChain(List<Interceptor> interceptors, int index, Call call, HttpConnection connection) {
        this.interceptors = interceptors;
        this.index = index;
        this.call = call;
        this.connection = connection;
    }

    public Response process(HttpConnection connection) throws IOException {
        this.connection = connection;
        return process();
    }
    public Response process() throws IOException {
        if (index >= interceptors.size()) throw new IOException("Interceptor Chain Error");
        //获得拦截器 去执行
        Interceptor interceptor = interceptors.get(index);
        InterceptorChain next = new InterceptorChain(interceptors, index + 1, call,connection);
        Response response = interceptor.intercept(next);
        return response;
    }
}
