package com.doublesymmetry.trackplayer.kotlinaudio.notification;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Build;

public final class UtilRandom {
  /**
   * Like {@link Build.VERSION#SDK_INT}, but in a place where it can be conveniently overridden for
   * local testing.
   */
  public static final int SDK_INT = Build.VERSION.SDK_INT;

  public static Intent registerReceiverNotExported(
      Context context, BroadcastReceiver receiver, IntentFilter filter) {
    if (SDK_INT < 33) {
      return context.registerReceiver(receiver, filter);
    } else {
      return context.registerReceiver(receiver, filter, 4);
    }
  }
}
