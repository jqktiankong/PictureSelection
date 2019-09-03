package com.jqk.pictureselectorlibrary.view.show;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.LongSparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.adapter.ThumbnailAdapter;
import com.jqk.pictureselectorlibrary.bean.Video;
import com.jqk.pictureselectorlibrary.customview.MaskView;
import com.jqk.pictureselectorlibrary.util.AppConstant;
import com.jqk.pictureselectorlibrary.util.FileUtils;
import com.jqk.pictureselectorlibrary.util.FormatUtils;
import com.jqk.pictureselectorlibrary.util.L;
import com.jqk.pictureselectorlibrary.util.ScreenUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ShowVideoActivity extends AppCompatActivity {
    private LinearLayout videoView;
    private MaskView maskView;
    private RecyclerView recyclerview;
    private TextView startTime, endTime;
    private ImageView back;
    private Button ok;

    private Video video;
    private String videoPath;

    private int videoViewWidth;

    private float time_length;

    private int scrollX;

    private float maskLeft, maskRight;

    private boolean isFirst = true;

    Disposable disposable;

    private ShowVideoModel showVideoModel;

    private SimpleExoPlayer simpleExoPlayer;
    private SurfaceView surfaceView;
    float maxHeight;
    private LinearLayout parentView;
    int thumbWidth;
    int thumbHeight;
    private boolean isFirstPorgress = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showvideo);
        video = (Video) getIntent().getParcelableExtra("video");
        videoPath = video.getPath();
        L.d("videoPath = " + videoPath);

        init();
        initView();

        showVideoModel = new ShowVideoModel();

//        getPictures();
        video2pic();

        recyclerview.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                L.d("newState = " + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 滑动停止
                    countdown();
                    videoStart();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isFirst) {
                    initTime();
                    isFirst = false;
                } else {
                    scrollX += dx;
                    setTime();
                }

            }
        });

        maskView.setOnScrollXListener(new MaskView.OnScrollXListener() {
            @Override
            public void onLeftScroll(float leftX) {
                maskLeft = leftX;
                setTime();
            }

            @Override
            public void onRightScroll(float rightX) {
                maskRight = rightX;
                setTime();
            }

            @Override
            public void onDown() {
                videoStop();
            }

            @Override
            public void onRightUp() {
                countdown();
                videoStart();
            }

            @Override
            public void onLeftUp() {
                countdown();
                videoStart();
            }

            @Override
            public void onStop() {
                videoStop();
                disposable.dispose();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoStop();
                L.d("处理开始");
                ok.setClickable(false);

                String cachePath = FileUtils.createVideoCache();
                String fileName = "";
                if (cachePath != null) {
                    final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                    fileName = "MP4_" + timeStamp + ".mp4";
                } else {
                    ok.setClickable(true);
                    Toast.makeText(ShowVideoActivity.this, "创建文件失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String trimPath = cachePath + fileName;

                showVideoModel.trimVideo(
                        videoPath,
                        trimPath,
                        getStartTime(),
                        getDuration(),
                        new ShowVideoModel.ShowVideoOnCallback() {
                            @Override
                            public void onFinish(String path) {
                                ok.setClickable(true);
                                L.d("处理完成 = " + path);
                            }

                            @Override
                            public void onFail(String fail) {
                                ok.setClickable(true);
                                L.d("处理失败 = " + fail);
                            }

                            @Override
                            public void onProgress(String progress) {
                                L.d("处理进度 = " + progress);
                            }
                        });
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            L.d(parentView.getHeight() + "");
            maxHeight = parentView.getHeight();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }

        showVideoModel.onDestroy();
        videoStop();
        simpleExoPlayer.release();

        FileUtils.clearImgCache();
    }

    public void init() {
        recyclerview = (RecyclerView) findViewById(R.id.recyclerView);
        maskView = (MaskView) findViewById(R.id.maskView);
        videoView = (LinearLayout) findViewById(R.id.videoView);
        startTime = (TextView) findViewById(R.id.startTime);
        endTime = (TextView) findViewById(R.id.endTime);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        back = (ImageView) findViewById(R.id.back);
        ok = (Button) findViewById(R.id.ok);
        parentView = findViewById(R.id.parentView);
    }

    public void initView() {
        ViewGroup.LayoutParams lp = videoView.getLayoutParams();
        videoViewWidth = ScreenUtils.getScreenWidth(this) - (int) (ScreenUtils.getDensity(this) * 20 * 2);
        lp.width = videoViewWidth;
        videoView.setLayoutParams(lp);

        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(videoPath);
        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度

        float h2w = Float.parseFloat(height) / Float.parseFloat(width);

        thumbWidth = videoViewWidth / 15;
        thumbHeight = (int) (videoViewWidth / 15 * h2w);

        ViewGroup.LayoutParams lp2 = maskView.getLayoutParams();
        lp2.height = (int) (thumbHeight * 1.5);
        maskView.setLayoutParams(lp2);

        L.d("thumbHeight = " + thumbHeight);
        L.d("maskView.height = " + (int) (thumbHeight * 1.5));
    }

    public void initPlayView() {
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        simpleExoPlayer.setVideoSurfaceView(surfaceView);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(videoPath));
        simpleExoPlayer.prepare(videoSource);

        simpleExoPlayer.setPlayWhenReady(true);

        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_IDLE: // 播放器停止时的状态以及播放失败时的状态

                        break;
                    case Player.STATE_BUFFERING: // 加载中

                        break;
                    case Player.STATE_READY: // 准备好
                        countdown();
                        break;
                    case Player.STATE_ENDED: // 播放完

                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }
        });

        simpleExoPlayer.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                L.d("width = " + width);
                L.d("height = " + height);

                int screenWidth = ScreenUtils.getScreenWidth(ShowVideoActivity.this);
                float h = (float) screenWidth / width * height;
                L.d("screenWidth = " + screenWidth);
                L.d("width = " + width);
                L.d("height = " + height);
                L.d("maxHeight = " + maxHeight);
                L.d("h = " + h);

                if (h > maxHeight) {

                    float w = maxHeight / h * screenWidth;

                    L.d("w = " + w);

                    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
                    lp.width = (int) w;
                    lp.height = (int) maxHeight;
                    surfaceView.setLayoutParams(lp);
                } else {
                    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
                    lp.width = screenWidth;
                    lp.height = (int) h;
                    surfaceView.setLayoutParams(lp);
                }
            }

            @Override
            public void onSurfaceSizeChanged(int width, int height) {
            }

            @Override
            public void onRenderedFirstFrame() {

            }
        });

    }

    public void initTime() {
        maskRight = maskView.getMaskRight();
        startTime.setText(FormatUtils.duration2Time((int) (maskView.getMaskLeft() * time_length)));
        endTime.setText(FormatUtils.duration2Time((int) (maskView.getMaskRight() * time_length)));
    }

    public void setTime() {
        startTime.setText(FormatUtils.duration2Time((int) ((scrollX + maskLeft) * time_length)));
        endTime.setText(FormatUtils.duration2Time((int) ((scrollX + maskRight) * time_length)));
    }

    public void videoStart() {
        L.d("跳到毫秒 = " + (int) ((scrollX + maskLeft) * time_length));
        simpleExoPlayer.seekTo((int) ((scrollX + maskLeft) * time_length));
        simpleExoPlayer.setPlayWhenReady(true);
    }

    public void videoStop() {
        if (disposable != null) {
            disposable.dispose();
        }

        simpleExoPlayer.setPlayWhenReady(false);
    }

    public int getStartTime() {
        int startTime = (int) ((scrollX + maskLeft) * time_length) / 1000;
        L.d("startTime = " + startTime);
        return (int) ((scrollX + maskLeft) * time_length) / 1000;
    }

    public int getDuration() {
        int duration = (int) ((scrollX + maskRight) * time_length) / 1000 - (int) ((scrollX + maskLeft) * time_length) / 1000;
        L.d("duration = " + (duration < 1 ? 1 : duration));
        return duration < 1 ? 1 : duration;
    }

    public void countdown() {
        int time = (int) ((maskView.getMaskRight() * time_length - maskView.getMaskLeft() * time_length) / 1000);
        final int num = time * 1000 / 100;

//        L.d("num = " + num);

        Observable.interval(0, 100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
//                        if (aLong >= num) {
//                            videwStop();
//                            disposable.dispose();
//                        }

//                        L.d("当前播放进度 = " + playView.getCurrentPosition());
                        maskView.updateProgress((int) simpleExoPlayer.getCurrentPosition() - (int) (scrollX * time_length));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void video2pic() {

        String cachePath = FileUtils.createImgCache();

        if (cachePath != null) {

        } else {
            Toast.makeText(this, "创建文件夹失败", Toast.LENGTH_SHORT).show();
            return;
        }

        maskView.setUnit(thumbWidth);
        L.d("thumbWidth = " + thumbWidth);
        time_length = 1000 / (float) thumbWidth;
        maskView.setTime_length(time_length);
        L.d("time_length = " + time_length);

        String path = videoPath;
        int num = video.getDuration() / 1000;
        List<String> names = new ArrayList<>();
        long time = new Date().getTime();
        for (int i = 1; i <= num; i++) {
            names.add(cachePath + time + "_" + i + ".jpg");
        }

        ThumbnailAdapter thumbnailAdapter = new ThumbnailAdapter(ShowVideoActivity.this, names, thumbWidth, thumbHeight);
        recyclerview.setAdapter(thumbnailAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ShowVideoActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerview.setLayoutManager(linearLayoutManager);

        showVideoModel.video2pic(path, cachePath + time + "_%d.jpg", thumbWidth, thumbHeight, new ShowVideoModel.ShowVideoOnCallback() {
            @Override
            public void onFail(String fail) {
                L.d("解析失败 = " + fail);
            }

            @Override
            public void onFinish(String path) {

            }

            @Override
            public void onProgress(String progress) {
                L.d("刷新 = " + progress);
               if (isFirstPorgress) {
                   isFirstPorgress = false;
                   initPlayView();
               }
                thumbnailAdapter.notifyDataSetChanged();
            }
        });
    }


    public void getPictures() {
        Observable.create(new ObservableOnSubscribe<LongSparseArray<Bitmap>>() {
            @Override
            public void subscribe(ObservableEmitter<LongSparseArray<Bitmap>> emitter) throws Exception {
                LongSparseArray<Bitmap> thumbnailList = new LongSparseArray<>();

                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(videoPath);

                // Retrieve media data
                long videoLengthInMs = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                L.d("videoLengthInMs = " + videoLengthInMs);

                // Set thumbnail properties (Thumbs are squares)
                final int thumbWidth = videoViewWidth / 15;
                final int thumbHeight = videoViewWidth / 15;

                maskView.setUnit(thumbWidth);

                L.d("thumbWidth = " + thumbWidth);

                time_length = 1000 / (float) thumbWidth;

                maskView.setTime_length(time_length);

                L.d("time_length = " + time_length);

                int numThumbs = (int) (videoLengthInMs / 1000);

                final long interval = 1000;

                for (int i = 0; i < numThumbs; i++) {
                    Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    // TODO: bitmap might be null here, hence throwing NullPointerException. You were right
                    try {
                        bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    thumbnailList.put(i, bitmap);
                }

                mediaMetadataRetriever.release();

                emitter.onNext(thumbnailList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LongSparseArray<Bitmap>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LongSparseArray<Bitmap> bitmaps) {
//                        ThumbnailAdapter thumbnailAdapter = new ThumbnailAdapter(ShowVideoActivity.this, bitmaps);
//                        recyclerview.setAdapter(thumbnailAdapter);
//                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ShowVideoActivity.this);
//                        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                        recyclerview.setLayoutManager(linearLayoutManager);

                        initPlayView();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
