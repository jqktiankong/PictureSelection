package com.jqk.pictureselectorlibrary.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.bean.Folder;
import com.jqk.pictureselectorlibrary.util.AppConstant;

import java.util.List;

/**
 * Created by  on 2017/9/6.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private List<Folder> folderList;
    private Context context;
    private OnFolderSelectListener onFolderSelectListener;

    public void setOnFolderSelectListener(OnFolderSelectListener onFolderSelectListener) {
        this.onFolderSelectListener = onFolderSelectListener;
    }

    public interface OnFolderSelectListener {
        void onFolderSelect(int position);
    }

    public FolderAdapter(Context context, List<Folder> folderList) {
        this.context = context;
        this.folderList = folderList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (folderList.get(position).getType() == AppConstant.FOLDER_TYPE_PICTURE) {
            holder.videoStart.setVisibility(View.GONE);
            Glide
                    .with(context)
                    .load(folderList.get(position).getPictureList().get(0).getUrl())
                    .centerCrop()
                    .placeholder(R.drawable.icon_placeholder)
                    .into(holder.picture);

            holder.folderName.setText(folderList.get(position).getName());
            holder.number.setText(folderList.get(position).getPictureList().size() + "张");

            if (folderList.get(position).isCheck()) {
                holder.check.setVisibility(View.VISIBLE);
            } else {
                holder.check.setVisibility(View.INVISIBLE);
            }

            holder.folder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFolderSelectListener.onFolderSelect(position);
                }
            });
        } else if (folderList.get(position).getType() == AppConstant.FOLDER_TYPE_VIDEO) {
            holder.videoStart.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(folderList.get(position).getVideoList().get(0).getPath())
                    .centerCrop()
                    .placeholder(R.drawable.icon_placeholder)
                    .into(holder.picture);

            holder.folderName.setText(folderList.get(position).getName());
            holder.number.setText(folderList.get(position).getVideoList().size() + "张");

            if (folderList.get(position).isCheck()) {
                holder.check.setVisibility(View.VISIBLE);
            } else {
                holder.check.setVisibility(View.INVISIBLE);
            }

            holder.folder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFolderSelectListener.onFolderSelect(position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public void onDestroy() {

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout folder;
        private ImageView picture;
        private TextView folderName, number;
        private ImageView check;
        private ImageView videoStart;

        public ViewHolder(View itemView) {
            super(itemView);

            folder = itemView.findViewById(R.id.folder);
            picture = itemView.findViewById(R.id.picture);
            folderName = itemView.findViewById(R.id.folderName);
            number = itemView.findViewById(R.id.number);
            check = itemView.findViewById(R.id.check);
            videoStart = itemView.findViewById(R.id.videoStart);
        }
    }
}
