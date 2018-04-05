package com.omorni.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class AddAccountActivity extends BaseActivity {
    AddAccountActivity context;
    private SavePref savePref;
    private EditText first_name, account_no, confirm_account_no, mobile_number, bank_address, lastname;
    private TextView add_account, bank_name, currency_text, edit_account;
    String userFirstName = "", userLastName = "", accountNumber = "", confAccountNumber = "",
            mobileNumber = "", bank_address_string = "", bankId = "", otpNumber = "", currency = "SAR", account_id = "";
    ArrayList<String> bankArray;
    ArrayList<String> bankIdArray;
    String currency_value[] = {"SAR", "USD", "EUR"};
    String[] dsf;
    Dialog dialog;
    private boolean flag = false;
    ArrayList<JsonObject> countryList;
    private Spinner country_spinner1;
    private TextView country_code_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        savePref = new SavePref(AddAccountActivity.this);

        if (Utils.isConfigRtl(AddAccountActivity.this)) {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_add_account_seller_rtl);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_add_account_rtl);
            }
        } else {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_add_account_seller);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_add_account);
            }
        }


        initialize();
        setToolbar();
        getDeviceSimCOuntry();
        if (ConnectivityReceiver.isConnected()) {
            getBankListApi();
        } else {
            Utils.showToast(AddAccountActivity.this, getResources().getString(R.string.internet_error));
        }
    }

    private void initialize() {
        first_name = (EditText) findViewById(R.id.first_name);
        lastname = (EditText) findViewById(R.id.lastname);
        account_no = (EditText) findViewById(R.id.account_no);
        confirm_account_no = (EditText) findViewById(R.id.confirm_account_no);
        mobile_number = (EditText) findViewById(R.id.mobile_number);
        bank_address = (EditText) findViewById(R.id.bank_address);
        currency_text = (TextView) findViewById(R.id.currency);
        add_account = (TextView) findViewById(R.id.add_account);
        bank_name = (TextView) findViewById(R.id.bank_name);
//        country_spinner = (Spinner) findViewById(R.id.country_spinner);
        edit_account = (TextView) findViewById(R.id.edit_account);
        country_code_text = (TextView) findViewById(R.id.country_code_text);

        country_code_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCountryListDialog();
            }
        });

        bank_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openListDialog();
            }
        });

        currency_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openListCurrencyDialog();
            }
        });

        add_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
                if (ConnectivityReceiver.isConnected()) {
                    if (userFirstName.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_firstname), Toast.LENGTH_SHORT).show();
                    } else if (userLastName.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_lastname), Toast.LENGTH_SHORT).show();
                    } else if (accountNumber.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_iban), Toast.LENGTH_SHORT).show();
                    } else if (confAccountNumber.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_confirm_iban), Toast.LENGTH_SHORT).show();
                    } else if (!accountNumber.equals(confAccountNumber)) {
                        Toast.makeText(context, getResources().getString(R.string.match_iban), Toast.LENGTH_SHORT).show();
                    } else if (mobileNumber.equals(getResources().getString(R.string.enter_mobile_refistered))) {
                        Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                    }/* else if (bankId.equals("")) {
                        Toast.makeText(context, "Please select bank name", Toast.LENGTH_SHORT).show();
                    }*/ else if (bank_name.getText().toString().equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_bank_name), Toast.LENGTH_SHORT).show();
                    } else if (currency.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_currency), Toast.LENGTH_SHORT).show();
                    } else if (bank_address_string.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_bank_address), Toast.LENGTH_SHORT).show();
                    } else {
                        sendOtpApi(false, "0");
                    }
                } else {
                    Utils.showToast(context, getResources().getString(R.string.internet_error));
                }
            }
        });

        edit_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
                if (ConnectivityReceiver.isConnected()) {
                    if (userFirstName.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_firstname), Toast.LENGTH_SHORT).show();
                    } else if (userLastName.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_lastname), Toast.LENGTH_SHORT).show();
                    } else if (accountNumber.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_iban), Toast.LENGTH_SHORT).show();
                    } else if (confAccountNumber.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_confirm_iban), Toast.LENGTH_SHORT).show();
                    } else if (!accountNumber.equals(confAccountNumber)) {
                        Toast.makeText(context, getResources().getString(R.string.match_iban), Toast.LENGTH_SHORT).show();
                    } else if (mobileNumber.equals(getResources().getString(R.string.enter_mobile_refistered))) {
                        Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                    }/* else if (bankId.equals("")) {
                        Toast.makeText(context, "Please select bank name", Toast.LENGTH_SHORT).show();
                    }*/ else if (bank_name.getText().toString().equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_bank_name), Toast.LENGTH_SHORT).show();
                    } else if (currency.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_currency), Toast.LENGTH_SHORT).show();
                    } else if (bank_address_string.equals("")) {
                        Toast.makeText(context, getResources().getString(R.string.error_bank_address), Toast.LENGTH_SHORT).show();
                    } else {
                        sendOtpApi(true, "1");
                    }
                } else {
                    Utils.showToast(context, getResources().getString(R.string.internet_error));
                }
            }
        });
    }

    private void addAccountDetail() {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.BANK_FIRST_NAME, userFirstName);
        formBuilder.add(AllOmorniParameters.BANK_LAST_NAME, userLastName);
        formBuilder.add(AllOmorniParameters.CURRENCY, currency);
        formBuilder.add(AllOmorniParameters.ACC_NUMBER, accountNumber);
        formBuilder.add(AllOmorniParameters.MOBILE_NUMBER, mobileNumber);
        formBuilder.add(AllOmorniParameters.BANK_ADDRESS, bank_address_string);
        formBuilder.add(AllOmorniParameters.BANK, bank_name.getText().toString());
//        formBuilder.add(AllOmorniParameters.BANK, bankId);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.OTP, otpNumber);
        formBuilder.add(AllOmorniParameters.MOBILE_CODE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.ADD_ACCOUNT_OTP_URL, formBody) {
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
                        Log.e("result", "" + result);
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            Utils.showToast(context, status.getString("message"));
                            dialog.dismiss();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("account_exist", "1");
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Utils.showToast(context, status.getString("message"));
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

    private void updateAccountApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.BANK_FIRST_NAME, userFirstName);
        formBuilder.add(AllOmorniParameters.BANK_LAST_NAME, userLastName);
        formBuilder.add(AllOmorniParameters.CURRENCY, currency);
        formBuilder.add(AllOmorniParameters.ACC_NUMBER, accountNumber);
        formBuilder.add(AllOmorniParameters.MOBILE_NUMBER, mobileNumber);
        formBuilder.add(AllOmorniParameters.BANK_ADDRESS, bank_address_string);
//        formBuilder.add(AllOmorniParameters.BANK, bankId);
        formBuilder.add(AllOmorniParameters.BANK, bank_name.getText().toString());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.OTP, otpNumber);
        formBuilder.add(AllOmorniParameters.ACCOUNT_ID, account_id);
        formBuilder.add(AllOmorniParameters.MOBILE_CODE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.UPDATE_ACCOUNT, formBody) {
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
                        Log.e("result", "" + result);
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            Utils.showToast(context, status.getString("message"));
                            dialog.dismiss();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("account_exist", "1");
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Utils.showToast(context, status.getString("message"));
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


    private void sendOtpApi(final boolean is_update_account, String check_type) {
        Log.e("otp", "here " + country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()));
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.ACC_NUMBER, accountNumber);
        formBuilder.add(AllOmorniParameters.CHECK_TYPE, check_type);
        if (is_update_account)
            formBuilder.add(AllOmorniParameters.ACCOUNT_ID, account_id);
        formBuilder.add(AllOmorniParameters.MOBILE_PHONE, country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()) + mobile_number.getText().toString());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.SEND_ACCOUNT_OTP_URL, formBody) {
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
                            if (!flag)
                                openDialog(is_update_account);
                        } else {
                            Utils.showToast(context, status.getString("message"));
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

    private void openDialog(final boolean is_update_account) {
        dialog = new Dialog(context, R.style.Theme_Dialog);
        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            dialog.setContentView(R.layout.otp_dialog_layout);
        } else {
            setTheme(R.style.AppTheme);
            dialog.setContentView(R.layout.otp_dialog_buyer_layout);
        }
        dialog.setCancelable(false);
        dialog.show();
        final EditText otpEditText = (EditText) dialog.findViewById(R.id.otp_number);
        TextView submitTextView = (TextView) dialog.findViewById(R.id.submit);
        TextView cancelTextView = (TextView) dialog.findViewById(R.id.cancel);
        TextView resendTextView = (TextView) dialog.findViewById(R.id.resend_otp);
        submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otpEditText.getText().toString().equals("")) {
                    Utils.showToast(context, getResources().getString(R.string.please_enter_otp));
                } else {
                    otpNumber = otpEditText.getText().toString();
                    if (is_update_account)
                        updateAccountApi();
                    else
                        addAccountDetail();
                }
            }
        });

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = false;
                dialog.dismiss();
            }
        });

        resendTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = true;
                if (is_update_account)
                    sendOtpApi(is_update_account, "1");
                else
                    sendOtpApi(is_update_account, "0");
            }
        });
    }

    private void getMyAccountDetail(final ProgressDialog progressDialog) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.GET_MY_ACCOUNT_DETAILS, formBody) {
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

                            JSONArray jsonArray = jsonObject.getJSONArray("body");

                            if (jsonArray.length() > 0) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                                first_name.setText(jsonObject1.getString("firstname"));
                                lastname.setText(jsonObject1.getString("lastname"));
                                account_no.setText(jsonObject1.getString("acc_number"));
                                confirm_account_no.setText(jsonObject1.getString("acc_number"));
                                mobile_number.setText(jsonObject1.getString("mobile_number"));
                                currency_text.setText(jsonObject1.getString("currency"));
                                bank_address.setText(jsonObject1.getString("bank_address"));
//                                bankId = jsonObject1.getString("bank");

//                                int bank_index = bankIdArray.indexOf(bankId);

//                                bank_name.setText(jsonObject1.getString("bank"));
                                bank_name.setText(jsonObject1.getString("bank"));
                                Log.e("code", "code " + jsonObject1.getString("mobile_code"));
                                int selected = 0;
                                for (int i = 0; i < countryList.size(); i++) {
                                    String iso = countryList.get(i).get("code").getAsString();
                                    if (jsonObject1.getString("mobile_code").equalsIgnoreCase(iso)) {
                                        selected = i;
                                        country_code_text.setText("(" + countryList.get(i).get("iso").getAsString() + ")" + " +" + countryList.get(i).get("code").getAsString());
                                        break;
                                    }
                                }

                                account_id = jsonObject1.getString("id");
                                add_account.setVisibility(View.GONE);
                                edit_account.setVisibility(View.VISIBLE);

//                                country_spinner.setSelection(selected);
                            } else {
                                add_account.setVisibility(View.VISIBLE);
                                edit_account.setVisibility(View.GONE);
                                TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                int selected = 0;
                                String CountryID = manager.getSimCountryIso().toUpperCase();
                                for (int i = 0; i < countryList.size(); i++) {
                                    String iso = countryList.get(i).get("iso").getAsString();
                                    if (CountryID.equalsIgnoreCase(iso)) {
                                        selected = i;
                                        country_code_text.setText("(" + countryList.get(i).get("iso").getAsString() + ")" + " +" + countryList.get(i).get("code").getAsString());
                                        break;
                                    }
                                }
//                                country_spinner.setSelection(selected);
                            }

                        } else {
                            Utils.checkAuthToken(AddAccountActivity.this, status.getString("auth_token"), status.getString("message"), savePref);
                        }

                    } catch (JSONException ex1) {
                        ex1.printStackTrace();
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

    private void getBankListApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();

        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.GET_BANKS_URL, formBody) {
            @Override
            public void getValueParse(String result) {
                if (result != null && !result.equalsIgnoreCase("")) {
                    bankArray = new ArrayList<>();
                    bankIdArray = new ArrayList<>();

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONArray bodyJsonArray = jsonObject.getJSONArray("body");
                            for (int i = 0; i < bodyJsonArray.length(); i++) {
                                JSONObject object = bodyJsonArray.getJSONObject(i);
                                String bankId = object.getString("id");
                                String bankName = object.getString("bank_name");
                                bankIdArray.add(bankId);
                                bankArray.add(bankName);
                            }
                        } else {
                            Utils.checkAuthToken(AddAccountActivity.this, status.getString("auth_token"), status.getString("message"), savePref);
                        }
                        getMyAccountDetail(progressDialog);

                    } catch (JSONException ex1) {
                        ex1.printStackTrace();
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


    private void openListDialog() {
        dsf = new String[bankArray.size()];
        for (int i = 0; i < bankArray.size(); i++) {
            dsf[i] = bankArray.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getResources().getString(R.string.select_bank));
        builder.setItems(dsf, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                bankId = bankIdArray.get(position);
                bank_name.setText(dsf[position]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openListCurrencyDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getResources().getString(R.string.account_currency));
        builder.setItems(currency_value, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                currency = currency_value[position];
                currency_text.setText(currency);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(getResources().getString(R.string.add_account));
    }

    private void getData() {
        userFirstName = first_name.getText().toString();
        userLastName = lastname.getText().toString();
        accountNumber = account_no.getText().toString();
        confAccountNumber = confirm_account_no.getText().toString();
        mobileNumber = mobile_number.getText().toString();
        bank_address_string = bank_address.getText().toString();
        currency = currency_text.getText().toString();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getDeviceSimCOuntry() {

        countryList = Utils.getCountries(context);

        /*CountryAdapter dataAdapter = new CountryAdapter(context, countryList, text, dialog);
        country_spinner.setAdapter(dataAdapter);

        //getNetworkCountryIso


        country_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final TextView name = (TextView) view.findViewById(R.id.name);
                final TextView code = (TextView) view.findViewById(R.id.code);
                name.setBackgroundResource(android.R.color.transparent);
                name.setTextColor(Color.BLACK);
                code.setBackgroundResource(android.R.color.transparent);
                code.setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

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
