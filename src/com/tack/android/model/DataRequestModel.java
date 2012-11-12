package com.tack.android.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.NameValuePair;

public class DataRequestModel {
  
  public enum RequestType {
    GET, PUT, POST, DELETE;
  }
  
  public String requestURL;
  public RequestType requestType = RequestType.GET;
  public List<NameValuePair> parameters;
  public String acceptType = "application/json";
  public String charset = "UTF-8";
  public String contentType = "application/x-www-form-urlencoded;charset=" + charset;
  
  public DataRequestModel(String requestURL) {
    this.requestURL = requestURL;
  }
  
  public String stringPostData() throws UnsupportedEncodingException {
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
  
  public byte[] postData() throws UnsupportedEncodingException {
    return stringPostData().getBytes(charset);
  }
  
}
