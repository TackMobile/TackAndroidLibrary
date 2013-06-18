package com.tack.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;

public final class TackUtil {
   
  private TackUtil(){}

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

  public static void appendStreamToStream(InputStream is, OutputStream os) throws IOException {
    final byte[] buffer = new byte[256];
    try {
      int n;
      while ((n = is.read(buffer)) != -1) {
        os.write(buffer, 0, n);
      }
    } finally {
      is.close();
    }
  }


  public static String convertStreamToString(InputStream is) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line = null;

    while ((line = reader.readLine()) != null) {
        sb.append(line);
    }

    is.close();

    return sb.toString();
}

  @SuppressLint("DefaultLocale")
  public static String removeExtraDecimals(double d) {
    if (d == (int) d)
      return String.format("%d", (int) d);
    else
      return String.format("%s", d);
  }
  
  public static String stringArrayToCsv(String[] stringArray) {
    if (stringArray == null) return null;
    final StringBuilder sb = new StringBuilder();
    final int count = stringArray.length;
    for(int i=0; i<count; i++) {
      sb.append(stringArray[i]);
      if (i<count-1)
        sb.append(",");
    }
    return sb.toString(); 
  }
}
