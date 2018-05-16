package mediaplayer.qmc.com.mediaplayer.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import mediaplayer.qmc.com.mediaplayer.R;
import mediaplayer.qmc.com.mediaplayer.bean.Lrc;
import mediaplayer.qmc.com.mediaplayer.utils.LyricLoader;
import mediaplayer.qmc.com.mediaplayer.utils.LyricsParser;


/**
 * Created by Administrator on 2018/5/15.
 */

public class LrcView extends AppCompatTextView {

    private int viewWith;
    private int viewHeight;
    private Paint paint;
    private float big_text;
    private float normal_text;
    private float line_height;
    private ArrayList<Lrc> lrcs;
    private int currentPosition = 10;
    private int duration;
    private int currentTime;
    private float passPercent;

    public LrcView(Context context) {
        this(context,null);
    }

    public LrcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
        initInfo();
    }

    public LrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initInfo();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewWith = w;
        viewHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);

    }

    private void initInfo() {
        big_text = getResources().getDimension(R.dimen.big_text);
        normal_text = getResources().getDimension(R.dimen.normal_text);
        line_height = getResources().getDimension(R.dimen.line_height);
        paint = new Paint();
        paint.setAntiAlias(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //在重新绘制界面的时候让画布平滑滚动
        if(lrcs !=null && lrcs.size()>0){
            canvas.translate(0,-getSmoothScroll());
            drawAllLrc(canvas);
        }
    }

    private void drawAllLrc(Canvas canvas) {
        Rect bounds = new Rect();
        for (int i = 0;i<lrcs.size();i++){
            Lrc lrc = lrcs.get(i);
            if(currentPosition==i){

                paint.setColor(Color.GREEN);
                paint.setTextSize(big_text);
                //绘制文字所占的大小
                paint.getTextBounds(lrc.getContent(),0,lrc.getContent().length(),bounds);
                //获取文本的宽高
                int width = bounds.width();
                int height = bounds.height();


                //view宽高-文本的宽高 得到文本在view中会只的起始点坐标也就是文本左下角的坐标

                float x = viewWith/2-width/2;
                float y = viewHeight/2+height/2;
                //歌词跟着进度涂色
                paint.setShader(new LinearGradient(x, y, x + width, y,new int[]{Color.GREEN,Color.WHITE},new float[]{passPercent,passPercent+0.2f},Shader.TileMode.CLAMP));
                canvas.drawText(lrc.getContent(),x,y,paint);
            }else{
                paint.setColor(Color.WHITE);
                paint.setTextSize(normal_text);
                //绘制文字所占的大小
                paint.getTextBounds(lrc.getContent(),0,lrc.getContent().length(),bounds);
                //获取文本的宽高
                int width = bounds.width();
                int height = bounds.height();
                //view宽高-文本的宽高 得到文本在view中会只的起始点坐标也就是文本左下角的坐标
                float x = viewWith/2-width/2;
                float y = viewHeight/2+height/2+(i-currentPosition)*line_height;
                //不是当前演唱的歌词不涂色
                paint.setShader(null);
                canvas.drawText(lrc.getContent(),x,y,paint);
            }
        }
    }
    private void changeCurrentPosition(int time){
        for (int i = 0;i<lrcs.size();i++){
            if(i==lrcs.size()-1){
                currentPosition = i;
                break;
            }
            if(time>lrcs.get(i).getTime()&&time<lrcs.get(i+1).getTime()){
                currentPosition = i;
                break;
            }
        }
    }
    public void updateLrcView(int time,int duration){
        currentTime = time;
        this.duration = duration;
        changeCurrentPosition(time);
        invalidate();
    }
    private float getSmoothScroll(){
        int startTime = lrcs.get(currentPosition).getTime();
        //唱了多久
        int passTime = currentTime - startTime;
        if(currentPosition==lrcs.size()-1){
            passPercent = passTime/(float)(duration-startTime);
            return line_height*passPercent;
        }
        int endTime = lrcs.get(currentPosition + 1).getTime();
        int totalTime = endTime-startTime;
        //进度百分比
        passPercent = passTime /(float) (totalTime);
        return line_height*passPercent;

    }
    public void loadLrc(String title){
        File file = LyricLoader.loadLyricFile(title);
        lrcs = LyricsParser.parserFromFile(file);
    }
}
