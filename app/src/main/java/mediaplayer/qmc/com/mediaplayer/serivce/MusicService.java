package mediaplayer.qmc.com.mediaplayer.serivce;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.vondear.rxtools.RxSPTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import mediaplayer.qmc.com.mediaplayer.R;
import mediaplayer.qmc.com.mediaplayer.base.MyApplication;
import mediaplayer.qmc.com.mediaplayer.bean.VideoAndMusic;
import mediaplayer.qmc.com.mediaplayer.utils.Flags;

/**
 * Created by Administrator on 2018/5/10.
 */

public class MusicService extends Service {
    private ArrayList<VideoAndMusic> list;
    private int position = -1;
    private MediaPlayer mediaPlayer;
    private PendingIntent prePendingIntent;
    private PendingIntent nextPendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean fromNotification = intent.getBooleanExtra("fromNotification", false);
        if(fromNotification){
            String operation = intent.getStringExtra("operation");
            switch (operation){
                case "pre":
                    playMode("pre");
                    break;
                case "next":
                    playMode("next");
                    break;
            }
        }else{
            list = (ArrayList<VideoAndMusic>) intent.getSerializableExtra("list");
            int temp = intent.getIntExtra("position",0);
            if(position==temp){
                MyApplication.context.sendBroadcast(new Intent("musicstart"));
            }else{
                position = temp;
                startPlay();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void playMode(String mode) {
        String string = RxSPTool.getString(MyApplication.getContext(), Flags.PLAY_TYPE);
        switch (string){
            case Flags.LIST_XUNHUAN:
            case "":
            case Flags.SINGLE_XUNHUAN:
                if(mode.equals("pre")){
                    if(position==0){
                        position = list.size()-1;
                    }else{
                        position--;
                    }
                }else if(mode.equals("next")){
                    position = ++position%list.size();
                }
                startPlay();
                break;
            case Flags.RANDOM:
                position = new Random().nextInt(list.size());
                startPlay();
                break;
        }
    }

    private void startPlay() {
        if(mediaPlayer==null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
            mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
        }else{
            mediaPlayer.reset();
        }
        palyMusic();
    }

    private void palyMusic() {
        try {
            mediaPlayer.setDataSource(list.get(position).getUrl());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PendingIntent getPrePendingIntent() {
        Intent intent = new Intent(MyApplication.getContext(), MusicService.class);
        intent.putExtra("operation","pre");
        intent.putExtra("fromNotification",true);
        PendingIntent service = PendingIntent.getService(MyApplication.getContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return service;
    }

    public PendingIntent getNextPendingIntent() {
        Intent intent = new Intent(MyApplication.getContext(), MusicService.class);
        intent.putExtra("operation","next");
        intent.putExtra("fromNotification",true);
        PendingIntent service = PendingIntent.getService(MyApplication.getContext(),1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return service;
    }

    private  class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            MyApplication.context.sendBroadcast(new Intent("musicstart"));
            showNotification();
        }

        private void showNotification() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.getContext());
            builder.setTicker("当前正在播放"+list.get(position).getName());
            builder.setSmallIcon(R.drawable.icon);
            builder.setCustomContentView(getRemoteViews());
            builder.setOngoing(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0,builder.getNotification());
        }
    }

    @NonNull
    private RemoteViews getRemoteViews() {
        RemoteViews remoteViews = new RemoteViews(MyApplication.getContext().getPackageName(), R.layout.normal_notification);
        remoteViews.setTextViewText(R.id.tv_notification_title, list.get(position).getName());
        remoteViews.setTextViewText(R.id.tv_notification_artist, list.get(position).getSingerName());
        remoteViews.setOnClickPendingIntent(R.id.iv_notification_pre, getPrePendingIntent());
        remoteViews.setOnClickPendingIntent(R.id.iv_notification_next, getNextPendingIntent());
        return remoteViews;
    }

    private  class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            String string = RxSPTool.getString(MyApplication.getContext(), Flags.PLAY_TYPE);
            switch (string){
                case Flags.LIST_XUNHUAN:
                case "":
                    position++;
                    if(position>list.size()-1){
                        position = 0;
                    }
                    mp.reset();
                    palyMusic();
                    break;
                case Flags.SINGLE_XUNHUAN:
                    mp.reset();
                    palyMusic();
                    break;
                case Flags.RANDOM:
                    position = new Random().nextInt(list.size());
                    mp.reset();
                    palyMusic();
                    break;
            }
        }
    }

    public class MusicBinder extends Binder {
        public void playAndPause(){
            if(mediaPlayer.isPlaying()){
                MyApplication.getContext().sendBroadcast(new Intent("stopUpdateUI"));
                mediaPlayer.pause();
            }else{
                mediaPlayer.start();
            }
        }
        public boolean isPlaying(){
            return mediaPlayer.isPlaying();
        }
        public VideoAndMusic getItem(){
            return list.get(position);
        }
        public int getDuration(){
            return mediaPlayer.getDuration();
        }
        public int getCurrent(){
            return mediaPlayer.getCurrentPosition();
        }
        public void seekTo(int progress) {
            mediaPlayer.seekTo(progress);
        }
        public void pre() {
            MyApplication.getContext().sendBroadcast(new Intent("stopUpdateUI"));
            String string = RxSPTool.getString(MyApplication.getContext(), Flags.PLAY_TYPE);
            switch (string){
                case Flags.LIST_XUNHUAN:
                case "":
                case Flags.SINGLE_XUNHUAN:
                    if(position!=0){
                        position--;
                        mediaPlayer.reset();
                    }else{
                        position = list.size()-1;
                        mediaPlayer.reset();

                    }
                    palyMusic();
                break;
                case Flags.RANDOM:
                    position = new Random().nextInt(list.size());
                    mediaPlayer.reset();
                    palyMusic();
                    break;
            }
        }

        public void next() {
            String string = RxSPTool.getString(MyApplication.getContext(), Flags.PLAY_TYPE);
            MyApplication.getContext().sendBroadcast(new Intent("stopUpdateUI"));
            switch (string){
                case Flags.LIST_XUNHUAN:
                case "":
                case Flags.SINGLE_XUNHUAN:
                    if(position<list.size()-1){
                        position++;
                        mediaPlayer.reset();
                    }else{
                        position = 0;
                        mediaPlayer.reset();
                    }
                    palyMusic();
                    break;
                case Flags.RANDOM:
                    position = new Random().nextInt(list.size());
                    mediaPlayer.reset();
                    palyMusic();
                    break;
            }
        }
    }
}
