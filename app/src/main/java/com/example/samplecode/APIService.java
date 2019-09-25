package com.example.samplecode;

import com.example.samplecode.models.SessionInfoResponse;
import com.example.samplecode.models.SessionBody;
import com.example.samplecode.models.SessionResponse_REMOVE;
import com.example.samplecode.models.SimpleUploadResponse;

import io.reactivex.Single;
import okhttp3.MultipartBody;
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
    Call<SessionResponse_REMOVE> createSession1(@Header("token") String token, @Body SessionBody body);

    //////////=========

    @POST("api/Upload/CreateUploadSession")
    Single<SessionInfoResponse> createSession(@Header("token") String token, @Body SessionBody body);

    @Multipart
    @PUT("api/Upload/UploadFile/{sessionId}")
    Call<SimpleUploadResponse> uploadSingleFile(@Header("token") String token, @Header("ContentLength") long ctLen, @Header("Content-Range") String ctRange,
                                                @Path("sessionId") String sessionId, @Part MultipartBody.Part b);  //@Body RequestBody b

    @GET("api/Upload/UploadFile/{sessionId}")
    Single<SessionInfoResponse> getSessionInfo(
            @Header("token") String token,
            @Path("sessionId") String sessionId
    );
}