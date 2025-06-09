import 'dart:async';

import 'package:flutter/services.dart';

import 'model/display_mode_json.dart';

class FlutterDisplayMode {
  const FlutterDisplayMode._();

  static const MethodChannel _channel = MethodChannel('display_mode');

  /// [supported] returns all the modes that can be set as the preferred mode.
  /// This always returns [DisplayModeJson.auto] as one of the modes.
  static Future<List<DisplayModeJson>> get supported async {
    final List<dynamic> rawModes =
        (await _channel.invokeMethod<List<dynamic>>('getSupportedModes'))!;
    final List<DisplayModeJson> modes = rawModes.map((dynamic i) {
      final Map<String, dynamic> item =
          (i as Map<dynamic, dynamic>).cast<String, dynamic>();
      return DisplayModeJson.fromJson(item);
    }).toList();
    modes.insert(0, DisplayModeJson.auto);
    return modes;
  }

  /// [active] fetches the currently active mode. This is not always the
  /// preferred mode set by [setPreferredMode]. It can be altered by the
  /// system based on the display settings.
  static Future<DisplayModeJson> get active async {
    final Map<dynamic, dynamic> mode =
        (await _channel.invokeMethod<Map<dynamic, dynamic>>('getActiveMode'))!;

    return DisplayModeJson.fromJson(mode.cast<String, dynamic>());
  }

  /// [preferred] returns the currently preferred mode. If not manually set
  /// with [setPreferredMode] then it will be [DisplayModeJson.auto].
  static Future<DisplayModeJson> get preferred async {
    final Map<dynamic, dynamic> mode = (await _channel
        .invokeMethod<Map<dynamic, dynamic>>('getPreferredMode'))!;

    return DisplayModeJson.fromJson(mode.cast<String, dynamic>());
  }

  /// [setPreferredMode] changes the preferred mode. It is upto the system
  /// to use this. Sometimes system can choose not switch to this based on
  /// internal heuristics. Check [active] to see if it actually switches.
  static Future<void> setPreferredMode(DisplayModeJson mode) async {
    return await _channel.invokeMethod<void>(
      'setPreferredMode',
      <String, dynamic>{'mode': mode.id},
    );
  }

  /// [setHighRefreshRate] changes preferred mode to highest refresh rate
  /// return the new displaymode
  /// available maintaining current resolution
  static Future<DisplayModeJson> setHighRefreshRate() async {
    final List<DisplayModeJson> modes = await supported;
    final DisplayModeJson activeMode = await active;

    DisplayModeJson newMode = activeMode;
    for (final DisplayModeJson mode in modes) {
      if (mode.height == newMode.height &&
          mode.width == newMode.width &&
          mode.refreshRate > newMode.refreshRate) {
        newMode = mode;
      }
    }

    await setPreferredMode(newMode);
    return newMode;
  }

  /// [setLowRefreshRate] changes preferred mode to lowest refresh rate
  /// return the new displaymode
  /// available maintaining current resolution
  static Future<DisplayModeJson> setLowRefreshRate() async {
    final List<DisplayModeJson> modes = await supported;
    final DisplayModeJson activeMode = await active;

    DisplayModeJson newMode = activeMode;
    for (final DisplayModeJson mode in modes) {
      if (mode.height == newMode.height &&
          mode.width == newMode.width &&
          mode.refreshRate < newMode.refreshRate) {
        newMode = mode;
      }
    }

    await setPreferredMode(newMode);
    return newMode;
  }
}
