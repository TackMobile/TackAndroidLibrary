package com.tack.android.model;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.SparseArray;

import com.tack.android.loader.AsyncDataLoader;
import com.tack.android.model.DataResponseModel.ResultType;

public abstract class LoaderPromiseModel  {
  
  private SparseArray<Bundle> commitments;
  private ArrayList<Integer> keptCommitments = new ArrayList<Integer>();
  private ArrayList<Integer> failedCommitments = new ArrayList<Integer>();
  private SparseArray<Object> results;
  private boolean resolved;

  public abstract AsyncDataLoader<?> onCreateLoader(int loaderId, Bundle args);
  public abstract void onPromiseKept();
  public abstract void onPromiseFailed();
  public abstract void onPromiseResolved();
  public void onCommitmentKept(int loaderId){};
  public void onCommitmentFailed(int loaderId, DataResponseModel<?> response){};
  
  public LoaderPromiseModel() {
  }
  
  public void initialize(LoaderManager loaderManager, SparseArray<Bundle> commitments) {
    this.commitments = commitments;
    results = new SparseArray<Object>(commitments.size());
    startLoaders(loaderManager);
  }
  
  public void startLoaders(LoaderManager loaderManager) {
    int size = commitments.size();
    for (int i=0; i<size; i++) {
      int key = commitments.keyAt(i);
      loaderManager.restartLoader(key, commitments.get(key), loaderCallbacks);
    }
  }
  
  public Object getResult(int loaderId) {
    return results.get(loaderId);
  }
  
  public boolean isKept() {
    return commitments.size() == keptCommitments.size();
  }
  
  public boolean isFailed() {
    return failedCommitments.size() > 0;
  }
  
  public boolean isResolved() {
    return resolved;
  }

  private int commitmentsToResolveCount() {
    return commitments.size() - (keptCommitments.size() + failedCommitments.size());
  }
  
  private void attemptToKeep() {
    if (isKept()) {
      onPromiseKept();
    }
    attemptToResolve();
  }
  
  private void attemptToResolve() {
    if (commitmentsToResolveCount() == 0) {
      resolved = true;
      onPromiseResolved();
    }
  }

  private LoaderCallbacks<Object> loaderCallbacks = new LoaderCallbacks<Object>() {
    @SuppressWarnings("unchecked")
    @Override
    public Loader<Object> onCreateLoader(int loaderId, Bundle args) {
      return (Loader<Object>) LoaderPromiseModel.this.onCreateLoader(loaderId, args);
    }
    
    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
      AsyncDataLoader<?> dataLoader = (AsyncDataLoader<?>) loader;
      if (null == dataLoader) {
        // Loader finished, but was null. Fail?
        return;
      }
      
      DataResponseModel<?> responseModel = dataLoader.getDataResponseModel();
      int loaderId = dataLoader.getId();
      results.put(loaderId, data);
      
      if (null != responseModel && responseModel.resultType == ResultType.SUCCESS) {
        // Keep commitment
        if (!keptCommitments.contains(loaderId)) {
          keptCommitments.add(loaderId);
          onCommitmentKept(loaderId);
        }
        attemptToKeep();
      } else {
        // Fail commitment
        if (!failedCommitments.contains(loaderId)) {
          failedCommitments.add(loaderId);
          onCommitmentFailed(loaderId, responseModel);
        }
        attemptToResolve();
      }
    }
    
    @Override
    public void onLoaderReset(Loader<Object> loader) {
    }
  };
  
}
