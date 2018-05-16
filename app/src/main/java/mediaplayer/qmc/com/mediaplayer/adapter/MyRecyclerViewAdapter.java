package mediaplayer.qmc.com.mediaplayer.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import mediaplayer.qmc.com.mediaplayer.R;
import mediaplayer.qmc.com.mediaplayer.base.MyApplication;
import mediaplayer.qmc.com.mediaplayer.bean.VideoAndMusic;
import mediaplayer.qmc.com.mediaplayer.utils.StringUtils;

/**
 * Created by Administrator on 2018/5/8.
 */

public class MyRecyclerViewAdapter extends BaseQuickAdapter<VideoAndMusic,BaseViewHolder> {

    private  int type;

    public MyRecyclerViewAdapter(int resId, int layoutResId, @Nullable List<VideoAndMusic> data) {
        super(layoutResId, data);
        this.type = resId;
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoAndMusic item) {
        if(type==1){
            helper.setImageResource(R.id.iv_icon,R.drawable.video_default_icon);
        }else if(type==2){
            helper.setImageResource(R.id.iv_icon,R.drawable.music_default_bg);
        }
        helper.setText(R.id.tv_video_title,item.getName());
        helper.setText(R.id.tv_video_duration,StringUtils.formatMediaTime(item.getTime()));
        helper.setText(R.id.tv_video_size, Formatter.formatFileSize(MyApplication.getContext(),item.getSize()));

    }
}
