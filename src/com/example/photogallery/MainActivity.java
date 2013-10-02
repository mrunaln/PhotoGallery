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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	Intent intent;
	static ProgressDialog	mainProgressdialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		intent = new Intent(this, PhotosActivity.class);
		Button b = (Button)findViewById(R.id.photobutton);
		mainProgressdialog = new ProgressDialog(this);
		
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				displaymainProgressDialog();
				intent.putExtra("Button", R.id.photobutton);
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
/*
	public static void dismissmainDialog(){
		Log.d("Mrunal","Dismissing dialog ------ " + mainProgressdialog.isShowing());
		if(mainProgressdialog.isShowing())
			mainProgressdialog.dismiss();
	}

*/
}
