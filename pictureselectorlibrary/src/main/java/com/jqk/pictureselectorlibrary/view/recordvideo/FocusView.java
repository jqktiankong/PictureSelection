package com.jqk.pictureselectorlibrary.view.recordvideo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class FocusView extends View {
    private Paint paint;
    private int radius = 100;
    private int x, y;
    private static boolean click = false;

    private CountDownTimer timer;

    public FocusView(Context context) {
        super(context);
        init();
    }

    public FocusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        timer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                click = false;
                invalidate();
            }
        };

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timer.cancel();
                        x = (int) event.getX();
                        y = (int) event.getY();

                        click = true;
                        invalidate();
                        timer.start();
                        break;
                }
                return false;
            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (click) {
            Rect rect = new Rect(x - radius, y - radius, x + radius, y + radius);
            canvas.drawRect(rect, paint);

            if (onFocusViewCallback != null) {
                onFocusViewCallback.onFocus(rect);
            }
        }

    }

    private OnFocusViewCallback onFocusViewCallback;

    public interface OnFocusViewCallback {
        void onFocus(Rect rect);
    }

    public void setOnFocusViewCallback(OnFocusViewCallback onFocusViewCallback) {
        this.onFocusViewCallback = onFocusViewCallback;
    }
}
