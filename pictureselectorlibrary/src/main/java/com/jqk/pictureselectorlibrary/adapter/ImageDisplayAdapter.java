package com.jqk.pictureselectorlibrary.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.bean.Display;
import com.jqk.pictureselectorlibrary.util.AppConstant;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/1/3.
 */

public class ImageDisplayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<Display> pictureDatas;
    private int screenWidth;

    private AddClickListener addClickListener;

    public interface AddClickListener {
        void onAdd();

        void onDelete(int id, int position);
    }

    public void setOnAddClickListener(AddClickListener addClickListener) {
        this.addClickListener = addClickListener;
    }

    public ImageDisplayAdapter(Context context, ArrayList<Display> pictureDatas, int screenWidth) {
        this.context = context;
        this.pictureDatas = pictureDatas;
        this.screenWidth = screenWidth;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == AppConstant.VIEW_ADD) {
            View view = LayoutInflater.from(context).inflate(R.layout.display_add, parent, false);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = screenWidth / 5;
            layoutParams.height = screenWidth / 5;
            view.setLayoutParams(layoutParams);
            AddView addView = new AddView(view);
            return addView;
        } else if (viewType == AppConstant.VIEW_PICTURE) {
            View view = LayoutInflater.from(context).inflate(R.layout.display_picture, parent, false);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = screenWidth / 5;
            layoutParams.height = screenWidth / 5;
            view.setLayoutParams(layoutParams);
            PictureView pictureView = new PictureView(view);
            return pictureView;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AddView) {
            ((AddView) holder).picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addClickListener.onAdd();
                }
            });

        } else if (holder instanceof PictureView) {
            Glide.with(context)
                    .load(pictureDatas.get(position).getPicture().getUrl())
                    .centerCrop()
                    .placeholder(com.jqk.pictureselectorlibrary.R.drawable.icon_placeholder)
                    .into(((PictureView) holder).picture);

            ((PictureView) holder).delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addClickListener.onDelete(pictureDatas.get(position).getId(), position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return pictureDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return pictureDatas.get(position).getViewType();
    }

    class AddView extends RecyclerView.ViewHolder {

        private ImageView picture;

        public AddView(View itemView) {
            super(itemView);

            picture = (ImageView) itemView.findViewById(R.id.picture);
        }
    }

    class PictureView extends RecyclerView.ViewHolder {

        private ImageView picture, delete;

        public PictureView(View itemView) {
            super(itemView);

            picture = (ImageView) itemView.findViewById(R.id.picture);
            delete = (ImageView) itemView.findViewById(R.id.delete);
        }
    }
}
