package ind.mesut.ready;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("mode:", "ondestroy");
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

        if (isVoiceInteraction()) {
            VoiceInteractor.PickOptionRequest.Option option1 = new VoiceInteractor.PickOptionRequest.Option("Light", 0);
            option1.addSynonym("White");
            option1.addSynonym("Jedi");
            VoiceInteractor.PickOptionRequest.Option option2 = new VoiceInteractor.PickOptionRequest.Option("Dark", 1);
            option2.addSynonym("Black");
            option2.addSynonym("Sith");

            Option[] options = new Option[]{option1, option2};
            VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt("Which side are you on");

            this.getVoiceInteractor().submitRequest(new PickOptionRequest(prompt, options, null)
            {
                @Override
                public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
                    if (finished && selections.length == 1) {
                        if (selections[0].getIndex() == 0) Log.d("optionselected", "light");
                        else if (selections[0].getIndex() == 1) Log.d("optionselected", "dark");
                    }
                }

                @Override
                public void onCancel() {
                    Log.d("voice", "cancel");
                }
            }
            , "Theme Selector");

        }
    }
}
