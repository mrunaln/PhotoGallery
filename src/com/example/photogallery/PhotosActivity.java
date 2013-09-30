package com.example.photogallery;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
	            image = BitmapFactory.decodeStream(url.openStream());
	             if(image != null){
	            	 if(mode) 	sendPhotoToSlideShow(msg);
	            	 else 		sendPhoto(msg);
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
}

