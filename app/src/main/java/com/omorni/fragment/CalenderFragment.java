package com.omorni.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.activity.AddEventActivity;
import com.omorni.activity.MainActivity;

import com.omorni.adapter.MeetingsAdapter;
import com.omorni.model.EventResponse;
import com.omorni.model.MeetingResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;

import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by test on 3/1/2017.
 */

public class CalenderFragment extends Fragment implements View.OnClickListener {

    CaldroidFragment caldroidFragment;
    Calendar calendar;
    View view;
    SavePref savePref;
    TextView textView_month;
    RecyclerView recyclerView;
    MeetingsAdapter meetingsAdapter;
    ArrayList<MeetingResponse> arrayList;
    ArrayList<EventResponse> eventResponseArrayList;
    ImageView image_add_event;
    ArrayList<EventResponse> arrayList_selected;
    Toolbar toolbar;
    Date eventDate = null;
    String event_date = "";
    int eventDay, eventMonth = 0, eventYear = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        savePref = new SavePref(getActivity());
        if (savePref.getUserType().equals("1"))
            view = inflater.inflate(R.layout.fragment_calender_seller, container, false);
        else
            view = inflater.inflate(R.layout.fragment_calender, container, false);

        arrayList_selected = new ArrayList<>();
        eventResponseArrayList = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        image_add_event = (ImageView) view.findViewById(R.id.image_add_event);
        image_add_event.setOnClickListener(this);

        /*recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getApplicationContext(), recyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {

                if (arrayList_selected.get(position).getEven_type().equals("0")) {
                    Intent intent = new Intent(getActivity(), AddEventActivity.class);
                    intent.putExtra("event_response", arrayList_selected.get(position));
                    intent.putExtra("update", true);
                    startActivityForResult(intent, 100);
                } else {

                    Intent intent = null;
                    if (savePref.getUserType().equals("1"))
                        intent = new Intent(getActivity(), OrderSummerySellerActivity.class);
                    else
                        intent = new Intent(getActivity(), OrderSummeryActivity.class);
                    intent.putExtra("req_id", arrayList_selected.get(position).getId());
                    intent.putExtra("from_my_order", true);
                    startActivity(intent);

                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));*/
        toolbar.setVisibility(View.GONE);
        setCalender();
        if (ConnectivityReceiver.isConnected())
            getMeetings();
        else
            Utils.showToast(getActivity(), getResources().getString(R.string.internet_error));
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.toolbar_title.setText(R.string.calender);
        MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        MainActivity.filter.setVisibility(View.GONE);
    }


    private void setCalender() {
        textView_month = (TextView) view.findViewById(R.id.textView_month);

        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        calendar = Calendar.getInstance();
        if (eventMonth != 0) {
            args.putInt(CaldroidFragment.MONTH, eventMonth + 1);
            args.putInt(CaldroidFragment.YEAR, eventYear);
        } else {
            args.putInt(CaldroidFragment.MONTH, calendar.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, calendar.get(Calendar.YEAR));
        }

        if (savePref.getUserType().equals("1"))
            args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidDefaCustomSeller);
        else
            args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidDefaCustom);

        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, false);
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
        caldroidFragment.setArguments(args);
        caldroidFragment.clearDisableDates();

        FragmentTransaction t = this.getChildFragmentManager().beginTransaction();
        t.replace(R.id.calender, caldroidFragment);
        t.commitAllowingStateLoss();

        final CaldroidListener listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                ArrayList<Date> arrayList = new ArrayList<Date>();
                arrayList.add(date);
                caldroidFragment.clearDisableDates();
                caldroidFragment.setDisableDates(arrayList);
                caldroidFragment.refreshView();

                SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy");
                String datee = output.format(date);
                eventDate = date;
                setAdapter(datee);
            }

            @Override
            public void onChangeMonth(int month, int year) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.YEAR, year);
                SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                String month_name = month_date.format(calendar.getTime());
                textView_month.setText(month_name + " " + year);


                //setEventsOnCalender(month, year);
            }

            @Override
            public void onLongClickDate(Date date, View view) {
            }

            @Override
            public void onCaldroidViewCreated() {

            }
        };

        caldroidFragment.setCaldroidListener(listener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }


    private void getMeetings() {
        final ProgressDialog progressDialog = Utils.initializeProgress(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        eventResponseArrayList.clear();
        caldroidFragment.clearSelectedDates();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(getActivity(), AllOmorniApis.GET_CALENDER_MEETINGS, formBody) {
            @Override
            public void getValueParse(String result) {
                Log.e("calendar", "response " + result);
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
                            JSONObject body = jsonObject.getJSONObject("body");
                            JSONArray jsonArray = body.getJSONArray("schedule");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                EventResponse meetingResponse = new EventResponse();
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                meetingResponse.setId(jsonObject1.getString("id"));
                                meetingResponse.setStart_time(jsonObject1.getString("start_time"));
                                meetingResponse.setEnd_time(jsonObject1.getString("end_time"));
                                meetingResponse.setPrice(jsonObject1.getString("price"));
                                meetingResponse.setEvent_location(jsonObject1.getString("req_location"));
                                meetingResponse.setName(jsonObject1.getString("first_name") + " " + jsonObject1.getString("last_name"));
                                meetingResponse.setRequest_type(jsonObject1.getString("request_type"));
                                meetingResponse.setNormal_charges(jsonObject1.getString("normal_charges"));
                                meetingResponse.setEven_type("1");
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                Date date = dateFormat.parse(jsonObject1.getString("start_date"));
                                meetingResponse.setEvent_date(String.valueOf(Utils.getTimestamp1(jsonObject1.getString("start_date")) / 1000));
                                eventResponseArrayList.add(meetingResponse);
                                caldroidFragment.setSelectedDate(date);
                            }
                            JSONArray jsonArray_events = body.getJSONArray("my_events");
                            for (int i = 0; i < jsonArray_events.length(); i++) {
                                EventResponse eventResponse = new EventResponse();
                                JSONObject jsonObject1 = jsonArray_events.getJSONObject(i);

                                eventResponse.setId(jsonObject1.getString("id"));
                                eventResponse.setEvent_name(jsonObject1.getString(AllOmorniParameters.EVENT_NAME));
                                eventResponse.setEvent_des(jsonObject1.getString(AllOmorniParameters.EVENT_DESCRIPTION));
                                eventResponse.setEvent_location(jsonObject1.getString(AllOmorniParameters.LOCATION));
                                eventResponse.setEvent_repeat(jsonObject1.getString(AllOmorniParameters.EVENT_REPEAT));
                                eventResponse.setRepeat_type(jsonObject1.getString(AllOmorniParameters.REPEAT_TYPE));
                                eventResponse.setAlert_type(jsonObject1.getString(AllOmorniParameters.ALERT_TYPE));
                                eventResponse.setLatitude(jsonObject1.getString(AllOmorniParameters.LAT));
                                eventResponse.setLongitude(jsonObject1.getString(AllOmorniParameters.LONG));
                                eventResponse.setStart_time(Utils.getHourMinute(Long.parseLong(jsonObject1.getString("start_datetime"))));
                                eventResponse.setEnd_time(Utils.getHourMinute(Long.parseLong(jsonObject1.getString("end_datetime"))));
                                eventResponse.setEvent_date(jsonObject1.getString("start_datetime"));
                                eventResponse.setEven_type("0");
                                eventResponse.setEvent_end_date(jsonObject1.getString("end_datetime"));

                                eventResponseArrayList.add(eventResponse);
                            }
                            setAdapter(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
                            Calendar calendar = Calendar.getInstance();

                            setEventsOnCalender(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
                        } else {
                            Utils.checkAuthToken(getActivity(), status.getString("auth_token"), status.getString("message"), savePref);
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
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
            }
        };
        mAsync.execute();
    }


    private void setAdapter(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        arrayList_selected.clear();
        for (EventResponse meetingResponse : eventResponseArrayList) {
            String meeting_date = Utils.getDateCurrentTimeZone(Long.parseLong(meetingResponse.getEvent_date()));
            if (meetingResponse.getEven_type().equals("1") && meeting_date.equalsIgnoreCase(date)) {
                arrayList_selected.add(meetingResponse);
            } else if (meetingResponse.getEven_type().equals("0")) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(Long.parseLong(meetingResponse.getEvent_date()) * 1000);
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTimeInMillis(Long.parseLong(meetingResponse.getEvent_end_date()) * 1000);
                Calendar calendar3 = Calendar.getInstance();
                try {
                    calendar3.setTime(dateFormat.parse(date));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                try {
                    String end_date = Utils.getDateCurrentTimeZone(Long.parseLong(meetingResponse.getEvent_end_date()));
                    if (meetingResponse.getEvent_repeat().equals("1")) {
                        if (dateFormat.parse(date).getTime() >= dateFormat.parse(meeting_date).getTime()) {
                            if (meetingResponse.getRepeat_type().equals("1")) {
                                if (meeting_date.equals(end_date)) {
                                    arrayList_selected.add(meetingResponse);
                                } else if (calendar3.getTimeInMillis() <= calendar2.getTimeInMillis()) {
                                    arrayList_selected.add(meetingResponse);
                                }
                            }
                            if (meetingResponse.getRepeat_type().equals("2")) {
                                if (meeting_date.equals(end_date)) {
                                    if (calendar1.get(Calendar.DAY_OF_WEEK) == calendar3.get(Calendar.DAY_OF_WEEK)) {
                                        arrayList_selected.add(meetingResponse);
                                    }
                                } else if (calendar1.get(Calendar.DAY_OF_WEEK) == calendar3.get(Calendar.DAY_OF_WEEK) && calendar3.getTimeInMillis() <= calendar2.getTimeInMillis()) {
                                    arrayList_selected.add(meetingResponse);
                                }

//                                calendar.setTime(dateFormat.parse(date));
//                                //calendar.add(Calendar.DAY_OF_MONTH, -1);
//                                int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
//                                if (Utils.getMeetingDaysOfWeek(meetingResponse.getEvent_date(), meetingResponse.getEvent_end_date()).contains(day_of_week)) {
//                                    arrayList_selected.add(meetingResponse);
//                                }
                            }

                            if (meetingResponse.getRepeat_type().equals("3")) {
                                if (meeting_date.equals(end_date)) {
                                    if (calendar1.get(Calendar.DAY_OF_MONTH) == calendar3.get(Calendar.DAY_OF_MONTH)) {
                                        arrayList_selected.add(meetingResponse);
                                    }
                                } else if (calendar1.get(Calendar.DAY_OF_MONTH) == calendar3.get(Calendar.DAY_OF_MONTH) && calendar3.getTimeInMillis() <= calendar2.getTimeInMillis()) {
                                    arrayList_selected.add(meetingResponse);
                                }
//                                calendar.setTime(dateFormat.parse(date));
//                                int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
//                                ArrayList<Integer> arrayList = Utils.getMeetindDaysOfMonth(meetingResponse.getEvent_date(), meetingResponse.getEvent_end_date());
//                                if (arrayList.contains(day_of_month)) {
//                                    arrayList_selected.add(meetingResponse);
//                                } else if (day_of_month == 31 && arrayList.contains(30) && arrayList.contains(1)) {
//                                    arrayList_selected.add(meetingResponse);
//                                }
                            }

                            if (meetingResponse.getRepeat_type().equals("4")) {
                                if (meeting_date.equals(end_date)) {
                                    if (calendar1.get(Calendar.DAY_OF_MONTH) == calendar3.get(Calendar.DAY_OF_MONTH) && calendar1.get(Calendar.MONTH) == calendar3.get(Calendar.MONTH)) {
                                        arrayList_selected.add(meetingResponse);
                                    }
                                } else if (calendar1.get(Calendar.DAY_OF_MONTH) == calendar3.get(Calendar.DAY_OF_MONTH) && calendar3.getTimeInMillis() <= calendar2.getTimeInMillis() && calendar1.get(Calendar.MONTH) == calendar3.get(Calendar.MONTH)) {
                                    arrayList_selected.add(meetingResponse);
                                }

//                                calendar.setTime(dateFormat.parse(date));
//                                calendar.add(Calendar.DAY_OF_MONTH, -1);
//                                int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
//                                int month = calendar.get(Calendar.MONTH);
//                                calendar.setTime(dateFormat.parse(meeting_date));
//                                int month2 = calendar.get(Calendar.MONTH);
//                                ArrayList<Integer> arrayList = Utils.getMeetindDaysOfMonth(meetingResponse.getEvent_date(), meetingResponse.getEvent_end_date());
//                                if (arrayList.contains(day_of_month) && month == month2) {
//                                    arrayList_selected.add(meetingResponse);
//                                } else if (day_of_month == 31 && arrayList.contains(30) && arrayList.contains(1)) {
//                                    caldroidFragment.setSelectedDate(calendar.getTime());
//                                }
                            }
                        }
                    } else if (meetingResponse.getEvent_repeat().equals("2")) {
                        checkDateRepeat(Long.parseLong(meetingResponse.getEvent_date()) * 1000, Long.parseLong(meetingResponse.getEvent_end_date()) * 1000, date, meetingResponse);
                    }
                } catch (ParseException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (arrayList_selected.size() > 0) {
            meetingsAdapter = new MeetingsAdapter(getActivity(), arrayList_selected, CalenderFragment.this, true);
            recyclerView.setAdapter(meetingsAdapter);
        } else {
            recyclerView.setAdapter(null);
        }
    }


    private void checkDateRepeat(long timestamp1, long timestamp2, String date, EventResponse eventResponse) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(timestamp1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(timestamp2);
        do {
            String calender_date = Utils.getDateCurrentTimeZone(calendar1.getTimeInMillis() / 1000);
            if (calender_date.equals(date)) {
                arrayList_selected.add(eventResponse);
            }
            calendar1.add(Calendar.DAY_OF_MONTH, 1);
        } while (calendar1.getTimeInMillis() <= calendar2.getTimeInMillis());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_add_event:
                Intent intent = new Intent(getActivity(), AddEventActivity.class);
                intent.putExtra("update", false);
                startActivityForResult(intent, 100);
                break;
        }
    }


    private void setEventsOnCalender(int month, int year) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
            for (int i = 0; i < eventResponseArrayList.size(); i++) {
                EventResponse eventResponse = eventResponseArrayList.get(i);
                String meeting_date = Utils.getDateCurrentTimeZone(Long.parseLong(eventResponse.getEvent_date()));
                if (eventResponse.getEven_type().equals("0")) {
                    String end_date = Utils.getDateCurrentTimeZone(Long.parseLong(eventResponse.getEvent_end_date()));
                    if (eventResponse.getEvent_repeat().equals("1")) {
                        if (eventResponse.getRepeat_type().equals("1")) {
                            Utils.setDaily(dateFormat.parse(meeting_date), dateFormat.parse(end_date), caldroidFragment, month, year);
                        } else if (eventResponse.getRepeat_type().equals("2")) {
                            Utils.setWeekly(dateFormat.parse(meeting_date), dateFormat.parse(end_date), caldroidFragment, month, year);
                        } else if (eventResponse.getRepeat_type().equals("3")) {
                            Utils.setMonthly(dateFormat.parse(meeting_date), dateFormat.parse(end_date), caldroidFragment, month, year);
                        } else if (eventResponse.getRepeat_type().equals("4")) {
                            Utils.setYearly(dateFormat.parse(meeting_date), dateFormat.parse(end_date), caldroidFragment, month, year);
                        }
                    } else {
                        setNonRepeat(eventResponse.getEvent_date(), eventResponse.getEvent_end_date());
                    }
                }
            }
        } catch (ParseException exception) {
            exception.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        caldroidFragment.refreshView();

        ArrayList<Date> arrayList = new ArrayList<Date>();
        arrayList.add(eventDate);
        caldroidFragment.clearDisableDates();
        caldroidFragment.setDisableDates(arrayList);
        caldroidFragment.refreshView();

        SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy");
        String datee = output.format(eventDate);
        setAdapter(datee);
    }


    private void setNonRepeat(String timestamp1, String timestamp2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp1) * 1000);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(Long.parseLong(timestamp2) * 1000);
        if (calendar.getTimeInMillis() == calendar1.getTimeInMillis()) {
            caldroidFragment.setSelectedDate(calendar.getTime());
        }
        do {
            caldroidFragment.setSelectedDate(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        } while (calendar.getTimeInMillis() <= calendar1.getTimeInMillis());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("add_update", false)) {
                    String event_date = data.getStringExtra("event_date");
                    event_date = event_date.substring(0, event_date.indexOf(" "));
                    Log.e("event_date", "now " + event_date);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");

                    try {
                        eventDate = dateFormat.parse(event_date);
                        event_date = outputFormat.format(eventDate);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.e("event_date", "now " + event_date + " " + eventDate);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(eventDate);
                    eventDay = cal.get(Calendar.DAY_OF_MONTH);
                    eventMonth = cal.get(Calendar.MONTH);
                    eventYear = cal.get(Calendar.YEAR);
                    setCalender();
                    getMeetings();
                } else {
                    getMeetings();
                }
            }
        }
    }

    public void removeItem(int pos) {
        getMeetings();
    }
}
