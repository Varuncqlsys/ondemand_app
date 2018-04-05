package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class PhoneNumberActivity extends BaseActivity implements View.OnClickListener {
    private EditText mobile_edittext;
    private Context context;
    private Spinner country_spinner1;
    private Button submit_btn;
    ArrayList<JsonObject> countryList;
    private SavePref savePref;
    private TextView country_code_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);
        initialize();
        getDeviceSimCOuntry();
    }

    private void initialize() {
        context = PhoneNumberActivity.this;
        savePref = new SavePref(PhoneNumberActivity.this);
        mobile_edittext = (EditText) findViewById(R.id.mobile_edittext);

        submit_btn = (Button) findViewById(R.id.submit_btn);
        country_code_text = (TextView) findViewById(R.id.country_code_text);

        submit_btn.setOnClickListener(this);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_btn:
                if (ConnectivityReceiver.isConnected()) {
                    if (mobile_edittext.getText().toString().isEmpty()) {
                        Utils.showSnackBar(view, getString(R.string.error_phone), context);
                    } else {
                        uploadMobileNumberApi();
                    }
                } else {
                    Toast.makeText(context, getResources().getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.country_code_text:
                openCountryListDialog();
                break;
        }
    }

    private void uploadMobileNumberApi() {
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
        formBuilder.add(AllOmorniParameters.USER_ID, getIntent().getStringExtra("user_id"));
        formBuilder.add(AllOmorniParameters.MOBILE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()) + mobile_edittext.getText().toString());
        formBuilder.add(AllOmorniParameters.MOBILE_NO, mobile_edittext.getText().toString());
        formBuilder.add(AllOmorniParameters.MOBILE_CODE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()));
        formBuilder.add(AllOmorniParameters.APP_LANG, app_language);

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.SEND_MOBILE_NUMBER, formBody) {
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
                            Utils.showToast(context, status.getString("message"));
                            Intent intent = new Intent(context, OtpActivity.class);
                            intent.putExtra("user_id", getIntent().getStringExtra("user_id"));
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showToast(context, getResources().getString(R.string.internet_error));
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
