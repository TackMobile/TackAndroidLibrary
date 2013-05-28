package com.tack.android.util;

public class HTMLUtil {
  
  public static String bold(String s) {
    return "<b>" + s + "</b>";
  }
  
  public static String lineBreak() {
    return "<br/>";
  }
  
  public static String bullet() {
    return "\u2022";
  }
 
  public static String ellipsize(String text, int max) {
      int wordCount = 0;
      int end = 0;
      do {
         end = text.indexOf(' ', end + 1);
         wordCount++;
      } while (end > -1 && wordCount < max);

      if(end == -1){
        return text;
      }else{
        return text.substring(0, end) + "...";
      }
  }
  
// source (http://stackoverflow.com/questions/3597550/ideal-method-to-truncate-a-string-with-ellipsis)
  
//  private final static String NON_THIN = "[^iIl1\\.,']";
//
//  private static int textWidth(String str) {
//      return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
//  }
//
//  public static String ellipsize(String text, int max) {
//
//      if (textWidth(text) <= max)
//          return text;
//
//      // Start by chopping off at the word before max
//      // This is an over-approximation due to thin-characters...
//      int end = text.lastIndexOf(' ', max - 3);
//
//      // Just one long word. Chop it off.
//      if (end == -1)
//          return text.substring(0, max-3) + "...";
//
//      // Step forward as long as textWidth allows.
//      int newEnd = end;
//      do {
//          end = newEnd;
//          newEnd = text.indexOf(' ', end + 1);
//
//          // No more spaces.
//          if (newEnd == -1)
//              newEnd = text.length();
//
//      } while (textWidth(text.substring(0, newEnd) + "...") < max);
//
//      return text.substring(0, end) + "...";
//  }

}
