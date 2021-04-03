package com.app.fresy;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.fresy.adapter.AdapterOrderHistory;
import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.data.Constant;
import com.app.fresy.data.ThisApp;
import com.app.fresy.fragment.FragmentOrderDetails;
import com.app.fresy.model.Order;
import com.app.fresy.model.User;
import com.app.fresy.utils.NetworkCheck;
import com.app.fresy.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityOrderHistory extends AppCompatActivity {

    // activity transition
    public static void navigate(Context context) {
        Intent i = new Intent(context, ActivityOrderHistory.class);
        context.startActivity(i);
    }

    private int page_no = -1;
    private boolean is_order_data_loaded = false;
    private SwipeRefreshLayout swipe_refresh;
    private RecyclerView recyclerView;
    private AdapterOrderHistory mAdapter;
    private ShimmerFrameLayout shimmer;
    private Call<List<Order>> callbackOrder = null;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        user = ThisApp.get().getUser();

        initToolbar();
        iniComponent();
        requestAction(1);
        Tools.RTLMode(getWindow());
    }

    private void initToolbar() {
        ActionBar actionBar;
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_activity_order_history);
        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
    }

    private void iniComponent() {
        shimmer = findViewById(R.id.shimmer_order);
        swipe_refresh = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AdapterOrderHistory(this, recyclerView);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterOrderHistory.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Order obj, AdapterOrderHistory.ActionType type, int position) {
                if (type == AdapterOrderHistory.ActionType.NORMAL) {
                    showDialogOrderDetails(obj);
                } else {

                }
            }
        });

        mAdapter.setOnLoadMoreListener(new AdapterOrderHistory.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (!is_order_data_loaded && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (callbackOrder != null && callbackOrder.isExecuted()) callbackOrder.cancel();
                is_order_data_loaded = false;
                mAdapter.resetListData();
                requestAction(1);
            }
        });
    }

    private void showDialogOrderDetails(Order order) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentOrderDetails newFragment = FragmentOrderDetails.newInstance(order);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "", R.drawable.logo_small);
        showNoItemView(false);
        this.page_no = page_no;
        if (page_no == 1) {
            showProgress(true);
        } else {
            mAdapter.setLoading();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestOrderHistory(page_no);
            }
        }, 500);
    }

    private void requestOrderHistory(final int page_no) {
        API api = RestAdapter.createAPI();
        callbackOrder = api.orders(user.id, page_no, Constant.PRODUCT_PER_REQUEST);
        callbackOrder.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                List<Order> resp = response.body();
                if (resp != null) {
                    if (resp.size() < Constant.ORDER_PER_REQUEST) is_order_data_loaded = true;
                    displayApiResult(resp);
                } else {
                    showProgress(false);
                    mAdapter.setLoaded();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }
        });
    }

    private void displayApiResult(final List<Order> items) {
        mAdapter.insertData(items);
        showProgress(false);
        if (items.size() == 0) showNoItemView(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgress(final boolean show) {
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(show);
            }
        });
        if (!show) {
            shimmer.setVisibility(View.GONE);
            shimmer.stopShimmer();
            recyclerView.setVisibility(View.VISIBLE);
            return;
        }
        recyclerView.setVisibility(View.INVISIBLE);
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
    }

    private void onFailRequest() {
        mAdapter.setLoaded();
        showProgress(false);
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text), R.drawable.img_failed);
        } else {
            showFailedView(true, getString(R.string.no_internet_text), R.drawable.img_no_internet);
        }
    }

    private void showFailedView(boolean show, String message, @DrawableRes int icon) {
        View lyt_failed = findViewById(R.id.lyt_failed);

        ((ImageView) findViewById(R.id.failed_icon)).setImageResource(icon);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.INVISIBLE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        (findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(page_no);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_failed);
        (findViewById(R.id.failed_retry)).setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.failed_icon)).setImageResource(R.drawable.img_no_item);
        ((TextView) findViewById(R.id.failed_message)).setText(getString(R.string.no_item));
        if (show) {
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            lyt_no_item.setVisibility(View.GONE);
        }
    }

}