/*
 * Mrunal Nargunde
 * Assignment 4
 * 
 */


package com.example.photogallery;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotosActivity extends Activity {
	ProgressDialog	progressdialog;
	String[] 		urls;
    Bitmap 			image;
	ImageView 		iv;
	ImageButton		left, right;
	private  long 	delay = 2000;
	int 			photoIndex;
	
    ExecutorService taskPool;
	Handler 		handler;
	ArrayList<Data> gotData;
	
	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		File cacheDir = getDiskCacheDir(this, DISK_CACHE_SUBDIR);
	    new InitDiskCacheTask().execute(cacheDir);
		progressdialog = new ProgressDialog(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);
		
		photoIndex = 0;
		iv = (ImageView) findViewById(R.id.imageView1);
		left = (ImageButton) findViewById(R.id.leftButton);
		right = (ImageButton) findViewById(R.id.rightButton);
		left.setAlpha(0.0f);
		right.setAlpha(0.0f);
		
		/* Displaying image in image-view in handler callback */
		handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch(msg.what){
					case 1:
						if (MainActivity.mainProgressdialog.isShowing())
							MainActivity.mainProgressdialog.dismiss();
						    Log.d("Mrunal","title photoIndex = " + gotData.get(photoIndex).toString());
						    TextView tv = (TextView)findViewById(R.id.Title);
						    tv.setText(gotData.get(photoIndex).getTitle());
						    tv = (TextView)findViewById(R.id.views);
						    tv.setText(gotData.get(photoIndex).getViews());

							iv.setImageBitmap(image);
						break;
					default:
						Log.d("Mrunal"," Image in processing");
						break;
				}
				return false;
			}
		});
		/* Find out which button was clicked on the mainActivity */
		Log.d("Mrunal","getting intent here");
		if(getIntent().getExtras() != null){
			int id = getIntent().getExtras().getInt("Button", 0);
			//taskPool = Executors.newFixedThreadPool(urls.length);
			taskPool = Executors.newFixedThreadPool(10);
			Log.d("Mrunal","TRying to get THISDATA now");
			    gotData = getIntent().getParcelableArrayListExtra("THISDATA");
			Log.d("Mrunal","Dismissing dialog and switching among the button pressed.");
			switch(id){
				case R.id.photobutton:
					
					taskPool.execute(new imageDownload(photoIndex, false));
					right.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							displayProgressDialog();
							photoIndex = photoIndex == 0 ? gotData.size() - 1 : (photoIndex - 1) % gotData.size();
							taskPool.execute(new imageDownload(photoIndex,false));
						}
					});
					left.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							displayProgressDialog();
							photoIndex = (photoIndex + 1) % gotData.size();
							taskPool.execute(new imageDownload(photoIndex,false));
						}
					});
					break;
				case R.id.slideshowbutton:
					Log.d("Mrunal","You clicked slideshow button !");
					taskPool.execute(new imageDownload(photoIndex, true));
					break;
			}
		}else{
			Log.d("Mrunal","Intent value NULL ");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photos, menu);
		return true;
	}
	
	/*Runnable downloads image if it does not reside in the Lru cache */
	public class imageDownload implements Runnable {
		int photoindex;
		boolean mode;
		public imageDownload(int nowIndex, boolean thisMode) {
			photoindex = nowIndex;		//current photo index index that should be displayed 
			mode = thisMode;			// photo mode or slideshow mode 
		}
		@Override
		public void run() {
			Message msg = new Message();
			msg.what = 0;
			handler.sendEmptyMessage(msg.what);
	        try {
	        	URL url = new URL(gotData.get(photoindex).getImageurl());
	        	String photoKey = extractPhotoKeyFromPhotoURL();
	        	image = getBitmapFromDiskCache(photoKey);
	            if(image == null) //Cache MISS
	            {
	            	// Cache miss occured. Hence downloading the image from url
	            image = BitmapFactory.decodeStream(url.openStream());	
	            }
	        	addBitmapToCache(photoKey, image);
	            
	             if(image != null){
	            	 msg.what = 1;
	            	 handler.sendEmptyMessage(msg.what);
	            	 Bundle bundle = new Bundle();
	            	 bundle.putParcelable("Image", image);
	            	 msg.setData(bundle);
	            	 if(mode){ 	
	            		 handler.sendMessageDelayed(msg, delay);
	            		 Thread.sleep(delay);
	            		 photoIndex = (photoIndex + 1) % gotData.size();
	            		 taskPool.execute(new imageDownload(photoIndex, true));
	            	 }
	            	 else{ 		
	            		 handler.sendMessage(msg);
	            		 dismissDialog();
	            	 }
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
	
	public void displayProgressDialog(){
		
		progressdialog.setCancelable(false);
		progressdialog.setMessage("Loading Image");
		progressdialog.setCanceledOnTouchOutside(false);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.show();
	}

	public void dismissDialog(){
		if(progressdialog.isShowing())
			progressdialog.dismiss();
	}

	@Override
	public void onBackPressed() {
		taskPool.shutdown();
		super.onBackPressed();
	}
	
	
	
	/* LRU cache implementation */
	
	 //returns the key extracted from the url in string.xml
 	public String extractPhotoKeyFromPhotoURL() {
 		String[] urlSplit = gotData.get(photoIndex).getImageurl().split("\\/");
		String keyFromURL = urlSplit[urlSplit.length - 1].split("\\.")[0];
		return keyFromURL;
	}
	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	    @Override
	    protected Void doInBackground(File... params) {
	        synchronized (mDiskCacheLock) {
	            File cacheDir = params[0];
	        	try {
					mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1 ,DISK_CACHE_SIZE);
				} catch (IOException e) {
					e.printStackTrace();
				}
	            mDiskCacheStarting = false; // Finished initialization
	            mDiskCacheLock.notifyAll(); // Wake any waiting threads
	        }
	        return null;
	    }
	}
	
	public void addBitmapToCache(String key, Bitmap bitmap) throws IOException {
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

	@SuppressLint("NewApi")
	public static File getDiskCacheDir(Context context, String uniqueName) {
		final String cachePath = context.getCacheDir().getPath();
	    return new File(cachePath + File.separator + uniqueName);
	}
	
	public void putBitmap( String key, Bitmap data ) throws IOException {

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
        }catch (FileNotFoundException e){
        	File cacheDir = getDiskCacheDir(this, DISK_CACHE_SUBDIR);
        	mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1 ,DISK_CACHE_SIZE);
    	    cacheDir.mkdirs();
    	    File noMedia = new File(cacheDir.toString(), ".nomedia");
            try {
                noMedia.createNewFile();
                Log.d("Mrunal", "Cache created" + cacheDir.toString());
            } catch (IOException x) {
                Log.d("Mrunal", "Couldn't create .nomedia file");
                x.printStackTrace();
            }
        } 
        catch (IOException e) {
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
	        }finally {
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
                final BufferedInputStream buffIn = new BufferedInputStream( in, 8 * 1024 );
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
            Log.d( "Mrunal", bitmap == null ? "" : "image read from disk ");
        }
        return bitmap;

    }
	
	
	
}

