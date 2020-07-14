package com.example.androidnetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PicLoadActivity extends AppCompatActivity implements View.OnClickListener{

    private Button picLoad;
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_load);

        picLoad = findViewById(R.id.picLoad);
        picLoad.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.picLoad){
            loadPicture();
            loadBigPicture();
        }
    }

    /*
    * 大图片的加载
    * @Param
    * */
    private void loadBigPicture() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),,options);
    }

    /*
    * 连接网站加载图片
    * @Param
    * */
    private void loadPicture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:9102/imgs/1.png");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(20000);
                    connection.setRequestProperty("Accept-Language","zh-CN,zh;q=0.9");//语言
                    connection.setRequestProperty("Accept","*/*");
                    connection.connect();
                    //结果码
                    int code = connection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        //请求体
                        InputStream inputStream = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        updateUI(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
    * 将加载的图片放在ImageView上
    * */
    private void updateUI(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                image = findViewById(R.id.image);
                image.setImageBitmap(bitmap);
            }
        });
    }
}
