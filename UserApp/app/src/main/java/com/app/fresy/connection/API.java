package com.app.fresy.connection;

import com.app.fresy.connection.param.ParamLogin;
import com.app.fresy.connection.param.ParamOrder;
import com.app.fresy.connection.param.ParamRegisterUpdate;
import com.app.fresy.connection.response.RespDevice;
import com.app.fresy.connection.response.RespForgotPass;
import com.app.fresy.connection.response.RespLogin;
import com.app.fresy.connection.response.RespOrder;
import com.app.fresy.connection.response.RespRegister;
import com.app.fresy.connection.response.RespShipMethod;
import com.app.fresy.connection.response.RespShipZone;
import com.app.fresy.connection.response.RespState;
import com.app.fresy.data.Constant;
import com.app.fresy.model.Category;
import com.app.fresy.model.Coupon;
import com.app.fresy.model.DeviceInfo;
import com.app.fresy.model.Order;
import com.app.fresy.model.Payment;
import com.app.fresy.model.Product;
import com.app.fresy.model.Setting;
import com.app.fresy.model.Slider;
import com.app.fresy.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface API {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "User-Agent: Fresy";
    String SECURITY = "Security: " + Constant.SECURITY_CODE;

    @Headers({CACHE, AGENT, SECURITY})
    @POST("wp-json/woo-tools-app/user/login")
    Call<RespLogin> login(@Body ParamLogin paramLogin);

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/customers/{ID}")
    Call<User> user(@Path("ID") long id);

    @Headers({CACHE, AGENT, SECURITY})
    @GET("wp-json/woo-tools-app/user/forgot-password")
    Call<RespForgotPass> forgotPassword(@Query("usermail") String user_mail);

    @Headers({CACHE, AGENT})
    @POST("wp-json/wc/v3/customers")
    Call<RespRegister> register(@Body ParamRegisterUpdate paramRegister);

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/data/countries/{code}")
    Call<RespState> state(@Path("code") String code);

    @Headers({CACHE, AGENT})
    @PUT("wp-json/wc/v3/customers/{ID}")
    Call<RespRegister> updateUser(
            @Path("ID") long id,
            @Body ParamRegisterUpdate paramRegister
    );

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/products")
    Call<List<Product>> listProduct(
            @Query("page") Integer page,
            @Query("per_page") Integer per_page,
            @Query("category") Long category,
            @Query("search") String search
    );

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/products")
    Call<List<Product>> listRelatedProduct(@Query("include") String ids);

    @Headers({CACHE, AGENT, SECURITY})
    @GET("wp-json/woo-tools-app/version")
    Call<Setting> version(@Query("code") Integer code);

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/products/categories?page=1&per_page=100")
    Call<List<Category>> allCategory();

    @Headers({CACHE, AGENT, SECURITY})
    @GET("wp-json/woo-tools-app/post/sticky")
    Call<List<Slider>> allSlider();

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/payment_gateways")
    Call<List<Payment>> payment();

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/shipping/zones")
    Call<List<RespShipZone>> shippingZones();

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/shipping/zones/{ID}/methods")
    Call<List<RespShipMethod>> shippingZonesMethods(@Path("ID") long id);

    @Headers({CACHE, AGENT})
    @POST("wp-json/wc/v3/orders")
    Call<Order> order(
            @Body ParamOrder paramOrder
    );

    @Headers({CACHE, AGENT})
    @GET("wp-json/wc/v3/orders")
    Call<List<Order>> orders(
            @Query("customer") long id,
            @Query("page") Integer page,
            @Query("per_page") Integer per_page
    );

    @Headers({CACHE, AGENT, SECURITY})
    @POST("wp-json/woo-tools-app/fcm/register")
    Call<RespDevice> registerDevice(
            @Body DeviceInfo deviceInfo
    );


    @Headers({CACHE, AGENT, SECURITY})
    @GET("wp-json/woo-tools-app/coupon")
    Call<Coupon> coupon(@Query("code") String code);


}
