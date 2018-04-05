package com.omorni.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.utils.SavePref;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SellerWorkDoneActivity extends BaseActivity {
    private Toolbar toolbar;
    private TextView toolbar_title, job_started_time, rate_seller;
    private SavePref savePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(SellerWorkDoneActivity.this);

        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_seller_job_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_seller_job);
        }

        setToolbar();
        initialize();
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

    private void initialize() {
        rate_seller = (TextView) findViewById(R.id.rate_seller);
        job_started_time = (TextView) findViewById(R.id.job_started_time);

        long diff = printDifferencee(Long.parseLong(getIntent().getStringExtra("checkout_time")), Long.parseLong(getIntent().getStringExtra("checkin_time")));
        diff = diff * 1000;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        final String time = df.format(new Date(diff));
        job_started_time.setText(time);

        rate_seller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellerWorkDoneActivity.this, PostCommentByBuyerActivity.class);
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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String req = getIntent().getStringExtra("req_id");
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
        Intent intent = new Intent(SellerWorkDoneActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
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
