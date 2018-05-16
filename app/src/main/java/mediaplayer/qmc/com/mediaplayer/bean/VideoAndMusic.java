package mediaplayer.qmc.com.mediaplayer.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/5/8.
 */

public class VideoAndMusic implements Serializable{
    private String url;
    private String name;
    private long time;
    private long size;
    private String singerName;
    private String display_name;

    public VideoAndMusic(String url, String name, long time, long size, String singerName, String display_name) {
        this.url = url;
        this.name = name;
        this.time = time;
        this.size = size;
        this.singerName = singerName;
        this.display_name = display_name;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }

    public long getSize() {
        return size;
    }

    public String getSingerName() {
        return singerName;
    }

    public String getDisplay_name() {
        return display_name;
    }
}
