package com.app.fresy.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.fresy.R;
import com.app.fresy.adapter.AdapterHome;
import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.model.Category;
import com.app.fresy.model.Product;
import com.app.fresy.model.Slider;
import com.app.fresy.room.AppDatabase;
import com.app.fresy.room.DAO;
import com.app.fresy.room.table.CartEntity;
import com.app.fresy.utils.Tools;

import java.util.List;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentDetails extends Fragment {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    public enum ActionType {
        BUY, PLUS, MINUS
    }

    private Call<List<Product>> callbackProducts = null;
    private Product product;
    private CartEntity cart;

    private View root;
    private TextView name, brief, price, stock, original_price;
    private WebView description;
    private TextView cart_count;
    private ImageView image;
    private AppCompatButton btn_buy;
    private View lyt_plus_minus;
    private ImageButton bt_cart_minus, bt_cart_plus;
    private RecyclerView recycler_view_related;
    private ProgressBar progress_bar_details;
    private NestedScrollView nested_content;
    private DAO db = null;
    private AdapterHome adapterHome;

    private Communicator communicator;

    public FragmentDetails() {
    }

    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

    public static FragmentDetails newInstance(Product product) {
        FragmentDetails fragment = new FragmentDetails();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_OBJECT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        product = (Product) getArguments().getSerializable(EXTRA_OBJECT);
        db = AppDatabase.getDb(getContext()).get();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_details, container, false);
        initComponent();
        displayData();
        requestRelatedProduct();

        return root;
    }

    private void initComponent() {
        recycler_view_related = root.findViewById(R.id.recycler_view_related);
        progress_bar_details = root.findViewById(R.id.progress_bar_details);
        nested_content = root.findViewById(R.id.nested_scroll);
        name = root.findViewById(R.id.name);
        stock = root.findViewById(R.id.stock);
        brief = root.findViewById(R.id.brief);
        description = root.findViewById(R.id.description);
        price = root.findViewById(R.id.price);
        original_price = root.findViewById(R.id.original_price);
        image = root.findViewById(R.id.image);
        btn_buy = root.findViewById(R.id.btn_buy);
        lyt_plus_minus = root.findViewById(R.id.lyt_plus_minus);
        bt_cart_minus = root.findViewById(R.id.bt_cart_minus);
        bt_cart_plus = root.findViewById(R.id.bt_cart_plus);
        cart_count = root.findViewById(R.id.item_amount);

        recycler_view_related.setLayoutManager(new GridLayoutManager(getActivity(), Tools.getGridSpanCount(getActivity())));
        recycler_view_related.setHasFixedSize(true);
        adapterHome = new AdapterHome(getContext(), recycler_view_related);
        recycler_view_related.setAdapter(adapterHome);
        recycler_view_related.setNestedScrollingEnabled(false);

        (root.findViewById(R.id.img_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        adapterHome.setOnItemClickListener(new AdapterHome.OnItemClickListener() {
            @Override
            public void onProductClick(View view, Product obj, CartEntity c, AdapterHome.ActionType actionType, int position) {
                if (actionType == AdapterHome.ActionType.NORMAL) return;
                if (actionType == AdapterHome.ActionType.BUY) {
                    onActionClick(ActionType.BUY, obj, c);
                } else if (actionType == AdapterHome.ActionType.PLUS) {
                    onActionClick(ActionType.PLUS, obj, c);
                } else if (actionType == AdapterHome.ActionType.MINUS) {
                    onActionClick(ActionType.MINUS, obj, c);
                }
                adapterHome.notifyItemChanged(position);
            }

            @Override
            public void onCategoryClick(View view, Category obj, int position) {

            }

            @Override
            public void onSliderClick(View view, Slider obj, int position) {

            }
        });

        nested_content.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY < oldScrollY) { // up
                    communicator.onScroll(false);
                }
                if (scrollY > oldScrollY) { // down
                    communicator.onScroll(true);
                }
            }
        });
    }

    private void displayData() {
        cart = db.getCart(product.id);
        name.setText(product.name);
        brief.setText(Tools.fromHtml(product.short_description));
        price.setText(Tools.getPrice(product.price));

        String html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
        html_data += product.description;
        description.getSettings().setJavaScriptEnabled(true);
        description.getSettings().setBuiltInZoomControls(true);
        description.setBackgroundColor(Color.TRANSPARENT);
        description.setWebChromeClient(new WebChromeClient());
        description.loadData(html_data, "text/html; charset=UTF-8", null);

        if (product.sale_price.equals("")) {
            original_price.setVisibility(View.GONE);
        } else {
            original_price.setVisibility(View.VISIBLE);
            original_price.setText(Tools.getPrice(product.regular_price));
            original_price.setPaintFlags(original_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        String stock_text = getString(R.string.available);
        if (product.stock_status.equals("instock")) {
            if (product.manage_stock) {
                stock_text = stock_text + " " + product.stock_quantity;
            }
        } else {
            stock_text = getString(R.string.out_of_stock);
        }
        stock.setText(stock_text);

        if (product.images != null && product.images.size() > 0) {
            Tools.displayImage(getContext(), image, product.images.get(0).src);
        }

        lyt_plus_minus.setVisibility(cart == null ? View.GONE : View.VISIBLE);
        btn_buy.setVisibility(cart == null ? View.VISIBLE : View.GONE);
        if (cart != null) {
            cart_count.setText(cart.getAmount() + "");
        }

        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActionClick(ActionType.BUY, product, cart);
            }
        });

        bt_cart_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActionClick(ActionType.MINUS, product, cart);
            }
        });

        bt_cart_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActionClick(ActionType.PLUS, product, cart);
            }
        });
    }

    private void onActionClick(ActionType actionType, Product p_, CartEntity c_) {
        if (actionType == ActionType.BUY) {
            if (Tools.checkOutOfStockSnackBar(getActivity(), p_, 1)) return;
            CartEntity c = CartEntity.entity(p_);
            c.setAmount(1);
            c.setSaved_date(System.currentTimeMillis());
            db.insertCart(c);
        } else if (actionType == ActionType.PLUS) {
            long new_amount = c_.getAmount() + 1;
            if (Tools.checkOutOfStockSnackBar(getActivity(), p_, new_amount)) return;
            c_.setAmount(new_amount);
            db.updateCart(c_);
        } else if (actionType == ActionType.MINUS) {
            if (c_.getAmount() == 1) {
                db.deleteCart(c_.getId());
            } else {
                c_.setAmount(c_.getAmount() - 1);
                db.updateCart(c_);
            }
        }
        displayData();
        if (communicator != null) communicator.onCartUpdate();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (communicator != null) communicator.fragmentDetached();
    }

    private void requestRelatedProduct() {
        showProgress(true);
        API api = RestAdapter.createAPI();
        callbackProducts = api.listRelatedProduct(getRelatedIds(product.related_ids));
        callbackProducts.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                List<Product> resp = response.body();
                displayRelatedProduct(resp);
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                if (!call.isCanceled()) showProgress(false);
            }
        });
    }

    private void displayRelatedProduct(List<Product> resp) {
        showProgress(false);
        if (resp == null) return;
        if (resp.size() > 0) {
            adapterHome.insertData(resp);
        }
    }

    private void showProgress(final boolean show) {
        recycler_view_related.setVisibility(show ? View.GONE : View.VISIBLE);
        progress_bar_details.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String getRelatedIds(List<Long> related_ids) {
        String result = "";
        if (related_ids != null && related_ids.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Long s : related_ids) {
                sb.append(s).append(",");
            }
            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return result;
    }


    public interface Communicator {
        void fragmentDetached();

        void onScroll(boolean down);

        void onCartUpdate();
    }

}