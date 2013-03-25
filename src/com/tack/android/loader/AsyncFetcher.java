package com.tack.android.loader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.tack.android.R;
import com.tack.android.model.DataRequestModel;
import com.tack.android.model.DataRequestModel.RequestType;
import com.tack.android.model.DataResponseModel;
import com.tack.android.model.DataResponseModel.ResultType;
import com.tack.android.util.TackUtil;

public abstract class AsyncFetcher<T> {
  
  public interface ResponseHandler<T> {
    void handleResponse(DataResponseModel<T> responseData);
  }

  private static final String TAG = "Tack.AsyncFetcher";

  private Context mContext;
  private HttpURLConnection mUrlConnection;
  
  protected DataRequestModel mDataRequestModel;
  protected DataResponseModel<T> mDataResponseModel;
  protected AsyncFetcher.ResponseHandler<T> mResponseHandler;
  protected boolean mThrowOnNullModel = true;
  private boolean mIsCancelled;
  private boolean mDebug = false;
  
  public AsyncFetcher(Context context, DataRequestModel requestModel, AsyncFetcher.ResponseHandler<T> responseHandler) {
    mContext = context;
    mDataRequestModel = requestModel;
    mResponseHandler = responseHandler;
  }
  
  public void setDebugOn() {
    mDebug = true;
  }
  
  public void setDebugOff() {
    mDebug = false;
  }
  
  public Context getContext() {
    return mContext;
  }

  /**
   * Stops or prevents request
   */
  public void cancel() {
    mIsCancelled = true;
  }

  /**
   * @return
   *         Has a cancel been requested
   */
  public boolean isCancelled() {
    return mIsCancelled;
  }

  /**
   * @return
   *         The data response model
   */
  public DataResponseModel<T> getResponse() {
    return mDataResponseModel;
  }
  
  public void setData(T data) {
    if (mDataResponseModel != null)
      mDataResponseModel.data = data;
  }
  
  public DataResponseModel<T> execute() {
    preFetch();
    
    mDataResponseModel = fetchData();
    
    postFetch();
    return mDataResponseModel;
  }

  /**
   * Perform the network request asynchronously. Results are delivered via
   * <code>ResponseHandler.handleResponse(DataResponseModel<T>)</code> which
   * was assigned in the constructor.
   */
  public void executeAsync() {
    new AsyncTask<Void, Void, DataResponseModel<T>>() {
      protected void onPreExecute() {
        preFetch();
      }
      @Override
      protected DataResponseModel<T> doInBackground(Void... params) {
        return fetchData();
      }
      protected void onPostExecute(DataResponseModel<T> result) {
        postFetch();
      }
    }.execute();
  }
  
  private void preFetch() {
    if (mDataRequestModel == null) {
      // Can't do anything without a request model.
      cancel();
      return;
    }
    
    onPreFetch();
  }

  /**
   * Option override point. Gets called just before data fetch is attempted.
   */
  protected void onPreFetch() {}

  /**
   * Performs the network request
   * 
   * @return
   *         <code>DataResponseModel</code> containing the processed result of
   *         the network request
   */
  protected DataResponseModel<T> fetchData() {
    if (isCancelled()) return mDataResponseModel;

    if (null == mDataRequestModel) {
      if (mThrowOnNullModel)
        throw new NullPointerException("DataFetcher must have a non-null requestModel to make a request");
      else
        return null;
    }
    
    DataResponseModel<T> responseModel = new DataResponseModel<T>();
    URL url;
    
    // Make URL request
    try {
      URL_REQUEST: {
        try {
          Uri preparedUri = prepareUri(mDataRequestModel.requestURI);
          url = new URL(preparedUri.toString());
          //url = new URL(mDataRequestModel.requestURL);
        } catch (MalformedURLException mue) {
          // This really should never happen unless we typo one of the hard-coded urls
          responseModel.resultType = ResultType.ERROR_INVALID_URL;
          responseModel.responseMessage = "Invalid URL.";
          break URL_REQUEST;
        }
        
        if (mDebug) Log.d(TAG, "HttpUrlConnection :: Attempting connection - "+url.toString());
        
        // Check connectivity
        if (!TackUtil.isOnline(getContext())) {
          responseModel.resultType = ResultType.ERROR_NO_NETWORK;
          responseModel.responseMessage = getContext().getString(R.string.no_network_connection);
          break URL_REQUEST;
        }
        
        mUrlConnection = openURLConnection(url);
        prepareRequestHeaders(mUrlConnection, mDataRequestModel);
        
        if (mDebug) Log.d(TAG, "HttpUrlConnection :: Request header properties - "+mUrlConnection.getRequestProperties().toString());
        
        mUrlConnection.setDoInput(true);

        if (mUrlConnection == null || isCancelled()) return null;
        
        byte[] postData = mDataRequestModel.getPostData();
        if (postData != null) {
          mUrlConnection.setDoOutput(true);
          mUrlConnection.setRequestMethod("POST");
          mUrlConnection.setFixedLengthStreamingMode(postData.length);
          
          if (mDebug) Log.d(TAG, "HttpUrlConnection :: POSTing data - "+new String(postData));

          OutputStream out = null;
          try {
            // Attempt to post data (network connection occurs here)
            out = mUrlConnection.getOutputStream();
            out.write(postData);
            out.flush();
          } finally {
            if (null != out)
              out.close();
          }
        }
        
        if (mUrlConnection == null || isCancelled()) return null;
        
        // Attempt to retrieve the response (network connection occurs here)
        BufferedInputStream in = null;
        try {
          if (mDebug) Log.d(TAG, "HttpUrlConnection :: Attempting receive data");
          in = new BufferedInputStream(mUrlConnection.getInputStream());
        } catch (SSLHandshakeException sslhe) {
          // ignore
          Log.d(TAG, sslhe.getMessage());
          responseModel.resultType = ResultType.ERROR_SSL;
        }
        
        // Retrieve header values
        responseModel.headerFields = mUrlConnection.getHeaderFields();
        responseModel.responseCode = mUrlConnection.getResponseCode();
        responseModel.responseMessage = mUrlConnection.getResponseMessage();
        
        if (mDebug) Log.d(TAG, "HttpUrlConnection :: Connected - "+responseModel.responseCode+" "+responseModel.responseMessage);
        
        // Validate response code
        if (responseModel.responseCode / 100 != 2) break URL_REQUEST;

        if (mUrlConnection == null || isCancelled()) return null;
        
        if (in != null) {
          try {
            responseModel.data = processData(in);
            responseModel.resultType = ResultType.SUCCESS;
          } catch (Exception exception) {
            responseModel.resultType = ResultType.ERROR_DATA_PARSE_FAILED;
            responseModel.responseMessage = exception.getMessage();
            Log.d(TAG, responseModel.responseMessage);
          }
        }
      }
      
      if (mResponseHandler != null) {
        mResponseHandler.handleResponse(responseModel);
      }
    } catch (IOException e) {
      e.printStackTrace();
      responseModel.resultType = ResultType.ERROR_UNKNOWN;
      responseModel.headerFields = mUrlConnection.getHeaderFields();
      try {
        responseModel.responseCode = mUrlConnection.getResponseCode();
        responseModel.responseMessage = mUrlConnection.getResponseMessage();
      } catch (IOException e2) {
        // just give up
      }
    } finally {
      close();
    }
    
    return responseModel;
  }
  
  protected HttpURLConnection openURLConnection(URL url) throws IOException {
    return (HttpURLConnection) url.openConnection();
  }

  /**
   * Prepare the Uri before the connection is opened.
   * 
   * @param uri
   *          The unmodified uri
   * @return
   *         The modified uri
   */
  protected Uri prepareUri(Uri uri) {
    return uri;
  }
  
  /**
   * Prepare the request headers before the connection is opened.
   * 
   * @param urlConnection
   * @param requestModel
   * @throws ProtocolException
   */
  protected void prepareRequestHeaders(HttpURLConnection urlConnection, DataRequestModel requestModel) throws ProtocolException {
    // Enum request types match correct String names
    urlConnection.setRequestMethod(requestModel.requestType.toString());
    
    // TODO: Content-Length: 132 ???
    if (requestModel.requestType == RequestType.POST)
      urlConnection.setRequestProperty("Content-Length", String.valueOf(requestModel.getPostDataLength()));
    
    // Fake the Firefox user-agent to deal with potentially fussy servers
    urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");

    urlConnection.setRequestProperty("Accept", requestModel.acceptType);
    
    if (requestModel.requestType == RequestType.POST) {
      urlConnection.setRequestProperty("Accept-Charset", requestModel.charset);
      urlConnection.setRequestProperty("Content-Type", requestModel.getContentType());
    }
  }
  
  private void close() {
    if (null != mUrlConnection) { 
      mUrlConnection.disconnect();    
      mUrlConnection = null;
    }
  }

  
  /**
   * Method for processing data from URI request
   * 
   * @param inputStream
   *          Stream of data to be processed. Will be consumed after this method
   *          so must be handled here.
   * @return Data result
   * @throws Exception
   *           Errors can be thrown and reported in the DataResponseModel
   */
  protected abstract T processData(BufferedInputStream stream) throws Exception;
  
  private void postFetch() {
    onPostFetch();

    if (mResponseHandler != null)
      mResponseHandler.handleResponse(getResponse());
    
    mContext = null;
  }

  /**
   * Optional Override point to allow actions after data has been fetched. This
   * method is called before results are delivered for both
   * <code>execute()</code> and <code>executeAsync()</code>.
   */
  protected void onPostFetch() {}
}
