package com.example.samplecode;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.samplecode.models.ProgressRequestBody;
import com.example.samplecode.models.SimpleUploadResponse;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;

public class DemoUp {

    public Observable<Integer> uploadFiles(final List<String> lstFile) {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            int totalUploaded = 0;
            int previousTotal;
            Call lastCall;

            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                long total = 0 ;
                for (String s : lstFile) {
                    total+=(new File(s).length());
                }
                emitter.setCancellable(() -> lastCall.cancel());
                for (String path : lstFile) {
                    if (emitter.isDisposed()) {
                        break;
                    }
                    previousTotal = 0;
                    lastCall = uploadSingleFile(path, new UploadHelper.ProgressCallback() {
                        @Override
                        public void onProgressChanged(long uploadedBytes) {
                            totalUploaded += (uploadedBytes - previousTotal);
                            previousTotal = (int) uploadedBytes;
                            emitter.onNext(totalUploaded);
                        }
                    });
                    lastCall.execute();
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());

    }


    private static Call<SimpleUploadResponse> uploadSingleFile(@NonNull String filePath, UploadHelper.ProgressCallback progressCallback) {
        final File file = new File(filePath);
        long size = file.length();
        Log.i("UPLOAD", "INTO UPLOAD FILE>>>" + filePath + "size:" + size);

        APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);
        Log.i("UPLOAD", "INTO UPLOAD FILE<<<");

        String token = "D2-D8-A5-C2-12-48-BC-29-11-D5-34-39-76-9F-D7-1E-9F-F1-DB-92";
        String ctRange = "bytes 0-" + (size - 1) + "/397152";
        String sessionId = "B7B98AFC092319091527511163";
        return apiService.upload111(null, 1, null, null, null);
    }
}
