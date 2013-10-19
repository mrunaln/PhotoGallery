/*
 * Mrunal Nargunde
 * Assignment 4
 * 
 */
package com.example.photogallery;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends Activity {
	static Intent intent;
	static ProgressDialog	mainProgressdialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button b = (Button)findViewById(R.id.photobutton);
		mainProgressdialog = new ProgressDialog(this);
		intent = new Intent(this, PhotosActivity.class);
	/*	new FlickrAsyncTask().execute(
				"http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=37105cf55a263f51b8622ab0f966caa7&tags=uncc&per_page=100&extras=views,url_m"
				, "xml");
				*/
		RadioGroup radioButtonGroup = (RadioGroup) findViewById(R.id.xmljson);
		
		radioButtonGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.d("Mrunal","checkedid - " + checkedId);
				if(checkedId == R.id.radio0){
					new FlickrAsyncTask().execute(
							"http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=37105cf55a263f51b8622ab0f966caa7&tags=uncc&per_page=100&extras=views,url_m"
							, "xml");
				}else if (checkedId == R.id.radio1){
					new FlickrAsyncTask().execute(
							"http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=7c36a4e3686f019ccd34f970eabd4f69&tags=uncc&extras=views%2Curl_m&per_page=100&format=json&nojsoncallback=1&auth_token=72157636693295123-ec52b35f3c53d3bf&api_sig=1b4629aaf9d59193cdb6be54ede18324"
							, "json");
				}
			}
		});
		Log.d("Mrunal","Executed FlickrAsyncTask");
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				displaymainProgressDialog();
				intent.putExtra("Button", R.id.photobutton);
				Log.d("Mrunal","Starting next Activity !");
				startActivity(intent);
			}
		});
		
		b = (Button) findViewById(R.id.slideshowbutton);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("Button 2 clicked -----");
				displaymainProgressDialog();
				intent.putExtra("Button", R.id.slideshowbutton);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public static void displaymainProgressDialog(){
		
		mainProgressdialog.setCancelable(false);
		mainProgressdialog.setMessage("Loading Image");
		mainProgressdialog.setCanceledOnTouchOutside(false);
		mainProgressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mainProgressdialog.show();
	}
}
