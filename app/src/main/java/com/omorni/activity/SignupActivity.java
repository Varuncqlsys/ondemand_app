package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.omorni.R;
import com.omorni.adapter.CountryAdapter;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.ParameterClass;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class SignupActivity extends BaseActivity implements View.OnClickListener {
    private Context context;
    private SavePref savePref;
    private EditText firstname_edittext, last_edittext, email_edittext, mobile_edittext, password_edittext, conf_password_edittext;
    private LinearLayout main_layout;
    ArrayList<JsonObject> countryList;
    private Spinner country_spinner1;
    String terms = "";
    private TextView country_code_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SignupActivity.this;
        if (Utils.isConfigRtl(SignupActivity.this)) {
            setContentView(R.layout.activity_signup_rtl);
        } else {
            setContentView(R.layout.activity_signup);
        }
        if (ConnectivityReceiver.isConnected()) {
            getLegalData();
        } else {
            Utils.showToast(SignupActivity.this, getResources().getString(R.string.internet_error));
        }
        initialize();
        getDeviceSimCOuntry();
    }

    private void initialize() {

        savePref = new SavePref(context);
        Button signup_button = (Button) findViewById(R.id.signup_button);
        TextView terms = (TextView) findViewById(R.id.terms);
        main_layout = (LinearLayout) findViewById(R.id.main_layout);
        firstname_edittext = (EditText) findViewById(R.id.firstname_edittext);
        last_edittext = (EditText) findViewById(R.id.last_edittext);
        email_edittext = (EditText) findViewById(R.id.email_edittext);
        mobile_edittext = (EditText) findViewById(R.id.mobile_edittext);
        password_edittext = (EditText) findViewById(R.id.password_edittext);
        conf_password_edittext = (EditText) findViewById(R.id.conf_password_edittext);
        country_code_text = (TextView) findViewById(R.id.country_code_text);
//        country_spinner = (Spinner) findViewById(R.id.country_spinner);
//        country_spinner.setBackground(getResources().getDrawable(R.drawable.bg2));
//        country_spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        signup_button.setOnClickListener(this);
        terms.setOnClickListener(this);
        country_code_text.setOnClickListener(this);
    }

    public void getDeviceSimCOuntry() {
        TextView text = null;
        Dialog dialog = null;
        countryList = Utils.getCountries(context);
        /*CountryAdapter dataAdapter = new CountryAdapter(context, countryList, text, dialog);
        country_spinner.setAdapter(dataAdapter);*/

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        String CountryID = manager.getSimCountryIso().toUpperCase();

        int selected = 0;

        for (int i = 0; i < countryList.size(); i++) {
            String iso = countryList.get(i).get("iso").getAsString();
            if (CountryID.equalsIgnoreCase(iso)) {
                selected = i;
                country_code_text.setText("(" + countryList.get(i).get("iso").getAsString() + ")" + " +" + countryList.get(i).get("code").getAsString());
                break;
            }
        }
        /*country_spinner.setSelection(selected);

        country_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final TextView name = (TextView) view.findViewById(R.id.name);
                final TextView code = (TextView) view.findViewById(R.id.code);
                name.setBackgroundResource(android.R.color.transparent);
                name.setTextColor(Color.WHITE);
                code.setBackgroundResource(android.R.color.transparent);
                code.setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGpsOn())
            showGPSDisabledAlertToUser();
    }

    private boolean isGpsOn() {
        boolean isGpsOn = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGpsOn = false;

        } else {
            isGpsOn = true;
        }
        return isGpsOn;
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(getResources().getString(R.string.gps_disable))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.turn_on),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                                dialog.cancel();
                            }
                        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void signupAPI(String firstname, String lastname, String email, String mobile, final Dialog dialog) {
        String app_language= "";
        if(SavePref.getString(context, "app_language", "en").equals("en"))
            app_language = "0";
        else
            app_language = "1";

        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.FIRST_NAME, firstname);
        formBuilder.add(AllOmorniParameters.LAST_NAME, lastname);
        formBuilder.add(AllOmorniParameters.EMAIL, email);
        formBuilder.add(AllOmorniParameters.MOBILE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()) + mobile);
//        formBuilder.add(AllOmorniParameters.PASSWORD, password);
        formBuilder.add(AllOmorniParameters.MOBILE_NO, mobile);
        formBuilder.add(AllOmorniParameters.MOBILE_CODE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()));
        formBuilder.add(AllOmorniParameters.DEVICE_TYPE, "0");
        formBuilder.add(AllOmorniParameters.DEVICE_TOKEN, savePref.getString(context, "token", ""));
        formBuilder.add(AllOmorniParameters.LATTITUDE, savePref.getLat());
        formBuilder.add(AllOmorniParameters.LONGITUDE, savePref.getLong());
        formBuilder.add(AllOmorniParameters.APP_LANG, app_language);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.REGISTER_URL, formBody) {
            @Override
            public void getValueParse(String result) {
                Log.e("result", "heer " + result);
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    dialog.dismiss();
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
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");

                            Intent intent = new Intent(context, OtpActivity.class);
                            intent.putExtra("user_id", jsonObjectBody.getString("id"));
                            intent.putExtra("from_signup", true);
                            startActivity(intent);
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);


//                            finish();

                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showSnackBar(main_layout, getResources().getString(R.string.internet_error), context);
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    private void checkSignupAPI(String firstname, String lastname, String email, String mobile) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.FIRST_NAME, firstname);
        formBuilder.add(AllOmorniParameters.LAST_NAME, lastname);
        formBuilder.add(AllOmorniParameters.EMAIL, email);
        formBuilder.add(AllOmorniParameters.MOBILE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()) + mobile);
//        formBuilder.add(AllOmorniParameters.PASSWORD, password);
        formBuilder.add(AllOmorniParameters.MOBILE_NO, mobile);
        formBuilder.add(AllOmorniParameters.MOBILE_CODE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()));
        formBuilder.add(AllOmorniParameters.DEVICE_TYPE, "0");
        formBuilder.add(AllOmorniParameters.DEVICE_TOKEN, savePref.getString(context, "token", ""));
        formBuilder.add(AllOmorniParameters.LATTITUDE, savePref.getLat());
        formBuilder.add(AllOmorniParameters.LONGITUDE, savePref.getLong());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.CHECK_SIGNUP, formBody) {
            @Override
            public void getValueParse(String result) {
                Log.e("result", "heer " + result);
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
//                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            if (ConnectivityReceiver.isConnected())
                                openTermDialog(true);


//                            finish();

                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showSnackBar(main_layout, getResources().getString(R.string.internet_error), context);
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_button:
                String email_value = email_edittext.getText().toString().trim();

                if (ConnectivityReceiver.isConnected()) {
                    if (firstname_edittext.getText().toString().isEmpty()) {
                        Utils.showSnackBar(main_layout, getResources().getString(R.string.error_firstname), context);
                    } else if (last_edittext.getText().toString().isEmpty()) {
                        Utils.showSnackBar(main_layout, getResources().getString(R.string.error_lastname), context);
                    } else if (email_value.isEmpty()) {
                        Utils.showSnackBar(main_layout, getResources().getString(R.string.error_email), context);
                    } else if (!Utils.isValidEmail(email_value)) {
                        Utils.showSnackBar(main_layout, getResources().getString(R.string.error_valid_email), context);
                    } else if (mobile_edittext.getText().toString().isEmpty()) {
                        Utils.showSnackBar(main_layout, getResources().getString(R.string.error_phone), context);
                    } else {
                        checkSignupAPI(firstname_edittext.getText().toString(), last_edittext.getText().toString(), email_value, mobile_edittext.getText().toString());
//                        signupAPI(firstname_edittext.getText().toString(), last_edittext.getText().toString(), email_value, mobile_edittext.getText().toString());
                    }
                } else {
                    Toast.makeText(context, getResources().getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.terms:
                openTermDialog(false);
                break;
            case R.id.country_code_text:
                openCountryListDialog();
                break;
        }
    }

    public void openTermDialog(boolean is_show_bottom) {
        final Dialog dialog = new Dialog(SignupActivity.this, R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.terms);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        WebView webView = (WebView) dialog.findViewById(R.id.webView);
        LinearLayout bottom_layout = (LinearLayout) dialog.findViewById(R.id.bottom_layout);
        if (is_show_bottom)
            bottom_layout.setVisibility(View.VISIBLE);
        else
            bottom_layout.setVisibility(View.GONE);

        ImageView back = (ImageView) dialog.findViewById(R.id.back);

        TextView agree = (TextView) dialog.findViewById(R.id.agree);
        TextView disagree = (TextView) dialog.findViewById(R.id.disagree);

        final TextView htmlTextView = (TextView) dialog.findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            htmlTextView.setText(Html.fromHtml(terms, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM));
        else
            htmlTextView.setText(Html.fromHtml(terms));

//        webView.loadDataWithBaseURL(null, terms, "text/html", "utf-8", null);
//        webView.setVerticalScrollBarEnabled(false);
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setDisplayZoomControls(false);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectivityReceiver.isConnected())
                    signupAPI(firstname_edittext.getText().toString(), last_edittext.getText().toString(), email_edittext.getText().toString(), mobile_edittext.getText().toString(), dialog);
            }
        });

        disagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void getLegalData() {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(ParameterClass.LANGUAGE_TYPE, SavePref.getString(SignupActivity.this, "app_language", "en"));
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
                            terms = jsonObjectBody.getString("content");
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

    private void openCountryListDialog() {
        Collections.sort(countryList, new Comparator<JsonObject>() {
            @Override
            public int compare(JsonObject jsonObject, JsonObject t1) {
                return jsonObject.get("name").getAsString().compareTo(t1.get("name").getAsString());
            }
        });

        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.dialog_country_spinner);
        dialog.setCanceledOnTouchOutside(true);
        CountryAdapter dataAdapter = new CountryAdapter(context, countryList, country_code_text, dialog);

        ListView listView = (ListView) dialog.findViewById(R.id.listview);
        listView.setAdapter(dataAdapter);

        dialog.show();
    }

}
