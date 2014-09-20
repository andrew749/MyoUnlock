package andrew749.com.myounlock;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
public class MyoListenerService extends Service implements MySensor.Sensors {
    private final IBinder mBinder = new LocalBinder();
    boolean connected = false;
    private DeviceListener mListener = new AbstractDeviceListener() {

        private Arm mArm = Arm.UNKNOWN;
        private XDirection mXDirection = XDirection.UNKNOWN;

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            Log.e("Myo:", "Connected");
            connected = true;
//            PowerManager mgr = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
//            wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
//            wakeLock.acquire();
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            Log.e("Myo:", "Disconnected");
            connected = false;
//            wakeLock.release();
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
//            Log.e("Myo", "Pitch=" + pitch);
            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (mXDirection == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }
            //add code to determine whether or not threshold is reached
            if (MySensor.myoEvent(pitch)) {

                unlockPhone();
            }
        }



        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            Log.e("Myo", "pose");
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            music.handlePose(getApplicationContext(), pose);
        }
    };
    Music music = new Music();
    Hub hub;
    PowerManager.WakeLock wakeLock;

    //method to unlock phone
    private void unlockPhone() {
        /*KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        wl.acquire();
        Log.e("Myo","unlock");*/
        PowerManager TempPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock TempWakeLock = TempPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "TempWakeLock");
        TempWakeLock.acquire();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        hub = Hub.getInstance();
        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        return START_NOT_STICKY;
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
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(new MySensor(this), sensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onCreate();
    }

    /**

     */
    public boolean isConnected() {
        return connected;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void sensorChanged(boolean threshold) {
        if (threshold) {
            unlockPhone();
        }
    }

    public class LocalBinder extends Binder {
        MyoListenerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyoListenerService.this;
        }
    }
}
