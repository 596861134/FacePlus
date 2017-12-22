package com.face.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * //
 * //                            _ooOoo_
 * //                           o8888888o
 * //                           88" . "88
 * //                           (| -_- |)
 * //                           O\  =  /O
 * //                        ____/`---'\____
 * //                      .'  \\|     |//  `.
 * //                     /  \\|||  :  |||//  \
 * //                    /  _||||| -:- |||||-  \
 * //                    |   | \\\  -  /// |   |
 * //                    | \_|  ''\---/''  |   |
 * //                    \  .-\__  `-`  ___/-. /
 * //                  ___`. .'  /--.--\  `. . __
 * //               ."" '<  `.___\_<|>_/___.'  >'"".
 * //              | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 * //              \  \ `-.   \_ __\ /__ _/   .-` /  /
 * //         ======`-.____`-.___\_____/___.-`____.-'======
 * //                            `=---='
 * //        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //                      佛祖保佑       永无BUG
 * Created by czf on 2017/8/17.
 */

public class HttpControl {
    private static byte[] lock_sync = new byte[0];
    //主域名
    private static OkHttpClient mOkHttpClient;
    private static HashMap<String, Retrofit> mMap = new HashMap<>();

    /**
     * 普通接口
     *
     * @return
     */
    public static ApiStores retrofit() {
        return retrofit(ROOT_URL_TYPE.TYPE_DEFAULT);
    }

    /**
     * @param tag
     * @return
     */
    public static ApiStores retrofit(ROOT_URL_TYPE tag) {
        Retrofit retrofit;
        synchronized (lock_sync) {
            if (mMap.containsKey(tag.getName()) && ((retrofit = mMap.get(tag.getName())) != null)) {
                return retrofit.create(ApiStores.class);
            } else {
                initHttpClient();
                retrofit = new Retrofit.Builder()
                        .baseUrl(tag.getUrl())
                        .client(mOkHttpClient)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();
                mMap.put(tag.getName(), retrofit);
            }
        }
        return retrofit.create(ApiStores.class);
    }

    private static String boundaryString = getBoundary();
    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-"
                    .charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }

    private static void initHttpClient() {


        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        if (httpClientBuilder.interceptors() != null) {
            httpClientBuilder.interceptors().clear();
        }
        /**
         //缓存相关
         File cacheFile = new File(GoodaApplication.getContext().getExternalCacheDir(), "XinWoCache");
         Cache cache = new Cache(cacheFile, 1024 * 1024 * 50);*/

        httpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //缓存处理
                Request request = chain.request();
                //                String marvelHash = ApiUtils.generateMarvelHash(mApiKey, mApiSecret);
                // 添加公共参数
                HttpUrl.Builder authorizedUrlBuilder = request.url()
                        .newBuilder()
                        .scheme(request.url().scheme())
                        .host(request.url().host());
                //                        .addQueryParameter("channel", Settings.get(""))//客户端渠道码

                // 新的请求，添加请求头
                Request newRequest = request.newBuilder()
                        .addHeader("accept", "*/*")
                        .addHeader("Content-Type", "multipart/form-data; boundary=" + boundaryString)
                        .addHeader("connection", "Keep-Alive")
                        .addHeader("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)")
                        .method(request.method(), request.body())
                        .url(authorizedUrlBuilder.build())
                        .build();
                Log.e("TAG","request_url:" + request.toString());
                return chain.proceed(newRequest);
            }
        })
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);
        //在debug模式下log拦截器
        if (true) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }
        //通过build模式构建实例
        mOkHttpClient = httpClientBuilder.build();
        /**
         //带缓存实例
         mOkHttpClient = httpClientBuilder.cache(cache).build();*/
    }

    public static void buildHttpRequest(Observable<String> observable, final ResponseListener listener) {
        if (!NetUtil.isNetworkConnected()) {
            ToastUtil.showToast("请检查网络");
        }
        listener.showProgress();
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {

                    @Override
                    public void onError(Throwable e) {
                        listener.disMissProgress();
                        listener.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        listener.disMissProgress();
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        listener.disMissProgress();
                        JSONObject object = null;
                        try {
                            object = new JSONObject(s);
                        } catch (JSONException e) {
                            listener.onFail(new JSONObject());
                        }
                        if (object == null) {
                            listener.onFail(new JSONObject());
                        } else {
                            if (object.optString("faces")!=null){
                                listener.onSuccess(object);
                            }else {
                                listener.onFail(object);
                            }

                        }
                    }
                });
    }
}
