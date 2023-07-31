package com.aniwoh.unzip;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> manageExternalStorageLauncher;
    private ActivityResultLauncher<String> filePickerLauncher;
    private TextView pathview;
    private TextView logview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pathview=findViewById(R.id.path);
        logview=findViewById(R.id.log);

        manageExternalStorageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 下列代码是回调函数
                });
        // 调用ActivityResultLauncher必须在初始化后才可以调用
        requestManageExternalStoragePermission();

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Read the selected file's path
                        String filePath = getFilePathFromUri(uri);
                        String despath=getPath(filePath);
                        String filename=getname(filePath);
                        // Set the file path to the TextView
                        pathview.setText(filePath);
                        String finalpath=despath+filename;
                        File dir = new File(finalpath); //以某路径实例化一个File对象
                        if (!dir.exists()){ //如果不存在
                            boolean dr = dir.mkdirs(); //创建目录
                            System.out.println(dr);
                        }
                        try {
                            UnzipUtility.main(filePath,finalpath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        logview.setText("开始压缩");
                    }
                });
    }
    public void onSelectFileButtonClick(View view) {
        // Method to handle the button click event
        // Launch the file picker
        filePickerLauncher.launch("*/*");
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // SAF file picker result
//            DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
//            filePath = "新方法："+documentFile.getUri().getPath();
//            filePath = documentFile.getName();
            filePath="该方式为SAF，暂不支持";
        } else {
            // For older file pickers
//            filePath = "老方法："+uri.getPath()+getFileNameFromUri(uri);
            filePath=getFileFromContentUri(this,uri);
        }
        return filePath;
    }

    private String getPath(String filepath){
        String target;
        int lastIndex = filepath.lastIndexOf("/");
        if (lastIndex != -1 && lastIndex < filepath.length() - 1) {
            target=filepath.substring(0,lastIndex + 1);
        } else {
            target = "";
        }
        return target;
    }

    private String getname(String filepath){
        String target;
        int lastIndex = filepath.lastIndexOf("/");
        int lastIndex2 = filepath.lastIndexOf(".");
        if (lastIndex != -1 && lastIndex < filepath.length() - 1) {
            target=filepath.substring(lastIndex + 1,lastIndex2);
        } else {
            target = "";
        }
        return target;
    }

    @SuppressLint("Range")
    public static String getFileFromContentUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String filePath;
        String[] filePathColumn = {MediaStore.DownloadColumns.DATA, MediaStore.DownloadColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            try {
                filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                return filePath;
            } catch (Exception ignored) {
            } finally {
                cursor.close();
            }
        }
        return "";
    }

    private void requestManageExternalStoragePermission() {
        // 只在Build.VERSION.SDK_INT >= Build.VERSION_CODES.R，即安卓11以上版本支持
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//            startActivity(intent);
            manageExternalStorageLauncher.launch(intent);
        }

    }
}