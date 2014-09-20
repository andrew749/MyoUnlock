package andrew749.com.myounlock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.thalmic.myo.Hub;
import com.thalmic.myo.scanner.ScanActivity;


public class MainActivity extends Activity {

    // This code will be returned in onActivityResult() when the enable Bluetooth activity exits.
    private static final int REQUEST_ENABLE_BT = 1;
    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do n ot override an event, the default behavior is to do nothing.
    TextView tv;
    private Handler handler = new Handler();
    private MyoListenerService service;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (service != null) {
                if (service.isConnected()) {
                    tv.setText("Connected");
                    handler.postDelayed(this, 2000);
                } else {
                    tv.setText("Not connected");
                }
            }
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyoListenerService.LocalBinder binder = (MyoListenerService.LocalBinder) iBinder;
            service = binder.getService();
            //TODO on connect
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (service.isConnected()) {
                        tv.setText("Connected");
                        Log.e("myo", "meeewow");
                        handler.postDelayed(this, 2000);
                    } else {
                        tv.setText("Not connected");
                    }
                }
            }, 2000);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    private Intent intent;

    @Override
    protected void onStart() {
        super.onStart();
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        r.run();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);
        tv = (TextView) findViewById(R.id.text);
        Button b = (Button) findViewById(R.id.disconnect);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hub.getInstance().unpair(Hub.getInstance().getConnectedDevices().get(0).getMacAddress());
            }
        });
        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            Log.e("Myo", "Could not initialize the Hub.");
            finish();
            return;
        }
        intent = new Intent(getApplicationContext(), MyoListenerService.class);
        startService(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // If Bluetooth is not enabled, request to turn it on.
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        handler.removeCallbacks(r);
        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth, so exit.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
}
