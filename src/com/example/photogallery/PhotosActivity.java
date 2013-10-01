package com.example.photogallery;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotosActivity extends Activity {
	ProgressDialog	progressdialog;
	Handler 		handler;
	String[] 		urls;
	Bitmap 			image;
	ImageView 		iv;
	ExecutorService taskPool;
	ImageButton		left, right;
	private long 	delay = 2000;
	int 			photoIndex;
	static Resources currResources;
	
	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		File cacheDir = getDiskCacheDir(this, DISK_CACHE_SUBDIR);
		Log.d("Mrunal", "cache File: " + cacheDir);
	    new InitDiskCacheTask().execute(cacheDir);
	    
	    
		progressdialog = new ProgressDialog(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);
		photoIndex = 0;
		urls = getResources().getStringArray(R.array.photo_urls);
		iv = (ImageView) findViewById(R.id.imageView1);
		
		left = (ImageButton) findViewById(R.id.leftButton);
		right = (ImageButton) findViewById(R.id.rightButton);
		
		left.setAlpha(0.0f);
	//	right.setMaxWidth((int) (iv.getWidth() * 0.2));
		right.setAlpha(0.0f);
		currResources = this.getResources();
		handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch(msg.what){
					case 1:
						if (MainActivity.mainProgressdialog.isShowing())
							MainActivity.mainProgressdialog.dismiss();
						iv.setImageBitmap(image);
						break;
					default:
						Log.d("Mrunal"," Image in processing");
						break;
				}
				return false;
			}
		});
		if(getIntent().getExtras() != null){
			int id = getIntent().getExtras().getInt("Button", 0);
			taskPool = Executors.newFixedThreadPool(urls.length);
			
			Log.d("Mrunal","got intent !");
			switch(id){
				case R.id.photobutton:
					
					Log.d("Mrunal","PHOTO button clicked !");
					taskPool.execute(new imageDownload(photoIndex, false));
					
					right.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							
							displayProgressDialog();
							Log.d("Mrunal"," Right button clicked !");
							photoIndex = (photoIndex + 1) % urls.length;
							taskPool.execute(new imageDownload(photoIndex,false));
						}
					});
					left.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							displayProgressDialog();
							Log.d("Mrunal"," Left button clicked !");
							
							photoIndex = photoIndex == 0 ? urls.length - 1 : (photoIndex - 1) % urls.length;
							taskPool.execute(new imageDownload(photoIndex,false));
						}
					});
					break;
				case R.id.slideshowbutton:
					taskPool.execute(new imageDownload(photoIndex, true));
					break;
			}
		}else{
			Log.d("Mrunal","Intent value NULL ");
		}
	}
/*
	@Override
	protected void onPause(){
		taskPool.shutdown();
		
	}
*/
/*	
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
	    int width=iv.getWidth();
	    int height=iv.getHeight();

		left.getLayoutParams().width = (int)(iv.getWidth() * 0.2);
		right.getLayoutParams().width = (int)(iv.getWidth() * 0.2);
		
		ImageButton ib = (ImageButton)findViewById(R.id.leftButton);
	    Log.d("Mrunal"," IMAGEVIEW -width  = " + width + " ht = " + height + " measuredHT = " + iv.getMeasuredHeight() + " measured width  = " + iv.getMeasuredWidth());
	    Log.d("Mrunal","IMAGEBUTTON width = " + ib.getWidth() + " ht = " + ib.getHeight() + " measured HT = " + ib.getMeasuredHeight() + " measured WDT = " + ib.getMeasuredWidth());
	}
*/
	public void executeThread(){
		photoIndex = (photoIndex + 1) % urls.length;
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			Log.d("Mrunal","Thread unable to sleep exception !");
			e.printStackTrace();
		}
		taskPool.execute(new imageDownload(photoIndex, true));
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photos, menu);
		return true;
	}
	public class imageDownload implements Runnable {
		int photoindex;
		boolean mode;
		public imageDownload(int nowIndex, boolean thisMode) {
			photoindex = nowIndex;
			mode = thisMode;
		}
		@Override
		public void run() {
			Message msg = new Message();
			msg.what = 0;
			handler.sendEmptyMessage(msg.what);
	        try {
	        	URL url = new URL(urls[photoindex]);
	        	image = getBitmapFromDiskCache(urls[photoindex]);
	            if(image == null) //Cache MISS
	            {
	            	image = BitmapFactory.decodeStream(url.openStream());	
	            }
	        	addBitmapToCache(urls[photoindex], image);
	            
	             if(image != null){
	            	 if(mode) 	
	            		 sendPhotoToSlideShow(msg);
	            	 else 		
	            		 sendPhoto(msg);
	             } else{
	            	 Log.d("Mrunal","Image is Null , bad url !");
	            	 msg.what = -1;
	            	 handler.sendEmptyMessage(msg.what);
	             }
	        } catch (Exception e) {
	        	Log.d("Mrunal","Expection " + e + " occured while downloading the image");
	        	msg.what = -1;
	        	handler.sendEmptyMessage(msg.what);
	        } 
		}
	}
	
	public void sendPhoto(Message msg){
		msg.what = 1;
   	 	handler.sendEmptyMessage(msg.what);
   	 	Bundle bundle = new Bundle();
   	 	bundle.putParcelable("Image", image);
   	 	msg.setData(bundle);
		handler.sendMessage(msg);
		dismissDialog();

	}
	
	public void sendPhotoToSlideShow(Message msg){
		msg.what = 1;
   	 	handler.sendEmptyMessage(msg.what);
   	 	Bundle bundle = new Bundle();
   	 	bundle.putParcelable("Image", image);
   	 	msg.setData(bundle);
   	 	handler.sendMessageDelayed(msg, delay);
		executeThread();
	}
	
	public void displayProgressDialog(){
		
		progressdialog.setCancelable(false);
		progressdialog.setMessage("Loading Image");
		progressdialog.setCanceledOnTouchOutside(false);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.show();
	}

	public void dismissDialog(){
		Log.d("Mrunal","Dismissing dialog ------ " + progressdialog.isShowing());
		if(progressdialog.isShowing())
			progressdialog.dismiss();
	}
	
	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	    @Override
	    protected Void doInBackground(File... params) {
	        synchronized (mDiskCacheLock) {
	        	Log.d("Mrunal", "Cache file in asynctask: " + params);
	            File cacheDir = params[0];
	        	try {
					mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1 ,DISK_CACHE_SIZE);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            //mDiskLruCache = DiskLruCache.open(cacheDir, 1, urls.length, DISK_CACHE_SIZE);
	            mDiskCacheStarting = false; // Finished initialization
	            mDiskCacheLock.notifyAll(); // Wake any waiting threads
	        }
	        return null;
	    }
	}
	
	public void addBitmapToCache(String key, Bitmap bitmap) {

	    // Also add to disk cache
	    synchronized (mDiskCacheLock) {
	        if (mDiskLruCache != null && getBitmap(key) == null) {
	            putBitmap(key, bitmap);
	        }
	    }
	}

	public Bitmap getBitmapFromDiskCache(String key) {
	    synchronized (mDiskCacheLock) {
	        // Wait while disk cache is started from background thread
	        while (mDiskCacheStarting) {
	            try {
	                mDiskCacheLock.wait();
	            } catch (InterruptedException e) {}
	        }
	        if (mDiskLruCache != null) {
	            return getBitmap(key);
	        }
	    }
	    return null;
	}

	// Creates a unique subdirectory of the designated app cache directory. Tries to use external
	// but if not mounted, falls back on internal storage.
	@SuppressLint("NewApi")
	public static File getDiskCacheDir(Context context, String uniqueName) {
	    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
	    // otherwise use internal cache dir
	    //final String cachePath =
	    //        Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
	    //                !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
	    //                        context.getCacheDir().getPath();

		final String cachePath = context.getCacheDir().getPath();
		Log.d("Mrunal", "cache path: " + cachePath);
	    return new File(cachePath + File.separator + uniqueName);
	}
	
	public void putBitmap( String key, Bitmap data ) {

        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskLruCache.edit( key );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {               
                mDiskLruCache.flush();
                editor.commit();
                if ( BuildConfig.DEBUG ) {
                   Log.d( "cache_test_DISK_", "image put on disk cache " + key );
                }
            } else {
                editor.abort();
                if ( BuildConfig.DEBUG ) {
                    Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
                }
            }   
        } catch (IOException e) {
            if ( BuildConfig.DEBUG ) {
                Log.d( "Mrunal", "ERROR on: image put on disk cache " + key + e);
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }           
        }

    }

	private boolean writeBitmapToFile( Bitmap bitmap, DiskLruCache.Editor editor )
	        throws IOException, FileNotFoundException {
	        OutputStream out = null;
	        try {
	            out = new BufferedOutputStream( editor.newOutputStream( 0 ), 8 * 1024 );
	            return bitmap.compress( CompressFormat.JPEG, 70, out );
	        } finally {
	            if ( out != null ) {
	                out.close();
	            }
	        }
	    }
	
	
	public Bitmap getBitmap( String key ) {

        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskLruCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn = 
                new BufferedInputStream( in, 8 * 1024 );
                bitmap = BitmapFactory.decodeStream( buffIn );              
            }   
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        if ( BuildConfig.DEBUG ) {
            Log.d( "Mrunal", bitmap == null ? "" : "image read from disk " + key);
        }

        return bitmap;

    }
	
}

