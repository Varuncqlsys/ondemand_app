package com.omorni.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;
import com.omorni.R;
import com.omorni.model.EventResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class AddEventActivity extends BaseActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Context context;
    SavePref savePref;
    EditText edt_event_name, edt_event_description, edt_date, edt_month, edt_year;
    TextView txt_start_time, txt_end_time, txt_event_repeat, txt_alert_type, txt_event_location;
    public String[] repeat_array;
    public String[] alert_array;
    String minutes_before = "0";
    String repeat_type = "0", event_repeat = "2";
    private String start_time = "", latitude = "", longitude = "", location_name = "";
    Button btn_add_event, btn_delete;
    ProgressDialog progressDialog;
    boolean is_update = false;
    EventResponse eventResponse;
    String date_selected = "";
    String start_date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = AddEventActivity.this;
        savePref = new SavePref(context);
        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_add_event_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_add_event);
        }
        repeat_array = getResources().getStringArray(R.array.repeat_array);
        alert_array = getResources().getStringArray(R.array.alert_array);


        initialize();
        setToolbar();


    }

    private void checkStartDate() {
        String start_time = "", end_time = "";

        Date d = new Date();
        CharSequence start_date = DateFormat.format("MM/dd/yyyy", d.getTime());
        CharSequence end_date = DateFormat.format("MM/dd/yyyy", d.getTime());

        Calendar c = Calendar.getInstance();
        int start_minute = c.get(Calendar.MINUTE);
        int start_hour = c.get(Calendar.HOUR_OF_DAY);
        int end_minute = 0, end_hour = 0;

        if (start_minute > 30) {
            if (start_hour > 22) {
                start_hour = 00;

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    c.setTime(sdf.parse(start_date.toString()));
                    c.add(Calendar.DATE, 1);  // number of days to add
                    start_date = sdf.format(c.getTime());
                    end_date = sdf.format(c.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                start_hour = start_hour + 1;
            }
            start_minute = 00;
        } else {
            start_minute = 30;
        }

        end_hour = start_hour + 1;
        if (end_hour > 23) {
            end_hour = 00;

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            try {
                c.setTime(sdf.parse(end_date.toString()));
                c.add(Calendar.DATE, 1);  // number of days to add
                end_date = sdf.format(c.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        end_minute = start_minute;

        start_time = String.format("%02d:%02d", start_hour, start_minute);
        end_time = String.format("%02d:%02d", end_hour, end_minute);

        txt_start_time.setText(start_date + " " + start_time);
        txt_end_time.setText(end_date + " " + end_time);
    }


    private void getCurrentPlaceName() {

        final Geocoder geocoder = new Geocoder(AddEventActivity.this, Locale.getDefault());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                List<Address> addresses1 = null;
                try {
                    addresses1 = geocoder.getFromLocation(Double.parseDouble(savePref.getLat()), Double.parseDouble(savePref.getLong()), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final List<Address> addresses = addresses1;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (addresses != null) {
                            if (addresses.size() > 0) {
                                latitude = savePref.getLat();
                                longitude = savePref.getLong();
                                Address address1 = addresses.get(0);
                                 location_name =address1.getAddressLine(0);

                                /*for (int i = 0; i < address1.getMaxAddressLineIndex(); i++) {
                                    location_name = location_name + " " + address1.getAddressLine(i);
                                }*/

                                txt_event_location.setText(location_name);
                            }
                        }
                    }
                });
            }
        });
        thread.start();

    }


    private void initialize() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));

        is_update = getIntent().getBooleanExtra("update", false);

        btn_add_event = (Button) findViewById(R.id.btn_add_event);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        edt_event_name = (EditText) findViewById(R.id.edt_event_name);
        edt_event_description = (EditText) findViewById(R.id.edt_event_description);
        edt_date = (EditText) findViewById(R.id.edt_date);
        edt_month = (EditText) findViewById(R.id.edt_month);
        edt_year = (EditText) findViewById(R.id.edt_year);

        txt_event_location = (TextView) findViewById(R.id.txt_event_location);
        txt_start_time = (TextView) findViewById(R.id.txt_start_time);
        txt_end_time = (TextView) findViewById(R.id.txt_end_time);
        txt_event_repeat = (TextView) findViewById(R.id.txt_event_repeat);
        txt_alert_type = (TextView) findViewById(R.id.txt_alert_type);

        txt_start_time.setOnClickListener(this);
        txt_end_time.setOnClickListener(this);
        txt_event_repeat.setOnClickListener(this);
        txt_alert_type.setOnClickListener(this);
        txt_event_location.setOnClickListener(this);
        btn_add_event.setOnClickListener(this);

        if (is_update) {
            eventResponse = (EventResponse) getIntent().getParcelableExtra("event_response");
            setValues();
            btn_delete.setVisibility(View.VISIBLE);
            btn_add_event.setText(getResources().getString(R.string.save));
        } else {
            checkStartDate();
            getCurrentPlaceName();
        }
        btn_delete.setOnClickListener(this);
    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);

        if (is_update)
            toolbar_title.setText(getResources().getString(R.string.edit_event));
        else
            toolbar_title.setText(getResources().getString(R.string.add_event));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
    }


    private void setValues() {
        edt_event_name.setText(eventResponse.getEvent_name());
        edt_event_description.setText(eventResponse.getEvent_des());
        txt_event_location.setText(eventResponse.getEvent_location());
        txt_start_time.setText(Utils.getDateCurrentTimeZone1(Long.parseLong(eventResponse.getEvent_date())));
        txt_end_time.setText(Utils.getDateCurrentTimeZone1(Long.parseLong(eventResponse.getEvent_end_date())));
//        edt_date.setText(eventResponse.getEvent_date().split("/")[1]);
//        edt_month.setText(eventResponse.getEvent_date().split("/")[0]);
//        edt_year.setText(eventResponse.getEvent_date().split("/")[2]);

        latitude = eventResponse.getLatitude();
        longitude = eventResponse.getLongitude();
        location_name = eventResponse.getEvent_location();
        event_repeat = eventResponse.getEvent_repeat();
        repeat_type = eventResponse.getRepeat_type();
        minutes_before = eventResponse.getAlert_type();

        if (eventResponse.getEvent_repeat().equals("2")) {
            txt_event_repeat.setText(repeat_array[0]);
        } else {
            switch (repeat_type) {
                case "1":
                    txt_event_repeat.setText(repeat_array[1]);
                    break;
                case "2":
                    txt_event_repeat.setText(repeat_array[2]);
                    break;
                case "3":
                    txt_event_repeat.setText(repeat_array[3]);
                    break;
                case "4":
                    txt_event_repeat.setText(repeat_array[4]);
                    break;
              /*  case "5":
                    txt_event_repeat.setText(repeat_array[5]);
                    break;*/
            }
        }


        switch (minutes_before) {
            case "0":
                txt_alert_type.setText(alert_array[1]);
                break;
            case "5":
                txt_alert_type.setText(alert_array[2]);
                break;
            case "15":
                txt_alert_type.setText(alert_array[3]);
                break;
            case "30":
                txt_alert_type.setText(alert_array[4]);
                break;
            case "60":
                txt_alert_type.setText(alert_array[5]);
                break;
            case "120":
                txt_alert_type.setText(alert_array[6]);
                break;
            case "1440":
                txt_alert_type.setText(alert_array[7]);
                break;
            case "2880":
                txt_alert_type.setText(alert_array[8]);
                break;
            case "10080":
                txt_alert_type.setText(alert_array[9]);
                break;

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_start_time:
                date_selected = "start";
                openDatePicker(txt_start_time, txt_end_time);
                break;
            case R.id.txt_end_time:
                date_selected = "end";
                openDatePicker(txt_start_time, txt_end_time);
                break;
            case R.id.txt_event_repeat:
                openListDialog(repeat_array, "0");
                break;
            case R.id.txt_alert_type:
                openListDialog(alert_array, "1");
                break;
            case R.id.txt_event_location:
                Intent intent = new Intent(AddEventActivity.this, SelectLocationActivity.class);
                intent.putExtra("selected_lat", latitude);
                intent.putExtra("selected_long", longitude);
                intent.putExtra("selected_place", location_name);
                startActivityForResult(intent, 105);
                break;
            case R.id.btn_add_event:
                checkValidation(v);
                break;
            case R.id.btn_delete:
                showDeleteDialog();
                break;
        }
    }


    private void openDatePicker(TextView start_date, TextView end_date) {
        String selected_date = "";

        if (date_selected.equals("start")) {
            selected_date = start_date.getText().toString().substring(0, start_date.getText().toString().indexOf(" "));
        } else {
            selected_date = end_date.getText().toString().substring(0, end_date.getText().toString().indexOf(" "));
        }

        int month = Integer.parseInt(selected_date.substring(0, selected_date.indexOf("/")));
        int day = Integer.parseInt(selected_date.substring(selected_date.indexOf("/") + 1, selected_date.lastIndexOf("/")));
        int year = Integer.parseInt(selected_date.substring(selected_date.lastIndexOf("/") + 1, selected_date.length()));
        month = month - 1;
        DatePickerDialog dpd = DatePickerDialog.newInstance(this, year, month, day);

        dpd.setThemeDark(false);
        dpd.vibrate(true);
        dpd.dismissOnPause(true);
        dpd.showYearPickerFirst(false);
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }


    private void checkValidation(View view) {
        String event_name = edt_event_name.getText().toString();
        String event_description = edt_event_description.getText().toString();
        String start_time = txt_start_time.getText().toString();
        start_date = start_time;
        String end_time = txt_end_time.getText().toString();
        String event_day = edt_date.getText().toString();
        String event_month = edt_month.getText().toString();
        String event_year = edt_year.getText().toString();
        String date = event_month + "/" + event_day + "/" + event_year;
        if (event_name.isEmpty()) {
            Utils.showSnackBar(view, getString(R.string.enter_event_name), this);
        } /*else if (event_description.isEmpty()) {
            Utils.showSnackBar(view, getString(R.string.enter_event_description), this);
        } else if (location_name.isEmpty()) {
            Utils.showSnackBar(view, getString(R.string.enter_location), this);
        }*/ else if (start_time.isEmpty()) {
            Utils.showSnackBar(view, getString(R.string.enter_start_time), this);
        } else if (end_time.isEmpty()) {
            Utils.showSnackBar(view, getString(R.string.enter_end_time), this);
        } else {
            Log.e("starttime", "end " + start_time + " " + end_time);
            long timestamp_start = Utils.getTimestamp(start_time) / 1000;
            long timestamp_end = Utils.getTimestamp(end_time) / 1000;
            long current_time = Utils.getTimestamp(new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date())) / 1000;

            if (current_time > timestamp_start) {
                Utils.showSnackBar(view, getString(R.string.enter_date_after), this);
            } else if (timestamp_start >= timestamp_end) {
                Utils.showSnackBar(view, getString(R.string.end_date_greater), this);
            } else {
                addEventApi(event_name, event_description, String.valueOf(timestamp_start), String.valueOf(timestamp_end), date);
            }
        }
    }

    private void openListDialog(final String[] categoryArray, final String dialog_type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.select_category));
        builder.setItems(categoryArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (dialog_type.equalsIgnoreCase("0")) {
                    txt_event_repeat.setText(categoryArray[position]);
                    switch (position) {
                        case 0:
                            event_repeat = "2";
                            repeat_type = "0";
                            break;
                        case 1:
                            event_repeat = "1";
                            repeat_type = "1";
                            break;
                        case 2:
                            event_repeat = "1";
                            repeat_type = "2";
                            break;
                        case 3:
                            event_repeat = "1";
                            repeat_type = "3";
                            break;
                        case 4:
                            event_repeat = "1";
                            repeat_type = "4";
                            break;
                    }
                } else if (dialog_type.equalsIgnoreCase("1")) {
                    txt_alert_type.setText(categoryArray[position]);

                    switch (position) {
                        case 0:
                            minutes_before = "0";
                            break;
                        case 1:
                            minutes_before = "0";
                            break;
                        case 2:
                            minutes_before = "5";
                            break;
                        case 3:
                            minutes_before = "15";
                            break;
                        case 4:
                            minutes_before = "30";
                            break;
                        case 5:
                            minutes_before = "60";
                            break;
                        case 6:
                            minutes_before = "120";
                            break;
                        case 7:
                            minutes_before = String.valueOf(24 * 60);
                            break;
                        case 8:
                            minutes_before = String.valueOf(2 * 24 * 60);
                            break;
                        case 9:
                            minutes_before = String.valueOf(7 * 24 * 60);
                            break;
                    }

                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void openTimePickerStartDialog(final String type, final String date) {
        final Dialog dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.time_picker_dialog);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView ok = (TextView) dialog.findViewById(R.id.ok);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        final TimePicker tp = (TimePicker) dialog.findViewById(R.id.tp);
        title.setText(getResources().getString(R.string.work_time));

        if (savePref.getUserType().equals("1")) {
            title.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimarySeller));
        } else {
            title.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        tp.setIs24HourView(true);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    start_time = String.format("%02d:%02d", tp.getHour(), tp.getMinute());
                else
                    start_time = String.format("%02d:%02d", tp.getCurrentHour(), tp.getCurrentMinute());

                dialog.dismiss();

                if (type.equals("0")) {
                    txt_start_time.setText(date + " " + start_time);
                } else if (type.equals("1")) {
                    txt_end_time.setText(date + " " + start_time);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        dialog.show();
    }


    private void addEventApi(String event_name, String event_desc, String start_time, String end_time, String event_date) {
        progressDialog.show();
        String url = "";
        FormBody.Builder formBuilder = new FormBody.Builder();

        Log.e("add", "event " + event_name + " " + start_time + " " + end_time);
        if (is_update) {
            url = AllOmorniApis.UPDATE_EVENT_URL;
            formBuilder.add(AllOmorniParameters.EVENT_ID, eventResponse.getId());
        } else {
            url = AllOmorniApis.ADD_EVENT_URl;
        }
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.EVENT_NAME, event_name);
        formBuilder.add(AllOmorniParameters.EVENT_DESCRIPTION, event_desc);
        formBuilder.add(AllOmorniParameters.LOCATION, location_name);
        formBuilder.add(AllOmorniParameters.LAT, latitude);
        formBuilder.add(AllOmorniParameters.LONG, longitude);
        formBuilder.add(AllOmorniParameters.START_DATE_TIME, start_time);
        formBuilder.add(AllOmorniParameters.END_DATE_TIME, end_time);
        formBuilder.add(AllOmorniParameters.EVENT_REPEAT, event_repeat);
        formBuilder.add(AllOmorniParameters.REPEAT_TYPE, repeat_type);
        formBuilder.add(AllOmorniParameters.ALERT_TYPE, minutes_before);
        // formBuilder.add(AllOmorniParameters.EVENT_DATE, event_date);
        RequestBody formBody = formBuilder.build();
        Log.e("body", "string " + Utils.bodyToString(formBody));

        GetAsync mAsync = new GetAsync(this, url, formBody) {
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
                Log.e("resulttt", result);
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObj = new JSONObject(result);
                        JSONObject status = jsonObj.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            if (status.getString("message").equals(""))
                                Utils.showToast(AddEventActivity.this, getResources().getString(R.string.event_created));
                            else {
                                Utils.showToast(AddEventActivity.this, status.getString("message"));
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showToast(AddEventActivity.this, getResources().getString(R.string.event_created));
                                    }
                                }, 2000);
                            }
                            Intent intent = new Intent();
                            intent.putExtra("event_date", start_date);
                            intent.putExtra("add_update", true);
                            setResult(RESULT_OK, intent);
                            finish();
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


    private void deleteEventAPI() {
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.EVENT_ID, eventResponse.getId());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.DELETE_EVENT_URL, formBody) {
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
                        JSONObject jsonObj = new JSONObject(result);
                        JSONObject status = jsonObj.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            Intent intent = new Intent();
                            intent.putExtra("add_update", false);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        Utils.showToast(AddEventActivity.this, status.getString("message"));
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


    public void showDeleteDialog() {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(getResources().getString(R.string.want_delete))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteEventAPI();
                            }
                        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        android.support.v7.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 105) {
            if (resultCode == RESULT_OK) {
                latitude = data.getStringExtra("selected_lat");
                longitude = data.getStringExtra("selected_long");
                location_name = data.getStringExtra("selected_place");
                txt_event_location.setText(location_name);
            }
        }

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = String.valueOf(monthOfYear + 1) + "/" + String.valueOf(dayOfMonth) + "/" + String.valueOf(year);
        if (date_selected.equals("start"))
            openTimePickerStartDialog("0", date);
        else if (date_selected.equals("end"))
            openTimePickerStartDialog("1", date);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

    }
}
