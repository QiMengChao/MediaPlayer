package mediaplayer.qmc.com.mediaplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.vondear.rxtools.RxLogTool;
import com.vondear.rxtools.view.RxToast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mediaplayer.qmc.com.mediaplayer.R;
import mediaplayer.qmc.com.mediaplayer.base.BaseActivity;
import mediaplayer.qmc.com.mediaplayer.bean.VideoAndMusic;
import mediaplayer.qmc.com.mediaplayer.utils.StringUtils;

/**
 * Created by Administrator on 2018/5/8.
 */

public class VideoActivity extends BaseActivity {
    private static final int ISSHOW = 3;
    @Bind(R.id.vv_video)
    VideoView vvVideo;
    @Bind(R.id.tv_video_title)
    TextView tvVideoTitle;
    @Bind(R.id.tv_video_system_time)
    TextView tvVideoSystemTime;
    @Bind(R.id.iv_video_battery)
    ImageView ivVideoBattery;
    @Bind(R.id.iv_video_volume)
    ImageView ivVideoVolume;
    @Bind(R.id.tv_video_current_playedtime)
    TextView tvVideoCurrentPlayedtime;
    @Bind(R.id.sb_position)
    SeekBar sbPosition;
    @Bind(R.id.tv_video_duration)
    TextView tvVideoDuration;
    @Bind(R.id.back)
    ImageButton back;
    @Bind(R.id.ib_pre)
    ImageButton ibPre;
    @Bind(R.id.ib_playpause)
    ImageButton ibPlaypause;
    @Bind(R.id.ib_next)
    ImageButton ibNext;
    @Bind(R.id.ib_screensize)
    ImageButton ibScreensize;
    @Bind(R.id.sb_volume)
    SeekBar sbVolume;
    @Bind(R.id.view_alpha)
    View viewAlpha;
    @Bind(R.id.rl_top)
    RelativeLayout rlTop;
    @Bind(R.id.ll_bottom)
    LinearLayout llBottom;
    @Bind(R.id.ll_loading)
    LinearLayout llLoading;
    private VideoAndMusic video;
    private MyBatteryReceiver myBatteryReceiver;
    private static final int TPYE = 1;
    private static final int BROADCAST = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TPYE:
                    updateTime();
                    break;
                case BROADCAST:
                    updateProgress();
                    break;
                case ISSHOW:
                    hide(topHeight, bottomHeight);
                    break;
            }
        }
    };
    private AudioManager audioManager;
    private int typeVolume;
    private float startY;
    private int width;
    private int currentVolume;
    private float startX;
    private float currentAlpha;
    private ArrayList<VideoAndMusic> list;
    private int position;
    private boolean isScreen = false;
    private int height;
    private int videoHeight;
    private int videoWidth;
    private GestureDetector onGestureListener;
    private boolean isShow = true;
    private int topHeight;
    private int bottomHeight;

    @Override
    protected void initData() {
        //获取bundle存储的对象
        list = (ArrayList<VideoAndMusic>) getIntent().getSerializableExtra("list");
        position = getIntent().getIntExtra("position", 0);
        video = list.get(position);
        tvVideoTitle.setText(video.getName());
        //设置播放路径
        //http://video.chinanews.com/flv/2016/1118/quancheng.mp4 网络视频
        vvVideo.setVideoPath(list.get(position).getUrl());
        //设置异步是否准备好播放的监听
        vvVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                llLoading.setVisibility(View.GONE);
                vvVideo.start();
                //播放视频中间网络不好,显示进度条加载中,缓冲好后隐藏进度条
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        switch (what){
                            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                                llLoading.setVisibility(View.VISIBLE);
                                break;
                            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                                llLoading.setVisibility(View.GONE);
                                break;
                        }
                        return false;
                    }
                });
                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        RxLogTool.i("缓冲进度:"+percent+" 当前进度"+mp.getDuration());
                        sbPosition.setSecondaryProgress(mp.getDuration()*percent/100);
                    }
                });
                mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        llLoading.setVisibility(View.GONE);
                        return true;
                    }
                });
                ibPlaypause.setImageResource(R.drawable.btn_pause_normal);
                tvVideoDuration.setText(StringUtils.formatMediaTime(video.getTime()));
                //获取时长,实时更新播放进度
                sbPosition.setMax((int) video.getTime());
                updateProgress();
                videoHeight = mp.getVideoHeight();
                videoWidth = mp.getVideoWidth();
            }
        });
        vvVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                handler.removeMessages(BROADCAST);
                tvVideoCurrentPlayedtime.setText(StringUtils.formatMediaTime(vvVideo.getDuration()));
                sbPosition.setProgress(sbPosition.getMax());
                ibPlaypause.setImageResource(R.drawable.btn_play_normal);

            }
        });
        //监听电量的变化
        myBatteryReceiver = new MyBatteryReceiver();
        registerReceiver(myBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //设置音量的变化
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        sbVolume.setMax(streamMaxVolume);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        sbVolume.setProgress(currentVolume);
        typeVolume = currentVolume;
        MySeekBarOnChangeListener mySeekBarOnChangeListener = new MySeekBarOnChangeListener();
        sbVolume.setOnSeekBarChangeListener(mySeekBarOnChangeListener);
        sbPosition.setOnSeekBarChangeListener(mySeekBarOnChangeListener);
        //获取屏幕的宽
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        preAndNext();
        //测量上面板的宽高,拿到宽高
        rlTop.measure(0, 0);
        topHeight = rlTop.getMeasuredHeight();
        llBottom.measure(0, 0);
        bottomHeight = llBottom.getMeasuredHeight();
        //单击显示隐藏上下面板
        onGestureListener = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            //点击事件
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return super.onSingleTapUp(e);
            }

            //双击事件
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setVideoViewSize();
                return super.onDoubleTap(e);
            }

            //单击事件
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                RxLogTool.i("单击事件onSingleTapConfirmed");
                //显示
                if (isShow) {
                    hide(topHeight, bottomHeight);
                    //移除handler
                    handler.removeMessages(ISSHOW);

                    //隐藏
                } else {
                    show();
                }
                return super.onSingleTapConfirmed(e);
            }
        });
        handler.sendEmptyMessageDelayed(ISSHOW, 3000);
    }

    private void hide(int translationY, int bottomHeight) {
        rlTop.setTranslationY(-translationY);
        llBottom.setTranslationY(bottomHeight);
        isShow = false;
    }


    private void show() {
        rlTop.setTranslationY(0);
        llBottom.setTranslationY(0);
        handler.sendEmptyMessageDelayed(ISSHOW, 3000);
        isShow = true;
    }

    private void preAndNext() {
        if (position == 0) {
            ibPre.setImageResource(R.drawable.btn_pre_gray);
            ibPre.setEnabled(false);
        } else {
            ibPre.setImageResource(R.drawable.btn_pre_normal);
            ibPre.setEnabled(true);
        }
        if (position == list.size() - 1) {
            ibNext.setImageResource(R.drawable.btn_next_gray);
            ibNext.setEnabled(false);
        } else {
            ibNext.setImageResource(R.drawable.btn_next_normal);
            ibNext.setEnabled(true);
        }
    }

    private class MySeekBarOnChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int id = seekBar.getId();
            switch (id) {
                //时间
                case R.id.sb_position:
                    //判断是否是用户拖动的进度条,如果不是表明是handler在发消息更新进度,避免卡顿
                    if (fromUser) {
                        vvVideo.seekTo(progress);
                    }
                    break;
                //音量
                case R.id.sb_volume:
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    currentVolume = progress;
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //当用户开始操作seekbar
            handler.removeMessages(ISSHOW);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //当用户停止操作
            handler.sendEmptyMessageDelayed(ISSHOW, 3000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //更新时间
        updateTime();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        onGestureListener.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                RxLogTool.i("按下的坐标:" + event.getX() + "  " + event.getY());
                startY = event.getY();
                startX = event.getX();
                currentAlpha = viewAlpha.getAlpha();
                break;
            case MotionEvent.ACTION_MOVE:
                RxLogTool.i("移动的坐标:" + event.getX() + "  " + event.getY());
                float moveX = event.getX();
                if (moveX > width / 2) {
                    int temp = (int) (currentVolume + (startY - event.getY()) / 50);
                    if (temp < 0) {
                        currentVolume = 0;
                    } else if (temp > sbVolume.getMax()) {
                        currentVolume = sbVolume.getMax();
                    } else {
                        currentVolume = temp;
                    }
                    sbVolume.setProgress(currentVolume);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                } else {
                    float alpha = (float) (currentAlpha + (startY - event.getY()) / 100 * 0.1);
                    if (alpha > 0.8f) {
                        currentAlpha = 0.8f;
                    } else if (alpha < 0.0f) {
                        currentAlpha = 0.0f;
                    } else {
                        currentAlpha = alpha;
                    }
                    viewAlpha.setAlpha(currentAlpha);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void updateTime() {
        tvVideoSystemTime.setText(StringUtils.getSystemTiem());
        handler.sendEmptyMessageDelayed(TPYE, 1000);
    }

    private void updateProgress() {
        tvVideoCurrentPlayedtime.setText(StringUtils.formatMediaTime(vvVideo.getCurrentPosition()));
        sbPosition.setProgress(vvVideo.getCurrentPosition());
        handler.sendEmptyMessageDelayed(BROADCAST, 1000);
    }

    //当界面不可见的收取消handler,不然由于handler是内部类持有当前Activity的引用,当界面销毁时activity不会销毁
    //点一次开一个handler,最终导致oom;
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_video;
    }


    @OnClick({R.id.iv_video_volume, R.id.back, R.id.ib_pre, R.id.ib_playpause, R.id.ib_next, R.id.ib_screensize})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //静音是否开启
            case R.id.iv_video_volume:
                int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (streamVolume != 0) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    sbVolume.setProgress(0);
                    typeVolume = streamVolume;
                    currentVolume = 0;
                } else {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, typeVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    sbVolume.setProgress(typeVolume);
                    currentVolume = typeVolume;
                }
                break;
            //返回
            case R.id.back:
                finish();
                break;
            //上一首
            case R.id.ib_pre:
                if (position == 0) {
                    RxToast.error("已经是第一首了");
                } else {
                    position = position - 1;
                    vvVideo.stopPlayback();
                    vvVideo.setVideoPath(list.get(position).getUrl());
                    preAndNext();
                }
                break;
            //暂停/播放
            case R.id.ib_playpause:
                if (vvVideo.isPlaying()) {
                    vvVideo.pause();
                    ibPlaypause.setImageResource(R.drawable.btn_play_normal);
                    handler.removeMessages(BROADCAST);
                } else {
                    vvVideo.start();
                    ibPlaypause.setImageResource(R.drawable.btn_pause_normal);
                    updateProgress();
                }
                break;
            //下一首
            case R.id.ib_next:
                if (position == list.size() - 1) {
                    RxToast.error("已经是最后一首了");
                } else {
                    position = position + 1;
                    vvVideo.stopPlayback();
                    vvVideo.setVideoPath(list.get(position).getUrl());
                    preAndNext();
                }
                break;
            //全屏
            case R.id.ib_screensize:
                setVideoViewSize();
                break;
        }
    }

    private void setVideoViewSize() {

        if (isScreen) {
            vvVideo.getLayoutParams().width = videoWidth * height / videoHeight;
            vvVideo.getLayoutParams().height = height;
            ibScreensize.setImageResource(R.drawable.btn_default_screen_normal);
        } else {
            vvVideo.getLayoutParams().width = width;
            vvVideo.getLayoutParams().height = height;
            ibScreensize.setImageResource(R.drawable.btn_full_screen_normal);
        }
        isScreen = !isScreen;
        //重新绘制宽高
        vvVideo.requestLayout();
    }

    private class MyBatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 100);
            if (level > 90) {
                ivVideoBattery.setImageResource(R.drawable.ic_battery_100);
            } else if (level > 80) {
                ivVideoBattery.setImageResource(R.drawable.ic_battery_80);
            } else if (level > 60) {
                ivVideoBattery.setImageResource(R.drawable.ic_battery_60);
            } else if (level > 40) {
                ivVideoBattery.setImageResource(R.drawable.ic_battery_40);
            } else if (level > 20) {
                ivVideoBattery.setImageResource(R.drawable.ic_battery_20);
            } else if (level > 10) {
                ivVideoBattery.setImageResource(R.drawable.ic_battery_10);
            } else {
                ivVideoBattery.setImageResource(R.drawable.ic_battery_0);
            }
        }
    }

    //要在super前调用控件,不然会调用父类的onDestory,Butterknife解绑,导致控件引用为空
    @Override
    protected void onDestroy() {
        vvVideo.stopPlayback();
        unregisterReceiver(myBatteryReceiver);
        super.onDestroy();
    }
}
