package com.omorni.parser;

/**
 * Class for all possible constants for application
 */
public class AllOmorniApis {

      // http://202.164.42.226/staging/omorni/api/api_doc.txt
     //  http://34.208.208.104/api2/api_doc.txt
    //   public static String BASE_ADMIN_URL = "http://202.164.42.226/staging/omorni/api/";

    // live url
    public static String BASE_ADMIN_URL = "http://52.36.147.86/api/";

    // testing url
//    public static String BASE_ADMIN_URL = "http://34.208.208.104/api/";
    public static String LOGIN_URL = BASE_ADMIN_URL + "user_login.php";
    public static String REGISTER_URL = BASE_ADMIN_URL + "signup_user.php";
    public static String USER_VERIFY_URL = BASE_ADMIN_URL + "user_varify.php";
    public static String FORGOT_PASSWORD_URL = BASE_ADMIN_URL + "user_forget_password.php";
    public static String SIGNUP_PASSWORD = BASE_ADMIN_URL + "signup_password.php";
    public static String UPDATE_NEW_PASSWORD_URL = BASE_ADMIN_URL + "new_password.php";
    public static String SEND_MOBILE_NUMBER = BASE_ADMIN_URL + "send_otp_onSocial_Login.php";
    public static String RESEND_OTP_URL = BASE_ADMIN_URL + "user_resend_otp.php";
    public static String UPDATE_DEVICE_TOKEN = BASE_ADMIN_URL + "update_token.php";


    public static String LOGOUT_URL = BASE_ADMIN_URL + "user_logout.php";
    public static String UPDATE_PROFILE_URL = BASE_ADMIN_URL + "user_update_profile.php";
    public static String UPDATE_PASSWORD_URL = BASE_ADMIN_URL + "user_update_pass.php";

    public static String SOCIAL_LOGIN_URL = BASE_ADMIN_URL + "social_login.php";
    public static String NEAR_ME_URL = BASE_ADMIN_URL + "near-me.php";
    public static String SELLER_DETAIL = BASE_ADMIN_URL + "seller_detail.php";
    public static String DELETE_FAVOURITE_URL = BASE_ADMIN_URL + "delete_favourite.php";
    public static String ADD_REVIEW_URL = BASE_ADMIN_URL + "add_review.php";
    public static String USER_ON_DUTY_URL = BASE_ADMIN_URL + "user_on_duty.php";
    public static String PACKAGES_URL = BASE_ADMIN_URL + "packages.php";
    public static String UPDATE_CARD_INFO_URL = BASE_ADMIN_URL + "user_card_info_update.php";
    public static String DELETE_CARD_INFO_URL = BASE_ADMIN_URL + "delete_card_info.php";
    public static String DELETE_CARD = BASE_ADMIN_URL + "delete_card.php";
    public static String ADD_PACKAGE_URL = BASE_ADMIN_URL + "add_user_package.php";
    public static String UPDATE_PACKAGE_URL = BASE_ADMIN_URL + "update_user_package.php";
    public static String UPDATE_REQUEST_URL = BASE_ADMIN_URL + "update_request.php";
    public static String UPDATE_ORDER_URL = BASE_ADMIN_URL + "update_order_status.php";
    public static String BECOME_SELLER = BASE_ADMIN_URL + "became_a_seller.php";
    public static String BUYER_ORDER = BASE_ADMIN_URL + "buyer_order_listing.php";
    public static String FAV_SELLER_LISTING = BASE_ADMIN_URL + "buyer_favourite_listing.php";
    public static String DUTY_ON_OFF = BASE_ADMIN_URL + "user_on_duty.php";
    public static String ADD_BUYER = BASE_ADMIN_URL + "add_buyer_job.php";
    public static String BUYER_POSTED_JOBS = BASE_ADMIN_URL + "buyer_jobs_listing.php";
    public static String STRIPE_PAYMENT = BASE_ADMIN_URL + "stripe_payment.php";
    public static String SELLER_AVAILABLE_JOBS = BASE_ADMIN_URL + "seller_avilable_jobs.php";
    public static String SELLER_MY_QUOTES = BASE_ADMIN_URL + "seller_my_quote.php";
    public static String SELLER_PROVIDES_QUOTES = BASE_ADMIN_URL + "seller_provide_quotes.php";
    public static String GET_CHAT_LIST = BASE_ADMIN_URL + "my_chat.php";
    public static String GET_MESSAGES = BASE_ADMIN_URL + "get_message.php";
    public static String SEND_MESSAGES = BASE_ADMIN_URL + "send_sms.php";
    public static String ADD_REQUEST = BASE_ADMIN_URL + "add_request.php";
    public static String ACCEPT_REJECT_JOB = BASE_ADMIN_URL + "accept_seller_request.php";
    public static String PAYPAL_PAYMENT = BASE_ADMIN_URL + "paypal_payment.php";
    public static String CASHU_PAYMENT = BASE_ADMIN_URL + "cashu_paymenttest.php";
    public static String GET_UPDATED_LAT_LONG = BASE_ADMIN_URL + "get_user_loc.php";
    public static String SET_UPDATED_LAT_LONG = BASE_ADMIN_URL + "user_update_" + "loc.php";
    public static String ORDER_SUMMARY = BASE_ADMIN_URL + "buyer_order_summary.php";
    public static String NEAR_ME_SCHEDULE = BASE_ADMIN_URL + "near-me2.php";
    public static String CHECK_IN = BASE_ADMIN_URL + "check_in.php";
    public static String CHECK_OUT = BASE_ADMIN_URL + "check_out.php";
    public static String ADD_REVIEW = BASE_ADMIN_URL + "add_review.php";
    public static String TRACK_WORK = BASE_ADMIN_URL + "track_work.php";
    public static String ADD_FAV = BASE_ADMIN_URL + "add_favourite.php";
    public static String GET_NOTIFICATIONS = BASE_ADMIN_URL + "get_notiffication.php";
    public static String Add_SELLER_QUOTE = BASE_ADMIN_URL + "add_seller_quote.php";
    public static String DELETE_SELLER_QUOTE = BASE_ADMIN_URL + "delete_seller_quote.php";
    public static String SELLER_JOB_REQUESTS = BASE_ADMIN_URL + "seller_myjob_listing.php";
    public static String JOB_DETAILS_LISTING = BASE_ADMIN_URL + "buyer_jobDetail_listing.php";
    public static String DELETE_POST = BASE_ADMIN_URL + "delete_post.php";
    public static String GET_RATING = BASE_ADMIN_URL + "get_rating.php";
    public static String CHECK_SELLER_FREE = BASE_ADMIN_URL + "check_status.php";
    public static String SELLER_VERIFY = BASE_ADMIN_URL + "seller_verify.php";


    public static String FAV_UNFAV = BASE_ADMIN_URL + "like_unlike.php";
    public static String ADD_CARD = BASE_ADMIN_URL + "add_user_card.php";
    public static String GET_CARDS = BASE_ADMIN_URL + "get_user_cards.php";
    public static String MOBILE_VERIFY_UPDATE = BASE_ADMIN_URL + "mobile_varify.php";
    public static String Become_Seller1 = BASE_ADMIN_URL + "become_seller_step1.php";
    public static String Become_Seller2 = BASE_ADMIN_URL + "become_seller_step2.php";
    public static String Become_Seller3 = BASE_ADMIN_URL + "become_seller_step3.php";
    public static String Become_Seller4 = BASE_ADMIN_URL + "become_seller_step4.php";
    public static String GET_SELLER1_SCREEN = BASE_ADMIN_URL + "get_first_screen.php";
    public static String GET_SELLER2_SCREEN = BASE_ADMIN_URL + "get_second_screen.php";
    public static String GET_SELLER3_SCREEN = BASE_ADMIN_URL + "get_third_screen.php";
    public static String GET_SELLER4_SCREEN = BASE_ADMIN_URL + "get_forth_screen.php";
    public static String ACCEPT_NEW_SELLER = BASE_ADMIN_URL + "accept_new_seller.php";
    public static String REJECT_NEW_SELLER = BASE_ADMIN_URL + "reject_new_seller.php";
    public static String GET_RATING_USERS = BASE_ADMIN_URL + "seller_reviews.php";
    public static String ENROUTE_JOB = BASE_ADMIN_URL + "enroute_job.php";
    public static String ACCEPT_CHECKIN = BASE_ADMIN_URL + "approve_checkin.php";
    public static String ADD_REQUEST_POSTED_JOB = BASE_ADMIN_URL + "add_request_post.php";
    public static String ACCEPT_REMINDER = BASE_ADMIN_URL + "accept_nottification.php";
    public static String GET_SETTING_LEGAL = BASE_ADMIN_URL + "get_setting.php";
    public static String TERMS = BASE_ADMIN_URL + "terms.php";
    public static String TERMS_CONDITION = BASE_ADMIN_URL + "termsAndConditions.php";
    public static String EARNINGS_LEGAL = BASE_ADMIN_URL + "earnings.php";
    public static String GET_PACKAGE_DATA = BASE_ADMIN_URL + "getpackages.php";

    public static String GET_CALENDER_MEETINGS = BASE_ADMIN_URL + "buyer_calender.php";
    public static String ADD_EVENT_URl = BASE_ADMIN_URL + "create_event.php";
    public static String UPDATE_EVENT_URL = BASE_ADMIN_URL + "update_event.php";
    public static String DELETE_EVENT_URL = BASE_ADMIN_URL + "delete_event.php";

    public static String GET_BANKS_URL = BASE_ADMIN_URL + "get_banks.php";
    public static String SEND_ACCOUNT_OTP_URL = BASE_ADMIN_URL + "send_account_otp.php";
    public static String ADD_ACCOUNT_OTP_URL = BASE_ADMIN_URL + "add_account.php";
    public static String UPDATE_ACCOUNT = BASE_ADMIN_URL + "update_account.php";
    public static String GET_MY_BALANCE = BASE_ADMIN_URL + "get_myBalance.php";
    public static String GET_MY_ACCOUNT_DETAILS = BASE_ADMIN_URL + "get_user_account.php";
    public static String GET_ONLY_BALANCE = BASE_ADMIN_URL + "get_balance.php";
    public static String VIEW_REFUND_CANCEL_LIST = BASE_ADMIN_URL + "getrefundcancellist.php";
    public static String GET_WITHDRAW_HISTORY = BASE_ADMIN_URL + "getwithdrawalhistory.php";
    public static String WITHDRAWL_AMOUNT = BASE_ADMIN_URL + "withdrawal.php";
    public static String REFUND_CANCEL_REQUEST = BASE_ADMIN_URL + "refundcancel.php";
    public static String NOTIFICATION_DETAIL = BASE_ADMIN_URL + "notification.php";
    public static String NO_THANKS = BASE_ADMIN_URL + "no_thanks.php";
    public static String GET_WITHDRAWL_DETAIL = BASE_ADMIN_URL + "get_withdrawal_detail.php";
    public static String SEND_QUERY = BASE_ADMIN_URL + "send_query.php";
    public static String PENDING_PAYMENT_DETAIL = BASE_ADMIN_URL + "getPendingPaymentDetail.php";
    public static String PAY_PENDING_PAYPAL = BASE_ADMIN_URL + "paypal_payment_pending.php";
    public static String PAY_PENDING_STRIPE = BASE_ADMIN_URL + "stripe_payment_pending.php";
    public static String SELLER_CANCEL_CHECKIN = BASE_ADMIN_URL + "sellerCancel.php";
    public static String PAYFORT_PAYMENT = BASE_ADMIN_URL + "payfort_payment.php";
    public static String CONVERT_CURRENCY = BASE_ADMIN_URL + "convertCurrency.php";
    public static String PAYFORT_API = BASE_ADMIN_URL + "payfort_token.php";
    public static String PAYFORT_PAYMENT_PENDING = BASE_ADMIN_URL + "payfort_payment_pending.php";
    public static String CHECK_SIGNUP = BASE_ADMIN_URL + "check_signup_user.php";
    public static String UPDATE_LANG = BASE_ADMIN_URL + "user_update_lang.php";
    public static String PAY_FORT_SANDBOX = "https://sbpaymentservices.payfort.com/FortAPI/paymentApi";
    public static String PAY_FORT_PRODUCTION = "https://paymentservices.payfort.com/FortAPI/paymentApi";
}
