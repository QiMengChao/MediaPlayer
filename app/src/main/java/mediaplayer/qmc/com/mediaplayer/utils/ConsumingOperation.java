package mediaplayer.qmc.com.mediaplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;

import com.vondear.rxtools.RxLogTool;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import mediaplayer.qmc.com.mediaplayer.bean.VideoAndMusic;


/**
 * Created by Administrator on 2018/5/8.
 */

public class ConsumingOperation {
    public static CompositeDisposable compositeDisposable;
    public static void queryMusicAndVedio(final int type, final Context context, final ResultListener resultListener){
        if(compositeDisposable==null){
            compositeDisposable = new CompositeDisposable();
        }
        Observable<List<VideoAndMusic>> listObservable = Observable.create(new ObservableOnSubscribe<List<VideoAndMusic>>() {
            @Override
            public void subscribe(ObservableEmitter<List<VideoAndMusic>> e) throws Exception {
                ArrayList<VideoAndMusic> list = new ArrayList<>();
                ContentResolver contentResolver = context.getContentResolver();
                if(type==2) {
                    list.clear();
                    Cursor query = contentResolver.query(Audio.Media.EXTERNAL_CONTENT_URI, new String[]{Audio.Media.DATA, Audio.Media.TITLE, Audio.Media.DURATION, Audio.Media.SIZE,Audio.Media.ARTIST,Audio.Media.DISPLAY_NAME}, null, null, null);
                    while (query.moveToNext()) {
                        String url = query.getString(0);
                        String name = query.getString(1);
                        long dur = query.getLong(2);
                        long size = query.getLong(3);
                        String singerName = query.getString(4);
                        String display_name = query.getString(5);
                        RxLogTool.i(url + " " + name + " " + dur + " " + size+" "+singerName);
                        RxLogTool.i(Thread.currentThread().getName());
                        VideoAndMusic videoAndMusic = new VideoAndMusic(url, name, dur, size,singerName,display_name);
                        list.add(videoAndMusic);
                    }
                    e.onNext(list);
                }else if(type==1){
                    list.clear();
                    Cursor query = contentResolver.query(Video.Media.EXTERNAL_CONTENT_URI, new String[]{Video.Media.DATA, Video.Media.TITLE, Video.Media.DURATION, Video.Media.SIZE}, null, null, null);
                    while (query.moveToNext()) {
                        String url = query.getString(0);
                        String name = query.getString(1);
                        long dur = query.getLong(2);
                        long size = query.getLong(3);
                        RxLogTool.i(url + " " + name + " " + dur + " " + size);
                        RxLogTool.i(Thread.currentThread().getName());
                        VideoAndMusic videoAndMusic = new VideoAndMusic(url, name, dur, size,"","");
                        list.add(videoAndMusic);
                    }
                    e.onNext(list);
                }
            }
        });

        DisposableObserver<List<VideoAndMusic>> disposableObserver = new DisposableObserver<List<VideoAndMusic>>() {
            @Override
            public void onNext(List<VideoAndMusic> value) {
                if(resultListener!=null){
                    resultListener.getResult(value);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        listObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);
        compositeDisposable.add(disposableObserver);
    }
    public static void clearDisposable(){
        if(compositeDisposable!=null){
            compositeDisposable.clear();
        }
    }
}
