package com.jqk.pictureselectorlibrary.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jqk.pictureselectorlibrary.R;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {
    private Context context;
    private List<String> pictures;
    private int width;
    private int height;

    public ThumbnailAdapter(Context context, List<String> pictures, int width, int height) {
        this.context = context;
        this.pictures = pictures;
        this.width = width;
        this.height = height;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thumbnail, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        L.d("position = " + position);
        ViewGroup.LayoutParams  lp = holder.thumbnailView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        holder.thumbnailView.setLayoutParams(lp);

        Glide.with(context).load(pictures.get(position)).into(holder.thumbnailView);
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnailView;

        public ViewHolder(View itemView) {
            super(itemView);

            thumbnailView = itemView.findViewById(R.id.thumbnailView);
        }
    }
}
