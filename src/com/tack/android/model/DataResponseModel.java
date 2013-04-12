package com.tack.android.model;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class DataResponseModel<T> {
  
  public enum ResultType {
    SUCCESS,
    ERROR_NO_NETWORK,
    ERROR_DENIED,
    ERROR_TOKEN_REFRESH_FAILED,
    ERROR_DATA_PARSE_FAILED, 
    ERROR_UNKNOWN, 
    ERROR_SSL,
    ERROR_INVALID_URL
  }
  
  public ResultType resultType;
  public int responseCode;
  public String responseMessage;
  public Map<String, List<String>> headerFields;
  public T data;
  public HttpURLConnection urlConnection;
}
