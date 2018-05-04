package com.pineteree.myokhttp.net.chain;

import android.util.Log;

import com.pineteree.myokhttp.net.HttpConnection;
import com.pineteree.myokhttp.net.databean.HttpClient;
import com.pineteree.myokhttp.net.databean.HttpUrl;
import com.pineteree.myokhttp.net.databean.Request;
import com.pineteree.myokhttp.net.databean.Response;

import java.io.IOException;

/**
 * Created by Administrator on 2018/5/4.
 * 获取有效链接(Socket)的拦截器
 */

public class ConnectionInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        Log.e("interceptor", "获取连接拦截器");
        Request request = chain.call.request();
        HttpClient client = chain.call.client();
        HttpUrl url = request.url();
        //从连接池中获得连接
        HttpConnection connection = client.connectionPool().get(url.getHost(), url.getPort());
        //没有可复用的连接
        if (null == connection) {
            connection = new HttpConnection();
        } else {
            Log.e("interceptor", "从连接池中获得连接");
        }
        connection.setRequest(request);
        //执行下一个拦截器
        try {
            Response response = chain.process(connection);
            if (response.isKeepAlive()) {
                client.connectionPool().put(connection);
            }else{
                connection.close();
            }
            return response;
        } catch (IOException e) {
            connection.close();
            throw e;
        }
    }
}
