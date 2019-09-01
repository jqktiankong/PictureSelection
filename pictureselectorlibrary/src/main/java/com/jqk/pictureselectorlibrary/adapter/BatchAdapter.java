package com.jqk.pictureselectorlibrary.adapter;

import android.app.Activity;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.bean.Picture;

import java.util.List;

/**
 * Created by Administrator on 2017/12/6 0006.
 */

public class BatchAdapter extends RecyclerView.Adapter<BatchAdapter.ViewHolder> {

    private List<Picture> pictures;
    private Activity context;
    private int screenWidth;
    private int checkedNumber = 0;

    private BatchAdapter.BatchClickListener batchClickListener;

    public interface BatchClickListener {
        void onBatchClick(Picture picture);

        void onShowClick(String imgPath, int position);
    }

    public void setOnBatchClickListener(BatchAdapter.BatchClickListener batchClickListener) {
        this.batchClickListener = batchClickListener;
    }


    public BatchAdapter(Activity context, List<Picture> pictures, int screenWidth) {
        this.context = context;
        this.pictures = pictures;
        this.screenWidth = screenWidth;
    }

    @Override
    public BatchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_batch, parent, false);
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        layoutParams.width = screenWidth / 3;
        layoutParams.height = screenWidth / 3;
        view.setLayoutParams(layoutParams);
        BatchAdapter.ViewHolder viewHolder = new BatchAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BatchAdapter.ViewHolder holder, final int position) {

        final Picture picture = pictures.get(position);

        Glide.with(context)
                .load(picture.getUrl())
                .centerCrop()
                .placeholder(R.drawable.icon_placeholder)
                .into(holder.picture);

        holder.picture.setTag(R.id.iv, picture.getUrl());

        if (picture.getCheck()) {
            holder.checkBox.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_checkbox_on));
            holder.contentView.setBackgroundColor(context.getResources().getColor(R.color.transblack));
        } else {
            holder.checkBox.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_checkbox));
            holder.contentView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (picture.getCheck()) {
                    picture.setCheck(false);
                    holder.checkBox.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_checkbox));
                    holder.contentView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                    batchClickListener.onBatchClick(picture);
                    checkedNumber--;
                } else {
                    if (checkedNumber >= 9) {
                        return;
                    }
                    picture.setCheck(true);
                    holder.checkBox.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_checkbox_on));
                    holder.contentView.setBackgroundColor(context.getResources().getColor(R.color.transblack));
                    batchClickListener.onBatchClick(picture);
                    checkedNumber++;
                }

            }
        });

        holder.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                batchClickListener.onShowClick(picture.getUrl(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView picture;
        private ImageView checkBox;
        private RelativeLayout contentView;

        public ViewHolder(View itemView) {
            super(itemView);

            picture = (ImageView) itemView.findViewById(R.id.picture);
            checkBox = (ImageView) itemView.findViewById(R.id.checkbox);
            contentView = (RelativeLayout) itemView.findViewById(R.id.contentView);
        }
    }
}

