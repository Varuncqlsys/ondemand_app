package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.omorni.R;

import com.omorni.model.PayPalResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class OrderSummeryActivity extends BaseActivity {
    private Toolbar toolbar;
    private TextView toolbar_title, processing_perc, order_number, track_btn, service_date_value, payment_method_value, subtotal_value, processing_fee, grand_total, date, buyer_name, buyer_address, buyer_mobile, seller_name_category, basic_value, text_basic, seller_mobile, seller_address, track_name;
    private PayPalResponse payPalResponse;
    SavePref savePref;
    boolean from_my_order = false, job_reminder = false;
    private ScrollView scroll_view;
    private LinearLayout basic_layout;
    private TextView package_dummy;
    Date current_date;
    private String message = "";
    private TextView order_status, extra_charge, vat_number, vat_perc, vat_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(OrderSummeryActivity.this);
        if (Utils.isConfigRtl(OrderSummeryActivity.this)) {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_order_summery_as_seller_rtl);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_order_summery_rtl);
            }
        } else {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_order_summery_as_seller);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_order_summery);
            }
        }
        setToolbar();

        from_my_order = getIntent().getBooleanExtra("from_my_order", false);
        job_reminder = getIntent().getBooleanExtra("job_reminder", false);
        message = getIntent().getStringExtra("message");

        initialize();

        if (from_my_order) {
            getOrderSummaryApi();
        } else {
            setData();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        from_my_order = intent.getBooleanExtra("from_my_order", false);
        job_reminder = intent.getBooleanExtra("job_reminder", false);
        message = intent.getStringExtra("message");

        if (from_my_order) {
            getOrderSummaryApi();
        }
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.order_summary);
    }

    private void initialize() {
        savePref = new SavePref(OrderSummeryActivity.this);
        if (!from_my_order)
            payPalResponse = getIntent().getParcelableExtra("paypal_reponse");

        vat_number = (TextView) findViewById(R.id.vat_number);
        vat_perc = (TextView) findViewById(R.id.vat_perc);
        vat_value = (TextView) findViewById(R.id.vat_value);
        extra_charge = (TextView) findViewById(R.id.extra_charge);
        track_btn = (TextView) findViewById(R.id.track_btn);
        processing_perc = (TextView) findViewById(R.id.processing_perc);
        service_date_value = (TextView) findViewById(R.id.service_date_value);
        order_number = (TextView) findViewById(R.id.order_number);
        payment_method_value = (TextView) findViewById(R.id.payment_method_value);
        subtotal_value = (TextView) findViewById(R.id.subtotal_value);
        processing_fee = (TextView) findViewById(R.id.processing_fee);
        grand_total = (TextView) findViewById(R.id.grand_total);
        date = (TextView) findViewById(R.id.date);
        buyer_name = (TextView) findViewById(R.id.buyer_name);
        buyer_address = (TextView) findViewById(R.id.buyer_address);
        buyer_mobile = (TextView) findViewById(R.id.buyer_mobile);
        seller_name_category = (TextView) findViewById(R.id.seller_name_category);
        basic_value = (TextView) findViewById(R.id.basic_value);
        text_basic = (TextView) findViewById(R.id.text_basic);
        seller_mobile = (TextView) findViewById(R.id.seller_mobile);
        seller_address = (TextView) findViewById(R.id.seller_address);
        track_name = (TextView) findViewById(R.id.track_name);
        order_status = (TextView) findViewById(R.id.order_status);
        scroll_view = (ScrollView) findViewById(R.id.scroll_view);

        package_dummy = (TextView) findViewById(R.id.package_dummy);
        basic_layout = (LinearLayout) findViewById(R.id.basic_layout);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Calendar c = Calendar.getInstance();
        String formattedDate = formatter.format(c.getTime());
        try {
            current_date = (Date) formatter.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (job_reminder) {
            finish();
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        } else {
            if (from_my_order) {
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            } else {
                Intent intent = new Intent(OrderSummeryActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            }

        }
    }

    private void showReminderDialog() {

        final Dialog dialog = new Dialog(OrderSummeryActivity.this);

        dialog.setCanceledOnTouchOutside(false);
        if (savePref.getUserType().equals("1"))
            dialog.setContentView(R.layout.reminder_dialog);
        else
            dialog.setContentView(R.layout.reminder_dialog_buyer);

        ImageView back = (ImageView) dialog.findViewById(R.id.back);
        TextView text_message = (TextView) dialog.findViewById(R.id.text_message);
        TextView submit = (TextView) dialog.findViewById(R.id.submit);


        text_message.setText(message);
        submit.setText(getResources().getString(R.string.ok));


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setData() {
        if (job_reminder) {
            showReminderDialog();
        }

        scroll_view.setVisibility(View.VISIBLE);
        order_number.setText(getResources().getString(R.string.order_no) + payPalResponse.getOrder_id());
        vat_number.setText(getResources().getString(R.string.vat_number) + " " + payPalResponse.getVat_number());
        if (payPalResponse.getVat_tax() != null)
            vat_perc.setText(getResources().getString(R.string.vat_amount) + " (" + payPalResponse.getVat_tax() + "%)");
        vat_value.setText(payPalResponse.getVat_tax_ammount() + " " + getResources().getString(R.string.sar));

        processing_perc.setText(getResources().getString(R.string.processing_fee) + " (" + payPalResponse.getProcessing_fees() + "%)");
        if (payPalResponse.getPayment_method().equals("0")) {
            payment_method_value.setText(getResources().getString(R.string.paypal));
        } else if (payPalResponse.getPayment_method().equals("1")) {
            payment_method_value.setText(getResources().getString(R.string.creditcard));
        } else {
            payment_method_value.setText(getResources().getString(R.string.cashu));
        }
        subtotal_value.setText(payPalResponse.getMain_amount() + " " + getResources().getString(R.string.sar));
        processing_fee.setText(payPalResponse.getProcessing_fees_ammount() + " " + getResources().getString(R.string.sar));
        grand_total.setText(payPalResponse.getTotal_amounts() + " " + getResources().getString(R.string.sar));
        extra_charge.setText(payPalResponse.getExtra_hours() + " " + getResources().getString(R.string.sar));
        if (payPalResponse.getIs_scheduled().equals("0"))
            service_date_value.setText(getResources().getString(R.string.request_service_now));
//            service_date_value.setText("Now - " + Utils.convertTimeStampDateTime(Long.parseLong(payPalResponse.getCreated_date())));

        else
            service_date_value.setText(payPalResponse.getStart_date() + " - " + payPalResponse.getStart_time());

        date.setText(Utils.convertTimeStampDateTime(Long.parseLong(payPalResponse.getCreated_date())));
        buyer_name.setText(payPalResponse.getBuyer_name());
        buyer_address.setText(payPalResponse.getReq_location());
        buyer_mobile.setText(getResources().getString(R.string.mobile) + "   " + payPalResponse.getBuyer_mobile());


        if (payPalResponse.getCategory().equals(getResources().getString(R.string.plumber_id))) {
            seller_name_category.setText(payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name() + " - " + getResources().getString(R.string.category) + " " + getResources().getString(R.string.plumber));
        } else if (payPalResponse.getCategory().equals(getResources().getString(R.string.electrician_id))) {
            seller_name_category.setText(payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name() + " - " + getResources().getString(R.string.category) + " " + getResources().getString(R.string.electrician));
        } else if (payPalResponse.getCategory().equals(getResources().getString(R.string.carpenter_id))) {
            seller_name_category.setText(payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name() + " - " + getResources().getString(R.string.category) + " " + getResources().getString(R.string.carpenter));
        } else if (payPalResponse.getCategory().equals(getResources().getString(R.string.ac_id))) {
            seller_name_category.setText(payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name() + " - " + getResources().getString(R.string.category) + " " + getResources().getString(R.string.ac));
        } else if (payPalResponse.getCategory().equals(getResources().getString(R.string.satellite_id))) {
            seller_name_category.setText(payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name() + " - " + getResources().getString(R.string.category) + " " + getResources().getString(R.string.satellite));
        } else if (payPalResponse.getCategory().equals(getResources().getString(R.string.painter_id))) {
            seller_name_category.setText(payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name() + " - " + getResources().getString(R.string.category) + " " + getResources().getString(R.string.painter));
        }


        if (payPalResponse.getRequest_type().equals("0")) {
            basic_value.setText(payPalResponse.getNormal_charges());
            text_basic.setText(payPalResponse.getPackage_name());
        } else {
            package_dummy.setText(getResources().getString(R.string.package_selected) + " " + getResources().getString(R.string.post_job));
            basic_value.setText(payPalResponse.getPrice());
//            basic_layout.setVisibility(View.GONE);
        }

        if (payPalResponse.getStatus().equals("0"))
            order_status.setText(getResources().getString(R.string.pending));
        else if (payPalResponse.getStatus().equals("1"))
            order_status.setText(getResources().getString(R.string.not_paid));
        else if (payPalResponse.getStatus().equals("2"))
            order_status.setText(getResources().getString(R.string.decline));
        else if (payPalResponse.getStatus().equals("3"))
            order_status.setText(getResources().getString(R.string.progress));
        else if (payPalResponse.getStatus().equals("4"))
            order_status.setText(getResources().getString(R.string.complete));
        else if (payPalResponse.getStatus().equals("5"))
            order_status.setText(getResources().getString(R.string.paid));
        else if (payPalResponse.getStatus().equals("6"))
            order_status.setText(getResources().getString(R.string.en_route_status));
        else if (payPalResponse.getStatus().equals("7"))
            order_status.setText(getResources().getString(R.string.finished));
        else if (payPalResponse.getStatus().equals("8"))
            order_status.setText(getResources().getString(R.string.canceled));
        else if (payPalResponse.getStatus().equals("9"))
            order_status.setText(getResources().getString(R.string.refunded));
        else if (payPalResponse.getStatus().equals("10"))
            order_status.setText(getResources().getString(R.string.seller_canceled));
        else if (payPalResponse.getStatus().equals("11"))
            order_status.setText(getResources().getString(R.string.on_hold));
        else if (payPalResponse.getStatus().equals("12"))
            order_status.setText(getResources().getString(R.string.refund_initiated));

        seller_mobile.setText(payPalResponse.getMobile());
        seller_address.setText(payPalResponse.getSeller_address());
        if (payPalResponse.getStatus().equals("9")) {
            track_btn.setVisibility(View.GONE);
        } else {
            track_btn.setVisibility(View.VISIBLE);
        }
        track_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (payPalResponse.getIs_scheduled().equals("0")) {

                    if (payPalResponse.getStatus().equals("3")) {
                        Intent intent1 = new Intent(OrderSummeryActivity.this, TrackJobStartedActivity.class);
                        intent1.putExtra("req_id", payPalResponse.getRequest_id());
                        intent1.putExtra("seller_name", payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name());
                        intent1.putExtra("seller_id", payPalResponse.getSeller_id());
                        intent1.putExtra("created_date", payPalResponse.getCreated_date());
                        intent1.putExtra("checkin_time", payPalResponse.getCheckin_time());
                        intent1.putExtra("start_date", payPalResponse.getStart_date());
                        intent1.putExtra("is_scheduled", payPalResponse.getIs_scheduled());
                        intent1.putExtra("order_id", payPalResponse.getOrder_id());
                        intent1.putExtra("package_name", payPalResponse.getPackage_name());
                        intent1.putExtra("normal_charges", payPalResponse.getNormal_charges());
                        intent1.putExtra("additional_charges", payPalResponse.getAdditional_charges());
                        intent1.putExtra("post_title", payPalResponse.getPost_title());
                        intent1.putExtra("request_type", payPalResponse.getRequest_type());
                        startActivity(intent1);
                        finish();
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    } else if (payPalResponse.getStatus().equals("4") || payPalResponse.getStatus().equals("7")) {

                        if (payPalResponse.getExtra_amount().equals("0")) {
                            Intent intent = new Intent(OrderSummeryActivity.this, PostCommentByBuyerActivity.class);
                            intent.putExtra("req_id", payPalResponse.getRequest_id());
                            intent.putExtra("seller_name", payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name());
                            intent.putExtra("seller_id", payPalResponse.getSeller_id());
                            intent.putExtra("created_date", payPalResponse.getCreated_date());
                            intent.putExtra("checkin_time", payPalResponse.getCheckin_time());
                            intent.putExtra("start_date", payPalResponse.getStart_date());
                            intent.putExtra("is_scheduled", payPalResponse.getIs_scheduled());
                            intent.putExtra("checkout_time", payPalResponse.getCheckout_time());
                            intent.putExtra("order_id", payPalResponse.getOrder_id());
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                        } else {
                            Intent intent = new Intent(OrderSummeryActivity.this, ExtraPaymentDetailActivity.class);
                            intent.putExtra("req_id", getIntent().getStringExtra("req_id"));
                            startActivity(intent);
                        }

                    } else if (payPalResponse.getStatus().equals("5") || payPalResponse.getStatus().equals("6")) {
                        Intent intent = new Intent(OrderSummeryActivity.this, TrackSellerActivity.class);
                        intent.putExtra("summary", payPalResponse);
                        startActivity(intent);
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    }
                } else {
                    if (payPalResponse.getStatus().equals("3")) {
                        Intent intent1 = new Intent(OrderSummeryActivity.this, TrackJobStartedActivity.class);
                        intent1.putExtra("req_id", payPalResponse.getRequest_id());
                        intent1.putExtra("seller_name", payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name());
                        intent1.putExtra("seller_id", payPalResponse.getSeller_id());
                        intent1.putExtra("created_date", payPalResponse.getCreated_date());
                        intent1.putExtra("checkin_time", payPalResponse.getCheckin_time());
                        intent1.putExtra("start_date", payPalResponse.getStart_date());
                        intent1.putExtra("is_scheduled", payPalResponse.getIs_scheduled());
                        intent1.putExtra("order_id", payPalResponse.getOrder_id());
                        intent1.putExtra("package_name", payPalResponse.getPackage_name());
                        intent1.putExtra("normal_charges", payPalResponse.getNormal_charges());
                        intent1.putExtra("additional_charges", payPalResponse.getAdditional_charges());
                        intent1.putExtra("post_title", payPalResponse.getPost_title());
                        intent1.putExtra("request_type", payPalResponse.getRequest_type());
                        startActivity(intent1);
                        finish();
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    } else if (payPalResponse.getStatus().equals("4") || payPalResponse.getStatus().equals("7")) {
                        Intent intent = new Intent(OrderSummeryActivity.this, PostCommentByBuyerActivity.class);
                        intent.putExtra("req_id", payPalResponse.getRequest_id());
                        intent.putExtra("seller_name", payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name());
                        intent.putExtra("seller_id", payPalResponse.getSeller_id());
                        intent.putExtra("created_date", payPalResponse.getCreated_date());
                        intent.putExtra("checkin_time", payPalResponse.getCheckin_time());
                        intent.putExtra("start_date", payPalResponse.getStart_date());
                        intent.putExtra("is_scheduled", payPalResponse.getIs_scheduled());
                        intent.putExtra("checkout_time", payPalResponse.getCheckout_time());
                        intent.putExtra("order_id", payPalResponse.getOrder_id());
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    } else if (payPalResponse.getStatus().equals("5") || payPalResponse.getStatus().equals("6")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                        Date departure_date_selected = null;
                        try {
                            departure_date_selected = sdf.parse(payPalResponse.getStart_date());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (departure_date_selected.compareTo(current_date) == 0) {
                            String jobSstartTime = payPalResponse.getStart_time().replace(":", "");
                            Calendar c = Calendar.getInstance();
                            int seconds = c.get(Calendar.SECOND);
                            int minutes = c.get(Calendar.MINUTE);
                            int hour = c.get(Calendar.HOUR_OF_DAY);
                            String currenttime = String.valueOf(hour) + String.valueOf(minutes);

                            // if remaining time is more than 1 hours then he will go to calendar screen
                            if (Integer.parseInt(jobSstartTime) - Integer.parseInt(currenttime) < 100) {
                                Intent intent = new Intent(OrderSummeryActivity.this, TrackSellerActivity.class);
                                intent.putExtra("summary", payPalResponse);
                                startActivity(intent);
                                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            }
                            // if time is less than 2 hours then he can track location of seller
                            else {
                                Intent intent = new Intent(OrderSummeryActivity.this, CalendarActivity.class);
                                intent.putExtra("event_date", payPalResponse.getStart_date());
                                startActivity(intent);
                                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            }
                        } else {
                            Intent intent = new Intent(OrderSummeryActivity.this, CalendarActivity.class);
                            intent.putExtra("event_date", payPalResponse.getStart_date());
                            startActivity(intent);
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                        }
                    }
                }
            }
        });

        // if this is request now service
        if (payPalResponse.getIs_scheduled().equals("0")) {
            if (payPalResponse.getStatus().equals("3")) {
                if (!Utils.isConfigRtl(OrderSummeryActivity.this))
                    track_btn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null, null, null);
                else
                    track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null);
                track_btn.setText(getResources().getString(R.string.track_work));
            } else if (payPalResponse.getStatus().equals("4") || payPalResponse.getStatus().equals("7")) {
                if (payPalResponse.getExtra_amount().equals("0")) {
                    if (!Utils.isConfigRtl(OrderSummeryActivity.this))
                        track_btn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null, null, null);
                    else
                        track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null);

                    if (payPalResponse.getIs_ratted().equals("0"))
                        track_btn.setText(getResources().getString(R.string.rate) + " " + payPalResponse.getFirst_name());
                    else if (payPalResponse.getIs_ratted().equals("1"))
                        track_btn.setText(getResources().getString(R.string.see_rating));
                    else if (payPalResponse.getIs_ratted().equals("2"))
                        track_btn.setVisibility(View.GONE);
                } else {
                    order_status.setText(getResources().getString(R.string.pending_payment));
                    if (savePref.getUserType().equals("1"))
                        track_btn.setBackground(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.place_order_seller_drawable));
                    else
                        track_btn.setBackground(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.place_order_drawable));
                    track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    track_btn.setText(getResources().getString(R.string.extra_payment_details));
                    track_btn.setGravity(Gravity.CENTER);
                }
            } else if (payPalResponse.getStatus().equals("5") || payPalResponse.getStatus().equals("6")) {
                if (!Utils.isConfigRtl(OrderSummeryActivity.this))
                    track_btn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null, null, null);
                else
                    track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null);
                track_btn.setText(getResources().getString(R.string.track) + " " + payPalResponse.getFirst_name());
            }
        }// if this is scheduled service
        else {
            if (payPalResponse.getStatus().equals("3")) {
                if (!Utils.isConfigRtl(OrderSummeryActivity.this))
                    track_btn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null, null, null);
                else
                    track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null);
                track_btn.setText(getResources().getString(R.string.track_work));
            } else if (payPalResponse.getStatus().equals("4") || payPalResponse.getStatus().equals("7")) {

                if (payPalResponse.getExtra_amount().equals("0")) {
                    if (!Utils.isConfigRtl(OrderSummeryActivity.this))
                        track_btn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null, null, null);
                    else
                        track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null);

                    if (payPalResponse.getIs_ratted().equals("0"))
                        track_btn.setText(getResources().getString(R.string.rate) + " " + payPalResponse.getFirst_name());
                    else if (payPalResponse.getIs_ratted().equals("1"))
                        track_btn.setText(getResources().getString(R.string.see_rating));
                    else if (payPalResponse.getIs_ratted().equals("2"))
                        track_btn.setVisibility(View.GONE);
                } else {
                    order_status.setText(getResources().getString(R.string.pending_payment));
                    if (savePref.getUserType().equals("1"))
                        track_btn.setBackground(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.place_order_seller_drawable));
                    else
                        track_btn.setBackground(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.place_order_drawable));
                    track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    track_btn.setText(getResources().getString(R.string.extra_payment_details));
                    track_btn.setGravity(Gravity.CENTER);
                }

            } else if (payPalResponse.getStatus().equals("5") || payPalResponse.getStatus().equals("6")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date departure_date_selected = null;
                try {
                    departure_date_selected = sdf.parse(payPalResponse.getStart_date());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (departure_date_selected.compareTo(current_date) == 0) {
                    String jobSstartTime = payPalResponse.getStart_time().replace(":", "");
                    Calendar c = Calendar.getInstance();
                    int seconds = c.get(Calendar.SECOND);
                    int minutes = c.get(Calendar.MINUTE);
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    String currenttime = String.valueOf(hour) + String.valueOf(minutes);

                    // if remaining time is more than 1 hour then he cannot track location of seller
                    if (Integer.parseInt(jobSstartTime) - Integer.parseInt(currenttime) > 100) {
                        if (!Utils.isConfigRtl(OrderSummeryActivity.this))
                            track_btn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.calendar_white), null, null, null);
                        else
                            track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.calendar_white), null);
                        track_btn.setText(getResources().getString(R.string.calender));
                    } else {
                        if (!Utils.isConfigRtl(OrderSummeryActivity.this))
                            track_btn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null, null, null);
                        else
                            track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time), null);

                        track_btn.setText(getResources().getString(R.string.track) + " " + payPalResponse.getFirst_name() + " " + payPalResponse.getLast_name());
                    }
                } else {

                    if (!Utils.isConfigRtl(OrderSummeryActivity.this))
                        track_btn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.calendar_white), null, null, null);
                    else
                        track_btn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.calendar_white), null);
                    track_btn.setText(getResources().getString(R.string.calender));
                }
            }

            /*if (payPalResponse.getStatus().equals("3")) {
                watch.setImageDrawable(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time));
                track_btn.setText("Track Work");
            } else if (payPalResponse.getStatus().equals("4")) {
                watch.setImageDrawable(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time));
                track_btn.setText("Rate " + payPalResponse.getFirst_name());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date departure_date_selected = null;
                try {
                    departure_date_selected = sdf.parse(payPalResponse.getStart_date());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (departure_date_selected.compareTo(current_date) == 0) {
                    String jobSstartTime = payPalResponse.getStart_time().replace(":", "");
                    Calendar c = Calendar.getInstance();
                    int seconds = c.get(Calendar.SECOND);
                    int minutes = c.get(Calendar.MINUTE);
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    String currenttime = String.valueOf(hour) + String.valueOf(minutes);

                    // if remaining time is more than 2 hours then he cannot track location of seller
                    if (Integer.parseInt(jobSstartTime) - Integer.parseInt(currenttime) > 200) {
                        watch.setImageDrawable(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.calendar_white));
                        track_btn.setText("Calendar");
                    } else {
                        watch.setImageDrawable(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.time));
                        track_btn.setText("Track " + payPalResponse.getFirst_name());
                    }

                } else {

                    watch.setImageDrawable(ContextCompat.getDrawable(OrderSummeryActivity.this, R.drawable.calendar_white));
                    track_btn.setText("Calendar");
                }
            }*/
        }
    }

    private void getOrderSummaryApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(OrderSummeryActivity.this);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.REQUEST_ID, getIntent().getStringExtra("req_id"));
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        Log.e("value", "here " + getIntent().getStringExtra("req_id") + " " + savePref.getUserId());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(OrderSummeryActivity.this, AllOmorniApis.ORDER_SUMMARY, formBody) {
            @Override
            public void getValueParse(String result) {
                Log.e("order", "result " + result);
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            Log.e("result", "buyer " + result);
//                            Utils.showToast(OrderSummerySellerActivity.this, status.getString("message"));
                            payPalResponse = new PayPalResponse();
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            payPalResponse.setRequest_id(jsonObject1.getString("id"));
                            payPalResponse.setOrder_id(jsonObject1.getString("order_id"));
                            payPalResponse.setBuyer_id(jsonObject1.getString("buyer_id"));
                            payPalResponse.setSeller_id(jsonObject1.getString("seller_id"));
                            payPalResponse.setPackage_id(jsonObject1.getString("package_id"));
                            payPalResponse.setReq_la(jsonObject1.getString("req_lat"));
                            payPalResponse.setReq_lng(jsonObject1.getString("req_lng"));
                            payPalResponse.setStatus(jsonObject1.getString("status"));
                            payPalResponse.setCreated_date(jsonObject1.getString("created_date"));
                            payPalResponse.setFirst_name(jsonObject1.getString("first_name"));
                            payPalResponse.setLast_name(jsonObject1.getString("last_name"));
                            payPalResponse.setMobile(jsonObject1.getString("mobile"));
                            payPalResponse.setSeller_latitude(jsonObject1.getString("seller_latitude"));
                            payPalResponse.setSeller_longitude(jsonObject1.getString("seller_longitude"));
                            payPalResponse.setBuyer_name(jsonObject1.getString("buyer_name"));
                            payPalResponse.setBuyer_mobile(jsonObject1.getString("buyer_mobile"));
                            payPalResponse.setReq_location(jsonObject1.getString("req_location"));

                            // means buyer request seller via selecting package

                            payPalResponse.setNormal_charges(jsonObject1.getString("normal_charges"));
                            payPalResponse.setAdditional_charges(jsonObject1.getString("additional_charges"));
                            payPalResponse.setPackage_name(jsonObject1.getString("package_name"));

                            // means buyer accepted qutation of seller

                            payPalResponse.setPrice(jsonObject1.getString("price"));
                            payPalResponse.setRequest_type(jsonObject1.getString("request_type"));


                            payPalResponse.setTotal_amounts(jsonObject1.getString("total_amounts"));
                            payPalResponse.setIs_scheduled(jsonObject1.getString("is_scheduled"));
                            payPalResponse.setStart_date(jsonObject1.getString("start_date"));
                            payPalResponse.setStart_time(jsonObject1.getString("start_time"));

                            payPalResponse.setMain_amount(jsonObject1.getString("main_amount"));
                            payPalResponse.setProcessing_fees(jsonObject1.getString("processing_fees"));
                            payPalResponse.setProcessing_fees_ammount(jsonObject1.getString("processing_fees_ammount"));
                            payPalResponse.setThread_id(jsonObject1.getString("thread_id"));
                            payPalResponse.setPayment_method(jsonObject1.getString("payment_method"));
                            payPalResponse.setCategory(jsonObject1.getString("category"));

                            payPalResponse.setCheckin_time(jsonObject1.getString("checkin_time"));
                            payPalResponse.setCheckout_time(jsonObject1.getString("checkout_time"));

                            payPalResponse.setSeller_address(jsonObject1.getString("seller_address"));
                            payPalResponse.setBuyer_address(jsonObject1.getString("buyer_address"));
                            payPalResponse.setIs_ratted(jsonObject1.getString("is_ratted"));
                            payPalResponse.setExtra_hours(jsonObject1.getString("extra_hours"));
                            payPalResponse.setExtra_amount(jsonObject1.getString("extra_amount"));
                            payPalResponse.setPost_title(jsonObject1.getString("post_title"));

                            payPalResponse.setVat_number(jsonObject1.getString("vat_number"));
                            payPalResponse.setVat_tax(jsonObject1.getString("vat_tax"));
                            payPalResponse.setVat_tax_ammount(jsonObject1.getString("vat_tax_ammount"));

                            setData();

                        } else {
                            Utils.showToast(OrderSummeryActivity.this, status.getString("message"));
                        }
                    } catch (JSONException ex) {

                        ex.printStackTrace();
                    } catch (Exception ex) {

                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

}
