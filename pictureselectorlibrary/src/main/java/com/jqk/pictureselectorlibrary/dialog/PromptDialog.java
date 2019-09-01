package com.jqk.pictureselectorlibrary.dialog;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.jqk.pictureselectorlibrary.R;

/**
 * Created by Administrator on 2018/1/5 0005.
 */

public class PromptDialog extends DialogFragment {

    private LinearLayout shoot, album;

    private PromptClickListener promptClickListener;


    public void setPromptClickListener(PromptClickListener promptClickListener) {
        this.promptClickListener = promptClickListener;
    }

    public interface PromptClickListener {
        void onShoot();

        void onAlbum();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置对话框样式
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomDialogStyle);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.getDecorView().setPadding(0, 0, 0, 0);
        // 一定要设置Background，如果不设置，window属性设置无效
        window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        // 对话框在底部显示
        window.setGravity(Gravity.BOTTOM);
        // 点击空白处对话框消失
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_prompt, container, false);
        shoot = (LinearLayout) view.findViewById(R.id.shoot);
        album = (LinearLayout) view.findViewById(R.id.album);

        shoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptClickListener.onShoot();
                dismiss();
            }
        });

        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptClickListener.onAlbum();
                dismiss();
            }
        });

        return view;
    }
}
