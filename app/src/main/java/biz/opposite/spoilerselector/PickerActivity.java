package biz.opposite.spoilerselector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class PickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        startActivityForResult(IntentFactory.buildChooserIntent(this), 3);
    }

    public Uri rewriteUri(Uri uri) {
        LinkedList<String> pathSegments = new LinkedList<String>(uri.getPathSegments());
        pathSegments.addFirst(uri.getAuthority());
        StringBuilder builder = new StringBuilder();
        for(String pathSegment : pathSegments) builder.append("/").append(pathSegment);
        return uri.buildUpon()
                .authority("biz.opposite.spoilerselector.spoilerprovider")
                .path(builder.toString())
                .build();
    }

    /*
     * When the Activity of the app that hosts files sets a result and calls
     * finish(), this method is invoked. The returned Intent contains the
     * content URI of a selected file. The result code indicates if the
     * selection worked or not.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent returnIntent) {
        // If the selection didn't work
        if (resultCode != RESULT_OK) {
            setResult(resultCode, returnIntent);
            finish();
            return;
        }
        // Get the file's content URI from the incoming Intent
        Uri returnUri = returnIntent.getData();
        Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        String ext = "";
        {
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                ext = fileName.substring(i);
            }
        }
        /*
         * Try to open the file for "read" access using the
         * returned URI. If the file isn't found, write to the
         * error log and return.
         */
        try {
            /*
             * Get the content resolver instance for this context, and use it
             * to get a ParcelFileDescriptor for the file.
             */
            File dir = new File(getExternalCacheDir(), "images");
            if (!((dir.exists() && dir.isDirectory()) || dir.mkdir())) {
                return;
            }
            ParcelFileDescriptor mInputPFD = getContentResolver().openFileDescriptor(returnUri, "r");
            InputStream inputStream = new FileInputStream(mInputPFD.getFileDescriptor());
            File file = File.createTempFile("SPOILER_", ext, dir);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            mInputPFD.close();
            Uri uri = (new Uri.Builder())
                    .scheme("content")
                    .authority("biz.opposite.spoilerselector.fileprovider")
                    .appendPath("cache")
                    .appendPath(file.getName())
                    .build();
            returnIntent = new Intent(returnIntent.getAction(), uri);
            returnIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            setResult(RESULT_OK, returnIntent);
            finish();
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            if(sharedPreferences.getBoolean("autoClearCache", true)) {
                int cacheTime = sharedPreferences.getInt("cacheTime", 60) * 60000;
                Intent intent = new Intent(this, CleanupService.class);
                PendingIntent pIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + cacheTime, pIntent);
            }
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        setResult(RESULT_FIRST_USER, new Intent());
        finish();
    }
}
