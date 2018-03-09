package ind.mesut.ready;

import android.app.SearchManager;
import android.app.VoiceInteractor;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.os.StrictMode;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import safety.com.br.android_shake_detector.core.ShakeCallback;
import safety.com.br.android_shake_detector.core.ShakeDetector;
import safety.com.br.android_shake_detector.core.ShakeOptions;

public class main extends AppCompatActivity {

    //Initialization of Mediaplayer
    MediaPlayer mP;
    SeekBar speed;
    TextView textV;
    String listening = "Dinliyorum...";
    Integer speechInputCode = 7823;

    private ShakeDetector shakeDetector;

    String[][] commands = new String[][] {
            {"play", "ready", "go", "take it", "ok", "Play", "Oynat"},
            {"pause", "stop", "wait", "Pause", "Durdur"},
            {"speed up", "faster", "increase", "fast", "up", "speed up", "hızlı", "hızlan"},
            {"speed down", "slow", "slower", "decrease", "down", "Yavaş", "yavaşla"},
            {"view", "info", "detail", "about", "bilgi"},
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mP = MediaPlayer.create(this, R.raw.sample);
        final Button startButton = (Button) findViewById(R.id.playButton);
        final Button micButton = (Button) findViewById(R.id.micButton);
        speed = (SeekBar) findViewById(R.id.speed);
        final TextView textV = (TextView) findViewById(R.id.textView);

        ShakeOptions options = new ShakeOptions().background(true).interval(1000).shakeCount(2).sensibility(2.0f);

        this.shakeDetector = new ShakeDetector(options).start(this, new ShakeCallback() {
            @Override
            public void onShake() {
                promptSpeechInput();
            }
        });

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

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mP.isPlaying()){
                    mP.pause();
                    startButton.setText("Start");


                }else{
                    mP.start();
                    startButton.setText("Pause");

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
        shakeDetector.destroy(getBaseContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("mode:", "onPause");
        if (mP.isPlaying()){
            mP.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get intent at onResume
        Intent receiveCommand = getIntent();
        if (receiveCommand.hasExtra("commandId")){
            applyCommand(receiveCommand.getIntExtra("commandId", 0));
        }
        // If no intent
        else{
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, listening);
        try {
            startActivityForResult(intent, speechInputCode);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Dinleyemiyorum", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 7823: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String output = result.get(0);
                    Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
                    Log.d("Voice", output);
                    String[] dictatedWords = output.split(" ");
                    for (int i = 0; i < dictatedWords.length; i++) {
                        String dictatedWord = dictatedWords[i];
                        Log.d("word", dictatedWord);
                        Integer commandId = seekCommand(dictatedWord);
                        Log.d("int", String.valueOf(commandId));
                            if (commandId >= 0) {
                                applyCommand(commandId);
                                break;
                            }
                        }
                    }
                }
                break;
            }

        }

    protected int seekCommand(String dictatedWord){
        for (int i = 0; i < commands.length; i++) {
            for (int j = 0; j < commands[i].length; j++) {
                if(dictatedWord.toLowerCase().equals(commands[i][j].toLowerCase())){
                    return i;
                }
            }
        }
        return -1;
    }

    protected void applyCommand(Integer commandId){
        switch (commandId){
            case 0:
                //Starting book
                mP.start();
                Log.d("case:", "start");
                break;
            case 1:
                //Pausing Book
                mP.pause();
                Log.d("case:", "pause");
                break;
            case 2:
                //Speeding Up
                Log.d("case:", "speedUp");
                if(speed.getProgress() != 5){
                    speed.setProgress(speed.getProgress() + 1);
                } else{
                    Toast.makeText(this, "Speed is highest already", Toast.LENGTH_LONG).show();
                }
                break;
            case 3:
                //Speeding Down
                Log.d("case:", "speedDown");
                if(speed.getProgress() != 0){
                    speed.setProgress(speed.getProgress() - 1);
                } else{
                    Toast.makeText(this, "Speed is lovest already", Toast.LENGTH_LONG).show();
                }
                break;
            case 4:
                //View Book Detail
                //Intent
                break;
        }
    }
}
