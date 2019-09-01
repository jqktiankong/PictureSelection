package com.jqk.pictureselectorlibrary.view.show;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.jqk.pictureselectorlibrary.R;

/**
 * Created by Administrator on 2018/4/12 0012.
 */

public class ShowImgFragment extends Fragment {

    private PhotoView img;
    private String path;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_showimg, container, false);
        img = view.findViewById(R.id.img);

        path = getArguments().getString("path");

        Glide
                .with(this)
                .load(path)
                .into(img);

        return view;
    }
}