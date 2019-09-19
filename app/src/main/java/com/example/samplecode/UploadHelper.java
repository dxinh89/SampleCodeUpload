package com.example.samplecode;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class UploadHelper {
    public static Observable<UploadProgress> uploadFeedbackAttachments(@NonNull List<String> filePathList, @NonNull String zipFileDir) {
        return zipMultiFile(filePathList, zipFileDir)
                .andThen(splitFile(zipFileDir)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.io())
                        .flatMapObservable((Function<List<String>, ObservableSource<UploadProgress>>) splitFileList -> Observable.create(new ObservableOnSubscribe<UploadProgress>() {
                            Disposable lastUploadDisposable;
                            int totalSize = 6000;
                            int uploaded = 0;

                            @Override
                            public void subscribe(ObservableEmitter<UploadProgress> emitter) {

                                emitter.setCancellable(() -> {
                                    if (lastUploadDisposable != null) {
                                        Log.v("test1", "cancel upload chunk");
                                        lastUploadDisposable.dispose();
                                    }
                                    emitter.onNext(new UploadProgress(0, totalSize));
                                    Log.v("test1", "thread bat dau upload: " + Thread.currentThread().getName());

                                    for (int i = 0; i < 6; i++) {
                                        if (!emitter.isDisposed()) {
                                            lastUploadDisposable = uploadFile(null, (uploadedBytes, totalBytes) -> {
                                                uploaded += uploadedBytes;
                                                emitter.onNext(new UploadProgress(uploaded, totalSize));
                                            }).retry(new Predicate<Throwable>() {
                                                int count = 0;
                                                final int maxRetry = 3;

                                                @Override
                                                public boolean test(Throwable throwable) throws Exception {
                                                    //trong trường hợp cancel (lastUploadDisposable.dispose();) thì cũng gây ra error nên cần kiểm tra xem throwable có phải do cancel ko
                                                    Thread.sleep(500);
                                                    return count++ < maxRetry && !emitter.isDisposed();
                                                }
                                            }).subscribe(result -> {
                                            }, error -> {
                                                //trong trường hợp cancel (lastUploadDisposable.dispose();) thì cũng gây ra error nên cần kiểm tra xem error có phải do cancel ko
                                                if (!emitter.isDisposed()) {
                                                    //trường hợp  cancel
                                                } else {

                                                }
                                            });
                                        }
                                    }
                                    emitter.onComplete();
                                });
                            }
                        }))
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    private static Completable zipMultiFile(@NonNull List<String> filePathList, @NonNull String zipFileDir) {
        return Completable.fromAction(() -> {
            Log.v("test1", "thread zipFile: " + Thread.currentThread().getName());

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
                throw new SampleActivity.GeneralException(1,"");

            }
        });
    }

    private static Single<List<String>> splitFile(@NonNull String fileDir) {

        new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onError(Throwable e) {

            }
        };

        return Single.fromCallable(() -> {
            //Split
            List<String> partFilePaths = new ArrayList<>();
            final int _5MB = 50000; // 5 * 1024 * 1024; //mb kb b

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

//    private static Completable splitFile2(@NonNull String fileDir) {
//        return Completable.fromAction(() -> {
//            Log.v("test1", "thread splitFile: " + Thread.currentThread().getName());
//
//
//
//        });
//    }

    private static Single<UploadChunkResponse> uploadFile(@NonNull String filePath, ProgressCallback progressCallback) {
        return Single.fromCallable(() -> {
            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                }
                progressCallback.onProgressChanged(500, 1000);
            }

            Log.v("test1", "thread run upload: " + Thread.currentThread().getName());
            return new UploadChunkResponse();
        });
    }

    static class UploadChunkResponse {
    }

    static class UploadResponse {
    }

    static class UploadProgress {
        int uploadedBytes;
        int totalBytes;

        public UploadProgress(int uploadedBytes, int totalBytes) {
            this.uploadedBytes = uploadedBytes;
            this.totalBytes = totalBytes;
        }

        public int getTotalBytes() {
            return totalBytes;
        }

        public int getUploadedBytes() {
            return uploadedBytes;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("uploaded: %d, total: %d", uploadedBytes, totalBytes);
        }
    }

    interface ProgressCallback {
        void onProgressChanged(long uploadedBytes, long totalBytes);
    }
}
