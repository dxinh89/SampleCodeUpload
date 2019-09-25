package com.example.samplecode;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.samplecode.models.SessionResponse_REMOVE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class UploadHelper {
    public static Observable<UploadProgress> uploadFeedbackAttachments(@NonNull List<String> filePathList, @NonNull String zipFileDir) {


        SharedPreferences sharedPreferences;

        //if(sharedPreferences.getBoolean("filePathList", false)){

        return zipMultiFile(filePathList, zipFileDir)
                .andThen(splitFile(zipFileDir))
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .flatMapObservable(new Function<List<String>, ObservableSource<UploadProgress>>() {
                    @Override
                    public ObservableSource<UploadProgress> apply(List<String> splitFileList) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<UploadProgress>() {
                            Disposable lastUploadDisposable;
                            long totalSize = 6000;
                            long uploaded = 0;
                            long partBytes = 0;

                            int indexFileUploaded = 0;
                            int maxIndex = splitFileList.size();

                            @Override
                            public void subscribe(ObservableEmitter<UploadProgress> emitter) {

                                emitter.setCancellable(new Cancellable() {
                                    @Override
                                    public void cancel() throws Exception {
                                        if (lastUploadDisposable != null) {
                                            Log.i("UPLOAD", "cancel upload chunk");
                                            lastUploadDisposable.dispose();
                                        }
                                    }
                                });

                                emitter.onNext(new UploadProgress(0, totalSize));


                                Log.i("UPLOAD", "thread bat dau upload: " + Thread.currentThread().getName());

//                              for (int i = 0; i < splitFileList.size(); i++) Log.i("UPLOAD", "Gia tri mang= " + splitFileList.get(i)); //getOrCreateSession ----//createSession();

                                for (; indexFileUploaded < maxIndex; indexFileUploaded++) {
                                    if (!emitter.isDisposed()) {

//                                        lastUploadDisposable = uploadFile(splitFileList.get(indexFileUploaded), new ProgressCallback() {
//                                            @Override
//                                            public void onProgressChanged(long uploadedBytes/*, long currentPartBytes*/) {
//                                                //partBytes = currentPartBytes;
//                                                uploaded += uploadedBytes;
//                                                //Log.i("UPLOAD", "--change=" + (uploaded + uploadedBytes));
//                                                emitter.onNext(new UploadProgress((uploaded), totalSize));
//                                                Log.i("UPLOAD", "--change=");
//                                            }
//                                        }).retry(new Predicate<Throwable>() {
//                                            int countRetry = 0;
//                                            final int maxRetry = 3;
//
//                                            @Override
//                                            public boolean test(Throwable throwable) throws Exception {
//                                                //trong trường hợp cancel (lastUploadDisposable.dispose();) thì cũng gây ra error nên cần kiểm tra xem throwable có phải do cancel ko
//                                                Thread.sleep(500);
//                                                return countRetry++ < maxRetry && !emitter.isDisposed();
//                                            }
//                                        }).subscribe(new Consumer<SimpleUploadResponse>() {
//                                            @Override
//                                            public void accept(SimpleUploadResponse result) throws Exception {
//                                                Log.i("UPLOAD", "Ket qua= " + result.toString());
////                                                        if (result.getCode() == 0) {
////                                                            uploaded += partBytes;
////                                                        }
//
//                                            }
//                                        }, error -> {
//                                            Log.i("UPLOAD", "ERR!!!!!!");
//                                            //trong trường hợp cancel (lastUploadDisposable.dispose();) thì cũng gây ra error nên cần kiểm tra xem error có phải do cancel ko
//                                            if (!emitter.isDisposed()) {
//                                                //trường hợp  cancel
//                                            } else {
//
//                                            }
//                                        });
                                    }
                                }

                                emitter.onComplete();
                            }
                        });
                    }
                });
    }

    private static Completable zipMultiFile(@NonNull List<String> filePathList, @NonNull String zipFileDir) {
        return Completable.fromAction(() -> {
            Log.i("test1", "thread zipFile: " + Thread.currentThread().getName());

            try {
                String outputZip = zipFileDir + "/compressed.zip";

                FileOutputStream fos = new FileOutputStream(outputZip);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                for (String srcFile : filePathList) {
                    File fileToZip = new File(srcFile);
                    FileInputStream fis = new FileInputStream(fileToZip);
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    fis.close();
                }
                zipOut.close();
                fos.close();
            } catch (IOException e) {

                // nem loi
                throw new SampleActivity.GeneralException(1, "");

            }
        });
    }

    private static Single<List<String>> splitFile(@NonNull String fileDir) {

        return Single.fromCallable(() -> {
            //Split
            List<String> partFilePaths = new ArrayList<>();
            final int _5MB = 50000; // 5 * 1024 * 1024; //mb kb b

            //String fileDir = "/data/user/0/com.example.samplecode/files";
            String filePath = fileDir + "/compressed.zip";
            File file = new File(filePath);
            int totalSize = Integer.parseInt(String.valueOf(file.length())); // - unit: bytes
            if (totalSize < _5MB) {
                partFilePaths.add(filePath);
                return partFilePaths;
            }

            if (file.exists()) {
                Log.i("ON_BROWSE", "File exists:" + totalSize);
            } else
                Log.i("ON_BROWSE", "File isn't exists!");

            InputStream inputStream = new FileInputStream(file);//getResources().openRawResource(R.raw.test);
            OutputStream out = null;

            //String dir = mContext.getFilesDir().toString();
            Log.i("ON_BROWSE", "real==" + fileDir); // /storage/emulated/0/DCIM/Camera/compressed.zip

            byte[] buf = new byte[1024];
            int len;
            int count = 0;
            int total = 0;
            while ((len = inputStream.read(buf, 0, Math.min(buf.length, _5MB - total))) > 0) {
                if (out == null) {

                    File folder = new File(fileDir + File.separator + "split");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }
                    if (success) {
                        String itemPath = fileDir + "/split/file_" + String.format(Locale.US, "%03d", count); ///data/user/0/com.example.samplecode/files
                        out = new FileOutputStream(new File(itemPath));

                        partFilePaths.add(itemPath);
                    }
                }
                out.write(buf, 0, len);
                total += len;
                if (total >= _5MB) {
                    out.flush();
                    out.close();
                    out = null;
                    total = 0;
                    count++;
                }
            }
            if (out != null) {
                out.close();
            }
            inputStream.close();

            return partFilePaths;
        });
    }

    private static Single<SessionResponse_REMOVE> createSession() {
        return Single.fromCallable(() -> {

            Log.i("UPLOAD", "----------- Vao tao session");

            APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);
//            Single<Response<SessionResponse_REMOVE>> responseSingle = apiService.createSession("D2-D8-A5-C2-12-48-BC-29-11-D5-34-39-76-9F-D7-1E-9F-F1-DB-92", new SessionBody("", 6000L));
//            responseSingle.subscribeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Response<SessionResponse_REMOVE>>() {
//                @Override
//                public void onSubscribe(Disposable d) {
//                    Log.i("UPLOAD", "-----------A");
//                }
//
//                @Override
//                public void onSuccess(Response<SessionResponse_REMOVE> sessionResponseResponse) {
//                    Log.i("UPLOAD", "-----------B");
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    Log.i("UPLOAD", "-----------C");
//                }
//            });

            return new SessionResponse_REMOVE();
        });
    }

//    private static Observable<SimpleUploadResponse> uploadFile(@NonNull String filePath, ProgressCallback progressCallback) {
//
//        final File file = new File(filePath);
//        long size = file.length();
//        Log.i("UPLOAD", "INTO UPLOAD FILE>>>" + filePath + "size:" + size);
//
//        APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);
//        Log.i("UPLOAD", "INTO UPLOAD FILE<<<");
//
//        String token = "D2-D8-A5-C2-12-48-BC-29-11-D5-34-39-76-9F-D7-1E-9F-F1-DB-92";
//        String ctRange = "bytes 0-" + (size - 1) + "/397152";
//        String sessionId = "B7B98AFC092319091527511163";
//
////        Observable<SimpleUploadResponse> tResponse = apiService.upload111(token, size, ctRange, sessionId, MultipartBody.Part.createFormData("filedbody", file.getName(), new ProgressRequestBody(file, "*", new ProgressRequestBody.UploadCallbacks() {
////            @Override
////            public void onProgressUpdate(long currentUploaded) {
////                Log.i("UPLOAD", "------------Load%:" + currentUploaded);
////                progressCallback.onProgressChanged(currentUploaded/*, size*/);
////            }
////        })));
//
//        return tResponse.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
//    }

    static class UploadChunkResponse {
    }

    static class UploadResponse {
    }

    static class UploadProgress {
        long uploadedBytes;
        long totalBytes;

        public UploadProgress(long uploadedBytes, long totalBytes) {
            this.uploadedBytes = uploadedBytes;
            this.totalBytes = totalBytes;
        }

        public long getTotalBytes() {
            return totalBytes;
        }

        public long getUploadedBytes() {
            return uploadedBytes;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.US, "uploaded: %d, total: %d", uploadedBytes, totalBytes);
        }
    }

    SessionInfo getSessionInfo(String id) throws Exception {
        //
        if (id.equals("1")) {
            return new SessionInfo(0);
        } else {
            throw new Exception("");
        }

    }


    class SessionInfo {
        public SessionInfo(int totalUploaded) {
            this.totalUploaded = totalUploaded;
        }

        int totalUploaded;

        public int getTotalUploaded() {
            return totalUploaded;
        }
    }

    interface ProgressCallback {
        void onProgressChanged(long uploadedBytes/*, long currentPartBytes*/);
    }
}
