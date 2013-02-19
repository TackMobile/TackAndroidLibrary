package com.tack.android.util;

import java.util.ArrayList;

public class Promise {
  private ArrayList<Object> commitments;
  private PromiseListener listener;
  
  public Promise(PromiseListener promiseListener) {
    commitments = new ArrayList<Object>();
    listener = promiseListener;
  }
  
  public void addCommitment(Object object) {
    if (commitments == null) {
      commitments = new ArrayList<Object>();
    }
    commitments.add(object);
  }
  
  public void keepCommitment(Object object) {
    if (commitments.contains(object)) {
      commitments.remove(object);
      if (commitments.isEmpty()) {
        listener.promiseKept(listener);
      }
    }
  }
  
  public void failCommitment(Object object) {
    if (commitments.contains(object)) {
      listener.promiseFailed(listener);
    }
  }
  
  public boolean containsCommitment(Object object) {
    return commitments.contains(object);
  }
  
  public boolean isEmpty() {
    return (commitments==null || commitments.isEmpty());
  }
  
  
  public interface PromiseListener {

    public void promiseKept(PromiseListener notifier);
    
    public void promiseFailed(PromiseListener notifier);
    
  }
}
