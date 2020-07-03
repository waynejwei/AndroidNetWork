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
