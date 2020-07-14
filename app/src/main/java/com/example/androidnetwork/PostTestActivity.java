package com.example.androidnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.androidnetwork.domain.CommentItem;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PostTestActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "PostTestActivity";
    private static final String BASIC_URL = "http://10.0.2.2:9102";
    private static final int PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_test);
        Button button = findViewById(R.id.postRequest);
        button.setOnClickListener(this);

        //动态请求权限
        int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }



    /*
     * 处理请求用户权限的操作
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE) {
            //判断结果
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"has permissions..");
                //有权限
            } else {
                Log.d(TAG,"no permissionS...");
                //没权限
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_CALENDAR)&&!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CALENDAR)) {
                    //走到这里，说明用户之前用户禁止权限的同时，勾选了不再询问
                    //那么，你需要弹出一个dialog，提示用户需要权限，然后跳转到设置里头去打开。
                    Log.d(TAG,"用户之前勾选了不再询问...");
                    //TODO:弹出一个框框，然后提示用户说需要开启权限。
                    //TODO:用户点击确定的时候，跳转到设置里去
                    //Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //Uri uri = Uri.fromParts("package", getPackageName(), null);
                    //intent.setData(uri);
                    ////在activity结果范围的地方，再次检查是否有权限
                    //startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR}, PERMISSION_REQUEST_CODE);
                    //请求权限
                    Log.d(TAG,"请求权限...");
                }
            }
        }
    }

    /*
     * getParam的点击事件
     * 在布局中写上Android：onclick="getParam"
     * url的带参请求——get
     * */
    public void getParam(View view){
        Map<String,String> param = new HashMap<>();
        param.put("keyword","这是我的关键字");
        param.put("page","12");
        param.put("order","0");
        startRequest(param,"GET","/get/param");
    }


    /*
     * postParam的点击事件
     * url的带参请求——post
     * */
    public void postParam(View view){
        Map<String,String> param = new HashMap<>();
        param.put("string","这是我提交的字符串");
        startRequest(param,"POST","/post/string");
    }

    /*
     * 获取url的参数,并连接网络
     * @Param param  method  api
     * */
    private void startRequest(final Map<String, String> param, final String method, final String api) {
        new Thread(new Runnable() {

            private InputStream inputStream = null;
            private BufferedReader reader = null;

            @Override
            public void run() {
                try {
                    StringBuffer buffer = new StringBuffer();
                    //组装参数
                    if (param!=null) {
                        buffer.append("?");
                        Iterator<Map.Entry<String, String>> iterator = param.entrySet().iterator();
                        while(iterator.hasNext()){
                            Map.Entry<String, String> next = iterator.next();
                            buffer.append(next.getKey());
                            buffer.append("=");
                            buffer.append(next.getValue());
                            if (iterator.hasNext()) {
                                buffer.append("&");
                            }
                        }
                        Log.d(TAG, "PostTest-startRequest-param"+buffer.toString());
                    }
                    String Params = buffer.toString();
                    URL url;
                    if (Params!=null && Params.length()>0) {
                        url = new URL(BASIC_URL+api+buffer);
                    }else{
                        url = new URL(BASIC_URL + api);
                    }
                    Log.d(TAG, "PostTestActivity-startRequest-url:"+url.toString());
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod(method);
                    httpURLConnection.setRequestProperty("Accept-Language","zh-CN,zh;q=0.9");//语言
                    httpURLConnection.setRequestProperty("Accept","*/*");
                    httpURLConnection.connect();
                    //获取结果码
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == 200) {
                        //获取请求体
                        inputStream = httpURLConnection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        String json = reader.readLine();
                        Log.d(TAG, "PostTestActivity-startRequest-json:" + json);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }


    /*
     * postRequest的点击事件
     * 通过post上传数据给网络并获取
     * */
    @Override
    public void onClick(View v) {
        new Thread(new Runnable() {

            private InputStream inputStream = null;
            private OutputStream outputStream = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(BASIC_URL+"/post/comment");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    //设置
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setConnectTimeout(30000);
                    httpURLConnection.setRequestProperty("Content-Type","application/json;charset=UTF-8");
                    httpURLConnection.setRequestProperty("Accept-Language","zh-CN,zh;q=0.9");//语言
                    httpURLConnection.setRequestProperty("Accept","application/json , text/plain , */*");
                    CommentItem commentItem = new CommentItem("2546534798","写的不错");
                    Gson gson = new Gson();
                    String jsonStr = gson.toJson(commentItem);
                    byte[] bytes = jsonStr.getBytes("UTF-8");
                    Log.d(TAG,"PostTestActivity-onclick-jsonStr:"+jsonStr);
                    Log.d(TAG, "PostTestActivity-onclick-content_length:"+bytes.length);
                    Log.d(TAG,"PostTestActivity-onclick-bytes:"+bytes);

                    httpURLConnection.setRequestProperty("Content-Length",String.valueOf(bytes.length));
                    //连接
                    httpURLConnection.connect();
                    //传输数据
                    outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(bytes);
                    outputStream.flush();
                    //获取数据
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        Log.d(TAG, "PostTestActivity-onclick-line:"+reader.readLine());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (outputStream!=null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream!=null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /*
     * 单文件上传
     * */

    public void postFile(View view){
        new Thread(new Runnable() {
            private InputStream inputStream = null;
            private BufferedInputStream bfi = null;
            private OutputStream outputStream = null;
            String BOUNDARY = "--------------------------954555323792164398227139";//边界值,后面是随机数
            //String BOUNDARY = "----------------------------954555323792164398227139--";
            //String BOUNDARY = "----------------------------954555323792164398227139";

            File file = new File("/storage/emulated/0/Download/naruto.jpg");

            String fileKey = "file";//相当于post上传时的param的key
            String fileName = file.getName();
            String fileType = "image/jpeg";  //对应文件后缀为jpg

            @Override
            public void run() {
                try {
                    URL url = new URL(BASIC_URL+"/file/upload");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //请求的设置
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(30000);
                    connection.setRequestProperty("User-Agent","Android/" + Build.VERSION.SDK_INT);
                    connection.setRequestProperty("Accept","*/*");
                    connection.setRequestProperty("Cache-Control","no-cache");
                    connection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY);
                    connection.setRequestProperty("Connection","keep-alive");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    //连接
                    connection.connect();
                    //准备数据
                    StringBuilder headerSbInfo = new StringBuilder();
                    headerSbInfo.append("--");
                    headerSbInfo.append(BOUNDARY);
                    headerSbInfo.append("\r\n");
                    headerSbInfo.append("Content-Disposition: form-data; name=\"" + fileKey + "\"; filename=\"" + fileName + "\"");
                    headerSbInfo.append("\r\n");
                    headerSbInfo.append("Content-Type: " + fileType);
                    headerSbInfo.append("\r\n");
                    headerSbInfo.append("\r\n");
                    byte[] headerInfoBytes = headerSbInfo.toString().getBytes("UTF-8");
                    outputStream = connection.getOutputStream();
                    outputStream.write(headerInfoBytes);
                    //文件内容
                    FileInputStream fos = new FileInputStream(file);
                    BufferedInputStream bfi = new BufferedInputStream(fos);
                    byte[] buffer = new byte[1024];
                    int len;
                    while((len = bfi.read(buffer,0,buffer.length)) != -1) {
                        outputStream.write(buffer,0,len);
                    }
                    //写尾部信息
                    StringBuilder footerSbInfo = new StringBuilder();
                    footerSbInfo.append("\r\n");
                    footerSbInfo.append("--");
                    footerSbInfo.append(BOUNDARY);
                    footerSbInfo.append("--");
                    footerSbInfo.append("\r\n");
                    footerSbInfo.append("\r\n");
                    outputStream.write(footerSbInfo.toString().getBytes("UTF-8"));
                    outputStream.flush();
                    //读取返回结果
                    int connectionCode = connection.getResponseCode();
                    Log.d(TAG, "PostTestActivity-postFile-connectionCode:"+connectionCode);
                    if (connectionCode == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String result = bufferedReader.readLine();
                        Log.d(TAG, "PostTestActivity-postFile-result:"+result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (bfi != null) {
                        try {
                            bfi.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }


    /*
     * 加载文件上传信息
     * 包括数据部分、文件内容、尾部信息
     * */
    private void uploadFile(String BOUNDARY, String fileKey, String fileType, String fileName, OutputStream outputStream, File file,boolean isLast) throws IOException {
        //准备数据
        StringBuilder headerSbInfo = new StringBuilder();
        headerSbInfo.append("--");
        headerSbInfo.append(BOUNDARY);
        headerSbInfo.append("\r\n");
        headerSbInfo.append("Content-Disposition: form-data; name=\"" + fileKey + "\"; filename=\"" + fileName + "\"");
        headerSbInfo.append("\r\n");
        headerSbInfo.append("Content-Type: " + fileType);
        headerSbInfo.append("\r\n");
        headerSbInfo.append("\r\n");
        byte[] headerInfoBytes = headerSbInfo.toString().getBytes("UTF-8");
        outputStream.write(headerInfoBytes);
        //文件内容
        FileInputStream fos = new FileInputStream(file);
        BufferedInputStream bfi = new BufferedInputStream(fos);
        byte[] buffer = new byte[1024];
        int len;
        while((len = bfi.read(buffer,0,buffer.length)) != -1) {
            outputStream.write(buffer,0,len);
        }
        //写尾部信息
        StringBuilder footerSbInfo = new StringBuilder();
        footerSbInfo.append("\r\n");
        footerSbInfo.append("--");
        footerSbInfo.append(BOUNDARY);
        if(isLast) {//只有上传的最后一个文件后面才会又--
            footerSbInfo.append("--");
            footerSbInfo.append("\r\n");
        }
        footerSbInfo.append("\r\n");
        outputStream.write(footerSbInfo.toString().getBytes("UTF-8"));
    }


    /*
     * 多文件上传
     * */
    public void postFiles(View view){
        new Thread(new Runnable() {
            private InputStream inputStream = null;
            private BufferedInputStream bfi = null;
            private OutputStream outputStream = null;

            String BOUNDARY = "--------------------------954555323792164398227139";//边界值,后面是随机数
            //String BOUNDARY = "----------------------------954555323792164398227139--";
            //String BOUNDARY = "----------------------------954555323792164398227139";

            File file1 = new File("/storage/emulated/0/Download/naruto.jpg");
            File file2 = new File("/storage/emulated/0/Download/timg.jpeg");
            File file3 = new File("/storage/emulated/0/Download/u=2221979916,487946013&fm=26&gp=0.jpg");
            File file4 = new File("/storage/emulated/0/Download/u=3394313492,1153728278&fm=26&gp=0.jpg");

            String fileKey = "files";//相当于post上传时的param的key
            String fileType = "image/jpeg";  //对应文件后缀为jpg

            @Override
            public void run() {
                try {
                    URL url = new URL(BASIC_URL+"/files/upload");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //请求的设置
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(30000);
                    connection.setRequestProperty("User-Agent","Android/" + Build.VERSION.SDK_INT);
                    connection.setRequestProperty("Accept","*/*");
                    connection.setRequestProperty("Cache-Control","no-cache");
                    connection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY);
                    connection.setRequestProperty("Connection","keep-alive");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    //连接
                    connection.connect();
                    outputStream = connection.getOutputStream();
                    uploadFile(BOUNDARY,fileKey,fileType,file1.getName(),outputStream,file1,false);
                    uploadFile(BOUNDARY,fileKey,fileType,file2.getName(),outputStream,file2,false);
                    uploadFile(BOUNDARY,fileKey,fileType,file3.getName(),outputStream,file3,false);
                    uploadFile(BOUNDARY,fileKey,fileType,file4.getName(),outputStream,file4,true);//最后一个上传的文件
                    outputStream.flush();
                    //读取返回结果
                    int connectionCode = connection.getResponseCode();
                    Log.d(TAG, "PostTestActivity-postFile-connectionCode:"+connectionCode);
                    if (connectionCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String result = bufferedReader.readLine();
                        Log.d(TAG, "PostTestActivity-postFile-result:"+result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (bfi != null) {
                        try {
                            bfi.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }).start();
    }

}
