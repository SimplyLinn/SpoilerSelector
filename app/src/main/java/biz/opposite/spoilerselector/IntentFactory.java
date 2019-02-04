package biz.opposite.spoilerselector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.util.LinkedList;
import java.util.List;

public class IntentFactory {
    private static final String GOOGLE_DRIVE_PACKAGE = "com.google.android.apps.docs";

    private static List<Intent> getAllIntents(Context context) {
        if(context == null) return new LinkedList<>();
        String[] mimetypes = new String[]{"text/*", "image/*", "video/*"};
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(
                new Intent(Intent.ACTION_GET_CONTENT)
                .setType("application/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true),
            0);
        List<Intent> intents = new LinkedList<Intent>();
        intents.add(new Intent(Intent.ACTION_PICK)
                .setType("image/*")
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true));
        boolean hasDrive = false;
        for(ResolveInfo resolveInfo : resolveInfos) {
            if(resolveInfo.activityInfo.packageName.equals(context.getApplicationContext().getPackageName())) continue;
            ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            intents.add(
                    new Intent(Intent.ACTION_GET_CONTENT)
                    .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setComponent(componentName)
                    .setPackage(resolveInfo.activityInfo.packageName)
                    .setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            );
            if(resolveInfo.activityInfo.packageName == GOOGLE_DRIVE_PACKAGE) hasDrive = true;
        }
        if(!hasDrive) {
            Intent driveIntent = getDriveIntent(context);
            if(driveIntent != null) intents.add(driveIntent);
        }
        return intents;
    }

    private static Intent getDriveIntent(Context context) {
        if(context == null) return null;
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(
                new Intent(Intent.ACTION_PICK)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true),
            0);
        for(ResolveInfo resolveInfo : resolveInfos) {
            if (resolveInfo.activityInfo.name == GOOGLE_DRIVE_PACKAGE+".app.PickActivity") {
                ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                return new Intent(Intent.ACTION_PICK)
                        .setComponent(componentName)
                        .setPackage(resolveInfo.activityInfo.packageName);
            }
        }
        return null;
    }

    public static Intent buildChooserIntent(Context context) {
        return Intent.createChooser(new Intent(), context.getResources().getString(R.string.launcherTitle))
                .putExtra(Intent.EXTRA_INITIAL_INTENTS, getAllIntents(context).toArray(new Intent[0]));
    }
}
