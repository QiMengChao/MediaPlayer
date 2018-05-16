package mediaplayer.qmc.com.mediaplayer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import mediaplayer.qmc.com.mediaplayer.R;
import mediaplayer.qmc.com.mediaplayer.activity.VideoActivity;
import mediaplayer.qmc.com.mediaplayer.adapter.MyRecyclerViewAdapter;
import mediaplayer.qmc.com.mediaplayer.base.BaseFragment;
import mediaplayer.qmc.com.mediaplayer.bean.VideoAndMusic;
import mediaplayer.qmc.com.mediaplayer.utils.ConsumingOperation;
import mediaplayer.qmc.com.mediaplayer.utils.ResultListener;

/**
 * Created by Administrator on 2018/5/7.
 */

public class VedioFragment extends BaseFragment {
    @Bind(R.id.rc)
    RecyclerView rc;
    @Bind(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;

    @Override
    protected void initData() {
        refreshLayout.autoRefresh();
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadData();
                refreshLayout.finishRefresh();
            }
        });
        loadData();
    }

    private void loadData() {
        ConsumingOperation.queryMusicAndVedio(1, getContext(), new ResultListener() {
            @Override
            public void getResult(final List<VideoAndMusic> list) {
                rc.setLayoutManager(new LinearLayoutManager(getContext()));
                myRecyclerViewAdapter = new MyRecyclerViewAdapter(1, R.layout.item, list);
                rc.setAdapter(myRecyclerViewAdapter);
                myRecyclerViewAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                        ArrayList<VideoAndMusic> data = (ArrayList<VideoAndMusic>) adapter.getData();
                        VideoAndMusic o = (VideoAndMusic) data.get(position);
                        Intent intent = new Intent(getActivity(), VideoActivity.class);
                        intent.putExtra("list",data);
                        intent.putExtra("position",position);
                        startActivity(intent);
                    }
                });
            }
        });
    }


    @Override
    protected View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_vedio, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
