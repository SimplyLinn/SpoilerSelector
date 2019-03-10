package biz.opposite.spoilerselector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;

import androidx.preference.PreferenceManager;

public class CleanupService extends Service {

    public class LocalBinder extends Binder {
        CleanupService getService() {
            return CleanupService.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    public static boolean clearCache (Context context, boolean force) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        int cacheTime = sharedPreferences.getInt("cacheTime", 60) * 60000;
        File dir = new File(context.getExternalCacheDir(), "images");
        if (!((dir.exists() && dir.isDirectory()) || dir.mkdir())) {
            return false;
        }
        File[] files = dir.listFiles();
        for (File file : files)
        {
            if(force || file.lastModified() + cacheTime < System.currentTimeMillis())
                file.delete();
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (clearCache(this, false)) {
            stopSelfResult(startId);
            return START_STICKY;
        }
        stopSelfResult(startId);
        return START_NOT_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
