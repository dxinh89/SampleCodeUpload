package com.example.samplecode;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SampleActivity extends AppCompatActivity {
    Disposable uploadDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = new FrameLayout(getBaseContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Button cancelButton = new Button(getBaseContext());
        cancelButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cancelButton.setText("cancel upload");
        frameLayout.addView(cancelButton);
        setContentView(frameLayout);


        cancelButton.setOnClickListener(v -> {
            if (uploadDisposable != null) {
                uploadDisposable.dispose();
            }
        });


        //TEST-------
        List<String> filePathList = new ArrayList<>();
        filePathList.add("/storage/emulated/0/DCIM/Camera/IMG_20190916_152933.jpg");
        filePathList.add("/storage/emulated/0/DCIM/Camera/IMG_20190916_152936.jpg");

        String zipFileDir = getFilesDir().toString();

        ///-----------------------
        List<String> lstFileUp = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            lstFileUp.add("/data/user/0/com.example.samplecode/files/split/file_00" + i);
        }



        DemoUp.uploadFiles(lstFileUp).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<UploadHelper.UploadProgress>() {
            @Override
            public void onSubscribe(Disposable d) {
                uploadDisposable = d;
                Log.i("UPLOAD", "onSubscribe");
            }

            @Override
            public void onNext(UploadHelper.UploadProgress uploadProgress) {
                Log.i("UPLOAD", Thread.currentThread().getName() + " onNext: " + uploadProgress.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.i("UPLOAD", "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.i("UPLOAD", Thread.currentThread().getName() + " onComplete");
            }
        });


//        UploadHelper.uploadFeedbackAttachments(filePathList,zipFileDir)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<UploadHelper.UploadProgress>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        uploadDisposable = d;
//                        Log.i("UPLOAD", "onSubscribe");
//                    }
//
//                    @Override
//                    public void onNext(UploadHelper.UploadProgress uploadProgress) {
//                        Log.i("UPLOAD", Thread.currentThread().getName() + " onNext: " + uploadProgress.toString());
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        if (e instanceof GeneralException) {
////                            if(((GeneralException) e).code == 1){
////
////                            }
//                        }
//                        Log.i("UPLOAD", "onError: " + e.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.i("UPLOAD", Thread.currentThread().getName() + " onComplete");
//
//                    }
//                });

    }

    static class GeneralException extends Exception {
        int code;
        String message;

        GeneralException(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
