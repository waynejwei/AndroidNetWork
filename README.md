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
