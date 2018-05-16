package mediaplayer.qmc.com.mediaplayer.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.jaeger.library.StatusBarUtil;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/5/7.
 */

public abstract class BaseActivity extends FragmentActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        ButterKnife.bind(this);
        setStatus();
        initData();
    }

    private void setStatus() {
        StatusBarUtil.setTranslucent(this,0);
    }

    protected abstract void initData();

    public abstract int getLayout();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
