package com.tack.android.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.NameValuePair;

import android.net.Uri;
import android.text.TextUtils;

public class DataRequestModel {
  
  public enum RequestType {
    GET, PUT, POST, DELETE;
  }
  
  public Uri requestURI;
  public RequestType requestType = RequestType.GET;
  public final String acceptType = "application/json";
  public final String charset = "UTF-8";
  private String contentType = "application/x-www-form-urlencoded;charset=" + charset;

  private byte[] mPostData;
  
  public DataRequestModel(Uri requestURI) {
    this.requestURI = requestURI;
  }
  
  public void setPostData(List<NameValuePair> nameValuePairs) throws UnsupportedEncodingException {
    String data = nameValuePairsToString(nameValuePairs, charset);
    if (!TextUtils.isEmpty(data))
      setPostDataBytes(data.getBytes(charset));
    else
      setPostDataBytes(null);
  }
  
  public void setPostDataBytes(byte[] data) {
    requestType = data == null ? RequestType.GET : RequestType.POST;
    mPostData = data;
  }
  
  public byte[] getPostData() throws UnsupportedEncodingException {
    return mPostData;
  }
  
  public int getPostDataLength() {
    return mPostData.length;
  }
  
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  
  public String getContentType() {
    return contentType;
  }
  
  public static String nameValuePairsToString(List<NameValuePair> parameters, String charset) throws UnsupportedEncodingException {
    if (parameters == null) return null;
    
    String query = "";
    
    try {
      StringBuilder sb = new StringBuilder();
      int count = parameters.size();
      for (int i=0; i<count; i++) {
        NameValuePair pair = parameters.get(i);
        sb.append(pair.getName()).append("=").append(URLEncoder.encode(pair.getValue(), charset));
        if (i < count - 1)
          sb.append("&");
      }
      query = sb.toString();
    } catch (UnsupportedEncodingException e) {
    }
    
    return query;
  }
  
}
