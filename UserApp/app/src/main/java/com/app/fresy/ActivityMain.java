package com.app.fresy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.fresy.adapter.AdapterHome;
import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.data.Constant;
import com.app.fresy.data.SharedPref;
import com.app.fresy.data.ThisApp;
import com.app.fresy.fragment.FragmentDetails;
import com.app.fresy.model.Category;
import com.app.fresy.model.Product;
import com.app.fresy.model.Slider;
import com.app.fresy.model.User;
import com.app.fresy.room.AppDatabase;
import com.app.fresy.room.DAO;
import com.app.fresy.room.table.CartEntity;
import com.app.fresy.utils.NetworkCheck;
import com.app.fresy.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityMain extends AppCompatActivity {

    private Call<List<Product>> callbackProducts = null;
    private Call<List<Category>> callbackCategories = null;
    private Call<List<Slider>> callbackSliders = null;

    public static final int REQUEST_CHECKOUT = 5000;

    List<Category> categories;
    List<Slider> sliders;
    List<Product> products;

    private View parent_view;
    private View notif_badge = null;
    private SwipeRefreshLayout swipe_refresh;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private AppBarLayout appbar_layout;
    private View lyt_cart_sheet, lyt_main_content;
    private TextView tv_total_cart;
    private EditText et_search;
    private AppCompatButton counter_badge, btn_continue;
    private ImageView bt_clear;
    private DrawerLayout drawer;
    private RecyclerView recyclerView;
    private AdapterHome mAdapter;
    private ShimmerFrameLayout shimmer;
    private boolean is_product_load_all = false;
    private boolean is_request_product_finish = false;
    private boolean is_request_category_finish = false;
    private boolean is_request_slider_finish = false;
    private int page_no = -1;
    private boolean from_category = false;
    private boolean isSearchBarHide = false;
    private int notification_count = -1;
    private Category category = null;
    private DAO db = null;

    private boolean is_login = false;
    private User user = new User();
    private FragmentManager manager;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDb(this).get();
        manager = getSupportFragmentManager();
        sharedPref = new SharedPref(this);

        initToolbar();
        initComponent();
        initDrawerMenu();
        Tools.hideKeyboard(this);

        mAdapter.resetListData();
        // request action will be here
        requestAction(1, true);
        Tools.RTLMode(getWindow());

        // launch instruction when first launch
        if (sharedPref.isFirstLaunch()) {
            startActivity(new Intent(this, ActivityIntro.class));
            sharedPref.setFirstLaunch(false);
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this);
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);
        swipe_refresh = findViewById(R.id.swipe_refresh);
        shimmer = findViewById(R.id.shimmer_main);
        appbar_layout = findViewById(R.id.appbar_layout);
        lyt_cart_sheet = findViewById(R.id.lyt_cart_sheet);
        lyt_main_content = findViewById(R.id.lyt_main_content);
        tv_total_cart = findViewById(R.id.tv_total_cart);
        bt_clear = findViewById(R.id.bt_clear);
        et_search = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recycler_view_main);
        counter_badge = findViewById(R.id.counter_badge);
        btn_continue = findViewById(R.id.btn_continue);

        et_search.setFocusableInTouchMode(true);
        lyt_cart_sheet.setVisibility(View.GONE);
        swipe_refresh.setProgressViewOffset(false, 0, Tools.dpToPx(this, 75));
        recyclerView.setLayoutManager(new GridLayoutManager(this, Tools.getGridSpanCount(this)));
        recyclerView.setHasFixedSize(true);
        //set data and list adapter
        mAdapter = new AdapterHome(this, recyclerView);
        recyclerView.setAdapter(mAdapter);
        refreshCartSheet();

        mAdapter.setOnItemClickListener(new AdapterHome.OnItemClickListener() {
            @Override
            public void onProductClick(View view, Product obj, CartEntity cart, AdapterHome.ActionType actionType, int position) {
                if (actionType == AdapterHome.ActionType.NORMAL) {
                    showFragmentDetails(obj);
                    return;
                }
                if (actionType == AdapterHome.ActionType.BUY) {
                    if (Tools.checkOutOfStockSnackBar(ActivityMain.this, obj, 1)) return;
                    CartEntity c = CartEntity.entity(obj);
                    c.setAmount(1);
                    c.setSaved_date(System.currentTimeMillis());
                    db.insertCart(c);

                } else if (actionType == AdapterHome.ActionType.PLUS) {
                    long new_amount = cart.getAmount() + 1;
                    if (Tools.checkOutOfStockSnackBar(ActivityMain.this, obj, new_amount)) return;
                    cart.setAmount(new_amount);
                    db.updateCart(cart);

                } else if (actionType == AdapterHome.ActionType.MINUS) {
                    if (cart.getAmount() == 1) {
                        db.deleteCart(cart.getId());
                    } else {
                        cart.setAmount(cart.getAmount() - 1);
                        db.updateCart(cart);
                    }
                }
                mAdapter.notifyItemChanged(position);
                refreshCartSheet();
            }

            @Override
            public void onCategoryClick(View view, Category obj, int position) {
                category = obj;
                productFilterAction();
            }

            @Override
            public void onSliderClick(View view, Slider obj, int position) {

            }
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new AdapterHome.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (!is_product_load_all && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page, next_page == 1);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 5) { // down / show
                    animateSearchBar(true);
                } else if (dy < -5) { // up / hide
                    animateSearchBar(false);
                }
            }
        });

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (callbackProducts != null && callbackProducts.isExecuted())
                    callbackProducts.cancel();
                if (callbackCategories != null && callbackCategories.isExecuted())
                    callbackCategories.cancel();
                if (callbackSliders != null && callbackSliders.isExecuted())
                    callbackSliders.cancel();
                is_product_load_all = false;
                from_category = false;
                requestAction(1, true);
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    productFilterAction();
                    return true;
                }
                return false;
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                if (c.toString().trim().length() == 0) {
                    bt_clear.setVisibility(View.GONE);
                } else {
                    bt_clear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
                productFilterAction();
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCart.navigate(ActivityMain.this, REQUEST_CHECKOUT);
            }
        });
    }

    private void requestListProduct(final int page_no) {
        Long category_id = category != null ? category.id : null;
        String search = et_search.getText().toString();
        API api = RestAdapter.createAPI();
        callbackProducts = api.listProduct(page_no, Constant.PRODUCT_PER_REQUEST, category_id, search);
        callbackProducts.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                List<Product> resp = response.body();
                is_request_product_finish = true;
                if (resp == null) return;
                if (resp.size() == 0) {
                    showProgress(false);
                    mAdapter.setLoaded();
                } else {
                    if (resp.size() < Constant.PRODUCT_PER_REQUEST) is_product_load_all = true;
                    products = resp;
                    displayApiResult(resp);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }
        });
    }

    private void requestAllCategory() {
        API api = RestAdapter.createAPI();
        callbackCategories = api.allCategory();
        callbackCategories.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                is_request_category_finish = true;
                if (response.body() != null && response.body().size() > 0) {
                    categories = Tools.getSortedCategory(response.body());
                    categories.get(0).selected = true;
                    category = categories.get(0);
                    displayApiResult(null);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }
        });
    }

    private void requestAllSlider() {
        API api = RestAdapter.createAPI();
        callbackSliders = api.allSlider();
        callbackSliders.enqueue(new Callback<List<Slider>>() {
            @Override
            public void onResponse(Call<List<Slider>> call, Response<List<Slider>> response) {
                sliders = response.body();
                is_request_slider_finish = true;
                if (sliders != null && sliders.size() > 0) {
                    displayApiResult(null);
                }
            }

            @Override
            public void onFailure(Call<List<Slider>> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }
        });
    }

    private void productFilterAction() {
        finishFragmentDetails();
        if (callbackProducts != null && callbackProducts.isExecuted()) callbackProducts.cancel();
        is_product_load_all = false;
        from_category = true;
        mAdapter.clearListProduct();
        requestAction(1, false);
        Tools.hideKeyboard(this);
    }

    private void requestAction(final int page_no, boolean load_header) {
        showFailedView(false, "", R.drawable.logo_small);
        this.page_no = page_no;
        if (page_no == 1 && load_header) {
            mAdapter.resetListData();
            requestAllSlider();
            requestAllCategory();
            showProgress(true);
        } else {
            mAdapter.setLoading();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestListProduct(page_no);
            }
        }, 500);
    }

    private void displayApiResult(final List<Product> items) {
        if (page_no == 1 && !from_category && is_request_product_finish && is_request_category_finish && is_request_slider_finish) {
            mAdapter.resetListData();
            if (sliders != null && sliders.size() > 0) {
                mAdapter.setSlider(sliders);
            }
            if (categories != null && categories.size() > 0) {
                mAdapter.setCategory(categories);
            }
            mAdapter.insertData(products);
        } else if (items != null) {
            mAdapter.insertData(items);
        }
        showProgress(false);
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
            lyt_main_content.setVisibility(View.VISIBLE);
            return;
        }
        lyt_main_content.setVisibility(View.INVISIBLE);
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_notification);
        View actionView = MenuItemCompat.getActionView(menuItem);
        notif_badge = actionView.findViewById(R.id.notif_badge);

        setupBadge();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

    private void setupBadge() {
        if (notif_badge == null) return;
        notif_badge.setVisibility(notification_count == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private void onFailRequest(int page_no) {
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
                requestAction(page_no, category == null);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menu_id = item.getItemId();
        if (menu_id == android.R.id.home) {
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.END);
            }
        } else if (menu_id == R.id.action_notification) {
            ActivityNotification.navigate(this);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        et_search.clearFocus();
        mAdapter.notifyDataSetChanged();
        refreshCartSheet();

        is_login = ThisApp.get().isLogin();
        user = ThisApp.get().getUser();
        initDrawerMenu();

        int new_notif_count = db.getNotificationUnreadCount();
        if (new_notif_count != notification_count) {
            notification_count = new_notif_count;
            invalidateOptionsMenu();
        }
    }

    static boolean active = false;

    @Override
    public void onDestroy() {
        if (callbackProducts != null && !callbackProducts.isCanceled()) callbackProducts.cancel();
        if (callbackCategories != null && !callbackCategories.isCanceled())
            callbackCategories.cancel();
        if (callbackSliders != null && !callbackSliders.isCanceled()) callbackSliders.cancel();
        if (shimmer != null) shimmer.stopShimmer();
        active = false;
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        active = true;
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * appbar_layout.getHeight()) : 0;
        appbar_layout.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    private void refreshCartSheet() {
        List<CartEntity> cartEntities = db.getAllCart();
        if (cartEntities.size() == 0) {
            lyt_cart_sheet.setVisibility(View.GONE);
            return;
        }
        lyt_cart_sheet.setVisibility(View.VISIBLE);
        double price = 0d;
        long counter = 0;
        for (CartEntity c : cartEntities) {
            price = price + (Double.parseDouble(c.getPrice()) * c.getAmount());
            counter = counter + c.getAmount();
        }
        tv_total_cart.setText(Tools.getPrice(price));

        // animate badge
        ScaleAnimation fade_in = new ScaleAnimation(0.0f, 1f, 0.0f, 1f, 1, 0.5f, 1, 0.5f);
        fade_in.setDuration(200);
        fade_in.setFillAfter(true);
        counter_badge.startAnimation(fade_in);
        String counter_text = counter > 99 ? "99+" : counter + "";
        counter_badge.setText(counter_text);
    }

    private void initDrawerMenu() {
        NavigationView nav_view = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer_layout);
        View lyt_user = nav_view.findViewById(R.id.lyt_user);
        View tv_app_title = nav_view.findViewById(R.id.tv_app_title);
        TextView login_logout = nav_view.findViewById(R.id.tv_login_logout);
        TextView name = nav_view.findViewById(R.id.name);
        TextView email = nav_view.findViewById(R.id.email);
        View lyt_user_menu = nav_view.findViewById(R.id.lyt_user_menu);
        if (is_login) {
            login_logout.setText(getString(R.string.logout_title));
            name.setText(user.getName());
            email.setText(user.email);
            lyt_user.setVisibility(View.VISIBLE);
            tv_app_title.setVisibility(View.GONE);
            lyt_user_menu.setVisibility(View.VISIBLE);
        } else {
            login_logout.setText(getString(R.string.login_title));
            name.setText("");
            lyt_user.setVisibility(View.GONE);
            tv_app_title.setVisibility(View.VISIBLE);
            lyt_user_menu.setVisibility(View.GONE);
        }

        login_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLogout();
            }
        });

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //showInterstitial();
            }
        });
    }


    public void onDrawerMenuClick(View view) {
        int menu_id = view.getId();
        switch (menu_id) {
            case R.id.nav_menu_home:
                drawer.closeDrawers();
                break;
            case R.id.nav_menu_address:
                ActivityAddress.navigate(this);
                break;
            case R.id.nav_menu_profile:
                ActivityRegisterProfile.navigate(ActivityMain.this, user);
                break;
            case R.id.nav_menu_history:
                ActivityOrderHistory.navigate(this);
                break;
            case R.id.nav_menu_settings:
                ActivitySettings.navigate(this);
                break;
            case R.id.nav_menu_contact:
                Tools.directLinkToBrowser(this, Constant.CONTACT_US_URL);
                break;
            case R.id.nav_menu_about:
                Tools.openInAppBrowser(this, Constant.ABOUT_US_URL, false);
                break;
            case R.id.nav_menu_privacy:
                Tools.openInAppBrowser(this, Constant.PRIVACY_POLICY_URL, false);
                break;
            case R.id.nav_menu_intro:
                startActivity(new Intent(this, ActivityIntro.class));
                break;
            case R.id.nav_menu_notif:
                ActivityNotification.navigate(this);
                break;
        }
    }


    private void showDialogLogout() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setIcon(Tools.tintDrawable(this, R.drawable.ic_info_outline, R.color.colorPrimary));
        builder.setTitle(R.string.logout_confirmation_text);
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ThisApp.get().logout();
                onResume();
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    public void loginLogout() {
        if (is_login) {
            showDialogLogout();
        } else {
            ActivityLogin.navigate(this);
        }
    }

    public void showFragmentDetails(Product product) {
        final View frame_content_details = findViewById(R.id.frame_content_details);
        recyclerView.setVisibility(View.GONE);
        swipe_refresh.setEnabled(false);
        frame_content_details.setVisibility(View.VISIBLE);

        animateSearchBar(false);

        FragmentTransaction transaction = manager.beginTransaction();
        FragmentDetails fragmentDetails = FragmentDetails.newInstance(product);
        fragmentDetails.setCommunicator(new FragmentDetails.Communicator() {
            @Override
            public void fragmentDetached() {
                recyclerView.setVisibility(View.VISIBLE);
                frame_content_details.setVisibility(View.GONE);
                swipe_refresh.setEnabled(true);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(boolean down) {
                animateSearchBar(down);
            }

            @Override
            public void onCartUpdate() {
                refreshCartSheet();
            }
        });
        transaction.add(R.id.frame_content_details, fragmentDetails, product.id.toString());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void finishFragmentDetails() {
        if (manager != null && manager.popBackStackImmediate()) {
            recyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.frame_content_details).setVisibility(View.GONE);
            swipe_refresh.setEnabled(true);
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CHECKOUT && resultCode == RESULT_OK) {
                finishFragmentDetails();
            }
        } catch (Exception ex) {

        }
    }

}
