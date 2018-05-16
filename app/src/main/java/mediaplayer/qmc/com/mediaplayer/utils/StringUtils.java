package mediaplayer.qmc.com.mediaplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fullcircle on 2016/12/6.
 */

public class StringUtils {
    public static int hour = 1000*60*60;
    public static int minute = 1000*60;
    public static int second = 1000;

    public static String formatMediaTime(long millsec){
        //"hh:mm:ss"
        //"mm:ss"
        int h = (int) (millsec/hour);
        int m = (int) (millsec%hour/minute);
        int sec = (int) (millsec%minute/second);

        if(h>0){
            //"hh:mm:ss" "1:36:2"
            return String.format("%02d:%02d:%02d",h,m,sec);
        }else{
            return String.format("%02d:%02d",m,sec);
        }
    }
    public static String getSystemTiem(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
       return simpleDateFormat.format(new Date());
    }
    public static String updatePrgress(long l){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
       return simpleDateFormat.format(new Date(l));
    }
}
