package com.omorni.activity;


import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.omorni.R;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

;

public class VideoPlayActivity extends BaseActivity {
    VideoView videoView;
    private SavePref savePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        savePref = new SavePref(VideoPlayActivity.this);

        final ProgressDialog progressDialog = Utils.initializeProgress(VideoPlayActivity.this);
        progressDialog.show();
        videoView = (VideoView) findViewById(R.id.video_view);
        try {
            MediaController mediacontroller = new MediaController(VideoPlayActivity.this);
            mediacontroller.setMediaPlayer(videoView);
            mediacontroller.setAnchorView(videoView);
            videoView.setMediaController(mediacontroller);
            videoView.requestFocus();
            Uri video_uri = Uri.parse(getIntent().getStringExtra("url"));
            videoView.setVideoURI(video_uri);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            if (progressDialog != null)
                progressDialog.dismiss();
            e.printStackTrace();
        }

        if (Utils.isNetworkAvailable(VideoPlayActivity.this)) {


//            Intent mServiceIntent = new Intent(VideoPlayActivity.this, ConnectionManager.class);
//            mServiceIntent.putExtra("event", 0);
//            mServiceIntent.putExtra("user_id", savePref.getJid());
//            mServiceIntent.putExtra("password", "123456");
//            startService(mServiceIntent);
        }
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                try {
                    if (progressDialog != null)
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    if (progressDialog != null)
                        progressDialog.dismiss();
                    // Handle or log or ignore
                } catch (final Exception e) {
                    if (progressDialog != null)
                        progressDialog.dismiss();
                    // Handle or log or ignore
                }
                videoView.start();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        int current_position = videoView.getCurrentPosition();
//        Log.e("position", "saved " + current_position);
//        outState.putInt("position", current_position);
//        Log.e("position", "saved " + outState.getInt("position"));

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        int current_position = savedInstanceState.getInt("position");
//        Log.e("position", "restore " + current_position);
//        videoView.seekTo(current_position);
//        videoView.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else {
        }
    }


}
