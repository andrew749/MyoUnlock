package andrew749.com.myounlock;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Created by Andrew on 9/20/2014.
 */
public class server extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
