package com.tack.android.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.PublicKey;

import android.R.color;
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
  
  public static String removeExtraDecimals(double d)
  {
      if(d == (int) d)
          return String.format("%d",(int)d);
      else
          return String.format("%s",d);
  }
  
  public static final String kPDF ="PDF";
  public static final String kPPT ="PPT";
  public static final String kPNG = "PNG";
  public static final String kJPG ="JPG";
  public static final String kGIF ="GIF";
  public static final String kDOC ="DOC";
  public static final String kDOCX ="DOCX";
  public static final String kTXT ="TXT";
  public static final String kBMP ="BMP";
  public static final String kMP3 ="MP3";
  public static final String kZIP ="ZIP";
  public static final String kJPEG ="JPEG";
  public static final String kMOV ="MOV";
  public static final String kWAV ="WAV";
  
  public static String getColorForExtension(String ext) {
    //default
    String color = "#65656C";
    if (ext.equalsIgnoreCase(kPDF)) {
      color = "#8C0000";
    } else if (ext.equalsIgnoreCase(kPPT)) {
      color = "#E14117";
    } else if (ext.equalsIgnoreCase(kPNG)) {
      color = "#238C00";
    } else if (ext.equalsIgnoreCase(kJPEG)) {
      color = "#238C00";
    } else if (ext.equalsIgnoreCase(kJPG)) {
      color = "#238C00";
    } else if (ext.equalsIgnoreCase(kBMP)) {
      color = "#238C00";
    } else if (ext.equalsIgnoreCase(kGIF)) {
      color = "#238C00";
    } else if (ext.equalsIgnoreCase(kDOC)) {
      color = "#4481CC";
    } else if (ext.equalsIgnoreCase(kDOCX)) {
      color = "#4481CC";
    } else if (ext.equalsIgnoreCase(kZIP)) {
      color = "#ECC600";
    } else if (ext.equalsIgnoreCase(kMOV)) {
      color = "#47B0F1";
    } else if (ext.equalsIgnoreCase(kWAV)) {
      color = "#47B0F1";
    }
    
    return color;
  }
}  

