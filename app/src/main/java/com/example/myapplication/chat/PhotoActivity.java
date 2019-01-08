package com.example.myapplication.chat;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.myapplication.chat.ChatBoxAdapter.decodedImage_final;

public class PhotoActivity extends AppCompatActivity {
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //이미지 출력
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullimage);
        getIntent();
        ImageView imageview = findViewById(R.id.fullScreenImageView);
        imageview.setImageBitmap(decodedImage_final);

        //사진을 핸드폰에 저장하기 기능
        findViewById(R.id.fab_photoDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long mNow = System.currentTimeMillis();
                Date mDate = new Date(mNow);

                String ex_storage =Environment.getExternalStorageDirectory().getAbsolutePath(); // Get Absolute Path in External Sdcard
                String folder_name = "/"+ "DCIM/Camera" +"/";
                String file_name = "IMG_"+ mFormat.format(mDate) +".jpg";
                String string_path = ex_storage + folder_name;
                Log.e("String_Path", string_path);


                File file_path;
                try{
                    file_path = new File(string_path);
                    if(!file_path.isDirectory()){
                        file_path.mkdirs();
                    }
                    FileOutputStream out = new FileOutputStream(string_path+file_name);
                    Log.e("File_Path", string_path+file_name);
                    decodedImage_final.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                }catch(FileNotFoundException exception){
                    Log.e("FileNotFoundException", exception.getMessage());
                }catch(IOException exception){
                    Log.e("IOException", exception.getMessage());
                }


                /*
                String newFilePath = MediaStore.Images.Media.insertImage(getContentResolver(), decodedImage, mFormat.format(mDate), "description");
                Log.e("New File Path", newFilePath);

                Uri myUri = Uri.parse(newFilePath);

                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(myUri);
                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                File f = new File("file:/"+ Environment.
                        getExternalStorageDirectory()+"/DCIM/Camera/"+"IMG_"+ mFormat.format(mDate));

                Log.e("WantedFilePate", "file:/"+ Environment.
                        getExternalStorageDirectory()+"/DCIM/Camera/"+"IMG_"+ mFormat.format(mDate));
                Uri contentUri = Uri.fromFile(f);
                intent.setData(contentUri);
                sendBroadcast(intent);
                Log.e("Broadcast", "BroadCast the image");

                /*
                Uri uri = Uri.parse("file:/"+ Environment.
                        getExternalStorageDirectory()+"/DCIM/Camera/"+"IMG_"+ mFormat.format(mDate));
                intent.setData(uri);
                sendBroadcast(intent);
                Log.e("Broadcast", "BroadCast the image");


                MediaStore.Images.Media.insertImage(getContentResolver(), decodedImage, mFormat.format(mDate), "description");
                IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
                intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
                intentFilter.addDataScheme("file");
                registerReceiver(mReceiver, intentFilter);

//                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
//                        + Environment.getExternalStorageDirectory())));

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                    final Uri contentUri = Uri.fromFile(Uri.parse("file://" + Environment.getExternalStorageDirectory()));
//                    scanIntent.setData(contentUri);
//                    sendBroadcast(scanIntent);
//                } else {
//                    final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
//                    sendBroadcast(intent);
//                }
                String ext = Environment.getExternalStorageState();
                Log.e("Storage", ext);

                if (ext.equals(Environment.MEDIA_MOUNTED)) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file:/"+ Environment.
                            getExternalStorageDirectory()+"/DCIM/Camera/"+"IMG_"+ mFormat.format(mDate))));
                    Log.e("FilePath", Environment.
                            getExternalStorageDirectory()+"/DCIM/Camera/"+"IMG_"+ mFormat.format(mDate));

                } else {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse("file://"+ Environment.
                            getExternalStorageDirectory()+"/DCIM/Camera/"+"IMG_"+ mFormat.format(mDate))));
                }
                */
            }
        });


    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override

        public void onReceive(Context context, Intent intent)
        {

            if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED))
                Toast.makeText(PhotoActivity.this, "사진을 업데이트하고있습니다" ,
                        Toast.LENGTH_SHORT).show();

            else if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED))
                Toast.makeText(PhotoActivity.this, "사진이 업데이트 되었습니다" ,
                        Toast.LENGTH_SHORT).show();
        }

    };

    static public Bitmap resizeBitmap(Bitmap original) {

        int resizeWidth = 400;

        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();
        int targetHeight = (int) (resizeWidth * aspectRatio);
        Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, targetHeight, false);
        if (result != original) {
            original.recycle();
        }
        return result;
    }
}
