package com.omorni.activity;

import android.app.NotificationManager;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.model.PayPalResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class SellerWorkInProgressActivity extends BaseActivity {

    private Toolbar toolbar;
    private TextView toolbar_title, job_started_time;
    private LinearLayout check_out;
    PayPalResponse payPalResponse;
    long millisUntilFinished = 0;
    Timer timer;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeSeller);
        setContentView(R.layout.activity_seller_job_inprogress);
        setToolbar();
        initialize();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTagSeller");
        wakeLock.acquire();
//        Utils.showToast(SellerWorkInProgressActivity.this, payPalResponse.getCheckin_time());
        millisUntilFinished = printDifferencee(Long.parseLong(payPalResponse.getCheckin_time()));
//        millisUntilFinished = millisUntilFinished-4000;
        startTimer();

        /*// if already checkin and work is in progress
        if (payPalResponse.getStatus().equals("3")) {
            millisUntilFinished = printDifferencee(Long.parseLong(payPalResponse.getCheckin_time()));
            startTimer();
        } else {
            startWorkApi();
        }*/
    }

    private void setToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);

        toolbar_title.setText(R.string.work_in_progress);
    }


    private void initialize() {
        payPalResponse = getIntent().getParcelableExtra("summary");
        job_started_time = (TextView) findViewById(R.id.job_started_time);
        check_out = (LinearLayout) findViewById(R.id.check_out);

        check_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null)
                    timer.cancel();
                checkOutApi();
            }
        });

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
        super.onBackPressed();
        Intent intent = new Intent(SellerWorkInProgressActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    private void startWorkApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(SellerWorkInProgressActivity.this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.REQUEST_ID, payPalResponse.getRequest_id());
        formBuilder.add(AllOmorniParameters.SELLER_ID, payPalResponse.getSeller_id());
        formBuilder.add(AllOmorniParameters.BUYER_ID, payPalResponse.getBuyer_id());
        formBuilder.add(AllOmorniParameters.CHECK_IN_TIME, String.valueOf(System.currentTimeMillis() / 1000));
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(SellerWorkInProgressActivity.this, AllOmorniApis.CHECK_IN, formBody) {
            @Override
            public void getValueParse(String result) {
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
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
//                            payPalResponse.setStatus("3");

                            millisUntilFinished = printDifferencee(Long.parseLong(jsonObject1.getString("checkin_time")));
                            startTimer();
                        } else {
                            Utils.showToast(SellerWorkInProgressActivity.this, status.getString("message"));
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

    public static long printDifferencee(long timeStamp) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        //milliseconds
        Date endDate = new Date(cal.getTimeInMillis());
        Date startDate = new Date(timeStamp * 1000);
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

    private void checkOutApi() {

        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();

        final ProgressDialog progressDialog = Utils.initializeProgress(SellerWorkInProgressActivity.this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.REQUEST_ID, payPalResponse.getRequest_id());
        formBuilder.add(AllOmorniParameters.SELLER_ID, payPalResponse.getSeller_id());
        formBuilder.add(AllOmorniParameters.BUYER_ID, payPalResponse.getBuyer_id());
        formBuilder.add(AllOmorniParameters.CHECK_OUT_TIME, String.valueOf(System.currentTimeMillis() / 1000));
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(SellerWorkInProgressActivity.this, AllOmorniApis.CHECK_OUT, formBody) {
            @Override
            public void getValueParse(String result) {
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
                            Intent intent = new Intent(SellerWorkInProgressActivity.this, PostCommentBySellerActivity.class);
                            intent.putExtra("summary", payPalResponse);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                        } else {
                            Utils.showToast(SellerWorkInProgressActivity.this, status.getString("message"));

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

    private void startTimer() {
        millisUntilFinished = (millisUntilFinished * 1000);
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    millisUntilFinished = millisUntilFinished + 1000;
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                    df.setTimeZone(tz);
                    final String time = df.format(new Date(millisUntilFinished));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            job_started_time.setText(time);
                        }
                    });
                }
            }, 0, 1000 * 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        if (timer != null)
            timer.cancel();
    }
}
