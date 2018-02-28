package ind.mesut.ready;

import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.os.StrictMode;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.VoiceInteractor;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.PickOptionRequest.Option;

import java.util.Map;
import java.util.StringTokenizer;

public class main extends AppCompatActivity {

    //Initialization of Mediaplayer
    MediaPlayer mP;
    SeekBar speed;
    boolean autoPlay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mP = MediaPlayer.create(this, R.raw.sample);
        final Button startButton = (Button) findViewById(R.id.playButton);
        speed = (SeekBar) findViewById(R.id.speed);
        final TextView textV = (TextView) findViewById(R.id.textView);
        autoPlay = false;

        //Set Playing Speed
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float speed = 0.75f+i*0.25f;
                textV.setText("Speed: " + Float.toString(speed));
                if(mP.isPlaying()){
                    mP.setPlaybackParams(mP.getPlaybackParams().setSpeed(speed));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mP.isPlaying()){
                    mP.pause();
                    startButton.setText("Start");
                    autoPlay = false;

                }else{
                    mP.start();
                    startButton.setText("Pause");
                    autoPlay = true;
                }
                /*
                Log.d("position: ", Integer.toString(mP.getCurrentPosition()));
                Log.d("position: ", Integer.toString(mP.getDuration()));
                mP.stop();
                try{
                    mP.prepare();
                } catch (Exception e){};
                */
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("mode:", "ondestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("mode:", "onPause");
        if (mP.isPlaying()){
            mP.pause();
            autoPlay = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get intent at onResume
        Intent receiveCommand = getIntent();
        if (receiveCommand.hasExtra("cancelledVI")) { autoPlay = receiveCommand.getBooleanExtra("cancelledVI", false);}
        if (receiveCommand.hasExtra("commandId")){
            switch (receiveCommand.getIntExtra("commandId", 0)){
                case 0:
                    //Starting book
                    mP.start();
                    Log.d("case:", "start");
                    break;
                case 1:
                    //Pausing Book
                    mP.pause();
                    autoPlay = false;
                    Log.d("case:", "pause");
                    break;
                case 2:
                    //Speeding Up
                    Log.d("case:", "speedUp");
                    if(speed.getProgress() != 5){
                        speed.setProgress(speed.getProgress() + 1);
                    } else{
                        Toast.makeText(this, "Speed is highest already", Toast.LENGTH_LONG);
                    }
                    break;
                case 3:
                    //Speeding Down
                    Log.d("case:", "speedDown");
                    if(speed.getProgress() != 0){
                        speed.setProgress(speed.getProgress() - 1);
                    } else{
                        Toast.makeText(this, "Speed is lovest already", Toast.LENGTH_LONG);
                    }
                    break;
                case 4:
                    //View Book Detail
                    //Intent
                    break;
            }
        }
        // If no intent
        else{
            //autoPlay is stand for pausing the book for Voice Interaction use to avoid the intervention of TTS sound and book sound
            if (autoPlay){
                mP.start();
            }
        }
    }
}
