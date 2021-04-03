package com.app.fresy.data;

public class Constant {

    /*** -------------------- EDIT THIS WITH YOURS -------------------------------------------------*/

    // Edit WEB_URL with your url. Make sure you have backslash('/') in the end url
    public static String WOOCOMMERCE_URL = "http://fresy.dream-space.web.id/";

    public static final String CONSUMER_KEY = "ck_0000000000000000000000000000000000000000";
    public static final String CONSUMER_SECRET_KEY = "cs_0000000000000000000000000000000000000000";

    // Edit with your app portfolio
    public static String MORE_APP_URL = "http://portfolio.dream-space.web.id/";

    // Edit with your contact us url or your whatsapp number url
    public static String CONTACT_US_URL = "https://api.whatsapp.com/send?phone=628970865062";

    // Edit with your about us url
    public static String ABOUT_US_URL = "http://dream-space.web.id/";

    // Edit with your privacy policy url
    public static String PRIVACY_POLICY_URL = "http://dream-space.web.id/doc_privacy/privacy_policy.php?name=Fresy";

    /* [ IMPORTANT ] be careful when edit this security code */
    /* This string must be same with security code at Server, if its different android unable to submit data */
    public static final String SECURITY_CODE = "8V06LupAaMBLtQqyqTxmcN42nn27FlejvaoSM3zXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    /* left empty if your app publish to play store*/
    public static final String APP_MARKET_URL = "";

    /*** ------------------- DON'T EDIT THIS -------------------------------------------------------*/
    // this limit value used for give pagination (request and display) to decrease payload
    public static int PRODUCT_PER_REQUEST = 10;
    public static int ORDER_PER_REQUEST = 10;
    public static int NOTIFICATION_PAGE = 20;

    // retry load image notification
    public static int LOAD_IMAGE_NOTIF_RETRY = 3;

}
