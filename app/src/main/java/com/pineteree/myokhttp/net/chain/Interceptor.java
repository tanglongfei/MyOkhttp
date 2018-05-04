package com.pineteree.myokhttp.net.chain;




import com.pineteree.myokhttp.net.databean.Response;

import java.io.IOException;

/**
 * 拦截器
 * @author Lance
 * @date 2018/4/17
 */

public interface Interceptor {

    Response intercept(InterceptorChain chain) throws IOException;
}
