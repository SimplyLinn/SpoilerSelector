package biz.opposite.spoilerselector;

import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ParcelFileDescriptor mInputPFD;
    private final static String[] units = new String[] {"b", "kb", "mb", "gb", "tb"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = findViewById(R.id.clear_cache);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(IntentFactory.buildChooserIntent(MainActivity.this), 3);
            }
        });
        refreshCacheInfo();
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CleanupService.clearCache(MainActivity.this, true);
                refreshCacheInfo();
            }
        });
    }

    private void refreshCacheInfo() {
        int files = 0;
        int size = 0;
        int uIndex = 0;
        File dir = new File(getExternalCacheDir(), "images");
        if (dir.isDirectory()) {
            File[] fFiles = dir.listFiles();
            files = fFiles.length;
            for (File file : fFiles)
            {
                size += file.length();
            }
        }
        while(uIndex <= units.length && size/1024 > 0) {
            size /= 1024;
            uIndex++;
        }
        TextView cacheInfo = findViewById(R.id.cache_info);
        cacheInfo.setText(Html.fromHtml(
                getResources().getQuantityString(R.plurals.cache_info_files, files, files)+"<br/>"+
                getResources().getString(R.string.cache_info_space, size, units[uIndex])
        ));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_settings was selected
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }
}
