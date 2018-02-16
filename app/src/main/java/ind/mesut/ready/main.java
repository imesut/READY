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
    protected void onResume() {
        super.onResume();
        Log.d("voice: ", String.valueOf(isVoiceInteraction()));

        if (isVoiceInteraction()) {
            //Log.d("voice: ", "startVoiceTrigger: ");
            Option option = new Option("start", 0);
            option.addSynonym("ready");
            option.addSynonym("go");
            option.addSynonym("take it");
            option.addSynonym("ok");

            VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt("start");

            getVoiceInteractor().submitRequest(new PickOptionRequest(prompt, new Option[]{option}, null) {
                @Override
                public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
                    if (finished && selections.length == 1) {
                        Message message = Message.obtain();
                        message.obj = result;
                        mP.start();
                    } else {
                        getActivity().finish();
                    }
                }
                @Override
                public void onCancel() {
                    getActivity().finish();
                }
            });
        }
    }
}
