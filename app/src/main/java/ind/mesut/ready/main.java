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

    MediaPlayer mP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Log.d("intent", String.valueOf(intent.getData()));
        Log.d("voice: ", String.valueOf(isVoiceInteraction()));

        mP = MediaPlayer.create(this, R.raw.sample);
        final Button startButton = (Button) findViewById(R.id.playButton);
        final SeekBar speed = (SeekBar) findViewById(R.id.speed);
        final TextView textV = (TextView) findViewById(R.id.textView);

        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float speed = 0.75f+i*0.25f;
                textV.setText(Float.toString(speed));
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
                Log.d("position: ", Integer.toString(mP.getCurrentPosition()));
                Log.d("position: ", Integer.toString(mP.getDuration()));
                /*
                if (mP.isPlaying()){
                    mP.stop();
                    try{
                        mP.prepare();
                    } catch (Exception e){};

                    startButton.setText("Start");
                }else{
                    mP.start();
                    startButton.setText("Stop");
                }
                */
                if (mP.isPlaying()){
                    mP.pause();
                    startButton.setText("Start");

                }else{
                    mP.start();
                    startButton.setText("Stop");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("mode:", "ondestroy");
    }

    public void doOp(Integer optionId){
        Log.d("doOp", String.valueOf(optionId));
        if (mP.isPlaying()){
            mP.pause();

        }else{
            mP.start();
        }
    }

    void Prompt(Option[] options, String promptText){
        VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt(promptText);

        this.getVoiceInteractor().submitRequest(
                new PickOptionRequest(prompt, options, null) {
                    @Override
                    public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
                        Log.d("selections", selections.toString());
                        if (finished && selections.length == 1) {
                            Log.d("selections", selections.toString());
                            Log.d("id", String.valueOf(selections[0].getIndex()));
                            Log.d("text", String.valueOf(selections.toString()));
                            doOp(selections[0].getIndex());
                        }
                    }
                    @Override
                    public void onCancel() {
                        Log.d("voice", "cancel");
                    }
                }, "MainPrompt");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("voice: ", String.valueOf(isVoiceInteraction()));

        Intent intent = getIntent();

        Log.d("intent extra:", String.valueOf(intent.getExtras()));
        Log.d("intent data:", String.valueOf(intent.getData()));
        Log.d("intent data:", String.valueOf(intent.getDataString()));

        Log.d("state:", "onResume");

        // String[][] MainOptions = new String[][] {};

        /*
        String[] mainOption1 =  {"1", "bana kitap oku", "read a book", "begin", "play", "pause"};
        String[] mainOption2 =  {"2", "second", "end", "view"};
        String[] exit =  {"exit", "stop"};
        String[] option1Play = {"back", "before"};
        String[] option2Play = {"forward"};
        */

        // MainOptions[0] = mainOption1;
        // MainOptions[1] = mainOption2;

        if (isVoiceInteraction()) {

            Message message = Message.obtain();
            Log.d("a", String.valueOf(message.obj));

            /*
            VoiceInteractor.PickOptionRequest.Option mO1 = new VoiceInteractor.PickOptionRequest.Option("Play", 0);
            VoiceInteractor.PickOptionRequest.Option mO2 = new VoiceInteractor.PickOptionRequest.Option("View", 1);


            for (String option : mainOption1) {
                mO1.addSynonym(option);
            }
            for (String option : mainOption2) {
                mO2.addSynonym(option);
            }

            Option[] options = new Option[]{mO1, mO2};
            */

            VoiceInteractor.PickOptionRequest.Option option = new VoiceInteractor.PickOptionRequest.Option("play", 0);
            option.addSynonym("ready");
            option.addSynonym("go");
            option.addSynonym("take it");
            option.addSynonym("ok");
            option.addSynonym("pause");

            Option[] optionL = new Option[]{option};

            Prompt(optionL, "What do you want to do? Play or View?");

            //TODO: Play / Pause
            //TODO: Custom intent

        }
    }
}
