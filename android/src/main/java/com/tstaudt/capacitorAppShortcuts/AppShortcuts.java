package com.tstaudt.capacitorAppShortcuts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginResult;

import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@NativePlugin(
        permissions = {
                Manifest.permission.INSTALL_SHORTCUT,
                Manifest.permission.UNINSTALL_SHORTCUT
        }
)
public class AppShortcuts extends Plugin {

    private static final String TAG = "ShortcutsPlugin";

    /**
     * Latest intent, which is used to launch this activity
     */
    private JSONObject latestIntent = null;

    /**
     * Returns an object with the support info of the shortcuts
     * returns supportInfo {"appShortcuts":boolean;"pinnedShortcuts":boolean"}
     */
    @PluginMethod()
    public void isSupported(PluginCall call) {
        AppCompatActivity activity = getActivity();

        Context context = activity.getApplicationContext();
        boolean appShortcutsSupported = ShortcutManagerCompat.isRequestPinShortcutSupported(context);

        JSONObject json = new JSONObject();

        try {
            json.put("appShortcuts", Build.VERSION.SDK_INT >= 25);
            json.put("pinnedShortcuts", appShortcutsSupported);
            call.success(new JSObject(String.valueOf(json)));
        } catch (JSONException e) {
            call.error(e.getMessage(), e);
        }
    }

    /**
     * Adds a list of shortcuts to the app symbol
     *
     * @param call - {shortcuts: Array<Shortcut>}
     */
    @PluginMethod()
    public void setDynamic(PluginCall call) {

        try {
            JSArray args = call.getArray("shortcuts");
            if (args.length() < 1) {
                call.reject("Inserted value is not a array or has no values");
            }

            int count = args.length();

            ArrayList<ShortcutInfo> shortcuts = new ArrayList<ShortcutInfo>(count);

            for (int i = 0; i < count; ++i) {
                shortcuts.add(buildDynamicShortcut(args.optJSONObject(i)));
            }

            AppCompatActivity activity = getActivity();

            Context context = activity.getApplicationContext();
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            shortcutManager.setDynamicShortcuts(shortcuts);
            Log.i(TAG, String.format("Saved % dynamic shortcuts.", count));
            call.success();
        } catch (PackageManager.NameNotFoundException e) {
            call.reject(e.getMessage(), e);
        } catch (JSONException e) {
            call.reject(e.getMessage(), e);
        }
    }

    /**
     * Adds a list of shortcuts to the app symbol
     *
     * @param call - {type: 'pinned' or 'dynamic'}
     */
    @PluginMethod()
    public void getShortcuts(PluginCall call) {

        AppCompatActivity activity = getActivity();
        Context context = activity.getApplicationContext();
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        List<ShortcutInfo> infoList;
        JSONArray shortcutData = new JSONArray();


        try {

            String type = call.getString("type");

            switch (type) {
                case "pinned":
                    infoList = shortcutManager.getPinnedShortcuts();
                    break;
                case "dynamic":
                    infoList = shortcutManager.getDynamicShortcuts();
                    break;
                default:
                    infoList = shortcutManager.getDynamicShortcuts();
                    call.reject("Invalid type entered. Use the values 'pinned' or 'dynamic'");
                    break;
            }

            for (int i = 0; i < infoList.size(); i++) {
                ShortcutInfo result = infoList.get(i);
                shortcutData.put(result.getIntent().getData());
            }

            Log.i(TAG, String.format("Returned % dynamic shortcuts.", shortcutData.length()));
            call.success(new JSObject(String.valueOf(shortcutData)));
        } catch (JSONException e) {
            call.reject(e.getMessage(), e);
        } catch (NullPointerException e) {
            call.reject(e.getMessage(), e);
        }
    }

    /**
     * Adds a list of shortcuts to the app symbol
     *
     * @param call - {shortcuts: Array<Shortcut>}
     */
    @PluginMethod()
    public void addDynamic(PluginCall call) {

        try {
            JSArray args = call.getArray("shortcuts");
            if (args.length() < 1) {
                call.reject("Inserted value is not a array or has no values");
            }

            int count = args.length();

            ArrayList<ShortcutInfo> shortcuts = new ArrayList<ShortcutInfo>(count);

            for (int i = 0; i < count; ++i) {
                shortcuts.add(buildDynamicShortcut(args.optJSONObject(i)));
            }

            AppCompatActivity activity = getActivity();

            Context context = activity.getApplicationContext();
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            shortcutManager.addDynamicShortcuts(shortcuts);
            Log.i(TAG, String.format("Added % dynamic shortcuts.", count));
            call.success();
        } catch (PackageManager.NameNotFoundException e) {
            call.reject(e.getMessage(), e);
        } catch (JSONException e) {
            call.reject(e.getMessage(), e);
        }
    }

    /**
     * Creates a pinned shortcuts, which can be confirmed or cancelled by user
     *
     * @param call - {shortcut: Shortcut}
     */
    @PluginMethod()
    public void addPinnedShortcut(PluginCall call) {
        try {
            ShortcutInfoCompat shortcut = buildPinnedShortcut(call.getData());
            AppCompatActivity activity = getActivity();
            Context context = activity.getApplicationContext();
            boolean result = ShortcutManagerCompat.requestPinShortcut(context, shortcut, null);
            if (result) {
                call.success();
            } else {
                call.error("Cannot add pinned shortcut!");
            }
        } catch (PackageManager.NameNotFoundException e) {
            call.reject(e.getMessage(), e);
        } catch (JSONException e) {
            call.reject(e.getMessage(), e);
        }
    }

    /**
     * Disable pinned shortcuts by ids
     *
     * @param call - {ids:Array<string>, message?:string} - ids of shortcuts to disable
     */
    @PluginMethod()
    public void disablePinnedShortcuts(PluginCall call) {
        AppCompatActivity activity = getActivity();
        Context context = activity.getApplicationContext();
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        JSONArray array = call.getArray("ids");
        String message = call.getString("message");

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < array.length(); i++) {
            try {
                list.add(array.getString(i));
            } catch (JSONException e) {
                call.error(e.getMessage(), e);
            }
        }

        if (message != null) {
            //Disable shortcuts with a custom message!
            shortcutManager.disableShortcuts(list, message);
        } else {
            //Disable shortcuts with a custom message!
            shortcutManager.disableShortcuts(list);
        }

    }

    /**
     * Disable pinned shortcuts by ids
     *
     * @param call - {ids:Array<string>} - ids of shortcuts to enable
     */
    @PluginMethod()
    public void enablePinnedShortcuts(PluginCall call) {
        AppCompatActivity activity = getActivity();
        Context context = activity.getApplicationContext();
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        JSONArray array = call.getArray("ids");

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < array.length(); i++) {
            try {
                list.add(array.getString(i));
            } catch (JSONException e) {
                call.error(e.getMessage(), e);
            }

        }
        shortcutManager.enableShortcuts(list);
        call.success();
    }

    /**
     * Remove dynamic shortcuts by idsword
     *
     * @param call - {ids:Array<string>} - ids of shortcuts to enable
     */
    @PluginMethod()
    public void removeDynamicShortcuts(PluginCall call) {
        AppCompatActivity activity = getActivity();
        Context context = activity.getApplicationContext();
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        JSONArray array = call.getArray("ids");

        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                list.add(array.getString(i));
            } catch (JSONException e) {
                call.error(e.getMessage(), e);
            }

        }
        shortcutManager.removeDynamicShortcuts(list);
        call.success();
    }

    /**
     * Removes all dynamic shortcuts from the app icon
     */
    @PluginMethod()
    public void resetDynamicShortcuts(PluginCall call) {
        AppCompatActivity activity = getActivity();
        Context context = activity.getApplicationContext();
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        shortcutManager.removeAllDynamicShortcuts();
        call.success();
    }

    /**
     * Returns the intent data of the intent by shortcut
     * returns the data of the intent
     */
    @PluginMethod()
    public void onShortcutPressed(PluginCall call) {
        try {
            call.resolve(new JSObject(this.latestIntent.getString("data")));
            //Reset the latest intent to prevent, that
            // the same intent is returned on another call of this method
            this.latestIntent = null;
        } catch (JSONException e) {
            call.reject("App was started via app icon press");
        }
    }

    /**
     * Get called, if the activity receives a new intent
     */
    @Override
    public void handleOnNewIntent(Intent intent) {
        try {
            // TODO: event only for testing purposes!
            bridge.triggerWindowJSEvent("onNewIntent", String.valueOf(buildIntent(intent)));


            this.latestIntent = buildIntent(intent);
        } catch (Exception e) {
            Log.e(TAG, "Exception handling onNewIntent: " + e.getMessage());
        }
    }

    private JSONObject buildIntent(Intent intent) throws JSONException {
        JSONObject jsonIntent = new JSONObject();

        jsonIntent.put("action", intent.getAction());
        jsonIntent.put("flags", intent.getFlags());

        Set<String> categories = intent.getCategories();
        if (categories != null) {
            jsonIntent.put("categories", new JSONArray(categories));
        }

        Uri data = intent.getData();
        if (data != null) {
            jsonIntent.put("data", data.toString());
        }

        Bundle extras = intent.getExtras();
        if (extras != null) {
            JSONObject jsonExtras = new JSONObject();
            jsonIntent.put("extras", jsonExtras);
            Iterator<String> keys = extras.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = extras.get(key);
                if (value instanceof Boolean) {
                    jsonExtras.put(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    jsonExtras.put(key, (Integer) value);
                } else if (value instanceof Long) {
                    jsonExtras.put(key, (Long) value);
                } else if (value instanceof Float) {
                    jsonExtras.put(key, (Float) value);
                } else if (value instanceof Double) {
                    jsonExtras.put(key, (Double) value);
                } else {
                    jsonExtras.put(key, value.toString());
                }
            }
        }

        return jsonIntent;
    }

    private Intent parseIntent(JSONObject jsonIntent, JSONObject shortcut) {

        Intent intent = new Intent();
        AppCompatActivity activity = getActivity();


        String activityClass = jsonIntent.optString("activityClass",
                activity.getClass().getName());
        String activityPackage = jsonIntent.optString("activityPackage",
                activity.getPackageName());
        intent.setClassName(activityPackage, activityClass);

        int flags = jsonIntent.optInt("flags", Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.setAction(Intent.ACTION_RUN)
                .setFlags(flags);

        String data = String.valueOf(shortcut);
        if (data.length() > 0) {
            intent.setData(Uri.parse(data));
        }


        return intent;
    }

    private ShortcutInfo buildDynamicShortcut(
            JSONObject jsonShortcut) throws PackageManager.NameNotFoundException, JSONException {
        if (jsonShortcut == null) {
            throw new InvalidParameterException("Shortcut object cannot be null");
        }

        AppCompatActivity activity = getActivity();
        Context context = activity.getApplicationContext();
        String shortcutId = jsonShortcut.optString("id");
        if (shortcutId.length() == 0) {
            throw new InvalidParameterException("A value for 'id' is required");
        }

        ShortcutInfo.Builder builder = new ShortcutInfo.Builder(context, shortcutId);

        String shortLabel = jsonShortcut.optString("title");
        String longLabel = jsonShortcut.optString("subtitle");
        if (shortLabel.length() == 0 && longLabel.length() == 0) {
            throw new InvalidParameterException("A value for either 'shortLabel' or 'longLabel' is required");
        }

        if (shortLabel.length() == 0) {
            shortLabel = longLabel;
        }

        if (longLabel.length() == 0) {
            longLabel = shortLabel;
        }

        Icon icon;
        String iconBitmap = jsonShortcut.optString("iconBitmap");
        String iconFromResource = jsonShortcut.optString("iconFromResource");


        String activityPackage = activity.getPackageName();

        if (iconBitmap.length() > 0) {
            icon = Icon.createWithBitmap(decodeBase64Bitmap(iconBitmap));
        } else if (iconFromResource.length() > 0) {
            Resources activityRes = activity.getResources();
            int iconId = activityRes.getIdentifier(iconFromResource, "drawable", activityPackage);
            icon = Icon.createWithResource(context, iconId);
        } else {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(activityPackage, PackageManager.GET_META_DATA);
            icon = Icon.createWithResource(activityPackage, applicationInfo.icon);
        }

        JSONObject jsonIntent = jsonShortcut.optJSONObject("intent");
        if (jsonIntent == null) {
            jsonIntent = new JSONObject();
        }


        Intent intent = parseIntent(jsonIntent, jsonShortcut);

        return builder
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIntent(intent)
                .setIcon(icon)
                .build();
    }


    private ShortcutInfoCompat buildPinnedShortcut(
            JSONObject jsonShortcut
    ) throws PackageManager.NameNotFoundException, JSONException {
        if (jsonShortcut == null) {
            throw new InvalidParameterException("Parameters must include a valid shorcut.");
        }

        AppCompatActivity activity = getActivity();
        Context context = activity.getApplicationContext();
        String shortcutId = jsonShortcut.optString("id");
        if (shortcutId.length() == 0) {
            throw new InvalidParameterException("A value for 'id' is required");
        }

        ShortcutInfoCompat.Builder builder = new ShortcutInfoCompat.Builder(context, shortcutId);

        String shortLabel = jsonShortcut.optString("title");
        String longLabel = jsonShortcut.optString("subtitle");
        if (shortLabel.length() == 0 && longLabel.length() == 0) {
            throw new InvalidParameterException("A value for either 'title' or 'subtitle' is required");
        }

        if (shortLabel.length() == 0) {
            shortLabel = longLabel;
        }

        if (longLabel.length() == 0) {
            longLabel = shortLabel;
        }

        IconCompat icon;
        String iconBitmap = jsonShortcut.optString("iconBitmap");

        if (iconBitmap.length() > 0) {
            icon = IconCompat.createWithBitmap(decodeBase64Bitmap(iconBitmap));
        } else {
            String activityPackage = activity.getPackageName();
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(activityPackage, PackageManager.GET_META_DATA);
            icon = IconCompat.createWithResource(context, applicationInfo.icon);
        }

        JSONObject jsonIntent = jsonShortcut.optJSONObject("intent");
        if (jsonIntent == null) {
            jsonIntent = new JSONObject();
        }

        Intent intent = parseIntent(jsonIntent, jsonShortcut);

        return builder
                .setActivity(intent.getComponent())
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIcon(icon)
                .setIntent(intent)
                .build();
    }

    private static Bitmap decodeBase64Bitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
