package ind.mesut.ready;

import android.app.Dialog;
import android.app.SearchManager;
import android.app.VoiceInteractor;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.AlarmClock;
import android.provider.MediaStore;
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
import java.util.concurrent.TimeUnit;

import safety.com.br.android_shake_detector.core.ShakeCallback;
import safety.com.br.android_shake_detector.core.ShakeDetector;
import safety.com.br.android_shake_detector.core.ShakeOptions;

public class main extends AppCompatActivity {

    //Initialization of Mediaplayer
    MediaPlayer mP;
    SeekBar speed;
    TextView textV;
    TextView textLocal;
    TextView timeLabel;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    private Handler handler = new Handler();
    AudioManager audioManager;

    //Request code defined for RecognizerIntent at promptSpeechInput() method
    Integer speechInputCode = 7823;

    TextToSpeech tts;

    //Initiation of 3rd party Shaking tool
    private ShakeDetector shakeDetector;

    /*Command list structure
    0: play
    1: pause
    2: speedUp
    3: speedDown
    4: viewInfo
    5: forward
    6: backward*/
    String[][] commands = new String[][] {
            {"play", "ready", "go", "take it", "ok", "play", "oynat"},
            {"pause", "stop", "wait", "pause", "durdur", "dur"},
            {"up", "faster", "increase", "fast", "up", "speed up", "hızlı", "hızlan"},
            {"down", "slow", "slower", "decrease", "down", "yavaş", "yavaşla"},
            {"view", "info", "detail", "about", "bilgi", "hakkında", "details"},
            {"command", "commands", "komut", "komutlar", "help", "yardım"},
            {"ileri", "forward", "next"},
            {"geri", "back", "backward", "previous"},
            {"yüksek", "sesli"},
            {"alçak", "kısık"}
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Mediaplayer creation with a sample mp3 file
        mP = MediaPlayer.create(this, R.raw.bookpart);

        //Text to Speech Initiation for Voiced Warning Message
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                int result = tts.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getApplicationContext(), getString(R.string.unsupportedLanguage), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //UI View Definitions
        final Button startButton = (Button) findViewById(R.id.playButton);
        final Button micButton = (Button) findViewById(R.id.micButton);
        speed = (SeekBar) findViewById(R.id.speed);
        textV = (TextView) findViewById(R.id.textView);
        textLocal = (TextView) findViewById(R.id.localeText);
        timeLabel = (TextView) findViewById(R.id.time);

        //Shared preference for saving playing cursor
        sharedpreferences = getSharedPreferences("cursor", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        //Seek to Saved cursor position
        mP.seekTo(getSharedPreferences("cursor", Context.MODE_PRIVATE).getInt("cursor", 0));

        // Default Shaking Options
        ShakeOptions options = new ShakeOptions().background(true).interval(1000).shakeCount(2).sensibility(2.0f);

        //onShake Callback trigger promptSpeechInput()
        this.shakeDetector = new ShakeDetector(options).start(this, new ShakeCallback() {@Override public void onShake() {promptSpeechInput();}});

        //micButton onClick trigger promptSpeechInput()
        micButton.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {promptSpeechInput();}});

        //Long Click to button resets session data
        micButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                editor.clear().commit();
                mP.seekTo(0);
                Toast.makeText(getApplicationContext(), "Session data is cleaned", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //Init AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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
            }
        });

        //Re Set Duration Text View
        handler.postDelayed(updateBookTime,100);

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
        shakeDetector.destroy(getBaseContext());
        editor.putInt("cursor", mP.getCurrentPosition());
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO: replace pause method with audiofocus method
        if (mP.isPlaying()){
            mP.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        textLocal.setText("Your locale is: " + Locale.getDefault());
        // Get intent at onResume from Recognitor or Voice activity
        Intent receiveCommand = getIntent();
        if (receiveCommand.hasExtra("commandId")){
            //applyCommand method applies related method from commandId
            applyCommand(receiveCommand.getIntExtra("commandId", 0));
        }
    }

    Boolean pausedByIntent = false;

    //Voice Recognition Intent Prompt
    private void promptSpeechInput() {
        //Speech Recognition Intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.listening));

        //Starting Recognition Activity
        try {
            if (mP.isPlaying()){
                mP.pause();
                pausedByIntent = true;

            }
            startActivityForResult(intent, speechInputCode);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.couldntListening), Toast.LENGTH_SHORT).show();
        }
    }

    //Get result of Speech Recognition Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (pausedByIntent){
            mP.start();
            pausedByIntent = false;
        }
        switch (requestCode) {
            case 7823: {
                if (resultCode == RESULT_OK && null != data) {
                    //Store words at array
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String output = result.get(0);
                    Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
                    String[] dictatedWords = output.split(" ");
                    for (int i = 0; i < dictatedWords.length; i++) {
                        String dictatedWord = dictatedWords[i];
                        Integer commandId = seekCommand(dictatedWord);
                            if (commandId >= 0) {
                                applyCommand(commandId);
                                break;
                            }
                        }
                    } else {
                        //Say warning
                        tts.speak("Anlayamıyorum...", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
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
                break;
            case 1:
                //Pausing playing
                if (mP.isPlaying()){
                    mP.pause();
                } else{
                    Toast.makeText(this, getString(R.string.notStarted), Toast.LENGTH_LONG).show();
                    tts.speak(getString(R.string.notStarted), TextToSpeech.QUEUE_FLUSH, null, null);
                }
                break;
            case 2:
                //Speeding Up
                if(speed.getProgress() != 5){
                    speed.setProgress(speed.getProgress() + 1);
                } else{
                    Toast.makeText(this, getString(R.string.highestSpeed), Toast.LENGTH_LONG).show();
                    tts.speak(getString(R.string.highestSpeed), TextToSpeech.QUEUE_FLUSH, null, null);
                }
                break;
            case 3:
                //Speeding Down
                if(speed.getProgress() != 0){
                    speed.setProgress(speed.getProgress() - 1);
                } else{
                    Toast.makeText(this, getString(R.string.lowestSpeed), Toast.LENGTH_LONG).show();
                    tts.speak(getString(R.string.lowestSpeed), TextToSpeech.QUEUE_FLUSH, null, null);
                }
                break;
            case 4:
                //View Book Detail
                AlertDialog.Builder bookInfo = new AlertDialog.Builder(this);
                bookInfo.setTitle(getString(R.string.bookDetailTitle));
                bookInfo.setMessage(getString(R.string.bookDetailContent));
                bookInfo.show();
                break;
            case 5:
                //View Command Details
                AlertDialog.Builder commandsInfo = new AlertDialog.Builder(this);
                commandsInfo.setTitle(getString(R.string.commandDetailTitle));
                commandsInfo.setMessage(getString(R.string.commandDetailContent));
                commandsInfo.show();
                break;
            case 6:
                //Forward
                if (mP.getDuration() - mP.getCurrentPosition() > 30000){
                    mP.seekTo(mP.getCurrentPosition()+30000);
                } else{
                    Toast.makeText(this, getString(R.string.no30sfurther), Toast.LENGTH_LONG).show();
                    tts.speak(getString(R.string.no30sfurther), TextToSpeech.QUEUE_FLUSH, null, null);
                }
                break;
            case 7:
                //Backward
                if (mP.getCurrentPosition() > 30000){
                    mP.seekTo(mP.getCurrentPosition()-30000);
                } else{
                    Toast.makeText(this, getString(R.string.no30sback), Toast.LENGTH_LONG).show();
                    tts.speak(getString(R.string.no30sback), TextToSpeech.QUEUE_FLUSH, null, null);
                }
                break;
            case 8:
                //Inrease Sound
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+3, 0);
                break;
            case 9:
                //Decrease Sound
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)-3, 0);
                break;
        }
    }

    private Runnable updateBookTime = new Runnable() {
        public void run() {
            int startTime = mP.getCurrentPosition();
            timeLabel.setText(String.format("%d dk, %d saniye", TimeUnit.MILLISECONDS.toMinutes((long) startTime), TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));
            handler.postDelayed(this, 100);
        }
    };
}
