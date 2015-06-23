package uk.thinkling.simples;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    View myDrawView;
    Handler mHandler;
    SoundPool player;
    int clinkSound, clunkSound, placeSound, slideSound;

    public TextView ScoreText,TimeLeftText,HighScoreText;
    ViewGroup parent;
    int index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find the text views
        ScoreText = (TextView) findViewById(R.id.ScoreView);
        TimeLeftText = (TextView) findViewById(R.id.TimeLeftView);
        HighScoreText = (TextView) findViewById(R.id.HighScoreText);
        HighScoreText = (TextView) findViewById(R.id.HighScoreText);

        // find the drawView, parent and index (for switching)
        myDrawView = findViewById(R.id.drawView);
        parent = (ViewGroup) myDrawView.getParent();
        index = parent.indexOfChild(myDrawView);

        // start the timer handler that will invalidate the view
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 1000);

        //set up the sound player

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createSoundPoolWithBuilder();
        } else{
            createSoundPoolWithConstructor();
        }
        clinkSound = player.load(this, R.raw.clink, 1);
        clunkSound = player.load(this, R.raw.clunk, 1);
        placeSound = player.load(this, R.raw.place, 1);
        slideSound = player.load(this, R.raw.slide, 1);
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Save application preferences data - settings should also be stored here
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("hiscore", 99);
        editor.putString("test", "preferences OK");
        editor.commit();

        if (myDrawView instanceof DrawView3) {
            // serialize
            try {
                ((DrawView3) myDrawView).saveData();
                Toast.makeText(getBaseContext(), "onPause - OK", Toast.LENGTH_SHORT).show();
            } catch (IOException ex) {
                Toast.makeText(getBaseContext(), "onPause - Fail", Toast.LENGTH_SHORT).show();
                Log.d("onPause", ex.toString());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(getBaseContext(), "onStart", Toast.LENGTH_SHORT).show();
        String test ="";
        try {
            //Load lists from file or set defaults for some reason, | is not good delimiter
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int hiscore = preferences.getInt("hiscore", 0);
            test = preferences.getString("test", "prefs not found");

        } catch (Exception e){
            Log.d("LOADING LIST",  e.getMessage());
        }
        Toast.makeText(this, test, Toast.LENGTH_SHORT).show();


    }

    // Sound pool builder - TODO should converge with constructor version
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createSoundPoolWithBuilder(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        player = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(6).build();
    }

    @SuppressWarnings("deprecation")
    protected void createSoundPoolWithConstructor(){
        player = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
    }

    // this is the runnable action that is called by the handler - it will add itself to the handler again after delay
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            /** invalidate the view - forcing it to redraw **/
            myDrawView.invalidate();

            //do this again in 40 milliseconds - ie. 1/25th of a second
            mHandler.postDelayed(mRunnable, 40);
        }
    };


    @Override
    // Create the top bar menu options
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    //handle the clicks on the top bar menu options
        public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        parent.removeView(myDrawView);

        switch (item.getItemId()){

            case R.id.action_settings:
                File file = new File(getCacheDir(), "moveObjs");
                if (file.exists()) file.delete();
                file = new File(getCacheDir(), "Scores");
                if (file.exists()) file.delete();
                return true;

            case R.id.action_start1:
                myDrawView = new DrawView(this , null);
                break;

            case R.id.action_start2:
                myDrawView = new DrawView2(this , null);
                break;

            case R.id.action_start3:
                myDrawView = new DrawView3(this , null);
                break;

            case R.id.action_start4:
                myDrawView = new DrawView4(this , null);
                break;

            default:

        }

        parent.addView(myDrawView, index);
        return super.onOptionsItemSelected(item);
    }


}
