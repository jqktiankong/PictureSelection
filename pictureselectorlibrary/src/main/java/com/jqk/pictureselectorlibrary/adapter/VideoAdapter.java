package com.jqk.pictureselectorlibrary.adapter;

import android.app.Activity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.bean.Video;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<Video> videos;
    private Activity context;
    private int screenWidth;

    private VideoClickListener videoClickListener;

    public interface VideoClickListener {
        void onVideoClick(Video video);
    }

    public void setOnPictureClickListener(VideoClickListener videoClickListener) {
        this.videoClickListener = videoClickListener;
    }


    public VideoAdapter(Activity context, List<Video> videos, int screenWidth) {
        this.context = context;
        this.videos = videos;
        this.screenWidth = screenWidth;
    }

    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        layoutParams.width = screenWidth / 3;
        layoutParams.height = screenWidth / 3;
        view.setLayoutParams(layoutParams);
        VideoAdapter.ViewHolder viewHolder = new VideoAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final VideoAdapter.ViewHolder holder, final int position) {
        Glide.with(context)
                .load(videos.get(position).getPath())
                .centerCrop()
                .placeholder(R.drawable.icon_placeholder)
                .into(holder.picture);

        holder.time.setText(videos.get(position).getTime());

        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoClickListener.onVideoClick(videos.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void onDestroy() {

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView picture;
        private TextView time;

        public ViewHolder(View itemView) {
            super(itemView);

            picture = itemView.findViewById(R.id.picture);
            time = itemView.findViewById(R.id.time);
        }
    }
}