package mediaplayer.qmc.com.mediaplayer.base;

import android.app.Application;
import android.content.Context;

import com.vondear.rxtools.RxTool;

/**
 * Created by Administrator on 2018/5/7.
 */

public class MyApplication extends Application {
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        RxTool.init(this);
        context = this;
    }
    public static Context getContext(){
        return context;
    }
}
