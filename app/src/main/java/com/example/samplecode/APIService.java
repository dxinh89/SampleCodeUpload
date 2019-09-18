package com.example.samplecode;

import com.example.samplecode.models.SessionBody;
import com.example.samplecode.models.SessionResponse;
import com.example.samplecode.models.SimpleUploadResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface APIService {

    @POST("api/Upload/CreateUploadSession")
    Call<SessionResponse> createSession(@Header("token") String token, @Body SessionBody body);

    @Multipart
    @PUT("api/Upload/UploadFile/{sessionId}")
    Call<SimpleUploadResponse> upload(
            @Header("token") String token, @Header("ContentLength") long ctLen, @Header("Content-Range") String ctRange,
            @Path("sessionId") String sessionId,
            @Part MultipartBody.Part imageFile
    );

    //            @Part("item") RequestBody description,
//            @Part("imageNumber") RequestBody description,
}