# MyOkhttp
模仿Okhttp的网络框架  
## 仅供学习参考Okhttp源码使用(Okhttp源码比较复杂，我把其主要功能抽取出来，仅供参考)    
## 原理  
使用socket连接，参考Okhttp源码，拦截器使用责任链模式
```
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
```
## 使用  
和Okhttp使用方法相同  
- get
```
//retrys:重试次数
 client = new HttpClient.Builder().retrys(3).build();
   Request request = new Request.Builder()
                .url("****")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.e("响应体", response.getBody());

            }
        });
```
- post  
```
      RequestBody body = new RequestBody()
                .add("**", "***")
                .add("**", "***");
        Request request = new Request.Builder().url(***).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.e("响应体", response.getBody());
            }
        });
```
可以在HttpClient中自定义需要的功能，如缓存、自定义拦截器等。通过build模式进行使用
