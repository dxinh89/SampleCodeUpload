package com.example.samplecode.models;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private File mFile;
    private String mPath;
    private UploadCallbacks mListener;
    private String content_type;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(long percentage);
    }

    public ProgressRequestBody(final File file, String content_type, final UploadCallbacks listener) {
        this.content_type = content_type;
        mFile = file;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(content_type + "/*");
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        //long fileLength = mFile.length(); // 1 -- TOTAL

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new ProgressUpdater(uploaded));

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;

        public ProgressUpdater(long uploaded) {
            mUploaded = uploaded;
        }

        @Override
        public void run() {
            mListener.onProgressUpdate(mUploaded);
        }
    }
}