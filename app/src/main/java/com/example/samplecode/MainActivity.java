package com.example.samplecode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.samplecode.models.ChecksumUtils;
import com.example.samplecode.models.NetworkStateReceiver;
import com.example.samplecode.models.ProgressRequestBody;
import com.example.samplecode.models.SessionBody;
import com.example.samplecode.models.SessionResponse;
import com.example.samplecode.models.SimpleUploadResponse;
import com.example.samplecode.models.SplitFileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ProgressRequestBody.UploadCallbacks, NetworkStateReceiver.NetworkStateReceiverListener {

    static final String[] PERMISSIONS_STORAGE = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    static final int REQUEST_EXTERNAL_STORAGE = 601;
    static final int REQUEST_ACTIVITY_CHOOSE_FILE = 602;

    static final String DEFAULT_TEST_TOKEN = "D2-D8-A5-C2-12-48-BC-29-11-D5-34-39-76-9F-D7-1E-9F-F1-DB-92";

    Button btnChooseFile, btnUpload;
    //ProgressBar progressBar;

    String fileUploadPath;
    String fileUploadPathZip;
    long mTotalLeng;
    long mAllUploaded = 0;
    String mSessionId;

    int mCountRetryUpload;


    List<String> partFilePaths = new ArrayList<>();
    int currentPart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnChooseFile.setOnClickListener(this);

        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(this);

        //progressBar = findViewById(R.id.progressBar);

        NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.addListener(this);
    }

    @Override
    public void onNetworkAvailable() {

    }

    @Override
    public void onNetworkUnavailable() {

    }

//    protected void aaa(){
//        APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);
//        apiService.getUserDetails();
//    }

    // Ham Upload------
    protected void doUpload(String token, String sessionId, String itemFilePath) {

        //Vong lăp thu--------
        Log.i("UPLOAD", "VONG LAP=======: " + currentPart);

        final File file = new File(itemFilePath);
        final long ctLen = file.length(); // - unit: bytes
        final String ctRange = "bytes " + mAllUploaded + "-" + (mAllUploaded + ctLen - 1) + "/" + mTotalLeng;

        ProgressRequestBody fileBody = new ProgressRequestBody(file, "*", this);
        MultipartBody.Part body = MultipartBody.Part.createFormData("Image", file.getName(), fileBody);

        //creating our api
        APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);

        final Call<SimpleUploadResponse> call = apiService.upload1(token, ctLen, ctRange, sessionId, body);
        call.enqueue(new Callback<SimpleUploadResponse>() {
            @Override
            public void onResponse(Call<SimpleUploadResponse> call, Response<SimpleUploadResponse> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getCode() == 0) {
                        String s = response.body().getMessage() + ": " + response.body().getDescription();
                        Log.i("UPLOAD", s);

                        mAllUploaded += file.length();
                        currentPart++;
                        if (currentPart < partFilePaths.size())
                            doUpload(DEFAULT_TEST_TOKEN, mSessionId, partFilePaths.get(currentPart)); //đệ quy
                        else if (currentPart == partFilePaths.size()) {
                            //get link file
                            String linkFile = response.body().getDescription();
                            Log.i("UPLOAD", "Link file =" + linkFile);
                            mAllUploaded = 0;
                        }
                    }
                } else {
                    mCountRetryUpload++;
                    if (mCountRetryUpload <= 2)
                        doUpload(DEFAULT_TEST_TOKEN, mSessionId, partFilePaths.get(currentPart));
                }

            }

            @Override
            public void onFailure(Call<SimpleUploadResponse> call, Throwable t) {
                Log.i("UPLOAD", "=====LOI KET NOI" + t.getMessage());
            }
        });
    }

    @Override
    public void onProgressUpdate(long uploaded) {
        //progressBar.setProgress(percentage);
        int percentage = (int) (100 * (mAllUploaded + uploaded) / mTotalLeng);
        Log.i("UPLOAD", "Loading..." + percentage + "%");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChooseFile:
                verifyStoragePermissions(this);
                break;

            case R.id.btnUpload:

                File file = new File(fileUploadPathZip);
                final long totalSize = file.length(); // - unit: bytes
                String md5 = ChecksumUtils.checkSum(fileUploadPathZip);
                Log.i("UPLOAD", "size=" + totalSize + "-md5=" + md5);

                APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);
                apiService.createSession1(DEFAULT_TEST_TOKEN, new SessionBody(md5, totalSize)).enqueue(new Callback<SessionResponse>() {
                    @Override
                    public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                mSessionId = response.body().getSessionId(); //"F4D47F65091619091550679408";
                                Log.i("UPLOAD", "SSID=" + mSessionId);

                                //new UploadAsyncTask(getApplicationContext(), sessionId, fileUploadPathZip).execute();

                                currentPart = 0;
                                doUpload(DEFAULT_TEST_TOKEN, mSessionId, partFilePaths.get(currentPart)); // first load

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SessionResponse> call, Throwable t) {
                        Log.i("UPLOAD", "ERR=" + t.getMessage());
                    }
                });

                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission accepted", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        String path = "";
        if (requestCode == REQUEST_ACTIVITY_CHOOSE_FILE) {
            Uri uri = data.getData();
            //String FilePath = getRealPathFromURI(uri); // should the path be here in this string
            fileUploadPath = getRealPathFromURI_API19(this, uri);

            Log.i("ON_BROWSE", "=path=" + fileUploadPath);
            String baseUrl = fileUploadPath.substring(0, fileUploadPath.lastIndexOf(File.separator));
            Log.i("ON_BROWSE", "=pathOnly=" + baseUrl);

            try {
                fileUploadPathZip = zipFile(fileUploadPath);
                File file = new File(fileUploadPathZip);
                mTotalLeng = file.length(); // TOTAL LENGTH

                //split file
                partFilePaths.clear();
                partFilePaths = new SplitFileUtils(getApplicationContext()).splitFile(fileUploadPathZip);
                Log.i("ON_BROWSE", "split len= " + partFilePaths.size());

                //xóa file zip
                file.delete();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Zip file
    public String zipFile(String filePath) throws IOException {
        String outputZip = filePath.substring(0, filePath.lastIndexOf(File.separator)) + "/compressed.zip";

        FileOutputStream fos = new FileOutputStream(outputZip);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(ZipOutputStream.STORED);

        File fileToZip = new File(filePath);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        zipOut.close();
        fis.close();
        fos.close();

        return outputZip;
    }

    //Cap quyen truy cap bo nho
    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //check permission
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            //do action after permission accepted...
            onBrowse();
        }
    }

    //chon file
    public void onBrowse(/*View view*/) {
        Log.i("ON_BROWSE", "Vao hàm chọn");
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        //chooseFile.setType("file/*");
        chooseFile.setType("*/*");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, REQUEST_ACTIVITY_CHOOSE_FILE);
    }

    //real path
    public String getRealPathFromURI(Uri contentUri) {
        //Log.i("ON_BROWSE", "URI="+contentUri.getPath());

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        // String[] column = { MediaStore.Images.Media.DATA };// image
        String[] column = {MediaStore.Files.FileColumns.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

}
