package com.omorni.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;
import com.omorni.adapter.ChattingAdapter;
import com.omorni.model.GetChatResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ChattingActivity extends BaseActivity implements View.OnClickListener {
    private ListView listView;
    private ChattingAdapter adapter;
    private ImageView back, send;
    private SavePref savePref;
    private EditText edt_msg;
    private ArrayList<GetChatResponse> arrayList;
    private ImageView user_image, plus, camera;
    private TextView username;
    String opponent_name = "", opponent_image = "", thread_id = "", opponent_id = "", req_id = "", message_type = "";
    private RelativeLayout bottom_layout;

    protected static final int GALLERY = 1;
    protected static final int CAMARA = 2;
    protected static final int VIDEO = 3;
    private String selectedimage = "", selectedvideo = "";
    private String thumbnail_path = "";

    Dialog dialog;
    File file_upload;
    ImageView attachment;
    private LinearLayout attachment_layout;
    private TextView gallery, video;
    String fname_camera = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(ChattingActivity.this);
        if (Utils.isConfigRtl(ChattingActivity.this)) {
            // 1 means seller
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_chatting_seller_rtl);
            }// 2 means buyer
            else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_chatting_rtl);
            }
        } else {
            // 1 means seller
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_chatting_seller);
            }// 2 means buyer
            else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_chatting);
            }
        }

        opponent_name = getIntent().getStringExtra("opponent_name");
        opponent_image = getIntent().getStringExtra("opponent_image");
        thread_id = getIntent().getStringExtra("thread_id");
        opponent_id = getIntent().getStringExtra("opponent_id");
        req_id = getIntent().getStringExtra("req_id");

        initialize();
        setData();


    }

    private void Camera() {
        fname_camera = String.valueOf(System.currentTimeMillis() / 1000);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file_upload = new File(Environment.getExternalStorageDirectory() + File.separator + fname_camera + ".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file_upload));
        startActivityForResult(intent, CAMARA);
    }

    private void Gallery() {
        Intent photoPickerIntent = new Intent(
                Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY) {
                if (resultCode == RESULT_OK) {
                    selectedimage = getAbsolutePath(ChattingActivity.this, data.getData());
                    openDialog();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Cancelled",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMARA) {
                if (resultCode == RESULT_OK) {
                    String picturePath = file_upload.getAbsolutePath();

//                Bitmap photo = (Bitmap) data.getExtras().get("data");
//                Uri tempUri = getImageUri(getApplicationContext(), photo);
//                File finalFile = new File(getRealPathFromURI(tempUri));
//                selectedimage = finalFile.toString();
                    message_type = "1";
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + fname_camera + ".jpg");
//                selectedimage = file_upload.getAbsolutePath();
                    sendCameraImage(file);
//                openDialog();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Cancelled",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == VIDEO) {
                selectedvideo = getAbsolutePath(ChattingActivity.this, data.getData());
                Bitmap video_bitmap = ThumbnailUtils.createVideoThumbnail(selectedvideo, MediaStore.Video.Thumbnails.MINI_KIND);
                thumbnail_path = getInsertedPath(video_bitmap, "", "image");
                message_type = "2";
                sendMessageImageApi(selectedvideo);
            }
        }
    }

    private void openDialog() {
        dialog = new Dialog(ChattingActivity.this, R.style.Theme_Dialog_preview);
        dialog.setContentView(R.layout.image_dialog);
        dialog.setCancelable(false);
        dialog.show();
        LinearLayout bottom_layout = (LinearLayout) dialog.findViewById(R.id.bottom_layout);
        ImageView selectImageView = (ImageView) dialog.findViewById(R.id.select_image);
        TextView send_image = (TextView) dialog.findViewById(R.id.send_image);
        TextView cancel_image = (TextView) dialog.findViewById(R.id.cancel_image);
        Bitmap bitmapImage = BitmapFactory.decodeFile(selectedimage);
        selectImageView.setImageBitmap(bitmapImage);
//        Glide.with(ChattingActivity.this).load(selectedimage).placeholder(R.drawable.camera_placeholder).override(500, 500).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(selectImageView);
        if (savePref.getUserType().equals("1")) {
            cancel_image.setBackground(getResources().getDrawable(R.drawable.sort_bgd_seller_drawable));
            send_image.setBackground(getResources().getDrawable(R.drawable.sort_bgd_seller_drawable));
        } else {
            send_image.setBackground(getResources().getDrawable(R.drawable.sort_bg_drawable));
            cancel_image.setBackground(getResources().getDrawable(R.drawable.sort_bg_drawable));
        }
        cancel_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectedimage = "";
            }
        });

        send_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message_type = "1";
                sendMessageImageApi(selectedimage);
            }
        });
    }

    public static String getAbsolutePath(Activity activity, Uri uri) {

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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }


    /*public static void ImageUploadDialog(final FragmentActivity activity) {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(activity);
        myAlertDialog.setTitle("Upload Image Option");
        myAlertDialog.setMessage("How do you want to set your Image?");

        myAlertDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        Intent photoPickerIntent = new Intent(
                                Intent.ACTION_PICK);
                        photoPickerIntent.setType("image*//*");
                        activity.startActivityForResult(photoPickerIntent, GALLERY);
                    }
                });

        myAlertDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent pictureActionIntent = new Intent(
                                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        activity.startActivityForResult(pictureActionIntent, CAMARA);

                    }
                });

        myAlertDialog.show();
    }*/


    private void initialize() {
        listView = (ListView) findViewById(R.id.listView);
        back = (ImageView) findViewById(R.id.back);
        send = (ImageView) findViewById(R.id.send);
        plus = (ImageView) findViewById(R.id.plus);
        camera = (ImageView) findViewById(R.id.camera);
        user_image = (ImageView) findViewById(R.id.user_image);
        username = (TextView) findViewById(R.id.username);

        gallery = (TextView) findViewById(R.id.gallery);
        video = (TextView) findViewById(R.id.video);

        edt_msg = (EditText) findViewById(R.id.edt_msg);
        attachment = (ImageView) findViewById(R.id.attachment);
        bottom_layout = (RelativeLayout) findViewById(R.id.bottom_layout);
        attachment_layout = (LinearLayout) findViewById(R.id.attachment_layout);

        arrayList = new ArrayList<>();
        adapter = new ChattingAdapter(ChattingActivity.this, arrayList);
        listView.setAdapter(adapter);
        Log.e("status", "job " + getIntent().getStringExtra("job_status").equals("1"));
        if (getIntent().getStringExtra("job_status").equals("1")) {
            bottom_layout.setVisibility(View.GONE);
        }

        back.setOnClickListener(this);
        send.setOnClickListener(this);
        plus.setOnClickListener(this);
        camera.setOnClickListener(this);
        gallery.setOnClickListener(this);
        video.setOnClickListener(this);
    }

    private void setData() {
        username.setText(opponent_name);
        Glide.with(ChattingActivity.this).load(opponent_image).placeholder(R.drawable.user_placeholder_white).override(150, 150).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(user_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
        savePref.setUserOnline(true);
        savePref.setThreadId(thread_id);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(Utils.NOTIFICATION_MESSAGE));
        getChatApi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePref.setUserOnline(false);
        savePref.setThreadId("");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private BroadcastReceiver
            mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            GetChatResponse getChatResponse = new GetChatResponse();
            getChatResponse.setMessage(intent.getStringExtra("message"));
            getChatResponse.setThread_id(intent.getStringExtra("thread_id"));
            getChatResponse.setReceiver(intent.getStringExtra("receiver"));
            getChatResponse.setSender(intent.getStringExtra("opponent_id"));
            getChatResponse.setUser_image(intent.getStringExtra("opponent_image"));
            getChatResponse.setMessage_type(intent.getStringExtra("message_type"));
            getChatResponse.setCreated(intent.getStringExtra("created"));
            if (intent.getStringExtra("message_type").equals("2"))
                getChatResponse.setThumb(intent.getStringExtra("thumb"));
            arrayList.add(arrayList.size(), getChatResponse);
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                attachment_layout.setVisibility(View.GONE);
                onBackPressed();

                break;
            case R.id.send:
                attachment_layout.setVisibility(View.GONE);
                if (edt_msg.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.message_error), ChattingActivity.this);
                } else {
                    message_type = "0";
                    sendMessageApi();
                }
                break;
            case R.id.plus:
                if (attachment_layout.getVisibility() == View.VISIBLE) {
                    attachment_layout.setVisibility(View.GONE);
                } else {
                    attachment_layout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.camera:
                attachment_layout.setVisibility(View.GONE);
                Camera();
                break;

            case R.id.gallery:
                attachment_layout.setVisibility(View.GONE);
                Gallery();
                break;
            case R.id.video:
                attachment_layout.setVisibility(View.GONE);
                Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
                videoPickerIntent.setType("video/*");
                startActivityForResult(videoPickerIntent, VIDEO);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("from_track", true);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void sendMessageApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(ChattingActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.SENDER, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.RECEIVER, opponent_id);
        formBuilder.add(AllOmorniParameters.MESSAGE, edt_msg.getText().toString());
        formBuilder.add(AllOmorniParameters.REQUEST_ID, req_id);
        formBuilder.add(AllOmorniParameters.THREAD_ID, thread_id);
        formBuilder.add(AllOmorniParameters.MESSAGE_TYPE, message_type);
        formBuilder.add(AllOmorniParameters.CREATED, String.valueOf(System.currentTimeMillis() / 1000));

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(ChattingActivity.this, AllOmorniApis.SEND_MESSAGES, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            GetChatResponse getChatResponse = new GetChatResponse();
                            getChatResponse.setMessage(edt_msg.getText().toString());
                            getChatResponse.setSender(savePref.getUserId());
                            getChatResponse.setReceiver(opponent_id);
                            getChatResponse.setThread_id(thread_id);
                            getChatResponse.setMessage_type(message_type);
                            getChatResponse.setCreated(jsonObject1.getString("created"));
                            arrayList.add(getChatResponse);
                            edt_msg.setText("");
                            adapter.notifyDataSetChanged();
                        } else {
                            Utils.showToast(ChattingActivity.this, status.getString("message"));
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
                progressDialog.dismiss();
            }
        };
        mAsync.execute();
    }

    private void getChatApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(ChattingActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.SENDER, opponent_id);
        formBuilder.add(AllOmorniParameters.RECEIVER, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.THREAD_ID, thread_id);
        formBuilder.add(AllOmorniParameters.REQUEST_ID, req_id);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(ChattingActivity.this, AllOmorniApis.GET_MESSAGES, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();

                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            if (arrayList.size() > 0)
                                arrayList.clear();
                            JSONArray jsonArray = jsonObject.getJSONArray("body");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                GetChatResponse getChatResponse = new GetChatResponse();
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                getChatResponse.setMessage(jsonObject1.getString("message"));
                                getChatResponse.setCreated(jsonObject1.getString("created"));
                                getChatResponse.setThread_id(jsonObject1.getString("thread_id"));
                                getChatResponse.setReceiver(jsonObject1.getString("receiver"));
                                getChatResponse.setSender(jsonObject1.getString("sender"));
                                getChatResponse.setMsg_id(jsonObject1.getString("msg_id"));
                                getChatResponse.setMessage_type(jsonObject1.getString("message_type"));
                                getChatResponse.setUser_image(jsonObject1.getString("user_image"));
                                getChatResponse.setThumb(jsonObject1.getString("thumb"));
                                arrayList.add(getChatResponse);
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Utils.showToast(ChattingActivity.this, status.getString("message"));
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
                progressDialog.dismiss();
            }
        };
        mAsync.execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        opponent_name = intent.getStringExtra("opponent_name");
        opponent_image = intent.getStringExtra("opponent_image");
        thread_id = intent.getStringExtra("thread_id");
        opponent_id = intent.getStringExtra("opponent_id");
        setData();
        getChatApi();
    }

    private void sendMessageImageApi(String path) {
        RequestBody requestBody = null;
        final ProgressDialog progressDialog = new ProgressDialog(ChattingActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        MediaType MEDIA_TYPE = null;
        MediaType MEDIA_TYPE_THUMB = null;
        File File_thumb = null;
        if (message_type.equals("1")) {
            MEDIA_TYPE = path.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
//            uploadFileName = "message_url.png";
        } else if (message_type.equals("2")) {
            MEDIA_TYPE = path.endsWith("mp4") ? MediaType.parse("video/mp4") : MediaType.parse("video/mp4");
//            MEDIA_TYPE = path.endsWith("mp4") ? MediaType.parse("/mp4") : MediaType.parse("/mp4");
            MEDIA_TYPE_THUMB = thumbnail_path.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            File_thumb = new File(thumbnail_path);
//            uploadFileName = "message_url.mp4";
        }

        File file = new File(path);
        //MEDIA_TYPE_IMAGE = selectedimage.endsWith("png") ? MediaType.parse("/png") : MediaType.parse("/png");
//        MEDIA_TYPE_IMAGE = selectedimage.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
        if (message_type.equals("1")) {
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(AllOmorniParameters.SENDER, savePref.getUserId())
                    .addFormDataPart(AllOmorniParameters.RECEIVER, opponent_id)
                    .addFormDataPart(AllOmorniParameters.MESSAGE, file.getName(), RequestBody.create(MEDIA_TYPE, file))
                    .addFormDataPart(AllOmorniParameters.REQUEST_ID, req_id)
                    .addFormDataPart(AllOmorniParameters.THREAD_ID, thread_id)
                    .addFormDataPart(AllOmorniParameters.MESSAGE_TYPE, "1")
                    .addFormDataPart(AllOmorniParameters.CREATED, String.valueOf(System.currentTimeMillis() / 1000))
                    .build();
        } else if (message_type.equals("2")) {
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(AllOmorniParameters.SENDER, savePref.getUserId())
                    .addFormDataPart(AllOmorniParameters.RECEIVER, opponent_id)
                    .addFormDataPart(AllOmorniParameters.MESSAGE, file.getName(), RequestBody.create(MEDIA_TYPE, file))
                    .addFormDataPart(AllOmorniParameters.THUMB, File_thumb.getName(), RequestBody.create(MEDIA_TYPE_THUMB, File_thumb))
                    .addFormDataPart(AllOmorniParameters.REQUEST_ID, req_id)
                    .addFormDataPart(AllOmorniParameters.THREAD_ID, thread_id)
                    .addFormDataPart(AllOmorniParameters.MESSAGE_TYPE, "2")
                    .addFormDataPart(AllOmorniParameters.CREATED, String.valueOf(System.currentTimeMillis() / 1000))
                    .build();
        }
        GetAsync mAsync = new GetAsync(this, AllOmorniApis.SEND_MESSAGES, requestBody) {
            @Override
            public void getValueParse(String result) {
                Log.e("result", "here " + result);
                progressDialog.dismiss();
                if (dialog != null)
                    dialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            GetChatResponse getChatResponse = new GetChatResponse();
                            getChatResponse.setMessage(jsonObject1.getString("message"));
                            getChatResponse.setSender(savePref.getUserId());
                            getChatResponse.setReceiver(opponent_id);
                            getChatResponse.setThread_id(thread_id);
                            getChatResponse.setMessage_type(message_type);
                            getChatResponse.setThumb(jsonObject1.getString("thumb"));
                            getChatResponse.setCreated(jsonObject1.getString("created"));
                            arrayList.add(getChatResponse);
                            edt_msg.setText("");
                            selectedimage = "";
                            adapter.notifyDataSetChanged();
                        } else {
                            Utils.showToast(ChattingActivity.this, status.getString("message"));
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

    private void sendCameraImage(final File file) {
        RequestBody requestBody = null;
        final ProgressDialog progressDialog = new ProgressDialog(ChattingActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        MediaType MEDIA_TYPE = null;
        if (message_type.equals("1")) {
            MEDIA_TYPE = file.getAbsolutePath().endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
//            uploadFileName = "message_url.png";
        } else if (message_type.equals("4")) {
            MEDIA_TYPE = file.getAbsolutePath().endsWith("mp4") ? MediaType.parse("/mp4") : MediaType.parse("/mp4");
//            uploadFileName = "message_url.mp4";
        }

//        File file = new File(path);
        //MEDIA_TYPE_IMAGE = selectedimage.endsWith("png") ? MediaType.parse("/png") : MediaType.parse("/png");
//        MEDIA_TYPE_IMAGE = selectedimage.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
        requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(AllOmorniParameters.SENDER, savePref.getUserId())
                .addFormDataPart(AllOmorniParameters.RECEIVER, opponent_id)
                .addFormDataPart(AllOmorniParameters.MESSAGE, file.getName(), RequestBody.create(MEDIA_TYPE, file))
                .addFormDataPart(AllOmorniParameters.REQUEST_ID, req_id)
                .addFormDataPart(AllOmorniParameters.THREAD_ID, thread_id)
                .addFormDataPart(AllOmorniParameters.MESSAGE_TYPE, message_type)
                .addFormDataPart(AllOmorniParameters.CREATED, String.valueOf(System.currentTimeMillis() / 1000))
                .build();

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.SEND_MESSAGES, requestBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                if (dialog != null)
                    dialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            GetChatResponse getChatResponse = new GetChatResponse();
                            getChatResponse.setMessage(jsonObject1.getString("message"));
                            getChatResponse.setSender(savePref.getUserId());
                            getChatResponse.setReceiver(opponent_id);
                            getChatResponse.setThread_id(thread_id);
                            getChatResponse.setMessage_type(message_type);
                            getChatResponse.setThumb(jsonObject1.getString("thumb"));
                            getChatResponse.setCreated(jsonObject1.getString("created"));
                            arrayList.add(getChatResponse);
                            edt_msg.setText("");
                            selectedimage = "";
                            adapter.notifyDataSetChanged();
                        } else {
                            Utils.showToast(ChattingActivity.this, status.getString("message"));
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

    public String getInsertedPath(Bitmap bitmap, String path, String file_name) {
        String strMyImagePath = null;
//        File mFolder = new File(myDir_omorni);
        if (!Utils.myDir_omorni.exists()) {
            Utils.myDir_omorni.mkdir();
        }
        String s = file_name + ".jpg";
        File f = new File(Utils.myDir_omorni, s);

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
}
