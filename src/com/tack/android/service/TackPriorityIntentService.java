package com.tack.android.service;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

/**
 * IntentService is a base class for {@link Service}s that handle asynchronous
 * requests (expressed as {@link Intent}s) on demand. Clients send requests
 * through {@link android.content.Context#startService(Intent)} calls; the
 * service is started as needed, handles each Intent in turn using a worker
 * thread, and stops itself when it runs out of work.
 * 
 * <p>
 * This "work queue processor" pattern is commonly used to offload tasks from an
 * application's main thread. The IntentService class exists to simplify this
 * pattern and take care of the mechanics. To use it, extend IntentService and
 * implement {@link #onHandleIntent(Intent)}. IntentService will receive the
 * Intents, launch a worker thread, and stop the service as appropriate.
 * 
 * <p>
 * All requests are handled on a single worker thread -- they may take as long
 * as necessary (and will not block the application's main loop), but only one
 * request will be processed at a time.
 * 
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>
 * For a detailed discussion about how to create services, read the <a
 * href="{@docRoot}guide/topics/fundamentals/services.html">Services</a>
 * developer guide.
 * </p>
 * </div>
 * 
 * @see android.os.AsyncTask
 */
public abstract class TackPriorityIntentService extends Service {

  public static final String EXTRA_PRIORITY = "com.tack.android.service.EXTRA_PRIORITY";
  public static final int PRIORITY_CLEAR = -1;
  public static final int PRIORITY_DEFAULT = 0;
  public static final int PRIORITY_TOP = 1;

  private volatile Looper mServiceLooper;
  private volatile ServiceHandler mServiceHandler;
  private String mName;
  private boolean mRedelivery;
  PriorityBlockingQueue<QueuedIntent> mQueue;

  private final class QueuedIntent implements Comparable<QueuedIntent> {
    Intent intent;
    long priority;

    public QueuedIntent(Intent i) {
      intent = i;
    }
    
    @Override
    public int compareTo(QueuedIntent another) {
      if (priority == another.priority) return 0;
      return priority > another.priority ? 1 : -1;
    }
  }

  private static final class ServiceHandler extends Handler {
    private WeakReference<TackPriorityIntentService> mServiceReference;

    public ServiceHandler(Looper looper, TackPriorityIntentService service) {
      super(looper);
      mServiceReference = new WeakReference<TackPriorityIntentService>(service);
    }

    @Override
    public void handleMessage(Message msg) {
      TackPriorityIntentService service = mServiceReference.get();
      if (service != null) {
        try {
            final QueuedIntent qi = service.mQueue.take();
            service.onHandleIntent(qi.intent);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        
        if (service.mQueue.isEmpty()) {
          service.stopSelf();
        }
      }
    }
  }

  /**
   * Creates an IntentService. Invoked by your subclass's constructor.
   * 
   * @param name
   *          Used to name the worker thread, important only for debugging.
   */
  public TackPriorityIntentService(String name) {
    super();
    mName = name;
  }

  /**
   * Sets intent redelivery preferences. Usually called from the constructor
   * with your preferred semantics.
   * 
   * <p>
   * If enabled is true, {@link #onStartCommand(Intent, int, int)} will return
   * {@link Service#START_REDELIVER_INTENT}, so if this process dies before
   * {@link #onHandleIntent(Intent)} returns, the process will be restarted and
   * the intent redelivered. If multiple Intents have been sent, only the most
   * recent one is guaranteed to be redelivered.
   * 
   * <p>
   * If enabled is false (the default),
   * {@link #onStartCommand(Intent, int, int)} will return
   * {@link Service#START_NOT_STICKY}, and if the process dies, the Intent dies
   * along with it.
   */
  public void setIntentRedelivery(boolean enabled) {
    mRedelivery = enabled;
  }

  @Override
  public void onCreate() {
    // TODO: It would be nice to have an option to hold a partial wakelock
    // during processing, and to have a static startService(Context, Intent)
    // method that would launch the service & hand off a wakelock.

    super.onCreate();
    HandlerThread thread = new HandlerThread("TackPriorityIntentService[" + mName + "]");
    thread.start();

    mServiceLooper = thread.getLooper();
    mServiceHandler = new ServiceHandler(mServiceLooper, this);
    mQueue = new PriorityBlockingQueue<QueuedIntent>();
  }

  @Override
  public void onStart(Intent intent, int startId) {
    // bail out immediately if a duplicate exists
    if (checkForDuplicate(intent)) return;
    
    final QueuedIntent qi = new QueuedIntent(intent);
    final int priority = intent.getIntExtra(EXTRA_PRIORITY, PRIORITY_DEFAULT);
    switch (priority) {
    case PRIORITY_CLEAR:
      mQueue.clear();
    case PRIORITY_TOP:
      qi.priority = System.currentTimeMillis();
      break;
    case PRIORITY_DEFAULT:
    default:
      qi.priority = 0;
      break;
    }
    mQueue.add(qi);
    mServiceHandler.sendEmptyMessage(0);
  }
  
  public boolean checkForDuplicate(Intent intent) {
    if (intent == null) return true;
    if (mQueue == null || mQueue.isEmpty()) return false;
    
    final Iterator<QueuedIntent> iterator = mQueue.iterator();
    Comparator<Intent> comparator = getIntentComparator();
    QueuedIntent qi;
    while(iterator.hasNext()) {
      qi = iterator.next();
      if (comparator.compare(qi.intent, intent) == 0) {
        //Log.v("TACK.TackPriorityIntentService", "Removing Dupilicate!");
        return true;
      }
    }
    return false;
  }
  
  protected abstract Comparator<Intent> getIntentComparator();
  
  /**
   * You should not override this method for your IntentService. Instead,
   * override {@link #onHandleIntent}, which the system calls when the
   * IntentService
   * receives a start request.
   * 
   * @see android.app.Service#onStartCommand
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    onStart(intent, startId);
    return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    mServiceLooper.quit();
  }

  /**
   * Unless you provide binding for your service, you don't need to implement
   * this
   * method, because the default implementation returns null.
   * 
   * @see android.app.Service#onBind
   */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  /**
   * This method is invoked on the worker thread with a request to process.
   * Only one Intent is processed at a time, but the processing happens on a
   * worker thread that runs independently from other application logic.
   * So, if this code takes a long time, it will hold up other requests to
   * the same IntentService, but it will not hold up anything else.
   * When all requests have been handled, the IntentService stops itself,
   * so you should not call {@link #stopSelf}.
   * 
   * @param intent
   *          The value passed to
   *          {@link android.content.Context#startService(Intent)}.
   */
  protected abstract void onHandleIntent(Intent intent);
}
