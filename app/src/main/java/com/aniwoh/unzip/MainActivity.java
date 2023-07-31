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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> manageExternalStorageLauncher;
    private ActivityResultLauncher<String> filePickerLauncher;
    private TextView pathview;
    private TextView logview;

    @SuppressLint("SetTextI18n")
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
                // GetContent输入字符串，返回一个uri
                uri -> {
                    if (uri != null) {
                        //读取到了文件
                        String filePath = getFilePathFromUri(uri); //文件的真实路径
                        String dirpath=getPath(filePath); //文件所在的目录
                        String filename=getname(filePath); //文件名

                        pathview.setText(filePath);
                        String despath=dirpath+filename; //文件保存的路径，默认与源文件同目录
                        File dir = new File(despath); //以某路径实例化一个File对象
                        if (!dir.exists()){ //如果目录不存在
                            boolean dr = dir.mkdirs(); //创建目录
                            System.out.println(dr);
                        }
                        try {
                            logview.setText("");
                            logview.append(getTime()+" 开始解压缩"+'\n');
                            UnzipUtility.main(filePath,despath);
                            logview.append(getTime()+" 解压完成"+'\n');
                        } catch (IOException ignored) {
                        }
                    }
                });
    }
    public void onSelectFileButtonClick(View view) {
        // 点击button后的操作，在xml文件里调用
        filePickerLauncher.launch("*/*");
    }

    public String getTime(){
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return  time.format(formatter);
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