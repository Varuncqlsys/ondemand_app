package com.omorni.utils;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.omorni.R;
import com.omorni.activity.LoginActivity;
import com.omorni.service.UpdateLocationScheduler;
import com.roomorama.caldroid.CaldroidFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.card.payment.CreditCard;
import okhttp3.RequestBody;
import okio.Buffer;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by test on 12/27/2016.
 */

public class Utils {
    public static File myDir_images_sent = new File(Environment.getExternalStoragePublicDirectory("Omorni"), "Images/Sent");
    public static File myDir_omorni = new File(Environment.getExternalStoragePublicDirectory("Omorni"), "Data");
    public static String API_KEY = "AIzaSyDKglCK_B5B8CDYsYPIpwnYA_bat8K63GA";
    public static final String NOTIFICATION_MESSAGE = "com.omorni.android.omorni.Message_Handle_Brodcast_Reciever.DISPLAY_MESSAGE";
    public static final String NOTIFICATION_ACCEPTED_JOB = "com.omorni.android.omorni.Message_Handle_Brodcast_Reciever.ACCEPTED_JOB";
    public static final String NOTIFICATION_CHECKOUT = "com.omorni.android.omorni.Message_Handle_Brodcast_Reciever.CHECKOUT_JOB";
    public static final String NOTIFICATION_CHECKIN = "com.omorni.android.omorni.Message_Handle_Brodcast_Reciever.CHECKIN_JOB";
    public static final String NOTIFICATION_MADE_PAYMENT = "com.omorni.android.omorni.Message_Handle_Brodcast_Reciever.MADE_PAYMENT";
    public static final String NOTIFICATION_SELLER_REJECTED = "com.omorni.android.omorni.Message_Handle_Brodcast_Reciever.SELLER_REJECTED";
    public static final String NOTIFICATION_BUYER_REQUESTED_SELLER_QUOTATION = "com.omorni.android.omorni.Message_Handle_Brodcast_Reciever.BUYER_REQUESTED_SELLER_QUOTATION";
    public static final String NOTIFICATION_SELLER_CHECKIN_SCREEN = "com.omorni.android.omorni.Message_Handle_Brodcast_Reciever.SELLER_ON_CHECKIN_SCREEN";
    public static String Internet_Check = "Please Check Your Internet Check Connection";
    public static boolean flag = false;

    public static Uri getOutputMediaFileUri(File file) {
        return Uri.fromFile(file);
    }

    public static File getOutputMediaFile(int type, String file_name) {

        File mediaFile = null;
//        if (type == 1) {
        mediaFile = new File(myDir_images_sent, file_name + ".jpg");
//        } else if (type == 2) {
//            mediaFile = new File(myDir_images_profile_pic, file_name + ".jpg");
//        }

        return mediaFile;
    }

    public static void hideKeyboard(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(EditText editText, Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static File getImageFile(String file_name) {

        File mediaFile;

        mediaFile = new File(myDir_images_sent, file_name + ".jpg");

        return mediaFile;
    }

    public static File writeSendFileToExternalDirectory(Bitmap bitmap, String file_name, String path, String message_type) {


        File file = null;
// if image
        if (message_type.equals("1")) {
            if (!myDir_images_sent.exists()) {
                myDir_images_sent.mkdirs();
            }
            file = new File(myDir_images_sent, file_name);
            if (file.exists())
                file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (message_type.equals("2")) {
            // if file to be written is video
            if (!myDir_omorni.exists()) {
                myDir_omorni.mkdirs();
            }
            int length;
            try {
                File original_file = new File(path);
                file = new File(myDir_omorni, file_name);
                InputStream inStream = new FileInputStream(original_file);
                OutputStream outStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }

                inStream.close();
                outStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    public static ProgressDialog initializeProgress(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(context.getResources().getString(R.string.loading));
        return progressDialog;
    }

    public static void viewImage(Context activity, String url) {
//        Uri uri = Uri.parse(url);
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Holo_NoActionBar_Fullscreen);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.activity_picture_details);
        dialog.show();
        ImageView icn_back = (ImageView) dialog.findViewById(R.id.icn_cross);
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.activity_picture_details);

//        image.setImageURI(uri);

//        Glide.with(activity).load(Uri.parse(url)).error(R.drawable.camera_placeholder).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(image);
        Glide.with(activity).load(url).placeholder(R.drawable.camera_placeholder).dontAnimate().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(image);

        icn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void checkAuthToken(Activity context, String token, String message, SavePref savePref) {

        if (token.equals("0")) {
            savePref.clearPreferences();
            Intent in = new Intent(context, LoginActivity.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(in);
            context.finish();
            context.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
        Utils.showToast(context, message);
    }

    public static void showSnackBar(View mParentview, String message, Context context) {
        Snackbar snackbar = Snackbar.make(mParentview, message, Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snackbar.getView();
        group.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.show();
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getBitmapFromBytes(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static ArrayList<JsonObject> getCountries(Context context) {
        ArrayList<JsonObject> country = new ArrayList<JsonObject>();
        JsonParser parser = new JsonParser();
        InputStream stream;
        try {
            stream = context.getApplicationContext().getAssets().open("isd.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonArray array = (JsonArray) parser.parse(new InputStreamReader(stream));
        for (int i = 0; i < array.size(); i++) {
            JsonObject jsonObject = (JsonObject) array.get(i);
            country.add(jsonObject);
//                country.put("I18N_CO_" + jsonObject.get("cca2").getAsString(),jsonObject.get("name").getAsString());
        }
        return country;
    }

    public static boolean checkPermission(String strPermission, Context context) {
        int result = ContextCompat.checkSelfPermission(context, strPermission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static void requestPermission(String strPermission, int perCode, Context _c, Activity _a) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(_a, strPermission)) {
        } else {
            ActivityCompat.requestPermissions(_a, new String[]{strPermission}, perCode);
        }
    }

    public static boolean isGpsOn(Context context) {
        boolean isGpsOn = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGpsOn = false;

        } else {
            isGpsOn = true;
        }
        return isGpsOn;
    }


    public static String getAbsolutePath(Context activity, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = activity.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String timestampToDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy ", cal).toString();
        return date;
    }

    public static void setScheduler(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, UpdateLocationScheduler.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 2), (1000 * 2), pi);
    }

    public static void setStopScheduler(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, UpdateLocationScheduler.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.cancel(pi);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 10), (1000 * 10) - 1000, pi);
    }

    public static String convertTimeStampDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setTimeZone(tz);
        Date currenTimeZone = new Date(timestamp * 1000);
        return sdf.format(currenTimeZone);
    }

    public static String convertTimeStampTime(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(tz);
        Date currenTimeZone = new Date(timestamp * 1000);
        return sdf.format(currenTimeZone);
    }


    public static String convertTimeStampDateTime(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        sdf.setTimeZone(tz);
        Date currenTimeZone = new Date(timestamp * 1000);
        return sdf.format(currenTimeZone);
    }

    public static Bitmap returnCardType(String card_number, Context context) {
        CreditCard card = new CreditCard(card_number, 0, 0, null, null, null);
        return card.getCardType().imageBitmap(context);
    }

    public static String maskCardNumber(String cardNumber, String mask) {

        // format the number
        int index = 0;
        StringBuilder maskedNumber = new StringBuilder();
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == '#') {
                maskedNumber.append(cardNumber.charAt(index));
                index++;
            } else if (c == 'x') {
                maskedNumber.append(c);
                index++;
            } else {
                maskedNumber.append(c);
            }
        }

        // return the masked number
        return maskedNumber.toString();
    }

    public String getInsertedPath(Bitmap bitmap, String path, String file_name) {
        String strMyImagePath = null;
//        File mFolder = new File(myDir_omorni);
        if (!myDir_omorni.exists()) {
            myDir_omorni.mkdir();
        }
        String s = file_name + ".jpg";
        File f = new File(myDir_omorni, s);

        strMyImagePath = f.getAbsolutePath();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkDateBefore(String start_date) {
        boolean check_date = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date strDate = sdf.parse(start_date);
            if (strDate.getTime() >= System.currentTimeMillis()) {
                check_date = true;
            }
        } catch (ParseException parse) {
            parse.printStackTrace();
        }
        return check_date;
    }


    public static boolean isThisDateValid(String dateToValidate, String dateFromat) {

        if (dateToValidate == null) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
        sdf.setLenient(false);
        try {

            //if not valid, it will throw ParseException
            Date date = sdf.parse(dateToValidate);
            System.out.println(date);

        } catch (ParseException e) {

            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void setDaily(Date start_date, Date end_date, CaldroidFragment caldroidFragment, int current_month, int current_year) {
        current_month = current_month - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start_date);
        int date_month = calendar.get(Calendar.MONTH);
        int date_year = calendar.get(Calendar.YEAR);
        int start_month = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.MONTH, current_month);
        calendar.set(Calendar.YEAR, current_year);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(start_date.getTime());
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(end_date.getTime());
        Log.e("date", "time " + start_date.getTime() + " " + end_date.getTime() + " " + calendar1.getTimeInMillis() + " " + calendar2.getTimeInMillis());
        if (start_date.equals(end_date)) {
//            calendar2.add(Calendar.YEAR, 5);
            do {
                caldroidFragment.setSelectedDate(calendar1.getTime());
                calendar1.add(Calendar.DAY_OF_MONTH, 1);
                Log.e("same", "date " + calendar1.getTimeInMillis() + " " + calendar2.getTimeInMillis());
            } while (calendar1.getTimeInMillis() <= calendar2.getTimeInMillis());
        } else {

            do {
                caldroidFragment.setSelectedDate(calendar1.getTime());
                calendar1.add(Calendar.DAY_OF_MONTH, 1);
                Log.e("diff", "date " + calendar1.getTimeInMillis() + " " + calendar2.getTimeInMillis());
            } while (calendar1.getTimeInMillis() <= calendar2.getTimeInMillis());
        }
    }


    public static void setWeekly(Date start_date, Date end_date, CaldroidFragment caldroidFragment, int current_month, int current_year) {

        current_month = current_month - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start_date);
        int start_day = calendar.get(Calendar.DAY_OF_WEEK);
        int start_month = calendar.get(Calendar.DAY_OF_MONTH);

        int date_month = calendar.get(Calendar.MONTH);
        int date_year = calendar.get(Calendar.YEAR);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.MONTH, current_month);
        calendar1.set(Calendar.YEAR, current_year);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(start_date.getTime());
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTimeInMillis(end_date.getTime());

        if (start_date.equals(end_date)) {
//            calendar3.add(Calendar.YEAR, 5);
            do {
                if (start_day == calendar2.get(Calendar.DAY_OF_WEEK)) {
                    caldroidFragment.setSelectedDate(calendar2.getTime());
                }
                calendar2.add(Calendar.DAY_OF_MONTH, 1);
            } while (calendar2.getTimeInMillis() <= calendar3.getTimeInMillis());
        } else {
            do {
                if (start_day == calendar2.get(Calendar.DAY_OF_WEEK)) {
                    caldroidFragment.setSelectedDate(calendar2.getTime());
                }
                calendar2.add(Calendar.DAY_OF_MONTH, 1);
            } while (calendar2.getTimeInMillis() <= calendar3.getTimeInMillis());
        }
    }


    public static ArrayList<Integer> getMeetindDaysOfMonth(String timestamp1, String timestamp2) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp1) * 1000);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(Long.parseLong(timestamp2) * 1000);

        if (calendar.getTimeInMillis() == calendar1.getTimeInMillis()) {
            arrayList.add(calendar.get(Calendar.DAY_OF_MONTH));
            return arrayList;
        }

        do {
            arrayList.add(calendar.get(Calendar.DAY_OF_MONTH));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        } while (calendar.getTimeInMillis() <= calendar1.getTimeInMillis());

        return arrayList;
    }


    public static ArrayList<Integer> getMeetingDaysOfWeek(String timestamp1, String timestamp2) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp1) * 1000);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(Long.parseLong(timestamp2) * 1000);

        if (calendar.getTimeInMillis() == calendar1.getTimeInMillis()) {
            arrayList.add(calendar.get(Calendar.DAY_OF_WEEK));
            return arrayList;
        }

        do {
            arrayList.add(calendar.get(Calendar.DAY_OF_WEEK));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        } while (calendar.getTimeInMillis() <= calendar1.getTimeInMillis());
        return arrayList;
    }


    public static void setMonthly(Date start_date, Date end_date, CaldroidFragment caldroidFragment, int current_month, int current_year) {
        current_month = current_month - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start_date);
        int start_day = calendar.get(Calendar.DAY_OF_WEEK);
        int start_month = calendar.get(Calendar.DAY_OF_MONTH);

        int date_month = calendar.get(Calendar.MONTH);
        int date_year = calendar.get(Calendar.YEAR);

        calendar.set(Calendar.MONTH, current_month);
        calendar.set(Calendar.YEAR, current_year);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(start_date.getTime());
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTimeInMillis(end_date.getTime());

        if (start_date.equals(end_date)) {
//            calendar3.add(Calendar.YEAR, 5);
            do {
                int calender_month = calendar2.get(Calendar.DAY_OF_MONTH);
                if (start_month == calendar.getActualMaximum(Calendar.DAY_OF_MONTH) && calender_month == calendar2.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    caldroidFragment.setSelectedDate(calendar2.getTime());
                } else if (start_month == calender_month) {
                    caldroidFragment.setSelectedDate(calendar2.getTime());
                }
                calendar2.add(Calendar.DAY_OF_MONTH, 1);
            } while (calendar2.getTimeInMillis() <= calendar3.getTimeInMillis());
        } else {
            do {
                int calender_month = calendar2.get(Calendar.DAY_OF_MONTH);
                if (start_month == calendar.getActualMaximum(Calendar.DAY_OF_MONTH) && calender_month == calendar2.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    caldroidFragment.setSelectedDate(calendar2.getTime());
                } else if (start_month == calender_month) {
                    caldroidFragment.setSelectedDate(calendar2.getTime());
                }
                calendar2.add(Calendar.DAY_OF_MONTH, 1);
            } while (calendar2.getTimeInMillis() <= calendar3.getTimeInMillis());
        }
    }


    public static void setYearly(Date start_date, Date end_date, CaldroidFragment caldroidFragment, int current_month, int current_year) {
        current_month = current_month - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start_date);
        int start_month = calendar.get(Calendar.DAY_OF_MONTH);
        int date_month = calendar.get(Calendar.MONTH);
        int date_year = calendar.get(Calendar.YEAR);

        calendar.set(Calendar.MONTH, current_month);
        calendar.set(Calendar.YEAR, current_year);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(start_date.getTime());
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTimeInMillis(end_date.getTime());

        if (start_date.equals(end_date)) {
//            calendar3.add(Calendar.YEAR, 10);
            do {
                int calender_month = calendar2.get(Calendar.DAY_OF_MONTH);
                if (start_month == calender_month && date_month == calendar2.get(Calendar.MONTH)) {
                    caldroidFragment.setSelectedDate(calendar2.getTime());
                }
                calendar2.add(Calendar.DAY_OF_MONTH, 1);
            } while (calendar2.getTimeInMillis() <= calendar3.getTimeInMillis());
        } else {
            do {
                int calender_month = calendar2.get(Calendar.DAY_OF_MONTH);
                if (start_month == calender_month && date_month == calendar2.get(Calendar.MONTH)) {
                    caldroidFragment.setSelectedDate(calendar2.getTime());
                }
                calendar2.add(Calendar.DAY_OF_MONTH, 1);
            } while (calendar2.getTimeInMillis() <= calendar3.getTimeInMillis());
        }
    }


    public static String getHourMinute(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            // calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getDateCurrentTimeZone(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            //calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getDateCurrentTimeZone1(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            //calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }

    public static long getTimestamp(String str_date) {
        long time_stamp = 0;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            Date date = (Date) formatter.parse(str_date);
            time_stamp = date.getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return time_stamp;
    }


    public static long getTimestamp1(String str_date) {
        long time_stamp = 0;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date date = (Date) formatter.parse(str_date);
            time_stamp = date.getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return time_stamp;
    }

    public static boolean isConfigRtl(Context context) {
        /*boolean is_rtl = false;
        SavePref savePref = new SavePref();
        String language = "";
        if (savePref.getString(context, "app_language", "en").equals("en")) {
            is_rtl = false;
            language = "en";
        } else {
            is_rtl = true;
            language = "ar";
        }

        Locale locale = new Locale(language);
        locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLayoutDirection(locale);
        config.setLocale(locale);*/

        Locale current = context.getResources().getConfiguration().locale;
        Log.e("current_lang", "here " + current.getLanguage());
        boolean is_rtl = false;
        Configuration config = context.getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            is_rtl = true;
        } else {
            is_rtl = false;
        }
        return is_rtl;
    }

    /*public static boolean isConfigRtl(Context context) {
        boolean is_rtl = false;
        SavePref savePref = new SavePref();
        if (savePref.getString(context, "app_language", "en").equals("en")) {
            is_rtl = false;
        } else {
            is_rtl = true;
        }
        return is_rtl;
    }*/

    public static String getNotificationTime(long time_stamp) {
        Date date = null;
        try {
            date = new Date(time_stamp * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("dateeee" + date.toString());

        String string_date = "";
        Date current = Calendar.getInstance().getTime();
        long diffInSeconds = (current.getTime() - date.getTime()) / 1000;
        long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
        long weeks = days / 7;
        long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
        long years = (diffInSeconds = (diffInSeconds / 12));

        if (years > 0) {
            if (years == 1) {
                string_date = "1 year";
            } else {
                string_date = years + " years";
            }
        } else if (months > 0) {
            if (months == 1) {
                //sb.append("a month");
                string_date = "1 month";
            } else {
                //sb.append(months + " months");
                string_date = months + " months";
            }
        } else if (weeks > 0) {
            if (weeks == 1) {
                //sb.append("a month");
                string_date = "1 week";
            } else {
                //sb.append(months + " months");
                string_date = weeks + " Weeks";
            }
        } else if (days > 0) {
            if (days == 1) {
                //sb.append("a day");
                string_date = "1 day";
            } else {
                // sb.append(days + " days");
                string_date = days + " days";

            }
        } else if (hrs > 0) {
            if (hrs == 1) {
                //sb.append("an hour");
                string_date = "1 hour";
            } else {
                //sb.append(hrs + " hours");
                string_date = hrs + " hours";
            }
        } else if (min > 0) {
            if (min == 1) {
                //sb.append("a minute");
                string_date = "1 minute";
            } else {
                //sb.append(min + " minutes");
                string_date = min + " minutes";
            }
        }

        string_date = string_date + " ago";
        return string_date;
    }

    public static String bodyToString(final RequestBody request) {

        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            copy.writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}


