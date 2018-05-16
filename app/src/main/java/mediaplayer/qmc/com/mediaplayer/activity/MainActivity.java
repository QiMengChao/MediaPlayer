package mediaplayer.qmc.com.mediaplayer.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import mediaplayer.qmc.com.mediaplayer.R;
import mediaplayer.qmc.com.mediaplayer.adapter.ViewPagerAdapter;
import mediaplayer.qmc.com.mediaplayer.base.BaseActivity;
import mediaplayer.qmc.com.mediaplayer.fragment.MusiceFragment;
import mediaplayer.qmc.com.mediaplayer.fragment.VedioFragment;

/**
 * Created by Administrator on 2018/5/7.
 */

public class MainActivity extends BaseActivity {
    @Bind(R.id.tab)
    TabLayout tab;
    @Bind(R.id.viewpager)
    ViewPager viewpager;
    private ArrayList<Fragment> list = new ArrayList<>();
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void initData() {
        tab.setupWithViewPager(viewpager);
        list.add(new VedioFragment());
        list.add(new MusiceFragment());
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), list);
        viewpager.setAdapter(viewPagerAdapter);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }

}
