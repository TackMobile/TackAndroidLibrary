package com.tack.android.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

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
    } finally {
      try {
        stream.close();
      } catch (IOException e) {
      }
    }
    return sb.toString();
  }
  
  public static void streamToStream(InputStream is, OutputStream os) throws IOException {
    final byte[] buffer = new byte[256];
    try {
      int n;
      while ((n = is.read(buffer)) != -1) {
        os.write(buffer, 0, n);
      }
    } finally {
      os.flush();
      os.close();
      is.close();
    }
  }
  
  private TackUtil(){}
}
