package com.omorni.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.omorni.R;
import com.omorni.activity.AvailableJobDetail;
import com.omorni.activity.ChattingActivity;
import com.omorni.activity.MainActivity;
import com.omorni.activity.OrderSummeryActivity;
import com.omorni.activity.OrderSummerySellerActivity;
import com.omorni.activity.PostedJobsDetailActivity;
import com.omorni.activity.RequestSummaryPushActivity;
import com.omorni.activity.SelectedPackageActivity;
import com.omorni.activity.SellerAceeptRejectJobActivity;
import com.omorni.activity.SellerCheckoutActivity;
import com.omorni.activity.ShowRating;
import com.omorni.activity.TermConditionActivity;
import com.omorni.activity.TrackBuyerLocationActivity;
import com.omorni.activity.TrackSellerActivity;
import com.omorni.activity.TrackWorkActivity;
import com.omorni.activity.ViewRefundActivity;
import com.omorni.activity.WithdrawHistoryActivity;
import com.omorni.model.AllPackageResponse;
import com.omorni.model.PayPalResponse;
import com.omorni.model.SellerAvailableJobsResponse;
import com.omorni.model.SellerData;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;
import com.stripe.model.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


/**
 * Created by user on 10/27/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    SavePref savePref;
    private static int i;
    String request_id = "", req_lat = "", req_long = "", check_in_time = "", checkout_time = "", req_location = "", request_waiting_time = "", first_name = "", last_name = "", mobile = "", thread_id = "", total_rating_user = "", avgrating = "", category = "", seller_id = "", message_type = "", thumb = "", price = "";
    String receiver = "", sender = "", created = "", msg_id = "", message = "", sender_name = "", sender_image = "",
            normal_charges = "", buyer_name = "", seller_name = "", seller_image = "", package_name = "", processing_fee = "", vat_tax = "", extra_hours = "", main_hours = "", additional_charges = "", language = "", job_description = "", start_date = "",
            post_title = "", distance = "", start_time = "", is_scheduled = "", order_id = "", request_type = "0", post_id = "", package_selected = "";

    ArrayList<AllPackageResponse> allPackagesArrayList;
    SellerData sellerData;
    String selected_pacakge = "";
    String message2 = "";
    String CHANNEL_ID = "";// The id of the channel.
    String CHANNEL_ONE_NAME = "Channel One";
    NotificationManager notificationManager;
    NotificationChannel notificationChannel;
    Notification notification;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        getManager();
        CHANNEL_ID = getApplicationContext().getPackageName();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ONE_NAME, notificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        }


        Map<String, String> data = remoteMessage.getData();
        savePref = new SavePref(getApplicationContext());
        Log.e(TAG, "Notification Message Body: " + remoteMessage.getData());
        String notification_code = remoteMessage.getData().get("notification_code");
        message2 = remoteMessage.getData().get("message");

        // if customer submit request and request received by employee
        if (notification_code.equals("1")) {
            String message = remoteMessage.getData().get("message");
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                request_id = bodyArray.getString("id");
                req_lat = bodyArray.getString("req_lat");
                req_long = bodyArray.getString("req_lng");
                first_name = bodyArray.getString("first_name");
                last_name = bodyArray.getString("last_name");
                mobile = bodyArray.getString("mobile");
                created = bodyArray.getString("created_date");
                total_rating_user = bodyArray.getString("total_rating_user");
                avgrating = bodyArray.getString("avgrating");
                req_location = bodyArray.getString("req_location");
                request_waiting_time = bodyArray.getString("request_waiting_time");
                is_scheduled = bodyArray.getString("is_scheduled");
                start_date = bodyArray.getString("start_date");
                start_time = bodyArray.getString("start_time");
                distance = bodyArray.getString("distance");
                package_selected = bodyArray.getString("package_name");
                request_type = bodyArray.getString("request_type");
                normal_charges = bodyArray.getString("normal_charges");
                price = bodyArray.getString("price");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            sendNotification(getApplicationContext(), message, notification_code);
        }// if message is send between buyer or seller in chat
        else if (notification_code.equals("2")) {
            String message1 = remoteMessage.getData().get("message");
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                request_id = bodyArray.getString("request_id");
                receiver = bodyArray.getString("receiver");
                sender = bodyArray.getString("sender");
                created = bodyArray.getString("created");
                msg_id = bodyArray.getString("msg_id");
                message = bodyArray.getString("message");
                thread_id = bodyArray.getString("thread_id");
                sender_name = bodyArray.getString("name");
                sender_image = bodyArray.getString("user_image");
                message_type = bodyArray.getString("message_type");
                thumb = bodyArray.getString("thumb");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            if (savePref.getUserOnline()) {
                if (savePref.getThreadId().equals(thread_id)) {
                    publishResultsMessage(data);
                } else {
                    if (message_type.equals("1"))
                        message = "Image";
                    else if (message_type.equals("2"))
                        message = "Video";

                    sendNotificationMessage(getApplicationContext(), message, notification_code);
                }
            } else {
                if (message_type.equals("1"))
                    message = "Image";
                else if (message_type.equals("2"))
                    message = "Video";
                sendNotificationMessage(getApplicationContext(), message, notification_code);
            }
        }// if buyer make payment and seller recive push
        else if (notification_code.equals("3")) {
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                PayPalResponse payPalResponse = new PayPalResponse();
                payPalResponse.setRequest_id(bodyArray.getString("id"));
                payPalResponse.setOrder_id(bodyArray.getString("order_id"));
                payPalResponse.setBuyer_id(bodyArray.getString("buyer_id"));
                payPalResponse.setSeller_id(bodyArray.getString("seller_id"));
                payPalResponse.setPackage_id(bodyArray.getString("package_id"));
                payPalResponse.setReq_la(bodyArray.getString("req_lat"));
                payPalResponse.setReq_lng(bodyArray.getString("req_lng"));
                payPalResponse.setReq_location(bodyArray.getString("req_location"));
                payPalResponse.setStatus(bodyArray.getString("status"));
                payPalResponse.setCreated_date(bodyArray.getString("created_date"));
                payPalResponse.setFirst_name(bodyArray.getString("first_name"));
                payPalResponse.setLast_name(bodyArray.getString("last_name"));
                payPalResponse.setMobile(bodyArray.getString("mobile"));
                payPalResponse.setSeller_latitude(bodyArray.getString("seller_latitude"));
                payPalResponse.setSeller_longitude(bodyArray.getString("seller_longitude"));
                payPalResponse.setBuyer_name(bodyArray.getString("buyer_name"));
                payPalResponse.setBuyer_mobile(bodyArray.getString("buyer_mobile"));
                payPalResponse.setNormal_charges(bodyArray.getString("normal_charges"));
                payPalResponse.setPackage_name(bodyArray.getString("package_name"));
                payPalResponse.setTotal_amounts(bodyArray.getString("total_amounts"));
                payPalResponse.setIs_scheduled(bodyArray.getString("is_scheduled"));
                payPalResponse.setStart_date(bodyArray.getString("start_date"));
                payPalResponse.setStart_time(bodyArray.getString("start_time"));

                payPalResponse.setMain_amount(bodyArray.getString("main_amount"));
                payPalResponse.setProcessing_fees(bodyArray.getString("processing_fees"));
                payPalResponse.setProcessing_fees_ammount(bodyArray.getString("processing_fees_ammount"));
                payPalResponse.setThread_id(bodyArray.getString("thread_id"));
                payPalResponse.setPayment_method(bodyArray.getString("payment_method"));
                payPalResponse.setCategory(bodyArray.getString("category"));
                payPalResponse.setSeller_address(bodyArray.getString("seller_address"));
                payPalResponse.setBuyer_address(bodyArray.getString("buyer_address"));

                payPalResponse.setCheckin_time(bodyArray.getString("checkin_time"));
                payPalResponse.setCheckout_time(bodyArray.getString("checkout_time"));
                payPalResponse.setDistance(bodyArray.getString("distance"));
                payPalResponse.setSeller_fee(bodyArray.getString("seller_fee"));
                payPalResponse.setSeller_fee_amount(bodyArray.getString("seller_fee_amount"));
                payPalResponse.setSeller_amount(bodyArray.getString("seller_amount"));
                payPalResponse.setPrice(bodyArray.getString("price"));
                payPalResponse.setRequest_type(bodyArray.getString("request_type"));
                payPalResponse.setExtra_hours("0");
                payPalResponse.setExtra_amount("0");

                if (savePref.getUserOnlineForAcceptJob()) {
                    publishResultsMakePayment(payPalResponse, true);
                } else {
                    sendPushBuyerMakePayment(getApplicationContext(), payPalResponse, false);
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }

        }
        // if seller accept the job
        else if (notification_code.equals("4")) {
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                request_id = bodyArray.getString("id");
                post_id = bodyArray.getString("post_id");
                normal_charges = bodyArray.getString("normal_charges");
                seller_name = bodyArray.getString("first_name") + " " + bodyArray.getString("last_name");
                seller_image = bodyArray.getString("seller_image");
                package_name = bodyArray.getString("package_name");
                total_rating_user = bodyArray.getString("total_rating_user");
                avgrating = bodyArray.getString("avgrating");
                category = bodyArray.getString("category");

                processing_fee = bodyArray.getString("processing_fee");
                vat_tax = bodyArray.getString("vat_tax");
                extra_hours = bodyArray.getString("extra_hours");
                main_hours = bodyArray.getString("main_hours");
                additional_charges = bodyArray.getString("additional_charges");
                JSONArray jsonArray = new JSONArray(bodyArray.getString("language"));
                String language1 = "";
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        language1 += jsonObject.getString("language") + ",";
                    }
                    language = language1.substring(0, language1.length() - 1);
                } else {
                    language = language1;
                }

                job_description = bodyArray.getString("job_description");
                start_date = bodyArray.getString("start_date");
                start_time = bodyArray.getString("start_time");
                is_scheduled = bodyArray.getString("is_scheduled");
                seller_id = bodyArray.getString("seller_id");
                request_type = bodyArray.getString("request_type");
                price = bodyArray.getString("price");

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            // it means buyer selected particular package
            if (request_type.equals("0")) {
                if (savePref.getUserOnlineForPayment() && savePref.getSellerId().equals(seller_id)) {
                    publishResultsAcceptedJob("4", "");
                } else {
                    sendPushSellerAcceptJob(getApplicationContext(), message, notification_code);
                }
            }// if job is posted by buyer
            else {
                if (savePref.getBuyerRequestedSellerQuote() && savePref.getRequestId().equals(post_id)) {
                    publishResultsAcceptedJob("4", "");
                } else {
                    sendPushSellerAcceptJob(getApplicationContext(), message, notification_code);
                }
            }
        }// if seller reject the job
        else if (notification_code.equals("5")) {

            try {
                Object json = new JSONTokener(remoteMessage.getData().get("body")).nextValue();
                if (savePref.getUserOnlineForPayment()) {
                    // if there is no mattched seller
                    if (json instanceof JSONArray && ((JSONArray) json).length() == 0) {
                        publishResultsAcceptedJob("6", remoteMessage.getData().get("message"));
                    }// if there is snext eller which mattched details of current seller
                    else {
                        JSONObject jsonObject = (JSONObject) json;
                        getSellerData(jsonObject);
                        publishResultsAcceptedJob("5", remoteMessage.getData().get("message"));
                    }
                } else {
                    if (json instanceof JSONArray && ((JSONArray) json).length() == 0) {
                        sendPushSellerRejectJob(getApplicationContext(), remoteMessage.getData().get("message"), "6");
                    } else {
                        JSONObject jsonObject = (JSONObject) json;
                        getSellerData(jsonObject);
                        sendPushSellerRejectJob(getApplicationContext(), remoteMessage.getData().get("message"), "5");
                    }
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }// if seller checkin and buyer recive push
        else if (notification_code.equals("6")) {

            notificationManager.cancelAll();

            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;

            try {
                bodyArray = new JSONObject(body);
                request_id = bodyArray.getString("id");
                seller_name = bodyArray.getString("first_name") + " " + bodyArray.getString("last_name");
                seller_id = bodyArray.getString("seller_id");
                created = bodyArray.getString("created_date");
                check_in_time = bodyArray.getString("checkin_time");
                start_date = bodyArray.getString("start_date");
                is_scheduled = bodyArray.getString("is_scheduled");
                order_id = bodyArray.getString("order_id");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            if (savePref.getUserOnlineForTrackWork() && savePref.getRequestId().equals(request_id)) {
                publishResultsNotAcceptedRejected(true);
                sellerCheckinRequest();
            } else {
                sellerCheckinRequest();
            }

        }// if seller checkout and buyer recive push
        else if (notification_code.equals("7")) {

            notificationManager.cancelAll();
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                request_id = bodyArray.getString("id");
                seller_name = bodyArray.getString("first_name") + " " + bodyArray.getString("last_name");
                seller_id = bodyArray.getString("seller_id");
                created = bodyArray.getString("created_date");
                check_in_time = bodyArray.getString("checkin_time");
                checkout_time = bodyArray.getString("checkout_time");
                start_date = bodyArray.getString("start_date");
                is_scheduled = bodyArray.getString("is_scheduled");
                order_id = bodyArray.getString("order_id");
                extra_hours = bodyArray.getString("extra_hours");
                normal_charges = bodyArray.getString("normal_charges");
                package_name = bodyArray.getString("package_name");
                additional_charges = bodyArray.getString("additional_charges");
                main_hours = bodyArray.getString("main_hours");
                request_type = bodyArray.getString("request_type");
                post_title = bodyArray.getString("post_title");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            if (savePref.getUserOnlineForJobProgress()) {
                Log.e("push", "reqid " + savePref.getRequestId() + "  " + request_id);
                if (savePref.getRequestIdForCheckout().equals(request_id)) {
                    publishResultsCheckout(data);
                    sendPushSellerCheckout(getApplicationContext());
                } else {
                    sendPushSellerCheckout(getApplicationContext());
                }
            } else {
                sendPushSellerCheckout(getApplicationContext());
            }

        }// if seller not accept or rejected job in request time and push recived by seller
        else if (notification_code.equals("11")) {

            if (savePref.getTimeStamp().equals(remoteMessage.getData().get("timeStamp"))) {
                savePref.setTimeStamp(remoteMessage.getData().get("timeStamp"));
            } else {
                savePref.setTimeStamp(remoteMessage.getData().get("timeStamp"));
                if (savePref.getUserOnlineForAcceptJob()) {
                    publishResultsJobNotAccepted(message2, false);
                    sendSilentPush(getApplicationContext(), message2);
                } else {
                    sendPushNotReceivePayment(getApplicationContext(), message2);
                }
            }


        }// if seller not accept or not reject job in request time and push recived by buyer
        else if (notification_code.equals("111")) {

            if (savePref.getTimeStamp().equals(remoteMessage.getData().get("timeStamp"))) {
                savePref.setTimeStamp(remoteMessage.getData().get("timeStamp"));
            } else {
                savePref.setTimeStamp(remoteMessage.getData().get("timeStamp"));
                String body = remoteMessage.getData().get("body");
                JSONObject bodyArray = null;
                try {
                    bodyArray = new JSONObject(body);
                    request_type = bodyArray.getString("request_type");

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                if (request_type.equals("0")) {
                    if (savePref.getUserOnlineForPayment()) {
                        publishJobNotAcceptedByseller("111", message2);
                        sendSilentPush(getApplicationContext(), message2);
                    } else {
                        sendPushNotReceivePayment(getApplicationContext(), message2);
                    }
                } else {
                    if (savePref.getBuyerRequestedSellerQuote()) {
                        publishJobNotAcceptedByseller("111", message2);
                        sendSilentPush(getApplicationContext(), message2);
                    } else {
                        sendPushNotReceivePayment(getApplicationContext(), message2);
                    }
                }
            }


        }
        // if  buyer not made payment in request time and push recived by seller
        else if (notification_code.equals("12")) {
            if (savePref.getTimeStamp().equals(remoteMessage.getData().get("timeStamp"))) {
                savePref.setTimeStamp(remoteMessage.getData().get("timeStamp"));
            } else {
                savePref.setTimeStamp(remoteMessage.getData().get("timeStamp"));
                if (savePref.getUserOnlineForAcceptJob()) {
                    publishResultsPaymentNotPaid(message2, false);
                    sendSilentPush(getApplicationContext(), message2);
                } else {
                    sendPushNotReceivePayment(getApplicationContext(), message2);
                }
            }
        }
        // if Buyer not made payment within limited time and push recived by buyer
        else if (notification_code.equals("121")) {
            if (savePref.getTimeStamp().equals(remoteMessage.getData().get("timeStamp"))) {
                savePref.setTimeStamp(remoteMessage.getData().get("timeStamp"));
            } else {
                savePref.setTimeStamp(remoteMessage.getData().get("timeStamp"));
                sendPushToBuyerPaymentTimeExceeded(getApplicationContext(), message2);
                sendAdminPush(getApplicationContext(), message2);
            }

        }
        // if buyer posted a job and it is available for seller in available jobs
        else if (notification_code.equals("13")) {
            final ArrayList<SellerAvailableJobsResponse> availableJobsResponseArrayList = new ArrayList<SellerAvailableJobsResponse>();
            SellerAvailableJobsResponse availableJobsResponse = new SellerAvailableJobsResponse();
            String body = remoteMessage.getData().get("body");
            JSONObject body_obj = null;
            try {
                body_obj = new JSONObject(body);

                availableJobsResponse.setJob_id(body_obj.getString("id"));
                availableJobsResponse.setBuyer_id(body_obj.getString("buyer_id"));
                availableJobsResponse.setRequest_title(body_obj.getString("request_title"));
                availableJobsResponse.setJob_type(body_obj.getString("job_type"));
                availableJobsResponse.setJob_description(body_obj.getString("job_description"));
                availableJobsResponse.setWork_date(body_obj.getString("work_date"));
                availableJobsResponse.setWork_time(body_obj.getString("work_time"));
                availableJobsResponse.setPost_audio(body_obj.getString("post_audio"));
                availableJobsResponse.setPost_video(body_obj.getString("post_video"));
                availableJobsResponse.setLatitude(body_obj.getString("latitude"));
                availableJobsResponse.setLongitude(body_obj.getString("longitude"));
                availableJobsResponse.setCreated_date(body_obj.getString("created_date"));
                availableJobsResponse.setDistance(body_obj.getString("distance"));
                availableJobsResponse.setFirst_name(body_obj.getString("first_name"));
                availableJobsResponse.setLast_name(body_obj.getString("last_name"));
                availableJobsResponse.setBuyer_image(body_obj.getString("user_image"));
                availableJobsResponse.setOmorni_processing_fee(body_obj.getString("omorni_processing_fee"));
                availableJobsResponse.setTotal_rating_user(body_obj.getString("total_rating_user"));
                availableJobsResponse.setAvgrating(body_obj.getString("avgrating"));
                availableJobsResponse.setJob_location(body_obj.getString("job_location"));
                availableJobsResponse.setThumbnail(body_obj.getString("thumb_url"));
                availableJobsResponse.setOrder_status("0");
                availableJobsResponseArrayList.add(availableJobsResponse);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            sendPushBuyerAddedJob(getApplicationContext(), message2, availableJobsResponseArrayList);
        }// if seller provide quote on buyer job
        else if (notification_code.equals("14")) {
            String body = remoteMessage.getData().get("body");
            JSONObject body_obj = null;
            String job_id = "";
            String job_status = "";
            try {
                body_obj = new JSONObject(body);
                job_id = body_obj.getString("id");
                job_status = body_obj.getString("job_status");
                Log.e("jobid", "job " + job_id);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            sendPushSellerProvideQuote(getApplicationContext(), message2, job_id, job_status);
        }// if seller cancel his quote on available job
        else if (notification_code.equals("15")) {
            String body = remoteMessage.getData().get("body");
            JSONObject body_obj = null;
            String job_id = "";
            String job_status = "";
            try {
                body_obj = new JSONObject(body);
                job_id = body_obj.getString("id");
                job_status = body_obj.getString("job_status");

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            sendPushSellerProvideQuote(getApplicationContext(), message2, job_id, job_status);

        }// if buyer cancel quote of seller
        else if (notification_code.equals("16")) {
            String body = remoteMessage.getData().get("body");
            JSONObject body_obj = null;
            String job_id = "";
            try {
                body_obj = new JSONObject(body);
                job_id = body_obj.getString("id");
                Log.e("jobid", "job " + job_id);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            sendPushBuyerCancelSellerQuote(getApplicationContext(), message2);

        }// if admin approve you as a seller
        else if (notification_code.equals("17")) {
            sendPushAdminApprove(getApplicationContext(), message2);
        }// if admin decline you as a seller
        else if (notification_code.equals("18")) {
            sendPushAdminDecline(getApplicationContext(), message2);
        }// when admin send you notification message
        else if (notification_code.equals("19")) {
            sendAdminPush(getApplicationContext(), message2);
        }// when seller en route to job then buyer receive notification
        else if (notification_code.equals("20")) {

            notificationManager.cancelAll();
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                PayPalResponse payPalResponse = new PayPalResponse();
                payPalResponse.setRequest_id(bodyArray.getString("id"));
                payPalResponse.setOrder_id(bodyArray.getString("order_id"));
                payPalResponse.setBuyer_id(bodyArray.getString("buyer_id"));
                payPalResponse.setSeller_id(bodyArray.getString("seller_id"));
                payPalResponse.setPackage_id(bodyArray.getString("package_id"));
                payPalResponse.setReq_la(bodyArray.getString("req_lat"));
                payPalResponse.setReq_lng(bodyArray.getString("req_lng"));
                payPalResponse.setReq_location(bodyArray.getString("req_location"));
                payPalResponse.setStatus(bodyArray.getString("status"));
                payPalResponse.setCreated_date(bodyArray.getString("created_date"));
                payPalResponse.setFirst_name(bodyArray.getString("first_name"));
                payPalResponse.setLast_name(bodyArray.getString("last_name"));
                payPalResponse.setMobile(bodyArray.getString("mobile"));
                payPalResponse.setSeller_latitude(bodyArray.getString("seller_latitude"));
                payPalResponse.setSeller_longitude(bodyArray.getString("seller_longitude"));
                payPalResponse.setBuyer_name(bodyArray.getString("buyer_name"));
                payPalResponse.setBuyer_mobile(bodyArray.getString("buyer_mobile"));
                payPalResponse.setNormal_charges(bodyArray.getString("normal_charges"));
                payPalResponse.setPackage_name(bodyArray.getString("package_name"));
                payPalResponse.setTotal_amounts(bodyArray.getString("total_amounts"));
                payPalResponse.setIs_scheduled(bodyArray.getString("is_scheduled"));
                payPalResponse.setStart_date(bodyArray.getString("start_date"));
                payPalResponse.setStart_time(bodyArray.getString("start_time"));

                payPalResponse.setMain_amount(bodyArray.getString("main_amount"));
                payPalResponse.setProcessing_fees(bodyArray.getString("processing_fees"));
                payPalResponse.setProcessing_fees_ammount(bodyArray.getString("processing_fees_ammount"));
                payPalResponse.setThread_id(bodyArray.getString("thread_id"));
                payPalResponse.setPayment_method(bodyArray.getString("payment_method"));
                payPalResponse.setCategory(bodyArray.getString("category"));
                payPalResponse.setSeller_address(bodyArray.getString("seller_address"));
                payPalResponse.setBuyer_address(bodyArray.getString("buyer_address"));

                payPalResponse.setCheckin_time(bodyArray.getString("checkin_time"));
                payPalResponse.setCheckout_time(bodyArray.getString("checkout_time"));
                payPalResponse.setDistance(bodyArray.getString("distance"));
                payPalResponse.setPrice(bodyArray.getString("price"));
                payPalResponse.setRequest_type(bodyArray.getString("request_type"));

                sendPushBuyerMakePayment(getApplicationContext(), payPalResponse, true);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }// when buyer accept or reject checkin request of seller
        else if (notification_code.equals("21")) {

            notificationManager.cancelAll();
            String body = remoteMessage.getData().get("body");
            PayPalResponse payPalResponse = new PayPalResponse();
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);

                payPalResponse.setRequest_id(bodyArray.getString("id"));
                payPalResponse.setOrder_id(bodyArray.getString("order_id"));
                payPalResponse.setBuyer_id(bodyArray.getString("buyer_id"));
                payPalResponse.setSeller_id(bodyArray.getString("seller_id"));
                payPalResponse.setPackage_id(bodyArray.getString("package_id"));
                payPalResponse.setReq_la(bodyArray.getString("req_lat"));
                payPalResponse.setReq_lng(bodyArray.getString("req_lng"));
                payPalResponse.setReq_location(bodyArray.getString("req_location"));
                payPalResponse.setStatus(bodyArray.getString("status"));
                payPalResponse.setCreated_date(bodyArray.getString("created_date"));
                payPalResponse.setFirst_name(bodyArray.getString("first_name"));
                payPalResponse.setLast_name(bodyArray.getString("last_name"));
                payPalResponse.setMobile(bodyArray.getString("mobile"));
                payPalResponse.setSeller_latitude(bodyArray.getString("seller_latitude"));
                payPalResponse.setSeller_longitude(bodyArray.getString("seller_longitude"));
                payPalResponse.setBuyer_name(bodyArray.getString("buyer_name"));
                payPalResponse.setBuyer_mobile(bodyArray.getString("buyer_mobile"));
                payPalResponse.setNormal_charges(bodyArray.getString("normal_charges"));
                payPalResponse.setPackage_name(bodyArray.getString("package_name"));
                payPalResponse.setTotal_amounts(bodyArray.getString("total_amounts"));
                payPalResponse.setIs_scheduled(bodyArray.getString("is_scheduled"));
                payPalResponse.setStart_date(bodyArray.getString("start_date"));
                payPalResponse.setStart_time(bodyArray.getString("start_time"));

                payPalResponse.setMain_amount(bodyArray.getString("main_amount"));
                payPalResponse.setProcessing_fees(bodyArray.getString("processing_fees"));
                payPalResponse.setProcessing_fees_ammount(bodyArray.getString("processing_fees_ammount"));
                payPalResponse.setThread_id(bodyArray.getString("thread_id"));
                payPalResponse.setPayment_method(bodyArray.getString("payment_method"));
                payPalResponse.setCategory(bodyArray.getString("category"));
                payPalResponse.setSeller_address(bodyArray.getString("seller_address"));
                payPalResponse.setBuyer_address(bodyArray.getString("buyer_address"));
                payPalResponse.setRequest_type(bodyArray.getString("request_type"));

                payPalResponse.setCheckin_time(bodyArray.getString("checkin_time"));
                payPalResponse.setCheckout_time(bodyArray.getString("checkout_time"));
                payPalResponse.setDistance(bodyArray.getString("distance"));
                payPalResponse.setJob_started(bodyArray.getString("job_started"));

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            if (savePref.getSellerOnCheckin() && savePref.getRequestId().equals(payPalResponse.getRequest_id())) {
                publishResultBuyerAcceptCHeckin(payPalResponse);
                sendPushBuyerAcceptCheckin(payPalResponse);
            } else {
                sendPushBuyerAcceptCheckin(payPalResponse);
            }
        }
        // if there is no response from buyer to accept or reject job
        else if (notification_code.equals("22")) {

            notificationManager.cancelAll();
            String body = remoteMessage.getData().get("body");
            PayPalResponse payPalResponse = new PayPalResponse();
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);

                payPalResponse.setRequest_id(bodyArray.getString("id"));
                payPalResponse.setOrder_id(bodyArray.getString("order_id"));
                payPalResponse.setBuyer_id(bodyArray.getString("buyer_id"));
                payPalResponse.setSeller_id(bodyArray.getString("seller_id"));
                payPalResponse.setPackage_id(bodyArray.getString("package_id"));
                payPalResponse.setReq_la(bodyArray.getString("req_lat"));
                payPalResponse.setReq_lng(bodyArray.getString("req_lng"));
                payPalResponse.setReq_location(bodyArray.getString("req_location"));
                payPalResponse.setStatus(bodyArray.getString("status"));
                payPalResponse.setCreated_date(bodyArray.getString("created_date"));
                payPalResponse.setFirst_name(bodyArray.getString("first_name"));
                payPalResponse.setLast_name(bodyArray.getString("last_name"));
                payPalResponse.setMobile(bodyArray.getString("mobile"));
                payPalResponse.setSeller_latitude(bodyArray.getString("seller_latitude"));
                payPalResponse.setSeller_longitude(bodyArray.getString("seller_longitude"));
                payPalResponse.setBuyer_name(bodyArray.getString("buyer_name"));
                payPalResponse.setBuyer_mobile(bodyArray.getString("buyer_mobile"));
                payPalResponse.setNormal_charges(bodyArray.getString("normal_charges"));
                payPalResponse.setPackage_name(bodyArray.getString("package_name"));
                payPalResponse.setTotal_amounts(bodyArray.getString("total_amounts"));
                payPalResponse.setIs_scheduled(bodyArray.getString("is_scheduled"));
                payPalResponse.setStart_date(bodyArray.getString("start_date"));
                payPalResponse.setStart_time(bodyArray.getString("start_time"));

                payPalResponse.setMain_amount(bodyArray.getString("main_amount"));
                payPalResponse.setProcessing_fees(bodyArray.getString("processing_fees"));
                payPalResponse.setProcessing_fees_ammount(bodyArray.getString("processing_fees_ammount"));
                payPalResponse.setThread_id(bodyArray.getString("thread_id"));
                payPalResponse.setPayment_method(bodyArray.getString("payment_method"));
                payPalResponse.setCategory(bodyArray.getString("category"));
                payPalResponse.setSeller_address(bodyArray.getString("seller_address"));
                payPalResponse.setBuyer_address(bodyArray.getString("buyer_address"));

                payPalResponse.setCheckin_time(bodyArray.getString("checkin_time"));
                payPalResponse.setCheckout_time(bodyArray.getString("checkout_time"));
                payPalResponse.setDistance(bodyArray.getString("distance"));
                payPalResponse.setJob_started(bodyArray.getString("job_started"));

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            if (savePref.getUserId().equals(payPalResponse.getBuyer_id())) {
                if (savePref.getUserOnlineForTrackWork() && savePref.getRequestId().equals(payPalResponse.getRequest_id())) {
                    publishResultsNotAcceptedRejected(false);
                } else {
                    sendSilentPush(getApplicationContext(), message2);
                }
            } else {
                if (savePref.getSellerOnCheckin() && savePref.getRequestId().equals(payPalResponse.getRequest_id())) {
                    publishResultBuyerAcceptCHeckin(payPalResponse);
                    sendPushBuyerAcceptCheckin(payPalResponse);
                } else {
                    sendPushBuyerAcceptCheckin(payPalResponse);
                }
            }
        }
        // job reminder send to seller before 2 hours of starting job
        else if (notification_code.equals("23")) {
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                request_type = bodyArray.getString("request_type");
                request_id = bodyArray.getString("id");

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            sendReminderToBuyer(getApplicationContext(), "23");
        }

        // if seller reject the job when buyer accept qutation of particular seller
        else if (notification_code.equals("24")) {
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                post_id = bodyArray.getString("post_id");
                request_type = "1";
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            if (savePref.getBuyerRequestedSellerQuote() && savePref.getRequestId().equals(post_id)) {
                publishResultsAcceptedJob("6", remoteMessage.getData().get("message"));
            } else {
                sendPushSellerRejectJob(getApplicationContext(), remoteMessage.getData().get("message"), "6");
            }
        }
        // job reminder send to buyer before 2 hours of starting job or when seller accept job reminder then notification send to buyer
        else if (notification_code.equals("25") || notification_code.equals("26")) {
            String code = notification_code;
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                request_type = bodyArray.getString("request_type");
                request_id = bodyArray.getString("id");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            sendReminderToBuyer(getApplicationContext(), code);
        }// notification come before happening of your event
        else if (notification_code.equals("27")) {
            sendAdminPush(getApplicationContext(), message2);
        }
        // if job is canceled by buyer then push send to seller
        else if (notification_code.equals("29")) {
            sendAdminPush(getApplicationContext(), message2);
        }// when buyer or seller give rating then push received
        else if (notification_code.equals("30")) {

            notificationManager.cancelAll();
            String body = remoteMessage.getData().get("body");
            JSONObject bodyObject = null;
            try {
                bodyObject = new JSONObject(body);

                sendRatingPush(bodyObject.getString("created_date"), bodyObject.getString("start_date"), bodyObject.getString("order_id"), bodyObject.getString("rating"), bodyObject.getString("comment")
                        , bodyObject.getString("first_name"), bodyObject.getString("last_name"), bodyObject.getString("user_image"),
                        bodyObject.getString("is_scheduled"), bodyObject.getString("review_date"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }// if cancellation or refund request of buyer is approved or succesfully refund amount by admin
        else if (notification_code.equals("31")) {

            notificationManager.cancelAll();
            String body = remoteMessage.getData().get("body");
            JSONObject bodyObject = null;
            try {
                bodyObject = new JSONObject(body);
                sendRefundCancelPush(bodyObject.getString("type"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }// if withrawl request of seller is approved by admin
        else if (notification_code.equals("32")) {
            notificationManager.cancelAll();
            sendWithrawPush();
        }// when buyer made extra payment then push receive by seller
        else if (notification_code.equals("33")) {
            String body = remoteMessage.getData().get("body");
            JSONObject bodyArray = null;
            try {
                bodyArray = new JSONObject(body);
                request_id = bodyArray.getString("id");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            sendPushBuyerMakeExtraPayment(getApplicationContext());
        }
        // when seller is unblocked by admin
        else if (notification_code.equals("34")) {
            sendAdminPush(getApplicationContext(), message2);
        }// when seller is blocked by admin
        else if (notification_code.equals("35")) {
            sendAdminPush(getApplicationContext(), message2);
        }
        // notification send to buyer when seller cancel job on checkin popup
        else if (notification_code.equals("36")) {
            sendAdminPush(getApplicationContext(), message2);
        }
        // notification send when admin send notification regarding refund of payfort amount
        else if (notification_code.equals("40")) {
            sendAdminPush(getApplicationContext(), message2);
        }// if terms updated on admin panel
        else if (notification_code.equals("42")) {
            sendTermUpdatedPush(getApplicationContext(), message2);
        }
    }

    private void publishResultsMessage(Map<String, String> map) {

        Bundle bundle = new Bundle(map != null ? map.size() : 0);
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
        }
        Intent intent = new Intent(Utils.NOTIFICATION_MESSAGE);
        intent.putExtra("req_id", request_id);
        intent.putExtra("receiver", receiver);
        intent.putExtra("opponent_id", sender);
        intent.putExtra("created", created);
        intent.putExtra("msg_id", msg_id);
        intent.putExtra("message", message);
        intent.putExtra("thread_id", thread_id);
        intent.putExtra("opponent_image", sender_image);
        intent.putExtra("opponent_name", sender_name);
        intent.putExtra("message_type", message_type);
        intent.putExtra("thumb", thumb);

//        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void publishResultsMakePayment(final PayPalResponse payPalResponse, boolean payment_paid) {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Utils.showToast(getApplicationContext(), "Payment of " + payPalResponse.getTotal_amounts() + " SAR has been submitted succesfully by " + payPalResponse.getBuyer_name());
            }
        };
        mainHandler.post(myRunnable);

        Intent intent = new Intent(Utils.NOTIFICATION_MADE_PAYMENT);
        intent.putExtra("paypal_reponse", payPalResponse);
        intent.putExtra("payment_paid", payment_paid);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void publishResultsPaymentNotPaid(final String message, boolean payment_paid) {
        Intent intent = new Intent(Utils.NOTIFICATION_MADE_PAYMENT);
        intent.putExtra("payment_paid", payment_paid);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void publishResultsJobNotAccepted(final String message, boolean job_accepted) {
        Intent intent = new Intent(Utils.NOTIFICATION_MADE_PAYMENT);
        intent.putExtra("job_accepted", job_accepted);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void publishResultsAcceptedJob(String status, String message) {
        if (status.equalsIgnoreCase("4")) {
            Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(getApplicationContext(), seller_name + " accepted your job. Please proceed to pay");
                }
            };
            mainHandler.post(myRunnable);
        }
        Intent intent = null;
        if (request_type.equals("0"))
            intent = new Intent(Utils.NOTIFICATION_ACCEPTED_JOB);
        else
            intent = new Intent(Utils.NOTIFICATION_BUYER_REQUESTED_SELLER_QUOTATION);
        intent.putExtra("status", status);
        if (status.equalsIgnoreCase("4")) {
            intent.putExtra("req_id", request_id);
            intent.putExtra("is_scheduled", is_scheduled);
            intent.putExtra("start_date", start_date);
            intent.putExtra("start_time", start_time);
            intent.putExtra("req_type", request_type);
            intent.putExtra("vat_tax", vat_tax);
        } else if (status.equalsIgnoreCase("5")) {
            intent.putExtra("req_id", request_id);
            intent.putExtra("seller_data", sellerData);
            intent.putExtra("message", message);
            intent.putExtra("package", selected_pacakge);
            intent.putExtra("req_type", request_type);
        } else if (status.equalsIgnoreCase("6")) {
            intent.putExtra("message", message);
            intent.putExtra("req_type", request_type);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void publishJobNotAcceptedByseller(String status, final String message) {

        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Utils.showToast(getApplicationContext(), message);
            }
        };
        Intent intent = null;
        mainHandler.post(myRunnable);
        if (request_type.equals("0"))
            intent = new Intent(Utils.NOTIFICATION_ACCEPTED_JOB);
        else
            intent = new Intent(Utils.NOTIFICATION_BUYER_REQUESTED_SELLER_QUOTATION);
        intent.putExtra("status", status);
        intent.putExtra("message", status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendNotification(Context context, String message, String notification_code) {

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(getApplicationContext(), uri);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(context, SellerAceeptRejectJobActivity.class);
        intent.putExtra("request_id", request_id);
        intent.putExtra("req_lat", req_lat);
        intent.putExtra("req_long", req_long);
        intent.putExtra("first_name", first_name);
        intent.putExtra("last_name", last_name);
        intent.putExtra("mobile", mobile);
        intent.putExtra("created", created);
        intent.putExtra("total_rating_user", total_rating_user);
        intent.putExtra("avgrating", avgrating);
        intent.putExtra("req_location", req_location);
        intent.putExtra("request_waiting_time", request_waiting_time);
        intent.putExtra("is_scheduled", is_scheduled);
        intent.putExtra("start_date", start_date);
        intent.putExtra("start_time", start_time);
        intent.putExtra("distance", distance);

        intent.putExtra("package_selected", package_selected);
        intent.putExtra("request_type", request_type);
        intent.putExtra("normal_charges", normal_charges);
        intent.putExtra("price", price);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushToBuyerPaymentTimeExceeded(Context context, final String message) {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Utils.showToast(getApplicationContext(), message);
            }
        };
        mainHandler.post(myRunnable);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("payment_paid", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotificationMessage(Context context, String message, String notification_code) {


        Intent intent = null;
        intent = new Intent(context, ChattingActivity.class);
        intent.putExtra("req_id", request_id);
        intent.putExtra("receiver", receiver);
        intent.putExtra("opponent_id", sender);
        intent.putExtra("created", created);
        intent.putExtra("msg_id", msg_id);
        intent.putExtra("message", message);
        intent.putExtra("thread_id", thread_id);
        intent.putExtra("opponent_name", sender_name);
        intent.putExtra("opponent_image", sender_image);
        intent.putExtra("job_status", "0");
        intent.putExtra("message_type", message_type);
        intent.putExtra("thumb", thumb);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(sender_name)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).setOngoing(false)
                .setContentIntent(pendingIntent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = notificationBuilder.build();

        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushSellerAcceptJob(Context context, String message, String notification_code) {
        String category_name = "";
        if (category.equals(getResources().getString(R.string.plumber_id))) {
            category_name = getResources().getString(R.string.plumber);
        } else if (category.equals(getResources().getString(R.string.electrician_id))) {
            category_name = getResources().getString(R.string.electrician);
        } else if (category.equals(getResources().getString(R.string.carpenter_id))) {
            category_name = getResources().getString(R.string.carpenter);
        } else if (category.equals(getResources().getString(R.string.ac_id))) {
            category_name = getResources().getString(R.string.ac);
        } else if (category.equals(getResources().getString(R.string.satellite_id))) {
            category_name = getResources().getString(R.string.satellite);
        } else if (category.equals(getResources().getString(R.string.painter_id))) {
            category_name = getResources().getString(R.string.painter);
        }

        Intent intent = null;
        intent = new Intent(context, RequestSummaryPushActivity.class);
        intent.putExtra("req_id", request_id);
        intent.putExtra("normal_charges", normal_charges);
        intent.putExtra("seller_name", seller_name);
        intent.putExtra("seller_image", seller_image);
        intent.putExtra("package_name", package_name);
        intent.putExtra("request_id", request_id);
        intent.putExtra("total_rating_user", total_rating_user);
        intent.putExtra("avgrating", avgrating);
        intent.putExtra("category", category);
        intent.putExtra("processing_fee", processing_fee);
        intent.putExtra("vat_tax", vat_tax);
        intent.putExtra("extra_hours", extra_hours);
        intent.putExtra("main_hours", main_hours);
        intent.putExtra("additional_charges", additional_charges);
        intent.putExtra("language", language);
        intent.putExtra("job_description", job_description);
        intent.putExtra("start_date", start_date);
        intent.putExtra("start_time", start_time);
        intent.putExtra("is_scheduled", is_scheduled);
        intent.putExtra("seller_id", seller_id);
        intent.putExtra("req_type", request_type);
        intent.putExtra("price", price);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(category_name)
                .setContentText(seller_name + " " + context.getResources().getString(R.string.accepted_request))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setOngoing(false)
                .setContentIntent(pendingIntent);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = notificationBuilder.build();

        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushSellerRejectJob(Context context, String message, String notification_code) {
        Intent intent = null;
        if (notification_code.equalsIgnoreCase("6")) {
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("push_seller_rejected", true);
            intent.putExtra("message", message);
        } else if (notification_code.equalsIgnoreCase("5")) {
            intent = new Intent(context, SelectedPackageActivity.class);
            intent.putExtra("selected_package", sellerData);
            intent.putExtra("package", selected_pacakge);
            intent.putExtra("from_sellerdetail", false);
            intent.putExtra("seller_id", sellerData.getSellerId());
            intent.putExtra("omorni_procesing_fee", sellerData.getOmorni_processing_fee());
            intent.putExtra("req_id", request_id);
            intent.putExtra("push_screen", true);
            intent.putExtra("service_now", true);
            intent.putExtra("message", message);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(context.getResources().getString(R.string.request_declined))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = notificationBuilder.build();

        notificationManager.notify(i++, notification);
    }

    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    private void sendPushBuyerMakePayment(Context context, PayPalResponse payPalResponse, boolean is_track_seller_screen) {


        Intent intent = null;
        if (is_track_seller_screen) {
            intent = new Intent(context, TrackSellerActivity.class);
            intent.putExtra("summary", payPalResponse);
        } else {
            intent = new Intent(context, OrderSummerySellerActivity.class);
            intent.putExtra("paypal_reponse", payPalResponse);
            intent.putExtra("from_my_order", false);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder = null;
        if (is_track_seller_screen) {
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setOngoing(false).setStyle(new Notification.BigTextStyle()
                            .bigText(message2))
                    .setContentText(message2)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
        } else {
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                    .setContentTitle(context.getResources().getString(R.string.payment_completed))
                    .setOngoing(false).setStyle(new Notification.BigTextStyle()
                            .bigText(payPalResponse.getBuyer_name() + " " + context.getResources().getString(R.string.paid_amount_of) + " " + payPalResponse.getTotal_amounts() + " " + context.getResources().getString(R.string.sar)))
                    .setContentText(payPalResponse.getBuyer_name() + " " + context.getResources().getString(R.string.paid_amount_of) + " " + payPalResponse.getTotal_amounts() + " " + context.getResources().getString(R.string.sar))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushBuyerMakeExtraPayment(Context context) {
        Intent intent = null;

        intent = new Intent(context, OrderSummerySellerActivity.class);
        intent.putExtra("req_id", request_id);
        intent.putExtra("from_my_order", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(context.getResources().getString(R.string.payment_completed))
                .setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message2))
                .setContentText(message2)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    private void publishResultsNotAcceptedRejected(boolean seller_checkin_request) {

        Intent intent = new Intent(Utils.NOTIFICATION_CHECKIN);
        intent.putExtra("message", message2);
        intent.putExtra("seller_checkin_request", seller_checkin_request);
        intent.putExtra("is_show_checkin_dialog", true);
        intent.putExtra("message", message2);
        intent.putExtra("req_id", request_id);
        intent.putExtra("seller_name", seller_name);
        intent.putExtra("seller_id", seller_id);
        intent.putExtra("created_date", created);
        intent.putExtra("checkin_time", check_in_time);
        intent.putExtra("start_date", start_date);
        intent.putExtra("is_scheduled", is_scheduled);
        intent.putExtra("order_id", order_id);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushSellerCheckout(Context context) {
        Intent intent = null;
        intent = new Intent(context, SellerCheckoutActivity.class);
        intent.putExtra("req_id", request_id);
        intent.putExtra("seller_name", seller_name);
        intent.putExtra("seller_id", seller_id);
        intent.putExtra("created_date", created);
        intent.putExtra("checkin_time", check_in_time);
        intent.putExtra("start_date", start_date);
        intent.putExtra("is_scheduled", is_scheduled);
        intent.putExtra("checkout_time", checkout_time);
        intent.putExtra("order_id", order_id);

        intent.putExtra("extra_hours", extra_hours);
        intent.putExtra("normal_charges", normal_charges);
        intent.putExtra("package_name", package_name);
        intent.putExtra("additional_charges", additional_charges);
        intent.putExtra("main_hours", main_hours);
        intent.putExtra("request_type", request_type);
        intent.putExtra("post_title", post_title);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(seller_name + " " + context.getResources().getString(R.string.completed_job))
                .setAutoCancel(true)
                .setSound(defaultSoundUri).setOngoing(false)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    private void publishResultsCheckout(Map<String, String> map) {

        /*Bundle bundle = new Bundle(map != null ? map.size() : 0);
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
        }*/
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Utils.showToast(getApplicationContext(), seller_name + " " + getApplicationContext().getResources().getString(R.string.completed_job));
            }
        };
        mainHandler.post(myRunnable);

        Intent intent = new Intent(Utils.NOTIFICATION_CHECKOUT);
        intent.putExtra("req_id", request_id);
        intent.putExtra("seller_name", seller_name);
        intent.putExtra("seller_id", seller_id);
        intent.putExtra("created_date", created);
        intent.putExtra("checkin_time", check_in_time);
        intent.putExtra("start_date", start_date);
        intent.putExtra("is_scheduled", is_scheduled);
        intent.putExtra("checkout_time", checkout_time);
        intent.putExtra("order_id", order_id);

        intent.putExtra("extra_hours", extra_hours);
        intent.putExtra("normal_charges", normal_charges);
        intent.putExtra("package_name", package_name);
        intent.putExtra("additional_charges", additional_charges);
        intent.putExtra("main_hours", main_hours);
        intent.putExtra("request_type", request_type);
        intent.putExtra("post_title", post_title);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushNotReceivePayment(Context context, String message) {
        Intent intent = null;
        intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false)
                .setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendSilentPush(Context context, String message) {

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();

        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushBuyerAddedJob(Context context, String message, ArrayList<SellerAvailableJobsResponse> availableJobsResponseArrayList) {
        Intent intent = null;
        intent = new Intent(context, AvailableJobDetail.class);
        intent.putExtra("array", availableJobsResponseArrayList);
        intent.putExtra("selected_position", "0");
        intent.putExtra("flag", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushSellerProvideQuote(Context context, String message, String job_id, String job_status) {
        Intent intent = null;
        intent = new Intent(context, PostedJobsDetailActivity.class);
        intent.putExtra("job_id", job_id);
        intent.putExtra("job_status", job_status);
        intent.putExtra("from_push", true);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushBuyerCancelSellerQuote(Context context, String message) {
        Intent intent = null;
        intent = new Intent(context, MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushAdminApprove(Context context, String message) {
        Intent intent = null;
        intent = new Intent(context, MainActivity.class);
        intent.putExtra("flag", true);
        intent.putExtra("message", message);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushAdminDecline(Context context, String message) {
        Intent intent = null;
        intent = new Intent(context, MainActivity.class);
        intent.putExtra("flag", true);
        intent.putExtra("message", message);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendAdminPush(Context context, String message) {
        Intent intent = null;
        intent = new Intent(context, MainActivity.class);
        intent.putExtra("admin_push", true);
        intent.putExtra("message", message);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setOngoing(false)
                .setStyle(new Notification.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendTermUpdatedPush(Context context, String message) {
        Intent intent = null;
        intent = new Intent(context, TermConditionActivity.class);
        intent.putExtra("from_push", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setOngoing(false)
                .setStyle(new Notification.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);

    }

    private void getSellerData(JSONObject object1) {
        try {
            sellerData = new SellerData();
            allPackagesArrayList = new ArrayList<>();
            request_id = object1.getString("request_id");
            sellerData.setSellerId(object1.getString("id"));
            sellerData.setFirst_name(object1.getString("first_name"));
            sellerData.setLast_name(object1.getString("last_name"));
            sellerData.setEmail(object1.getString("email"));
            sellerData.setMobile(object1.getString("mobile"));
            sellerData.setLocation(object1.getString("location"));
            sellerData.setJob_description(object1.getString("job_description"));
            sellerData.setUser_image(object1.getString("user_image"));
            sellerData.setIs_verify(object1.getString("is_verify"));
            sellerData.setUser_type(object1.getString("user_type"));
            sellerData.setStatus(object1.getString("status"));
            sellerData.setCreated_date(object1.getString("created_date"));
            String lat = object1.getString("latitude");
            String longi = object1.getString("longitude");
            sellerData.setLatitude(lat);
            sellerData.setLongitude(longi);
            sellerData.setOn_duty(object1.getString("on_duty"));
            sellerData.setDistance(object1.getString("distance"));
            sellerData.setFavourite(object1.getString("favourite"));
            sellerData.setRating(object1.getString("avgrating"));
            sellerData.setOmorni_processing_fee(object1.getString("processing_fee"));
            sellerData.setVat_tax(object1.getString("vat_tax"));

            JSONArray jsonArray = new JSONArray(object1.getString("language"));
            String language = "";
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    language += jsonObject.getString("language") + ",";
                }
                sellerData.setLanguage(language.substring(0, language.length() - 1));
            } else {
                sellerData.setLanguage(language);
            }
            sellerData.setTotal_rating_user(object1.getString("total_rating_user"));
            JSONArray package_array = object1.getJSONArray("allpackage");

            for (int j = 0; j < package_array.length(); j++) {
                AllPackageResponse allpackages = new AllPackageResponse();
                JSONObject package_obj = package_array.getJSONObject(j);
                allpackages.setId(package_obj.getString("id"));
                allpackages.setNormal_charges(package_obj.getString("normal_charges"));
                allpackages.setAdditional_charges(package_obj.getString("additional_charges"));
                allpackages.setStatus(package_obj.getString("status"));
                allpackages.setPackage_name(package_obj.getString("package_name"));
                allpackages.setPackage_description(package_obj.getString("package_description"));
                allpackages.setMain_hours(package_obj.getString("main_hours"));
                allpackages.setExtra_hours(package_obj.getString("extra_hours"));
                allpackages.setPackage_status(package_obj.getString("package_status"));
                allpackages.setDescription(package_obj.getString("description"));
                if (package_obj.getString("id").equalsIgnoreCase(object1.getString("package_id"))) {
                    if (i == 0)
                        selected_pacakge = "50";
                    else if (i == 1)
                        selected_pacakge = "80";
                    else if (i == 2)
                        selected_pacakge = "120";
                }
                allPackagesArrayList.add(allpackages);
            }
            sellerData.setAllPackages(allPackagesArrayList);
        } catch (JSONException exception) {
            exception.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void sellerCheckinRequest() {
        Intent intent = null;
        intent = new Intent(getApplicationContext(), TrackWorkActivity.class);
        intent.putExtra("is_show_checkin_dialog", true);
        intent.putExtra("message", message2);
        intent.putExtra("req_id", request_id);
        intent.putExtra("seller_name", seller_name);
        intent.putExtra("seller_id", seller_id);
        intent.putExtra("created_date", created);
        intent.putExtra("checkin_time", check_in_time);
        intent.putExtra("start_date", start_date);
        intent.putExtra("is_scheduled", is_scheduled);
        intent.putExtra("order_id", order_id);
        intent.putExtra("from_push", true);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message2)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message2))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    private void publishResultBuyerAcceptCHeckin(final PayPalResponse payPalResponse) {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Utils.showToast(getApplicationContext(), message2);
            }
        };
        mainHandler.post(myRunnable);

        Intent intent = new Intent(Utils.NOTIFICATION_SELLER_CHECKIN_SCREEN);
        intent.putExtra("summary", payPalResponse);
        intent.putExtra("message", message2);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendPushBuyerAcceptCheckin(PayPalResponse payPalResponse) {
        Intent intent = null;
        intent = new Intent(getApplicationContext(), TrackBuyerLocationActivity.class);
        intent.putExtra("summary", payPalResponse);
        intent.putExtra("from_push", true);
        intent.putExtra("message", message2);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message2)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message2))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendReminderToBuyer(Context context, String code) {
        Intent intent = null;

        if (code.equals("23"))          // reminder for seller
            intent = new Intent(context, OrderSummerySellerActivity.class);
        else if (code.equals("25") || code.equals("26"))     // reminder for buyer
            intent = new Intent(context, OrderSummeryActivity.class);

        intent.putExtra("req_id", request_id);
        intent.putExtra("from_my_order", true);
        intent.putExtra("job_reminder", true);
        intent.putExtra("message", message2);
        intent.putExtra("from_push", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.job_reminder))
                .setContentText(message2)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message2))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendRatingPush(String created_date, String start_date, String order_id, String rating, String comment,
                                String first_name, String last_name, String user_image, String is_scheduled, String review_date) {

        Intent intent = new Intent(getApplicationContext(), ShowRating.class);

        intent.putExtra("created_date", created_date);
        intent.putExtra("start_date", start_date);
        intent.putExtra("order_id", order_id);
        intent.putExtra("rating", rating);
        intent.putExtra("comment", comment);
        intent.putExtra("first_name", first_name);
        intent.putExtra("last_name", last_name);
        intent.putExtra("user_image", user_image);
        intent.putExtra("is_scheduled", is_scheduled);
        intent.putExtra("review_date", review_date);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.job_reminder))
                .setContentText(message2)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message2))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendRefundCancelPush(String type) {

        Intent intent = new Intent(getApplicationContext(), ViewRefundActivity.class);
        intent.putExtra("type", type);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.job_reminder))
                .setContentText(message2)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message2))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendWithrawPush() {

        Intent intent = new Intent(getApplicationContext(), WithdrawHistoryActivity.class);
        intent.putExtra("from_push", true);
        intent.putExtra("message", message2);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.job_reminder))
                .setContentText(message2)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).
                        setOngoing(false).setStyle(new Notification.BigTextStyle()
                        .bigText(message2))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = notificationBuilder.build();
        notificationManager.notify(i++, notification);
    }

    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }
}
