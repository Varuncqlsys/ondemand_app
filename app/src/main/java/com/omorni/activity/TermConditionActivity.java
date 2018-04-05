package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.LocaleHelper;
import com.omorni.utils.ParameterClass;
import com.omorni.utils.SavePref;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by test on 6/23/2017.
 */

public class TermConditionActivity extends BaseActivity {
    SavePref savePref;
    TermConditionActivity context;
    WebView webView;
    TextView htmlTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        savePref = new SavePref(context);

        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.disclaimer_layout_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.disclaimer_layout);
        }

        setToolbar();

          htmlTextView = (TextView) findViewById(R.id.webView);


//        webView = (WebView) findViewById(R.id.webView);

        if (ConnectivityReceiver.isConnected())
            getLegalData();


        /*webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);*/
//        webView.getSettings().setDefaultFontSize(22);
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);

        toolbar_title.setText(R.string.terms_condition);
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
        if (getIntent().getBooleanExtra("from_push", false)) {
            Intent intent = new Intent(TermConditionActivity.this, MainActivity.class);
            startActivity(intent);
        }
        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }


    private void getLegalData() {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(ParameterClass.LANGUAGE_TYPE, SavePref.getString(TermConditionActivity.this, "app_language", "en"));
        if (savePref.getUserType().equals("1"))
            formBuilder.add(ParameterClass.TYPE, "1");
        else
            formBuilder.add(ParameterClass.TYPE, "0");

        RequestBody requestBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(context, AllOmorniApis.TERMS_CONDITION, requestBody) {
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
                Log.e("result", "here " + result);
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            String terms = jsonObjectBody.getString("content");

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                htmlTextView.setText(Html.fromHtml(terms, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM));
                            else
                                htmlTextView.setText(Html.fromHtml(terms));

//                            webView.loadDataWithBaseURL(null, terms, "text/html", "utf-8", null);
//                            webView.setVerticalScrollBarEnabled(false);
//                            webView.setHorizontalScrollBarEnabled(false);
//                            webView.getSettings().setBuiltInZoomControls(true);
//                            webView.getSettings().setDisplayZoomControls(false);
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
