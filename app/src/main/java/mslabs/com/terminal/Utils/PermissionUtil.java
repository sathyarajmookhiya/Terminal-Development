package mslabs.com.terminal.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.NFC;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PermissionUtil {

    public static final int APP_PERMISSION = 1;

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean hasRequiredAppPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, NFC) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, INTERNET) == PackageManager.PERMISSION_GRANTED ;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestAppPermission(Activity activity) {
        String[] permissions = new String[]{WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, NFC, ACCESS_WIFI_STATE, INTERNET};
        activity.requestPermissions(permissions, APP_PERMISSION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestAppPermissionIfNotDenied(Activity activity) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, READ_EXTERNAL_STORAGE)) {
            requestAppPermission(activity);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean shouldAskForAppPermission(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                READ_EXTERNAL_STORAGE);
    }

    public static boolean isAndroidMWithModifySettingsRequirementDefect() {
        return (Build.VERSION.RELEASE.equals("6.0"));
    }
}
