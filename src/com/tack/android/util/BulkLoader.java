package com.tack.android.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.AsyncTaskLoader;

public abstract class BulkLoader<T> extends AsyncTaskLoader<T> {

  private ArrayList<QueryArgs> mRequests;
  private HashMap<QueryArgs, ArrayList<? extends Parcelable>> mResults;

  public BulkLoader(Context context, QueryArgs... requests) {
    super(context);
    mRequests = new ArrayList<QueryArgs>();
    mResults = new HashMap<QueryArgs, ArrayList<? extends Parcelable>>();
    
    // Add any requests from the varargs
    for (QueryArgs qa : requests) {
      addRequest(qa);
    }
  }
  
  public void addRequest(final QueryArgs qa) {
    // Add request to the list if unique
    if (!mRequests.contains(qa))
      mRequests.add(qa);
    
    // Handle content changes
    final ContentResolver cr = getContext().getContentResolver();
    cr.registerContentObserver(qa.uri, false, new ContentObserver(new Handler()) {
      @Override
      public boolean deliverSelfNotifications() {
        return true;
      }
      @Override
      public void onChange(boolean selfChange) {
        // remove the previous results from the cache
        mResults.remove(qa);
        
        // notify that the content of this loader has changed
        onContentChanged();
      }
    });
  }
  
  @SuppressWarnings("unchecked")
  public <U extends Parcelable> ArrayList<U> getResultForRequest(QueryArgs qa) {
    return (ArrayList<U>) mResults.get(qa);
  }
  
  @Override
  protected void onReset() {
    mResults.clear();
  }
  
  @Override
  public T loadInBackground() {
    final ContentResolver cr = getContext().getContentResolver();
    for (QueryArgs qa : mRequests) {
      if (!mResults.containsKey(qa)) {
        Cursor cursor = cr.query(qa.uri, qa.projection, qa.selection, qa.selectionArgs, qa.sortOrder);
        try {
          if (cursor.moveToFirst()) {
            mResults.put(qa, BulkLoader.this.processCursor(qa, cursor));
          } else {
            mResults.put(qa, null);
          }
        } finally {
          cursor.close();
        }
      }
    }
    
    // processCursor() could have called addRequest() and thus we'd need to make more requests before
    //  we are actually done. Check if we have a result for each request. If so return the merge results
    //  otherwise recurse.
    if (mResults.keySet().containsAll(mRequests)) {
      return mergeResults();
    } else {
      // Woah, recursion!
      return onLoadInBackground(); 
    }
  }
  
  /**
   * Process a cursors results into the appropriate data type. Cursor.moveToFirst()
   * has already be called and returned true. Cursor will be closed by BulkLoader.
   * 
   * @param qa      The original QueryArgs request object 
   * @param cursor  The cursor resulting from the QueryArgs request
   * @return        A parcelable representation of the request data 
   */
  public abstract ArrayList<? extends Parcelable> processCursor(QueryArgs qa, Cursor cursor);
  
  /**
   * 
   * @return
   */
  public abstract T mergeResults();
  
}
