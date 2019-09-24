package com.example.samplecode;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.samplecode.models.ChecksumUtils;
import com.example.samplecode.models.SessionBody;
import com.example.samplecode.models.SessionInfoResponse;
import com.example.samplecode.models.ProgressRequestBody;
import com.example.samplecode.models.SessionResponse;
import com.example.samplecode.models.SimpleUploadResponse;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DemoUp {
    final static String token = "D2-D8-A5-C2-12-48-BC-29-11-D5-34-39-76-9F-D7-1E-9F-F1-DB-92";

    public static Observable<UploadHelper.UploadProgress> uploadFiles(final List<String> lstFile) {
        return Observable.create(new ObservableOnSubscribe<UploadHelper.UploadProgress>() {
            long totalUploaded = 0;
            long realTotalUploaded = 0;
            int previousTotal;
            Call<SimpleUploadResponse> lastCall;

            String sessionId = null;

            @Override
            public void subscribe(ObservableEmitter<UploadHelper.UploadProgress> emitter) throws Exception {
                long total = 0;
                int countRetry = 0;
                final int maxRetry = 3;

                for (String path : lstFile) {
                    total += (new File(path).length());
                }

                final long totalSize = total;

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        if (lastCall != null)
                            lastCall.cancel();
                    }
                });

                int maxSize = lstFile.size();
                for (int i = 0; i < maxSize; i++) {
                    if (emitter.isDisposed()) {
                        break;
                    }
                    previousTotal = 0;

                    SessionInfoResponse infoResponse = checkOrCreate(token, sessionId, totalSize).blockingGet();

                    long expirationTime = Long.valueOf(infoResponse.getExpirationDateTime());
                    long currentTime = System.currentTimeMillis() / 1000L;

                    if (currentTime > expirationTime)
                        sessionId = null;

                    if (sessionId == null)
                        sessionId = infoResponse.getSessionId();
                    Log.i("ss", "SSID:" + sessionId);

                    String path = lstFile.get(i);
                    lastCall = uploadSingleFile(sessionId, path, realTotalUploaded, total, new UploadHelper.ProgressCallback() {
                        @Override
                        public void onProgressChanged(long uploadedBytes) {
                            totalUploaded += (uploadedBytes - previousTotal);
                            previousTotal = (int) uploadedBytes;
                            Log.i("emitter", "emitter----------->>>>:" + totalUploaded);
                            emitter.onNext(new UploadHelper.UploadProgress(totalUploaded, totalSize));
                        }
                    });

                    try {
                        Response response = lastCall.execute();
                        SimpleUploadResponse simpleUploadResponse = (SimpleUploadResponse) response.body();

                        //RETRY
                        if (simpleUploadResponse == null || simpleUploadResponse.getCode() != 0) {

                            Thread.sleep(500);
                            emitter.onError(new Throwable());

                            countRetry++;
                            Log.i("retry", "Lan thu:" + countRetry);

                            if (countRetry < maxRetry) {
                                Log.i("retry", "Continue index:" + i + "- " + countRetry);
                                i--;
                            } else
                                break;
                        } else {
                            countRetry = 0;

                            Log.i("res", "MSG:" + simpleUploadResponse.toString());
                            totalUploaded += (new File(path).length() - previousTotal);
                            realTotalUploaded = totalUploaded;
                            Log.i("total", "total=:" + totalUploaded + "-real:" + realTotalUploaded);
                        }
                    } catch (Exception err) {
                        Thread.sleep(500);
                        emitter.onError(new Throwable());

                        countRetry++;
                        Log.i("retry", "Lan thu:" + countRetry);

                        if (countRetry < maxRetry) {
                            Log.i("retry", "Continue index:" + i + "- " + countRetry);
                            i--;
                        } else
                            break;
                    }
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());
    }

    private static Call<SimpleUploadResponse> uploadSingleFile(String sessionId, @NonNull String filePath, long totalUploaded, long totalSize, UploadHelper.ProgressCallback progressCallback) {
        final File file = new File(filePath);
        long size = file.length();

        APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);
        Log.i("UPLOAD", "INTO UPLOAD FILE...");

        String ctRange = "bytes " + totalUploaded + "-" + (totalUploaded + size - 1) + "/" + totalSize;
        Log.i("ctr", ctRange);

        return apiService.uploadSingleFile(token, size, ctRange, sessionId, MultipartBody.Part.createFormData("filedbody", file.getName(), new ProgressRequestBody(file, "*", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(long currentUploaded) {
                progressCallback.onProgressChanged(currentUploaded);
            }
        })));
    }

    private static Single<SessionInfoResponse> checkOrCreate(@Nullable String token, @Nullable String sessionId, long total) {

        APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);
        Single<SessionInfoResponse> single;
        if (sessionId != null) {
            single = apiService.getSessionInfo(token, sessionId);
        } else {
            Log.i("ss", "===create session");
            single = apiService.createSession(token, new SessionBody("", total));
        }
        return single.subscribeOn(Schedulers.io());
    }
}
