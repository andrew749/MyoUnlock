package andrew749.com.myounlock;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;

/**
 * Created by Andrew on 9/20/2014.
 */
public class MyoListenerService extends Service {
    private final IBinder mBinder = new LocalBinder();
    boolean connected = false;
    Hub hub;
    PowerManager.WakeLock wakeLock;
    private DeviceListener mListener = new AbstractDeviceListener() {

        private Arm mArm = Arm.UNKNOWN;
        private XDirection mXDirection = XDirection.UNKNOWN;

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            Log.e("Myo:", "Connected");
            connected = true;
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            Log.e("Myo:", "Disconnected");
            connected = false;
        }

        // onArmRecognized() is called whenever Myo has recognized a setup gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmRecognized(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mArm = arm;
            mXDirection = xDirection;
        }

        // onArmLost() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmLost(Myo myo, long timestamp) {
            mArm = Arm.UNKNOWN;
            mXDirection = XDirection.UNKNOWN;
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (mXDirection == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }


        }

        //method to unlock phone
        private void unlockPhone() {
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
            kl.disableKeyguard();

            PowerManager mgr = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
            wakeLock.acquire();

        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    break;
                case REST:
                    int restTextId = R.string.hello_world;
                    switch (mArm) {
                        case LEFT:
                            restTextId = R.string.arm_left;
                            break;
                        case RIGHT:
                            restTextId = R.string.arm_right;
                            break;
                    }
                    Log.d("Myo:", "rest");
                    break;
                case FIST:
                    Log.d("Myo:", "Fist");
                    break;
                case WAVE_IN:
                    Log.d("Myo", "wavein");
                    break;
                case WAVE_OUT:
                    Log.d("Myo", "waveout");
                    break;
                case FINGERS_SPREAD:
                    Log.d("Myo", "Fingers Spread");
                    break;
                case THUMB_TO_PINKY:
                    Log.d("Myo", "thumb to pinky");
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        hub = Hub.getInstance();
        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        hub.removeListener(mListener);
        Log.e("Myo", "Service Destroyed");
        wakeLock.release();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        startForeground(100, new Notification());

        super.onCreate();
    }

    /**
     * @return returns 0 if not connected
     * 1 if connecting
     * 2 if connected
     */
    public boolean isConnected() {
        return connected;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        MyoListenerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyoListenerService.this;
        }
    }
}
