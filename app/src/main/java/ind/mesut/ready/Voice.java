package ind.mesut.ready;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Voice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        TextView text = (TextView) findViewById(R.id.text);
        text.setText("I'm Listening...");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isVoiceInteraction()) {

            //Define options
            VoiceInteractor.PickOptionRequest.Option play = new VoiceInteractor.PickOptionRequest.Option("play", 0);
            VoiceInteractor.PickOptionRequest.Option pause = new VoiceInteractor.PickOptionRequest.Option("pause", 1);
            VoiceInteractor.PickOptionRequest.Option speedUp = new VoiceInteractor.PickOptionRequest.Option("speed up", 2);
            VoiceInteractor.PickOptionRequest.Option speedDown = new VoiceInteractor.PickOptionRequest.Option("speed down", 3);
            VoiceInteractor.PickOptionRequest.Option viewInfo = new VoiceInteractor.PickOptionRequest.Option("view", 4);

            //Define Synonyms
            play.addSynonym("ready").addSynonym("go").addSynonym("take it").addSynonym("ok");
            pause.addSynonym("stop").addSynonym("wait");
            speedUp.addSynonym("faster").addSynonym("increase").addSynonym("fast").addSynonym("up");
            speedDown.addSynonym("slow").addSynonym("slower").addSynonym("decrease").addSynonym("down");
            viewInfo.addSynonym("info").addSynonym("detail").addSynonym("about");


            //Make a list of options
            VoiceInteractor.PickOptionRequest.Option[] optionList = new VoiceInteractor.PickOptionRequest.Option[]{play};

            //Set Prompt
            VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt("Give an order. Play, Pause, Speed up, Speed Down, View");

            //Create Request Object
            VoiceInteractor.Request request = new VoiceInteractor.PickOptionRequest(prompt, optionList, null) {
                @Override
                public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
                    if (finished && selections.length == 1) {
                        Log.d("id", String.valueOf(selections[0].getIndex()));
                        Log.d("text", String.valueOf(selections.toString()));
                        Intent openApp = new Intent(Voice.this, main.class);
                        openApp.putExtra("commandId", selections[0].getIndex());
                        startActivity(openApp);
                        finish();
                    }
                }
                @Override
                public void onCancel() {
                    Log.d("voice", "cancel");
                }
            };

            //Submit Request
            this.getVoiceInteractor().submitRequest(request, "mainPrompt");

            /*while (true){
                Log.d("requests", getVoiceInteractor().getActiveRequests().toString());
            }*/

            //TODO: Play / Pause
            //TODO: Custom intent
        }
    }
}
