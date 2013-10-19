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

public class MainActivity extends Activity {
	static Intent intent;
	static ProgressDialog	mainProgressdialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button b = (Button)findViewById(R.id.photobutton);
		mainProgressdialog = new ProgressDialog(this);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				displaymainProgressDialog();
				startParsing(R.id.photobutton);
			}
		});
		
		b = (Button) findViewById(R.id.slideshowbutton);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("Button 2 clicked -----");
				displaymainProgressDialog();
				startParsing(R.id.slideshowbutton);
			}
		});
	}
	
	public void startParsing(int buttonClicked){
		Log.d("Mrunal","starting parsing here");
		RadioGroup radioButtonGroup = (RadioGroup) findViewById(R.id.xmljson);
		int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
		View radioButton = radioButtonGroup.findViewById(radioButtonID);
		int idx = radioButtonGroup.indexOfChild(radioButton);
		Log.d("Mrunal"," idx = "+ idx); 
		if(idx == 0){
			new FlickrAsyncTask(this,buttonClicked).execute(
					"http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=37105cf55a263f51b8622ab0f966caa7&tags=uncc&per_page=100&extras=views,url_m"
					, "xml");
		}else if (idx == 1){
			new FlickrAsyncTask(getApplicationContext(),buttonClicked).execute(
					"http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=37105cf55a263f51b8622ab0f966caa7&tags=uncc&extras=views%2Curl_m&per_page=100&format=json&nojsoncallback=1"
					, "json");
		}
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
