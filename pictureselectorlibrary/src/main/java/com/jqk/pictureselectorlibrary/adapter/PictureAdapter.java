package com.jqk.pictureselectorlibrary.adapter;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.bean.Picture;

import java.io.File;
import java.util.List;

/**
 * Created by  on 2017/9/6.
 */

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

    private List<Picture> pictures;
    private Context context;
    private int screenWidth;

    private PictureClickListener pictureClickListener;

    public interface PictureClickListener {
        void onPictureClick(File file);
    }

    public void setOnPictureClickListener(PictureClickListener pictureClickListener) {
        this.pictureClickListener = pictureClickListener;
    }


    public PictureAdapter(Context context, List<Picture> pictures, int screenWidth) {
        this.context = context;
        this.pictures = pictures;
        this.screenWidth = screenWidth;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_picture, parent, false);
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        layoutParams.width = screenWidth / 3;
        layoutParams.height = screenWidth / 3;
        view.setLayoutParams(layoutParams);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Glide.with(context)
                .load(pictures.get(position).getUrl())
                .centerCrop()
                .placeholder(R.drawable.icon_placeholder)
                .into(holder.picture);

        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureClickListener.onPictureClick(pictures.get(position).getFile());
            }
        });
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView picture;

        public ViewHolder(View itemView) {
            super(itemView);

            picture = (ImageView) itemView.findViewById(R.id.picture);
        }
    }
}
