package com.zero.http.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GetRequestInterface {

    @GET("b1b25bd5f3110ba879282be93cd6d080ce7e8ab4f8c0-9b6QQd_fw658")
    Call<ResponseBody> getCall();
}
