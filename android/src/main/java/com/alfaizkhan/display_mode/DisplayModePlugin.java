package com.alfaizkhan.display_mode;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** DisplayModePlugin */
public class DisplayModePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private Activity activity;
  private MethodChannel channel;

  private static final String METHOD_GET_ACTIVE_MODE = "getActiveMode";
  private static final String METHOD_GET_SUPPORTED_MODES = "getSupportedModes";
  private static final String METHOD_GET_PREFERRED_MODE = "getPreferredMode";
  private static final String METHOD_SET_PREFERRED_MODE = "setPreferredMode";

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "display_mode");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    if (channel != null) {
      channel.setMethodCallHandler(null);
    }
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      result.error("noAPI", "API is supported only in Android 6 (Marshmallow) and later", null);
      return;
    }

    if (activity == null) {
      result.error("noActivity", "Activity not attached to plugin. App is probably in background.", null);
      return;
    }

    switch (call.method) {
      case METHOD_GET_ACTIVE_MODE:
        getActiveMode(result);
        break;
      case METHOD_GET_SUPPORTED_MODES:
        getSupportedModes(result);
        break;
      case METHOD_GET_PREFERRED_MODE:
        getPreferredMode(result);
        break;
      case METHOD_SET_PREFERRED_MODE:
        setPreferredMode(call, result);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  @SuppressWarnings("deprecation")
  private Display getDisplay() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      return activity.getDisplay();
    } else {
      WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
      return windowManager.getDefaultDisplay();
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void getActiveMode(@NonNull Result result) {
    Display.Mode mode = getDisplay().getMode();
    HashMap<String, Object> ret = new HashMap<>();
    ret.put("id", mode.getModeId());
    ret.put("width", mode.getPhysicalWidth());
    ret.put("height", mode.getPhysicalHeight());
    ret.put("refreshRate", mode.getRefreshRate());
    result.success(ret);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private ArrayList<HashMap<String, Object>> getSupportedModesList() {
    ArrayList<HashMap<String, Object>> ret = new ArrayList<>();
    Display display = getDisplay();
    Display.Mode[] modes = display.getSupportedModes();

    for (Display.Mode mode : modes) {
      HashMap<String, Object> item = new HashMap<>();
      item.put("id", mode.getModeId());
      item.put("width", mode.getPhysicalWidth());
      item.put("height", mode.getPhysicalHeight());
      item.put("refreshRate", mode.getRefreshRate());
      ret.add(item);
    }
    return ret;
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void getSupportedModes(@NonNull Result result) {
    result.success(getSupportedModesList());
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void getPreferredMode(@NonNull Result result) {
    Window window = activity.getWindow();
    WindowManager.LayoutParams params = window.getAttributes();

    Display display = getDisplay();
    Display.Mode[] modes = display.getSupportedModes();

    for (Display.Mode mode : modes) {
      if (params.preferredDisplayModeId == mode.getModeId()) {
        HashMap<String, Object> item = new HashMap<>();
        item.put("id", mode.getModeId());
        item.put("width", mode.getPhysicalWidth());
        item.put("height", mode.getPhysicalHeight());
        item.put("refreshRate", mode.getRefreshRate());
        result.success(item);
        return;
      }
    }

    HashMap<String, Object> ret = new HashMap<>();
    ret.put("id", 0);
    ret.put("width", 0);
    ret.put("height", 0);
    ret.put("refreshRate", 0.0);
    result.success(ret);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void setPreferredMode(@NonNull MethodCall call, @NonNull Result result) {
    Integer mode = call.argument("mode");
    if (mode == null) {
      result.error("invalidArgs", "Missing mode id", null);
      return;
    }

    Window window = activity.getWindow();
    WindowManager.LayoutParams params = window.getAttributes();
    params.preferredDisplayModeId = mode;
    window.setAttributes(params);
    result.success(null);
  }
}
