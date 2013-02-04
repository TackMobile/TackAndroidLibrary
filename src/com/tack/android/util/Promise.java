package com.tack.android.util;

import java.util.ArrayList;

public class Promise {
  private ArrayList<Object> commitments;
  private PromiseNotifier notifier;
  
  public Promise(PromiseNotifier promiseNotifier) {
    commitments = new ArrayList<Object>();
    notifier = promiseNotifier;
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
        notifier.promiseKept(notifier);
      }
    }
  }
  
  public void failCommitment(Object object) {
    if (commitments.contains(object)) {
      notifier.promiseFailed(notifier);
    }
  }
  
  public boolean containsCommitment(Object object) {
    return commitments.contains(object);
  }
  
  public boolean isEmpty() {
    return (commitments==null || commitments.isEmpty());
  }
  
  
  public interface PromiseNotifier {

    public void promiseKept(PromiseNotifier notifier);
    
    public void promiseFailed(PromiseNotifier notifier);
    
  }
}
