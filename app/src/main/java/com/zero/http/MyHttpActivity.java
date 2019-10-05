package com.zero.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.zero.http.retrofit.GetRequestInterface;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class MyHttpActivity extends BaseActivity {

    private String url = "http://hbimg.b0.upaiyun.com/264d0adda8f63c08ae373fbdeaf68d20a7eff8d1215be-VConOj_fw658";
    private String okUrl = "http://hbimg.b0.upaiyun.com/";

    private String path, okPath;
    private ImageView ivHttp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_http);
        ivHttp = findViewById(R.id.ivHttp);
        path = Environment.getExternalStorageDirectory().getPath() + File.separator + "0000/" + "0.png";
        okPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "0000/" + "1.png";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        Log.e("本地路径", "path = " + path);
    }

    //普通的网络请求
    //网络请求必须要设置网络权限：否则 会抛出无权限异常
    //网络请求存储本地，必须要设置本地内存读写权限，否则 会抛出无权限异常
    public void setHttpGet(View view) {
        //网络请求不能在主线程里面操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                runSetHttpGet();
            }
        }).start();
    }

    private void runSetHttpGet() {
        try {
            //创建一个URL对象
            URL url = new URL(this.url);
            //通过URL对象获取一个 HttpURLConnection 对象
            //HttpURLConnection 继承自 URLConnection.
            //HttpsURLConnection 继承自 HttpURLConnection;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //设置请求参数
            connection.setRequestMethod("GET"); //请求方式。必须大写
            connection.setConnectTimeout(5 * 1000); //设置超时时间
            //获取响应状态码（200是请求成功）
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {


                //请求成功后，获取流信息（可以将流信息写入本地 或 将流解码）
                InputStream inputStream = connection.getInputStream();
                //此处得到一个重大发现：：：：inputStream只能被使用一次，
                //也就是说 getStreamToStr 使用了 inputStream，那么 下面的 decodeStream 则无法使用
                //若是 decodeStream 先 使用了 inputStream，那么  getStreamToStr 怎无法使用

                getStreamToStr(inputStream, path);
                //通过 BitmapFactory 将流解码成 Bitmap 对象
                //final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                final Bitmap bitmap = BitmapFactory.decodeFile(path);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivHttp.setImageBitmap(bitmap);
                    }
                });
                //TODO 要么就先下载在展示，要么就直接展示图片
                //inputStream 对象只能被操作一次！！


                Log.e("myhttp", "code = " + code);
            } else {
                Log.e("myhttp", "code = " + code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param view 使用OkHttp请求
     */
    public void setOKHttpGet(View view) {
        //1. 获取 OkHttpClient 对象
        OkHttpClient client = new OkHttpClient();
        //2. 构造 Request 对象
        Request.Builder builder = new Request.Builder();

        //添加 header 头
        //builder.addHeader("name","value");

        Request request = builder.get().url(okUrl).build();
        //3. 将 Request 封装为 call
        Call call = client.newCall(request);

        //4. 执行 call
        //该方式是直接请求
        //Response response = call.execute();

        //实验证明： Callback 返回：onFailure 和 onResponse 属于子线程，切记不可更新UI
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("okhttp", "onFailure = " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //TODO  此处需要注意：response.body()转换成其他对象的时候，只能操作一次，不然会造成某些位置的错误
                //response.body() 只能被消费一次！
                //获取字符串
//                String string = response.body().string();
//                Log.e("okhttp", "onResponse = " + string);

                //获取字节流
                InputStream inputStream = response.body().byteStream();
                getStreamToStr(inputStream, okPath);
            }
        });
    }


    /**
     *
     * @param view 使用 Retrofit 框架请求网络
     */
    public void setRetrofitGet(View view) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(okUrl)
                .build();

        GetRequestInterface request = retrofit.create(GetRequestInterface.class);

        retrofit2.Call<ResponseBody> call = request.getCall();

        call.enqueue(new retrofit2.Callback<ResponseBody>() {

            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
//                String string = null;
//                try {
//                    string = response.body().string();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Log.e("Retrofit", "onResponse = " + string);

                InputStream inputStream = response.body().byteStream();
                try {
                    getStreamToStr(inputStream, okPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                Log.e("Retrofit", "onResponse = " + t.getMessage());
            }
        });


    }


    /**
     * 将 字节输入流 通过 字节输出流 写入某个路径
     *
     * @param inputStream 从网络请求返回的输入流
     * @param file        本地指定的文件
     */
    private void getStreamToStr(InputStream inputStream, String file) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytes = new byte[1024];
        int len;
        while ((len = bis.read(bytes)) != -1) {
            bos.write(bytes, 0, len);
        }
        bos.close();
        bis.close();
    }
}
