package com.zero.http.recyclerView.loadmore;


import com.zero.http.R;

/**
 * Created by BlingBling on 2016/10/11.
 */

public final class SimpleLoadMoreView extends LoadMoreView {

    @Override
    public int getLayoutId() {
        return R.layout.quick_view_load_more_account;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.load_more_loading_view_account;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view_account;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.load_more_load_end_view_account;
    }
}
