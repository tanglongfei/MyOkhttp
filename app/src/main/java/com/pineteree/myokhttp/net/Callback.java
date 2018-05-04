package com.pineteree.myokhttp.net;

import com.pineteree.myokhttp.net.databean.Response;

/**
 * Created by Administrator on 2018/5/2.
 */

public interface Callback {
    void onFailure (Call call, Throwable throwable);

    void onResponse(Call call, Response response);
}
