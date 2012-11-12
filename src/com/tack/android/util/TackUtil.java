package com.tack.android.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.net.ConnectivityManager;

public final class TackUtil {
  
  public static boolean isOnline(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
  }
  
  public static String streamToString(InputStream stream) {
    InputStreamReader reader = new InputStreamReader(stream);
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[256];
    try {
      while (reader.read(buf) != -1) {
        sb.append(buf);
      }
    } catch (IOException e) {
      // Darn
    }
    return sb.toString();
  }
  
  private TackUtil(){}
}
