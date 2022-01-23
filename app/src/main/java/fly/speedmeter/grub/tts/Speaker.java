package fly.speedmeter.grub.tts;

import android.speech.tts.UtteranceProgressListener;

/**
 * Created by Kigamba on 08/01/2022.
 */

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import static android.speech.tts.TextToSpeech.QUEUE_ADD;
import android.speech.tts.UtteranceProgressListener;

import java.util.ArrayList;
import java.util.Iterator;

import timber.log.Timber;

public class Speaker extends UtteranceProgressListener {


    private TextToSpeech textToSpeech = null;

    private ArrayList<String> speakingQueue = new ArrayList<>();
    private boolean isInitialised = false;

    public Speaker(Context context) {

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                if (i == TextToSpeech.SUCCESS) {
                    Timber.e("TTS Initialised successfully");
                    isInitialised = true;
                    processQueue();

                    textToSpeech.setOnUtteranceProgressListener(Speaker.this);
                } else {
                    Timber.e("TTS Initialisation failed");
                }
            }
        });
    }

    public void speak(String text) {
        speakingQueue.clear();
        speakingQueue.add(text);
        processQueue();
    }

    public void processQueue() {
        if (isInitialised) {
            Iterator<String> queueIterator = speakingQueue.iterator();

            while (queueIterator.hasNext()) {
                String speechItem = queueIterator.next();
                if (Build.VERSION.SDK_INT >= 21) {
                    textToSpeech.speak(speechItem, QUEUE_ADD, null, speechItem);
                } else {
                    textToSpeech.speak(speechItem, QUEUE_ADD, null);
                }

                queueIterator.remove();
            }
        }
    }

    public void onDone(String p0) {
        Timber.e("TTS onDone: %s", p0);
    }

    

    public void onError(String p0) {
        Timber.e("TTS onError : %s", p0);
    }


    public void onStart(String p0) {
        Timber.e("TTS onStart: %s", p0);
    }

    public void stop() {
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

}