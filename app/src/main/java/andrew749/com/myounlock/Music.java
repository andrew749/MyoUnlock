package andrew749.com.myounlock;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.thalmic.myo.Pose;

public class Music {
    Context context;

    private void sendMediaButton(Context context, int keyCode) {
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);
    }

    public void handlePose(Context context, Pose pose) {
        MyoListenerService.fist = false;

        switch (pose) {
            case FIST:
                Log.e("Myo:", "Fist");
                MyoListenerService.fist = true;
                break;
            case WAVE_IN:
                if (MyoListenerService.musiccontrol) {
                    Log.e("Myo", "wavein");
                    //this is previous
                    sendMediaButton(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                }

                break;
            case WAVE_OUT:
                if (MyoListenerService.musiccontrol) {
                    Log.e("Myo", "waveout");
                    //this is next
                    sendMediaButton(context, KeyEvent.KEYCODE_MEDIA_NEXT);
                }

                break;
            case FINGERS_SPREAD:
                if (MyoListenerService.musiccontrol) {
                    Log.e("Myo", "Fingers Spread");
                    sendMediaButton(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                }
                break;
            case THUMB_TO_PINKY:
                if (MyoListenerService.gnow) {
                    Log.e("Myo", "thumb to pinky");

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.google.android.googlequicksearchbox",
                            "com.google.android.googlequicksearchbox.VoiceSearchActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                break;
        }

    }
}
