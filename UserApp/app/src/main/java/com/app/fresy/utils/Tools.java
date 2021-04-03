package com.app.fresy.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.fresy.ActivityMain;
import com.app.fresy.ActivityWebView;
import com.app.fresy.R;
import com.app.fresy.data.AppConfig;
import com.app.fresy.data.Constant;
import com.app.fresy.data.ThisApp;
import com.app.fresy.model.BillingShipping;
import com.app.fresy.model.Category;
import com.app.fresy.model.DeviceInfo;
import com.app.fresy.model.Product;
import com.app.fresy.model.Setting;
import com.app.fresy.model.State;
import com.app.fresy.model.type.NotifType;
import com.app.fresy.room.table.CartEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class Tools {

    public static void RTLMode(Window window) {
        if (AppConfig.RTL_LAYOUT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }
    }

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static void setSystemBarColor(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(color));
        }
    }

    public static void setSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = act.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void changeMenuIconColor(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) continue;
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void changeOverflowMenuIconColor(Toolbar toolbar, @ColorInt int color) {
        try {
            Drawable drawable = toolbar.getOverflowIcon();
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } catch (Exception e) {
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + "";
    }

    public static int getVersionCode(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static String getVersionName(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return ctx.getString(R.string.app_version) + " " + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return ctx.getString(R.string.version_unknown);
        }
    }

    public static String getVersionNamePlain(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return ctx.getString(R.string.version_unknown);
        }
    }

    public static DeviceInfo getDeviceInfo(Context context) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.device_name = Tools.getDeviceName();
        deviceInfo.os_version = Tools.getAndroidVersion();
        deviceInfo.device_id = Tools.getDeviceID(context);
        return deviceInfo;
    }

    public static String getDeviceID(Context context) {
        String deviceID = Build.SERIAL;
        if (deviceID == null || deviceID.trim().isEmpty() || deviceID.equalsIgnoreCase("unknown") || deviceID.equals("0")) {
            try {
                deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
            }
        }
        return deviceID;
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yy hh:mm");
        return newFormat.format(new Date(dateTime));
    }


    // format from woocommerce
    // https://woocommerce.github.io/woocommerce-rest-api-docs/#request-response-format
    public static String getFormattedDateSimple(String time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = formatter.parse(time);
            return getFormattedDateSimple(date.getTime());
        } catch (ParseException e) {
            return time;
        }
    }

    public static String getStatusOrder(Context ctx, String status) {
        if (status.equalsIgnoreCase("pending")) {
            return ctx.getString(R.string.status_pending);
        } else if (status.equalsIgnoreCase("on-hold")) {
            return ctx.getString(R.string.status_on_hold);
        } else if (status.equalsIgnoreCase("processing")) {
            return ctx.getString(R.string.status_processing);
        } else if (status.equalsIgnoreCase("completed")) {
            return ctx.getString(R.string.status_completed);
        } else {
            return status.toUpperCase();
        }
    }


    public static void displayImage(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageCircle(Context ctx, ImageView img, Bitmap bmp) {
        try {
            Glide.with(ctx.getApplicationContext()).load(bmp)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageCircle(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageCircle(Context ctx, ImageView img, String url, float thumb) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(thumb)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageThumb(Context ctx, ImageView img, String url, float thumb) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(thumb)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void clearImageCacheOnBackground(final Context ctx) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(ctx).clearDiskCache();
                }
            }).start();
        } catch (Exception e) {
        }
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private static String appendQuery(String uri, String appendQuery) {
        try {
            URI oldUri = new URI(uri);
            String newQuery = oldUri.getQuery();
            if (newQuery == null) {
                newQuery = appendQuery;
            } else {
                newQuery += "&" + appendQuery;
            }
            URI newUri = new URI(
                    oldUri.getScheme(),
                    oldUri.getAuthority(),
                    oldUri.getPath(), newQuery, oldUri.getFragment()
            );
            return newUri.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return uri;
        }
    }

    public static String getHostName(String url) {
        try {
            URI uri = new URI(url);
            String new_url = uri.getHost();
            if (!new_url.startsWith("www.")) new_url = "www." + new_url;
            return new_url;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return url;
        }
    }

    public static void directLinkToBrowser(Activity activity, String url) {
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(intent);
    }

    public static void closeApplication(Activity ctx) {
        Intent intent = new Intent(ctx, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        ctx.startActivity(intent);
    }

    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static void backToMainActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        if (html == null) {
            return new SpannableString("");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static void resetItemDecoration(RecyclerView recycler_view) {
        while (recycler_view.getItemDecorationCount() > 0) {
            recycler_view.removeItemDecorationAt(0);
        }
    }

    public static List<Category> getSortedCategory(List<Category> categories) {
        if (categories == null || categories.size() <= 0) return categories;
        Collections.sort(categories, new Comparator<Category>() {
            @Override
            public int compare(final Category c1, final Category c2) {
                return c1.menu_order.compareTo(c2.menu_order);
            }
        });
        return categories;
    }

    public static Spanned getPrice(String price) {
        return getPrice(Double.parseDouble(price));
    }

    public static Spanned getPrice(Double price) {
        Setting setting = ThisApp.get().getSetting();
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator(setting.decimal_separator.charAt(0));
        decimalFormatSymbols.setGroupingSeparator(setting.thousand_separator.charAt(0));
        StringBuilder decimal = new StringBuilder();
        for (int i = 0; i < setting.number_of_decimals; i++) {
            if (i == 0) decimal.append(".");
            decimal.append("0");
        }
        DecimalFormat decimalFormat = new DecimalFormat("###,###" + decimal, decimalFormatSymbols);
        String result = decimalFormat.format(price);
        if (setting.currency_position.equalsIgnoreCase("left")) {
            result = setting.currency_symbol + result;
        } else if (setting.currency_position.equalsIgnoreCase("right")) {
            result = result + setting.currency_symbol;
        } else if (setting.currency_position.equalsIgnoreCase("right_space")) {
            result = result + " " + setting.currency_symbol;
        } else {
            result = setting.currency_symbol + " " + result;
        }
        return fromHtml(result);
    }

    public static boolean checkOutOfStock(Product product, long amount) {
        if (!product.manage_stock) return false;
        return product.stock_quantity < amount || product.stock_status.equalsIgnoreCase("outofstock");
    }

    public static boolean checkOutOfStockSnackBar(Activity act, Product product, long amount) {
        boolean out_of_stock = checkOutOfStock(product, amount);
        if (out_of_stock) {
            snackBarError(act, act.findViewById(android.R.id.content), R.string.insufficient_stock);
        }
        return out_of_stock;
    }


    public static boolean checkOutOfStockSnackBar(Activity act, CartEntity cart, long amount) {
        return checkOutOfStockSnackBar(act, cart.original(), amount);
    }

    public static Drawable tintDrawable(Context ctx, @DrawableRes int drawable, @ColorRes int color) {
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(ctx, drawable);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, ctx.getResources().getColor(color));
        return wrappedDrawable;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void snackBarError(Activity act, View view, String msg) {
        final Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(act.getResources().getColor(R.color.colorError));
        try {
            snackbar.show();
        } catch (Exception e) {
        }
    }

    public static void snackBarError(Activity act, View view, @StringRes int msg) {
        snackBarError(act, view, act.getResources().getString(msg));
    }


    public static void snackBarSuccess(Activity act, View view, String msg) {
        final Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(act.getResources().getColor(R.color.colorSuccess));
        try {
            snackbar.show();
        } catch (Exception e) {
        }
    }

    public static void snackBarSuccess(Activity act, View view, @StringRes int msg) {
        snackBarSuccess(act, view, act.getResources().getString(msg));
    }

    public static int getGridSpanCount(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.item_product_width);
        return Math.round(screenWidth / cellWidth);
    }

    public static String getNotificationType(Context ctx, String type) {
        if (type.equalsIgnoreCase(NotifType.LINK.name())) {
            return ctx.getString(R.string.type_link);
        } else if (type.equalsIgnoreCase(NotifType.IMAGE.name())) {
            return ctx.getString(R.string.type_image);
        } else {
            return "";
        }
    }

    public static void openInAppBrowser(Activity activity, String url, boolean from_notif) {
        url = appendQuery(url, "t=" + System.currentTimeMillis());
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show();
            return;
        }
        ActivityWebView.navigate(activity, url, from_notif);
    }

    public static void rateAction(Activity activity) {
        if (Constant.APP_MARKET_URL.equals("")) {
            Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                activity.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
            }
        } else {
            if (!URLUtil.isValidUrl(Constant.APP_MARKET_URL)) {
                Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.APP_MARKET_URL));
            activity.startActivity(intent);
        }
    }

    public static String getAddress(Context context, BillingShipping address) {
        if (address.first_name.trim().equals("")) {
            return context.getString(R.string.not_setup_address);
        }

        State state = ThisApp.get().getStateByCode(address.state);
        if (state != null) address.state_name = state.name;

        StringBuilder sb = new StringBuilder();
        sb.append(address.first_name).append(" ");
        sb.append(address.last_name).append("\n");
        if (!address.company.equals("")) sb.append(address.company).append("\n");
        sb.append(address.address_1);
        if (!address.address_2.equals("")) sb.append(", ").append(address.address_2);
        sb.append("\n");
        sb.append(address.city).append(", ");
        sb.append(address.state).append("\n");
        sb.append(address.state_name).append(", ");
        sb.append(address.postcode);
        if (address.phone != null) sb.append("\n").append(address.phone).append("\n");
        if (address.email != null) sb.append(address.email);
        return sb.toString();
    }
}
