package mediaplayer.qmc.com.mediaplayer.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import mediaplayer.qmc.com.mediaplayer.R;
import mediaplayer.qmc.com.mediaplayer.base.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void initData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_splash;
    }
}
