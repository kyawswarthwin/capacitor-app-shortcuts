package com.tstaudt.capacitorAppShortcuts;

import android.Manifest;
import android.app.Activity;
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
import java.util.Set;

@NativePlugin(
        permissions = {
                Manifest.permission.INSTALL_SHORTCUT,
                Manifest.permission.UNINSTALL_SHORTCUT
        }
)
public class AppShortcuts extends Plugin {

    private static final String TAG = "ShortcutsPlugin";
    private static final String ACTION_SUPPORTS_DYNAMIC = "supportsDynamic";
    private static final String ACTION_SUPPORTS_PINNED = "supportsPinned";
    private static final String ACTION_SET_DYNAMIC = "setDynamic";
    private static final String ACTION_ADD_PINNED = "addPinned";
    private static final String ACTION_GET_INTENT = "getIntent";
    private static final String ACTION_ON_NEW_INTENT = "onNewIntent";

    @PluginMethod()
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.success(ret);
    }

    @PluginMethod()
    public void setDynamic(PluginCall call) {

        try {
            JSArray args = call.getArray("shortcuts");

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

    @PluginMethod()
    public void supportsPinned(PluginCall call) {
        AppCompatActivity activity = getActivity();

        Context context = activity.getApplicationContext();
        boolean supported = ShortcutManagerCompat.isRequestPinShortcutSupported(context);

        if (supported) {
            call.success();
        } else {
            call.reject("Pinned shortcuts care not supported on this platform");
        }
    }

    @PluginMethod()
    public void getIntent(PluginCall call) {
        // Intent intent = new Intent(Intent.ACTION_VIEW);
        // Intent intent = this.cordova.getActivity().getIntent();
        try {
            AppCompatActivity activity = getActivity();
            Intent intent = activity.getIntent();

            PluginResult result = new PluginResult(new JSObject(String.valueOf(buildIntent(intent))));
            call.success(new JSObject(String.valueOf(result)));
        } catch (JSONException e) {
            call.reject(e.getMessage(), e);
        }
    }

    @PluginMethod()
    public void supportsDynamic(PluginCall call) {
        boolean supported = Build.VERSION.SDK_INT >= 25;
        if (supported) {
            call.success();
        } else {
            call.error("Dynamic shortcuts are not supported!");
        }
    }

    @PluginMethod()
    public void addPinned(PluginCall call) {
        try {
            JSONArray args = call.getArray("shortcuts");
            ShortcutInfoCompat shortcut = buildPinnedShortcut(args.optJSONObject(0));
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

    @Override
    public void handleOnNewIntent(Intent intent) {
        try {
            bridge.triggerWindowJSEvent("onNewIntent", String.valueOf(buildIntent(intent)));
        } catch (JSONException e) {
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

    private Intent parseIntent(JSONObject jsonIntent) throws JSONException {

        Intent intent = new Intent();
        AppCompatActivity activity = getActivity();


        String activityClass = jsonIntent.optString("activityClass",
                activity.getClass().getName());
        String activityPackage = jsonIntent.optString("activityPackage",
                activity.getPackageName());
        intent.setClassName(activityPackage, activityClass);

        String action = jsonIntent.optString("action", Intent.ACTION_VIEW);
        if (action.indexOf('.') < 0) {
            action = activityPackage + '.' + action;
        }
        Log.i(TAG, "Creating new intent with action: " + action);
        intent.setAction(action);

        int flags = jsonIntent.optInt("flags", Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(flags); // TODO: Support passing different flags

        JSONArray jsonCategories = jsonIntent.optJSONArray("categories");
        if (jsonCategories != null) {
            int count = jsonCategories.length();
            for (int i = 0; i < count; ++i) {
                String category = jsonCategories.getString(i);
                if (category.indexOf('.') < 0) {
                    category = activityPackage + '.' + category;
                }
                intent.addCategory(category);
            }
        }

        String data = jsonIntent.optString("data");
        if (data.length() > 0) {
            intent.setData(Uri.parse(data));
        }

        JSONObject extras = jsonIntent.optJSONObject("extras");
        if (extras != null) {
            Iterator<String> keys = extras.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = extras.get(key);
                if (value != null) {
                    if (key.indexOf('.') < 0) {
                        key = activityPackage + "." + key;
                    }
                    if (value instanceof Boolean) {
                        intent.putExtra(key, (Boolean) value);
                    } else if (value instanceof Integer) {
                        intent.putExtra(key, (Integer) value);
                    } else if (value instanceof Long) {
                        intent.putExtra(key, (Long) value);
                    } else if (value instanceof Float) {
                        intent.putExtra(key, (Float) value);
                    } else if (value instanceof Double) {
                        intent.putExtra(key, (Double) value);
                    } else {
                        intent.putExtra(key, value.toString());
                    }
                }
            }
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

        String shortLabel = jsonShortcut.optString("shortLabel");
        String longLabel = jsonShortcut.optString("longLabel");
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


        Intent intent = parseIntent(jsonIntent);

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

        String shortLabel = jsonShortcut.optString("shortLabel");
        String longLabel = jsonShortcut.optString("longLabel");
        if (shortLabel.length() == 0 && longLabel.length() == 0) {
            throw new InvalidParameterException("A value for either 'shortLabel' or 'longLabel' is required");
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

        Intent intent = parseIntent(jsonIntent);

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
