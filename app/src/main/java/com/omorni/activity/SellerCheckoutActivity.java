package com.omorni.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.utils.SavePref;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SellerCheckoutActivity extends BaseActivity {
    private Toolbar toolbar;
    private TextView toolbar_title, job_started_time, seller_name, button, extra_duration, package_duration_text, package_duration;
    SavePref savePref;
    private TextView order_number, seller_working, basic_value, text_basic, job_duration;
    private Context context;
    private TextView post_job_title, post_job;
    private LinearLayout package_duration_layout, extra_duration_layout, basic_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SellerCheckoutActivity.this;
        savePref = new SavePref(context);
        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_seller_checkout_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_seller_checkout);
        }
        setToolbar();
        initialize();
        setData();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.track_work);
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
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }


    private void initialize() {
        job_started_time = (TextView) findViewById(R.id.job_started_time);
        seller_name = (TextView) findViewById(R.id.seller_name);
        order_number = (TextView) findViewById(R.id.order_number);
        seller_working = (TextView) findViewById(R.id.seller_working);
        basic_value = (TextView) findViewById(R.id.basic_value);
        text_basic = (TextView) findViewById(R.id.text_basic);
        button = (TextView) findViewById(R.id.button);
        extra_duration = (TextView) findViewById(R.id.extra_duration);
        package_duration_text = (TextView) findViewById(R.id.package_duration_text);
        package_duration = (TextView) findViewById(R.id.package_duration);
        job_duration = (TextView) findViewById(R.id.job_duration);
        post_job_title = (TextView) findViewById(R.id.post_job_title);
        post_job = (TextView) findViewById(R.id.post_job);
        package_duration_layout = (LinearLayout) findViewById(R.id.package_duration_layout);
        extra_duration_layout = (LinearLayout) findViewById(R.id.extra_duration_layout);
        basic_layout = (LinearLayout) findViewById(R.id.basic_layout);

    }

    private void setData() {

        long diff = printDifferencee(Long.parseLong(getIntent().getStringExtra("checkout_time")), Long.parseLong(getIntent().getStringExtra("checkin_time")));
        diff = diff * 1000;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        final String time = df.format(new Date(diff));
        job_duration.setText(time);
        job_started_time.setText(time);

        seller_working.setText(getIntent().getStringExtra("seller_name") + " " + getResources().getString(R.string.complete_job));
        basic_value.setText(getIntent().getStringExtra("normal_charges"));
        package_duration_text.setText(getIntent().getStringExtra("package_name") + " " + getResources().getString(R.string.package_duration));
        if (getIntent().getStringExtra("main_hours").equals("1")) {
            package_duration.setText("01:00:00");
        } else if (getIntent().getStringExtra("main_hours").equals("2")) {
            package_duration.setText("02:00:00");
        } else if (getIntent().getStringExtra("main_hours").equals("3")) {
            package_duration.setText("03:00:00");
        }
        text_basic.setText(getIntent().getStringExtra("package_name"));
        order_number.setText(getResources().getString(R.string.your_order) + " " + getIntent().getStringExtra("order_id") + " " + getResources().getString(R.string.is_now_completed));


        if (getIntent().getStringExtra("request_type").equals("0")) {
            post_job_title.setVisibility(View.GONE);
            post_job.setVisibility(View.GONE);
            basic_layout.setVisibility(View.VISIBLE);
            package_duration_layout.setVisibility(View.VISIBLE);
            extra_duration_layout.setVisibility(View.VISIBLE);
            if (!getIntent().getStringExtra("extra_hours").equals("")) {
                if (getIntent().getStringExtra("extra_hours").equals("0")) {
                    button.setText(getResources().getString(R.string.rate_seller));
                    extra_duration.setText("00:00:00");
                } else {
                    String hours = "", min = "", sec = "";
//            Long extra_time = Long.parseLong(job_duration.getText().toString()) - Long.parseLong(extra_duration.getText().toString());
                    String extra_time = String.valueOf(Long.parseLong(job_duration.getText().toString().replace(":", "")) - Long.parseLong(package_duration.getText().toString().replace(":", "")));

                    if (extra_time.length() > 5) {
                        hours = extra_time.substring(0, 2);
                        min = extra_time.substring(2, 4);
                        sec = extra_time.substring(4, extra_time.length());
                    } else if (extra_time.length() == 5) {
                        hours = extra_time.substring(0, 1);
                        hours = "0" + hours;
                        min = extra_time.substring(1, 3);
                        sec = extra_time.substring(3, extra_time.length());
                    } else if (extra_time.length() == 4) {
                        hours = "00";
                        min = extra_time.substring(0, 2);
                        sec = extra_time.substring(2, extra_time.length());
                    } else if (extra_time.length() == 3) {
                        hours = "00";
                        min = extra_time.substring(0, 1);
                        min = "0" + min;
                        sec = extra_time.substring(1, extra_time.length());
                    } else if (extra_time.length() == 2) {
                        hours = "00";
                        min = "00";
                        sec = extra_time.substring(0, extra_time.length());
                    }

                /*if (extra_time.length() > 5) {
                    hours = extra_time.substring(0, 2);
                    min = extra_time.substring(2, 4);
                    sec = extra_time.substring(4, extra_time.length());
                } else {
                    hours = extra_time.substring(0, 1);
                    hours = "0" + hours;
                    min = extra_time.substring(1, 3);
                    sec = extra_time.substring(3, extra_time.length());
                }*/
                    button.setText(getResources().getString(R.string.extra_payment_details));
                    extra_duration.setText(hours + ":" + min + ":" + sec);
                }
            } else {
                button.setText(getResources().getString(R.string.rate_seller));
                extra_duration.setText("00:00:00");
            }

        } else {
            button.setText(getResources().getString(R.string.rate_seller));
            post_job_title.setVisibility(View.VISIBLE);
            post_job.setVisibility(View.VISIBLE);
            basic_layout.setVisibility(View.GONE);
            package_duration_layout.setVisibility(View.GONE);
            extra_duration_layout.setVisibility(View.GONE);
            post_job_title.setText(getIntent().getStringExtra("post_title"));
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getIntent().getStringExtra("request_type").equals("0")) {
                    if (!getIntent().getStringExtra("extra_hours").equals("")) {
                        if (getIntent().getStringExtra("extra_hours").equals("0")) {
                            Intent intent = new Intent(context, PostCommentByBuyerActivity.class);
                            intent.putExtra("req_id", getIntent().getStringExtra("req_id"));
                            intent.putExtra("seller_name", getIntent().getStringExtra("seller_name"));
                            intent.putExtra("seller_id", getIntent().getStringExtra("seller_id"));
                            intent.putExtra("created_date", getIntent().getStringExtra("created_date"));
                            intent.putExtra("checkin_time", getIntent().getStringExtra("checkin_time"));
                            intent.putExtra("start_date", getIntent().getStringExtra("start_date"));
                            intent.putExtra("is_scheduled", getIntent().getStringExtra("is_scheduled"));
                            intent.putExtra("checkout_time", getIntent().getStringExtra("checkout_time"));
                            intent.putExtra("order_id", getIntent().getStringExtra("order_id"));
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                        } else {
                            Intent intent = new Intent(context, ExtraPaymentDetailActivity.class);
                            intent.putExtra("req_id", getIntent().getStringExtra("req_id"));
                            startActivity(intent);
                        }
                    }else{
                        Intent intent = new Intent(context, PostCommentByBuyerActivity.class);
                        intent.putExtra("req_id", getIntent().getStringExtra("req_id"));
                        intent.putExtra("seller_name", getIntent().getStringExtra("seller_name"));
                        intent.putExtra("seller_id", getIntent().getStringExtra("seller_id"));
                        intent.putExtra("created_date", getIntent().getStringExtra("created_date"));
                        intent.putExtra("checkin_time", getIntent().getStringExtra("checkin_time"));
                        intent.putExtra("start_date", getIntent().getStringExtra("start_date"));
                        intent.putExtra("is_scheduled", getIntent().getStringExtra("is_scheduled"));
                        intent.putExtra("checkout_time", getIntent().getStringExtra("checkout_time"));
                        intent.putExtra("order_id", getIntent().getStringExtra("order_id"));
                        startActivity(intent);
                        finish();
                    }

                } else {
                    Intent intent = new Intent(context, PostCommentByBuyerActivity.class);
                    intent.putExtra("req_id", getIntent().getStringExtra("req_id"));
                    intent.putExtra("seller_name", getIntent().getStringExtra("seller_name"));
                    intent.putExtra("seller_id", getIntent().getStringExtra("seller_id"));
                    intent.putExtra("created_date", getIntent().getStringExtra("created_date"));
                    intent.putExtra("checkin_time", getIntent().getStringExtra("checkin_time"));
                    intent.putExtra("start_date", getIntent().getStringExtra("start_date"));
                    intent.putExtra("is_scheduled", getIntent().getStringExtra("is_scheduled"));
                    intent.putExtra("checkout_time", getIntent().getStringExtra("checkout_time"));
                    intent.putExtra("order_id", getIntent().getStringExtra("order_id"));
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
            }
        });
    }

    public static long printDifferencee(long endtimestamp, long starttimestamp) {
        //milliseconds
        Date endDate = new Date(endtimestamp * 1000);
        Date startDate = new Date(starttimestamp * 1000);
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays,
                elapsedHours, elapsedMinutes, elapsedSeconds);

        return elapsedSeconds + (elapsedMinutes * 60) + (elapsedHours * 60 * 60) + (elapsedDays * 24 * 60 * 60);
    }
}
