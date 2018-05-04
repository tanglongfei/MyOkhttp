package com.pineteree.myokhttp.net.databean;


import com.pineteree.myokhttp.net.Call;
import com.pineteree.myokhttp.net.ConnectionPool;
import com.pineteree.myokhttp.net.Dispatcher;

/**
 * Created by Administrator on 2018/4/27.
 */

public class HttpClient {
    private final int retrys;
    private final Dispatcher dispatcher;
    private final ConnectionPool connectionPool;

    public HttpClient(Builder builder) {
        dispatcher = builder.dispatcher;
        this.retrys = builder.retrys;
        connectionPool = builder.connectionPool;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public int retrys() {
        return retrys;
    }

    public ConnectionPool connectionPool(){
        return connectionPool;
    }

    //
    public Call newCall(Request request) {
        return new Call(request, this);
    }

    public static final class Builder {
        Dispatcher dispatcher;
        int retrys;
        ConnectionPool connectionPool;

        /**
         * 自定义调度器
         *
         * @param dispatcher
         * @return
         */
        public Builder dispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public Builder retrys(int retrys) {
            this.retrys = retrys;
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        public HttpClient build() {
            if (null == dispatcher){
                dispatcher = new Dispatcher();
            }
            if (null == connectionPool){
                connectionPool = new ConnectionPool();
            }
            return new HttpClient(this);
        }
    }
}