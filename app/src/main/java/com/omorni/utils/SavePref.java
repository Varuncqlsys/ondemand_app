package com.omorni.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by cqlsys on 1/15/2016.
 */
public class SavePref {
    Context context;
    public static final String PREF_TOKEN = "OmorniApp";
    public static final String PREF_SELER_DATA = "OmorniAppSellerData";
    SharedPreferences preferences;
    SharedPreferences preferences_seller;
    SharedPreferences.Editor editor;
    SharedPreferences.Editor editor_seller;

    public SavePref() {

    }

    public SavePref(Context c) {
        context = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

    }

    /*public SavePref(Context c, boolean seller) {
        context = c;
        preferences_seller = context.getSharedPreferences(PREF_SELER_DATA, Context.MODE_PRIVATE);
        editor_seller = preferences_seller.edit();
    }*/

    public void setUserId(String user_id) {
        editor.putString("user_id", user_id);
        editor.commit();
    }

    public String getUserId() {
        String user_id = preferences.getString("user_id", "");
        return user_id;
    }
    public void setTimeStamp(String time_stamp) {
        editor.putString("time_stamp", time_stamp);
        editor.commit();
    }

    public String getTimeStamp() {
        String time_stamp = preferences.getString("time_stamp", "");
        return time_stamp;
    }

    public void setUserType(String user_type) {
        editor.putString("user_type", user_type);
        editor.commit();
    }

    public String getUserType() {
        String user_type = preferences.getString("user_type", "");
        return user_type;
    }

    public void setFirstname(String first_name) {
        editor.putString("first_name", first_name);
        editor.commit();
    }

    public String getFirstname() {
        String first_name = preferences.getString("first_name", "");
        return first_name;
    }

    public void setIsSocialLogin(String is_social) {
        editor.putString("is_social", is_social);
        editor.commit();
    }

    public String getIsSocialLogin() {
        String is_social = preferences.getString("is_social", "");
        return is_social;
    }


    public void setLastname(String last_name) {
        editor.putString("last_name", last_name);
        editor.commit();
    }

    public String getLastname() {
        String last_name = preferences.getString("last_name", "");
        return last_name;
    }

    public void setEmail(String email) {
        editor.putString("email", email);
        editor.commit();
    }

    public String getEmail() {
        String email = preferences.getString("email", "");
        return email;
    }

    public void setLat(String lat) {
        editor.putString("lat", lat);
        editor.commit();
    }

    public String getLat() {
        String lat = preferences.getString("lat", "");
        return lat;
    }


    public void setLong(String longi) {
        editor.putString("longi", longi);
        editor.commit();
    }

    public String getLong() {
        String longi = preferences.getString("longi", "");
        return longi;
    }


    public void setAuthToken(String authToken) {
        editor.putString("authToken", authToken);
        editor.commit();
    }

    public String getAUthToken() {
        String authToken = preferences.getString("authToken", "");
        return authToken;
    }

    public void setUserImage(String userImage) {
        editor.putString("userImage", userImage);
        editor.commit();
    }

    public String getUserImage() {
        String userImage = preferences.getString("userImage", "");
        return userImage;
    }

    public void setCoverImage(String coverPic) {
        editor.putString("cover_pic", coverPic);
        editor.commit();
    }

    public String getCoverImage() {
        String coverPic = preferences.getString("cover_pic", "");
        return coverPic;
    }

    public void setphone(String phone) {
        editor.putString("phone", phone);
        editor.commit();
    }

    public String getphone() {
        String phone = preferences.getString("phone", "");
        return phone;
    }

    public void setSellerToggle(String toogle_value) {
        editor.putString("toogle_value", toogle_value);
        editor.commit();
    }

    public String getSellerToggle() {
        String toogle_value = preferences.getString("toogle_value", "");
        return toogle_value;
    }

    public void setUserOnline(boolean user_online) {
        editor.putBoolean("user_online", user_online);
        editor.commit();
    }

    public boolean getUserOnline() {
        boolean user_online = preferences.getBoolean("user_online", false);
        return user_online;
    }

    public void setThreadId(String thread_id) {
        editor.putString("thread_id", thread_id);
        editor.commit();
    }

    public String getThreadId() {
        String thread_id = preferences.getString("thread_id", "");
        return thread_id;
    }

    public void setRequestIdForCheckout(String request_id_checkout) {
        editor.putString("request_id_checkout", request_id_checkout);
        editor.commit();
    }

    public String getRequestIdForCheckout() {
        String request_id_checkout = preferences.getString("request_id_checkout", "");
        return request_id_checkout;
    }

    public void setRequestId(String request_id) {
        editor.putString("request_id", request_id);
        editor.commit();
    }

    public String getRequestId() {
        String request_id = preferences.getString("request_id", "");
        return request_id;
    }

    public void setUserOnlineForAcceptJob(boolean user_online_accept_job) {
        editor.putBoolean("user_online_accept_job", user_online_accept_job);
        editor.commit();
    }

    public boolean getUserOnlineForAcceptJob() {
        boolean user_online_accept_job = preferences.getBoolean("user_online_accept_job", false);
        return user_online_accept_job;
    }


    public void setUserOnlineForPayment(boolean user_online_payment) {
        editor.putBoolean("user_online_payment", user_online_payment);
        editor.commit();
    }

    public boolean getUserOnlineForPayment() {
        boolean user_online_payment = preferences.getBoolean("user_online_payment", false);
        return user_online_payment;
    }

    public void setSellerOnCheckin(boolean user_online_checkin) {
        editor.putBoolean("user_online_checkin", user_online_checkin);
        editor.commit();
    }

    public boolean getSellerOnCheckin() {
        boolean user_online_checkin = preferences.getBoolean("user_online_checkin", false);
        return user_online_checkin;
    }


    public void setBuyerRequestedSellerQuote(boolean buyer_on_job_detail) {
        editor.putBoolean("buyer_on_job_detail", buyer_on_job_detail);
        editor.commit();
    }

    public boolean getBuyerRequestedSellerQuote() {
        boolean buyer_on_job_detail = preferences.getBoolean("buyer_on_job_detail", false);
        return buyer_on_job_detail;
    }

    public void setUserOnlineForJobStart(boolean user_online_job_start) {
        editor.putBoolean("user_online_job_start", user_online_job_start);
        editor.commit();
    }

    public boolean getUserOnlineForJobStart() {
        boolean user_online_job_start = preferences.getBoolean("user_online_job_start", false);
        return user_online_job_start;
    }

    public void setUserOnlineForTrackWork(boolean user_online_track_work) {
        editor.putBoolean("user_online_track_work", user_online_track_work);
        editor.commit();
    }

    public boolean getUserOnlineForTrackWork() {
        boolean user_online_track_work = preferences.getBoolean("user_online_track_work", false);
        return user_online_track_work;
    }

    public void setUserOnlineForJobProgress(boolean user_online_job_inprogress) {
        editor.putBoolean("user_online_job_inprogress", user_online_job_inprogress);
        editor.commit();
    }

    public boolean getUserOnlineForJobProgress() {
        boolean user_online_job_inprogress = preferences.getBoolean("user_online_job_inprogress", false);
        return user_online_job_inprogress;
    }


    public void setOnlyMobileNumber(String mobile_only) {
        editor.putString("mobile_only", mobile_only);
        editor.commit();
    }

    public String getOnlyMobileNumber() {
        String mobile_only = preferences.getString("mobile_only", "");
        return mobile_only;
    }

    public void setCodeOnly(String code_only) {
        editor.putString("code_only", code_only);
        editor.commit();
    }

    public String getCodeOnly() {
        String code_only = preferences.getString("code_only", "");
        return code_only;
    }
    public void setPassword(String password) {
        editor.putString("password", password);
        editor.commit();
    }

    public String getPassword() {
        String password = preferences.getString("password", "");
        return password;
    }

    public void setSellerId(String seller_id) {
        editor.putString("seller_id", seller_id);
        editor.commit();
    }

    public String getSellerId() {
        String seller_id = preferences.getString("seller_id", "");
        return seller_id;
    }

    public void setAppLanguage(String app_language) {
        editor.putString("app_language", app_language);
        editor.commit();
    }

    public String getAppLanguage() {
        String app_language = preferences.getString("app_language", "");
        return app_language;
    }

    public static void setString(Context mContext, String key, String value) {
        SharedPreferences sharedpreferences = mContext.getSharedPreferences(PREF_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context mContext, String key,String def_value) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREF_TOKEN, Context.MODE_PRIVATE);
        String stringvalue = preferences.getString(key, def_value);
        return stringvalue;
    }

    public void clearPreferencesSeller() {
        preferences_seller.edit().clear().commit();
    }

    public void clearPreferences() {
        preferences.edit().clear().commit();
    }
}
