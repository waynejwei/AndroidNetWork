# AndroidNetWork
HttpURLConnection，RecyclerView，Gson
## 启动后台程序

**下载后台程序：**

> 阳光沙滩：<https://www.sunofbeach.net/c/1197725454275039232>里面看到后台程序的链接
>
> 链接：<https://github.com/TrillGates/SOBAndroidMiniWeb>。下载下来，然后在里面找到jar文件
>
> Android网络编程后台程序：<https://github.com/TrillGates/SOBAndroidMiniWeb>

在powershell中进入到jar文件的目录路径。然后使用命令`java -jar jar包名称.jar`

将jar包运行起来后，可以看到端口之类的，然后就可以在本地访问 `localhost:9102/get/text`

或者在模拟器上访问 `10.0.2.2:9102/get/text`(10.0.2.2是模拟器访问电脑本地的ip)

访问图片：`/imgs/x.png`  (x:0~16)


**Android案例：**`AndroidNetWork`

因为这里请求的是本地的后台程序，所以首先需要运行后台程序。

> 后台程序的路径：`F:\AndroidTest\SOBAndroidMiniWeb-master\SOBAndroidMiniWeb-master`

在`powershell`中运行，进入程序的路径，然后 `Java -jar 程序文件名.jar`（运行结束后注意观察路径是否正确）

```java
/*
 * 使用HTTP协议请求网络
 * */
private void requestWithHttpURLConnection() {
    //网页请求不能放在主线程,开启子线程
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                //使用HttpURLConnection请求
                URL url = new URL("http://10.0.2.2:9102/get/text");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //设置请求的各种属性
                connection.setConnectTimeout(10000);//请求时长
                connection.setRequestMethod("GET");//请求方式
                connection.setRequestProperty("Accept-Language","zh-CN,zh;q=0.9");//语言
                connection.setRequestProperty("Accept","*/*");
                connection.connect();
                //获取结果码
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    //获取请求头
                    Map<String, List<String>> headerFields = connection.getHeaderFields();
                    Set<Map.Entry<String,List<String>>> entities = headerFields.entrySet();
                    for(Map.Entry<String,List<String>> entry:entities){
                        Log.d(TAG,entry.getKey()+" = "+entry.getValue());
                    }
                    //获取请求体
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = reader.readLine();
                    Log.d(TAG,"line = "+line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }).start();
}
```

:bomb:**注意：**

1. 网络连接需要在子线程中进行
2. 网络连接需要在Manifest中配置权限：

`<uses-permission android:name="android.permission.INTERNET"/>`

3. api27以后不能直接使用http协议
   1. 在Manifest的application里加上 `android:usesCleartextTraffic="true"`



## 如何处理请求数据(`json`)

**Android案例：`AndroidNetWork`**

因为请求的是`json`数据，这里使用`Gson`来解析。添加Gson的依赖

`implementation 'com.google.code.gson:gson:2.8.6'`

**`Gson`解析：**

`Gson`会将`json`个数解析成对应的对象。

1. 创建`json`数据对应的对象。

   这里使用`GsonFormat`插件。插件下载(`settings——>plugins`)

   下载好后，新建一个类。右键——Generate——`GsonFormat`。将`Json`数据复制进去，就可自动生成对应的类

2. 使用`Gson`解析

```java
Gson gson = new Gson();
GetTextItem getTextItem = gson.fromJson(json, GetTextItem.class);
//此处的GetTextItem就是Json对应的类，json是通过网络请求得到的Json字符串
```



**数据处理：**

这里将解析的数据存放在`RecylerView`里面。

1. 写好主布局，以及`RecylerView`对应的`itemView`。
2. 配置适配器Adapter

```java
package com.example.androidnetwork.adpters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidnetwork.R;
import com.example.androidnetwork.domain.GetTextItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class getResultListAdapter extends RecyclerView.Adapter<getResultListAdapter.innerHolder> {

    private List<GetTextItem.DataBean> mData = new ArrayList<>();

    /*
    * 创建ViewHolder
    * */
    @NonNull
    @Override
    public getResultListAdapter.innerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.get_text_item, parent, false);
        return new innerHolder(view);
    }

    /*
    * 绑定数据
    * */
    @Override
    public void onBindViewHolder(@NonNull getResultListAdapter.innerHolder holder, int position) {
        //获取当前item的位置
        GetTextItem.DataBean dataBean = mData.get(position);
        View itemView = holder.itemView;
        //标题
        TextView titleText = itemView.findViewById(R.id.title_item);
        titleText.setText(dataBean.getTitle());

        //封面（使用Glide加载图片）
        ImageView cover = itemView.findViewById(R.id.cover);
        Glide.with(itemView.getContext()).load("http://10.0.2.2:9102"+dataBean.getCover()).into(cover);

        //阅览数
        TextView readCount = itemView.findViewById(R.id.viewCount);
        readCount.setText(String.valueOf(dataBean.getViewCount()));

        //评论数
        TextView commentTime = itemView.findViewById(R.id.commentCount);
        commentTime.setText(String.valueOf(dataBean.getCommentCount()));

        //发布时间
        TextView publishTime = itemView.findViewById(R.id.publishTime);
        publishTime.setText(dataBean.getPublishTime());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(GetTextItem getTextItem) {
        mData.clear();
        mData.addAll(getTextItem.getData());
        //动态更新ListView
        notifyDataSetChanged();
    }

    public class innerHolder extends RecyclerView.ViewHolder {
        public innerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

```

3. 设置`ReclerView`

```java
 /*
  * 设置RecyclerView
  * @Param
  * */
private void initView() {
    RecyclerView recyclerView = this.findViewById(R.id.result_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    //添加分割线
    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.top = 5;
            outRect.bottom = 5;
        }
    });
    myAdapter = new getResultListAdapter();
    recyclerView.setAdapter(myAdapter);
}
```

:bomb:最后不要忘了添加权限以及运行前打开后台。​


## Post提交文本内容

**Android案例：`AndroidNetWork——PostTestActivity`**

> 参考网址：<https://www.sunofbeach.net/a/1203978883376992256>

使用`OutputStream`来提交，`outputStream`里的write传输内容，方法是POST

```java
package com.example.androidnetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.androidnetwork.domain.CommentItem;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class PostTestActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "PostTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_test);
        Button button = findViewById(R.id.postRequest);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        new Thread(new Runnable() {

            private InputStream inputStream = null;
            private OutputStream outputStream = null;

            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:9102/post/comment");
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
}
```

**Android案例：`AndroidNetWork——PostTestActivity`**

> 参考网址：<https://www.sunofbeach.net/a/1204255007411654656>
>
> Mime类型列表(上传文件时所需要填的fileType)：<https://www.sunofbeach.net/a/1202497837313675264>
>
> Android6.0以上以后获取权限步骤：<https://www.sunofbeach.net/a/1192351879502237696>
>
> Android SDK 29+ 读取存储权限问题：<https://blog.csdn.net/imxiezy/article/details/104929402>

- **单文件上传**

```java
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

        File file = new File("/storage/emulated/0/Download/naruto.jpg");//模拟器上图片的路径

        String fileKey = "file";//相当于post上传时的param的key
        String fileName = file.getName();
        String fileType = "image/jpeg"; //对应文件后缀为jpg，具体是什么看上面参考网址中的Mime类型

        @Override
        public void run() {
            try {
                URL url = new URL(BASIC_URL+"/file/upload");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //请求的设置
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setRequestProperty("User-Agent","Android/" + Build.VERSION.SDK_INT);//后面接的是Android sdk的版本
                connection.setRequestProperty("Accept","*/*");
                connection.setRequestProperty("Cache-Control","no-cache");
                connection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY);  //后面接的是边界值
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
```

运行完了之后会在 `F:\AndroidTest\SOBAndroidMiniWeb-master\SOBAndroidMiniWeb-master\sobUpload`这个路径下看到之前上传的图片

:bomb:注意访问Storage需要用户权限：

1. Manifest里面加上用户权限：

   1. 读：`<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>`
   2. 写：`<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />`

2. 因为Android的版本太高，需要用户动态申请权限(详情看上面参考网址中的 `Android6.0以上以后获取权限`)

   ```java
   //在活动创建时候申请：oncreate()
   //动态请求权限
   int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
   
   if(permission != PackageManager.PERMISSION_GRANTED) {
       //请求权限
       requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
   }
   
   //覆写onRequestPermissionsResult方法，处理用户请求权限的操作(允许/不允许使用该权限)
   /*
    * 处理申请用户权限的操作
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
   ```


   3. 上面的操作仍旧没有用的话，就在Manifest中的application中加上：

   `android:requestLegacyExternalStorage="true"`
