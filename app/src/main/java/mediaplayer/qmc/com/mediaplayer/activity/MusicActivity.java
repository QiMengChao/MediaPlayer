package mediaplayer.qmc.com.mediaplayer.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.vondear.rxtools.RxLogTool;
import com.vondear.rxtools.RxSPTool;
import com.vondear.rxtools.view.RxToast;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mediaplayer.qmc.com.mediaplayer.R;
import mediaplayer.qmc.com.mediaplayer.base.BaseActivity;
import mediaplayer.qmc.com.mediaplayer.base.MyApplication;
import mediaplayer.qmc.com.mediaplayer.bean.VideoAndMusic;
import mediaplayer.qmc.com.mediaplayer.serivce.MusicService;
import mediaplayer.qmc.com.mediaplayer.utils.Flags;
import mediaplayer.qmc.com.mediaplayer.utils.StringUtils;
import mediaplayer.qmc.com.mediaplayer.view.LrcView;

/**
 * Created by Administrator on 2018/5/10.
 */

public class MusicActivity extends BaseActivity {

    private static final int UPDAT_SEEKBAR = 1;
    private static final int UPDAT_LRC = 2;
    @Bind(R.id.ib_back)
    ImageButton ibBack;
    @Bind(R.id.tv_music_name)
    TextView tvMusicName;
    @Bind(R.id.iv_zhen)
    ImageView ivZhen;
    @Bind(R.id.tv_singer_name)
    TextView tvSingerName;
    @Bind(R.id.tv_total_size)
    TextView tvTotalSize;
    @Bind(R.id.tv_xiexian)
    TextView tvXiexian;
    @Bind(R.id.tv_current_size)
    TextView tvCurrentSize;
    @Bind(R.id.sb_progress)
    SeekBar sbProgress;
    @Bind(R.id.ib_mode)
    ImageView ibMode;
    @Bind(R.id.ib_pre)
    ImageView ibPre;
    @Bind(R.id.ib_play_pause)
    ImageView ibPlayPause;
    @Bind(R.id.ib_next)
    ImageView ibNext;
    @Bind(R.id.tv_lyric)
    LrcView tvLyric;

    private ArrayList<VideoAndMusic> list;
    private int position;
    private MusicService.MusicBinder musicBinder;
    private MyConn myConn;
    private MyBroadcastReceiver myBroadcastReceiver;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDAT_SEEKBAR:
                    updateTimeTotal();
                    break;
                case UPDAT_LRC:
                    updateLrcView();
            }
            super.handleMessage(msg);
        }
    };
    private AnimationDrawable animationDrawable;


    @Override
    protected void initData() {
//        list = (ArrayList<VideoAndMusic>) getIntent().getSerializableExtra("list");
//        position = getIntent().getIntExtra("position",0);
//        VideoAndMusic videoAndMusic = list.get(position);
//        tvMusicName.setText(videoAndMusic.getName());
//        tvSingerName.setText(videoAndMusic.getSingerName());
        //播放帧动画
        ivZhen.setImageResource(R.drawable.music_zhen);
        animationDrawable = (AnimationDrawable) ivZhen.getDrawable();
        //混合模式开启服务
        Intent intent = getIntent();
        intent.setClass(this, MusicService.class);
        startService(intent);
        myConn = new MyConn();
        bindService(intent, myConn,BIND_AUTO_CREATE);
        //setImageIsPlaying();
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    musicBinder.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //开启广播
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver,new IntentFilter("musicstart"));
        String string = RxSPTool.getString(this, Flags.PLAY_TYPE);
        switch (string){
            case "":
            case Flags.LIST_XUNHUAN:
                ibMode.setImageResource(R.drawable.liebiao_xunhuan);
                break;
            case Flags.SINGLE_XUNHUAN:
                ibMode.setImageResource(R.drawable.single_xunhuan);
                break;
            case Flags.RANDOM:
                ibMode.setImageResource(R.drawable.random_play);
                break;
        }
        String string1 = RxSPTool.getString(this, Flags.IS_PLAYING);
        if(!TextUtils.isEmpty(string1)){
            ibPlayPause.setImageResource(R.drawable.music_pause);
            animationDrawable.stop();
        }else{
            ibPlayPause.setImageResource(R.drawable.music_play);
            animationDrawable.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(musicBinder!=null){
            updateTimeTotal();
            loadlyrc();
            updateLrcView();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    private void setImageIsPlaying() {
        if(musicBinder.isPlaying()){
            ibPlayPause.setImageResource(R.drawable.music_play);
            handler.sendEmptyMessageDelayed(UPDAT_SEEKBAR,300);
            animationDrawable.start();
        }else{
            ibPlayPause.setImageResource(R.drawable.music_pause);
            handler.removeMessages(UPDAT_SEEKBAR);
            animationDrawable.stop();
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_music;
    }
    private class MyConn implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.MusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("stopUpdateUI")){
                handler.removeCallbacksAndMessages(null);
            }else{
                textAndImage();
                updateTimeTotal();
                loadlyrc();
                updateLrcView();
            }
        }
    }

    private void loadlyrc() {
        if(musicBinder.getItem().getSingerName().equals("逃跑计划")){
            tvLyric.loadLrc("yekongzhongzuiliangdexing");
        }else{
            tvLyric.loadLrc("zz");
        }

    }

    private void updateLrcView() {
        tvLyric.updateLrcView(musicBinder.getCurrent(),musicBinder.getDuration());
        RxLogTool.i("进度:"+musicBinder.getCurrent());
        handler.sendEmptyMessageDelayed(UPDAT_LRC,50);
    }

    private void updateTimeTotal() {
        int current = musicBinder.getCurrent();
        String s = StringUtils.formatMediaTime(current);
        tvCurrentSize.setText(s);
        sbProgress.setProgress(current);
        handler.sendEmptyMessageDelayed(UPDAT_SEEKBAR,300);
    }

    private void textAndImage() {
        if(musicBinder.isPlaying()){
            ibPlayPause.setImageResource(R.drawable.music_play);
        }else{
            ibPlayPause.setImageResource(R.drawable.music_pause);
        }
        VideoAndMusic item = musicBinder.getItem();
        tvMusicName.setText(item.getName());
        tvSingerName.setText(item.getSingerName());
        int duration = musicBinder.getDuration();
        String s = StringUtils.formatMediaTime(duration);
        tvTotalSize.setText(s);
        sbProgress.setMax(duration);
    }

    @Override
    protected void onDestroy() {
        boolean playing = musicBinder.isPlaying();
        if(playing){
            RxSPTool.putString(this, Flags.IS_PLAYING,"");
        }else{
            RxSPTool.putString(this, Flags.IS_PLAYING,"pause");
        }
        unbindService(myConn);
        musicBinder = null;
        unregisterReceiver(myBroadcastReceiver);
        super.onDestroy();
    }

    @OnClick({R.id.ib_back, R.id.ib_mode, R.id.ib_pre, R.id.ib_play_pause, R.id.ib_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.ib_mode:
                String string = RxSPTool.getString(this, Flags.PLAY_TYPE);
                switch (string){
                    case Flags.LIST_XUNHUAN:
                    case "":
                        ibMode.setImageResource(R.drawable.single_xunhuan);
                        RxSPTool.putString(this,Flags.PLAY_TYPE,Flags.SINGLE_XUNHUAN);
                        RxToast.info("单曲循环");
                        break;
                    case Flags.SINGLE_XUNHUAN:
                        ibMode.setImageResource(R.drawable.random_play);
                        RxSPTool.putString(this,Flags.PLAY_TYPE,Flags.RANDOM);
                        RxToast.info("随机播放");
                        break;
                    case Flags.RANDOM:
                        ibMode.setImageResource(R.drawable.liebiao_xunhuan);
                        RxSPTool.putString(this,Flags.PLAY_TYPE,Flags.LIST_XUNHUAN);
                        RxToast.info("列表循环");
                        break;
                }
                break;
            case R.id.ib_pre:
                musicBinder.pre();
                break;
            case R.id.ib_play_pause:
               musicBinder.playAndPause();
               setImageIsPlaying();
                break;
            case R.id.ib_next:
                musicBinder.next();
                break;
        }
    }
}
