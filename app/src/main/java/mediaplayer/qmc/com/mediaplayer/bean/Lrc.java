package mediaplayer.qmc.com.mediaplayer.bean;

import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2018/5/15.
 */

public class Lrc implements Comparable<Lrc>{
    private String content;
    private int time;

    public Lrc(String content, int time) {
        this.content = content;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public int getTime() {
        return time;
    }

    @Override
    public int compareTo(Lrc o) {
        //compareTo 返回值 如果是负数 说明当前的对象应该排在前面 如果返回正数应该排在后面
        return this.time-o.time;
    }
}
