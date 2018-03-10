package ind.mesut.ready;

import android.app.Dialog;
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
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
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

    //TODO: String Resource Localization
    //TODO: String Localizations
    //TODO: create command help content
    //TODO: create bookinfo help content

    //Initialization of Mediaplayer
    MediaPlayer mP;
    SeekBar speed;
    TextView textV;

    //Request code defined for RecognizerIntent at promptSpeechInput() method
    Integer speechInputCode = 7823;

    //Text to Speech Initiation for Voiced Warning Message
    TextToSpeech tts = new TextToSpeech(main.this, new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int i) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getApplicationContext(), getString(R.string.unsupportedLanguage), Toast.LENGTH_SHORT).show();
            }
        }
    });

    //Initiation of 3rd party Shaking tool
    private ShakeDetector shakeDetector;

    /*Command list structure
    0: play
    1: pause
    2: speedUp
    3: speedDown
    4:viewInfo */
    String[][] commands = new String[][] {
            {"play", "ready", "go", "take it", "ok", "play", "oynat"},
            {"pause", "stop", "wait", "pause", "durdur"},
            {"up", "faster", "increase", "fast", "up", "speed up", "hızlı", "hızlan"},
            {"down", "slow", "slower", "decrease", "down", "yavaş", "yavaşla"},
            {"view", "info", "detail", "about", "bilgi", "hakkında", "details"},
            {"command", "commands", "komut", "komutlar", "help", "yardım"},
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Mediaplayer creation with a sample mp3 file
        mP = MediaPlayer.create(this, R.raw.sample);

        //UI View Definitions
        final Button startButton = (Button) findViewById(R.id.playButton);
        final Button micButton = (Button) findViewById(R.id.micButton);
        speed = (SeekBar) findViewById(R.id.speed);
        final TextView textV = (TextView) findViewById(R.id.textView);

        // Default Shaking Options
        ShakeOptions options = new ShakeOptions().background(true).interval(1000).shakeCount(2).sensibility(2.0f);

        //onShake Callback trigger promptSpeechInput()
        this.shakeDetector = new ShakeDetector(options).start(this, new ShakeCallback() {@Override public void onShake() {promptSpeechInput();}});

        //micButton onClick trigger promptSpeechInput()
        micButton.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {promptSpeechInput();}});

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Button text set for Start/Pause toggle
                if (mP.isPlaying()){
                    mP.pause();
                    startButton.setText(getString(R.string.start));
                }else{
                    mP.start();
                    startButton.setText(getString(R.string.pause));
                }
                /*
                //Stop method require a prepare method.
                Log.d("position: ", Integer.toString(mP.getCurrentPosition()));
                Log.d("position: ", Integer.toString(mP.getDuration()));
                mP.stop();
                try{
                    mP.prepare();
                } catch (Exception e){};
                */
            }
        });

        //Set Playing Speed on SeekBar
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float speed = 0.75f+i*0.25f;
                textV.setText("Speed: " + Float.toString(speed));
                if(mP.isPlaying()){
                    mP.setPlaybackParams(mP.getPlaybackParams().setSpeed(speed));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("mode:", "ondestroy");
        shakeDetector.destroy(getBaseContext());
        //TODO: Save current playing position
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("mode:", "onPause");
        //TODO: replace pause method with audiofocus method
        if (mP.isPlaying()){
            mP.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get intent at onResume from Recognitor or Voice activity
        Intent receiveCommand = getIntent();
        if (receiveCommand.hasExtra("commandId")){
            //applyCommand method applies related method from commandId
            applyCommand(receiveCommand.getIntExtra("commandId", 0));
        }
    }

    //Voice Recognition Intent Prompt
    private void promptSpeechInput() {
        //Speech Recognition Intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.listening));

        //Starting Recognition Activity
        try {
            startActivityForResult(intent, speechInputCode);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.couldntListening), Toast.LENGTH_SHORT).show();
        }
    }

    //Get result of Speech Recognition Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 7823: {
                if (resultCode == RESULT_OK && null != data) {
                    //Store words at array
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
                //Say warning
                tts.speak("undefined", TextToSpeech.QUEUE_FLUSH, null);
                break;
            }
        }

    //Seek commands from dictated string
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

    //Apply command from commandId input
    protected void applyCommand(Integer commandId){
        switch (commandId){
            case 0:
                //Start playing
                mP.start();
                Log.d("case:", "start");
                break;
            case 1:
                //Pausing playing
                mP.pause();
                Log.d("case:", "pause");
                break;
            case 2:
                //Speeding Up
                Log.d("case:", "speedUp");
                if(speed.getProgress() != 5){
                    speed.setProgress(speed.getProgress() + 1);
                } else{
                    Toast.makeText(this, getString(R.string.highestSpeed), Toast.LENGTH_LONG).show();
                }
                break;
            case 3:
                //Speeding Down
                Log.d("case:", "speedDown");
                if(speed.getProgress() != 0){
                    speed.setProgress(speed.getProgress() - 1);
                } else{
                    Toast.makeText(this, getString(R.string.lowestSpeed), Toast.LENGTH_LONG).show();
                }
                break;
            case 4:
                //View Book Detail
                AlertDialog.Builder bookInfo = new AlertDialog.Builder(this);
                bookInfo.setTitle(getString(R.string.bookDetailTitle));
                bookInfo.setMessage(getString(R.string.bookDetailTitle));
                bookInfo.show();
                break;
            case 5:
                //View Command Details
                AlertDialog.Builder commandsInfo = new AlertDialog.Builder(this);
                commandsInfo.setTitle(getString(R.string.commandDetailTitle));
                commandsInfo.setMessage(getString(R.string.commandDetailContent));
                commandsInfo.show();
                break;
        }
    }
}
