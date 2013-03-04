package com.tack.android.util;

import android.os.Build;

public class ConfigUtil {
  
  public static boolean hasV08() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
  }
  
  public static boolean hasV09() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
  }

  public static boolean hasV11() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
  }
  
  public static boolean hasV12() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
  }

  public static boolean hasV13() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
  }
  
  public static boolean hasV16() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
  }
}
