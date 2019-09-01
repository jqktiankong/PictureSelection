package com.jqk.pictureselectorlibrary.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jqk.pictureselectorlibrary.R;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {
    private Context context;
    private LongSparseArray<Bitmap> bitmaps;

    public ThumbnailAdapter(Context context, LongSparseArray<Bitmap> bitmaps) {
        this.context = context;
        this.bitmaps = bitmaps;
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
//        ViewGroup.LayoutParams  lp = holder.thumbnailView.getLayoutParams();
//        lp.width = 100;
//        lp.height = 100;
//        holder.thumbnailView.setLayoutParams(lp);
        holder.thumbnailView.setImageBitmap(bitmaps.get(position));
    }

    @Override
    public int getItemCount() {
        return bitmaps.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnailView;

        public ViewHolder(View itemView) {
            super(itemView);

            thumbnailView = itemView.findViewById(R.id.thumbnailView);
        }
    }
}
