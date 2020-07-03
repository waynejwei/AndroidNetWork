package com.example.androidnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.androidnetwork.adpters.getResultListAdapter;
import com.example.androidnetwork.domain.GetTextItem;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button loadJson;
    private static final String TAG = "MainActivity";
    private getResultListAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadJson = findViewById(R.id.loadJson);
        loadJson.setOnClickListener(this);
        //设置RecyclerView
        initView();
    }


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

    @Override
    public void onClick(View v) {
        requestWithHttpURLConnection();
    }

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
                    connection.setConnectTimeout(30000);//请求时长
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
                        String json = reader.readLine();
                        Log.d(TAG,"line = "+json);

                        //将json格式转化为一般格式
                        Gson gson = new Gson();
                        GetTextItem getTextItem = gson.fromJson(json, GetTextItem.class);
                        updateUI(getTextItem);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
    * 修改UI界面(在主线程中)
    * @Param getTextItem
    * */
    private void updateUI(final GetTextItem getTextItem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myAdapter.setData(getTextItem);
            }
        });
    }
}
