package com.example.photogallery;
/*
 * Mrunal Nargunde
 * Assignment 4
 * 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONException;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class FlickrAsyncTask extends AsyncTask<String,Void, ArrayList<Data>> {
	ArrayList<Data> data;
	Context context;
	int buttonClicked;
    FlickrAsyncTask(Context context, int buttonClicked) {
        this.context = context;
        this.buttonClicked = buttonClicked;
    }
    
	@Override
	protected ArrayList<Data> doInBackground(String... params) {
		Log.d("Mrunal","Doing in background");
		try {
			 URL url = new URL(params[0]);
			 HttpURLConnection con = (HttpURLConnection) url.openConnection();
			 con.setRequestMethod("GET");
			 con.connect();
			 int statusCode = con.getResponseCode();
			 Log.d("Mrunal","--" + statusCode);
			 if (statusCode == HttpURLConnection.HTTP_OK) {
				 if(params[1].equals("xml")){
					 InputStream in = con.getInputStream();
					 Log.d("Mrunal"," Will parse with xml");
				     data = ThisUtils.mySAXParser.parseData(in,null);
				 }else{
					 Log.d("Mrunal"," Will parse with JSON");
					 BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()));
					 StringBuilder sb = new StringBuilder();
					 String line = buffer.readLine();
					 while(line != null){
						 sb.append(line);
						 line = buffer.readLine();
					 }
					 data = ThisUtils.mySAXParser.parseData(null,sb.toString());
				 
				 }
				 //Log.d("Mrunal"," data = " + data.toString());
				 
				 con.disconnect();
				 return data;
			 }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}

	@Override
	protected void onPostExecute(ArrayList<Data> result) {
		super.onPostExecute(result);
		/* SORT the list on views*/
		
		Collections.sort(result, new Comparator<Data>() {
			@Override
			public int compare(Data arg0, Data arg1) {
				return Integer.parseInt(arg0.getViews()) > Integer.parseInt(arg1.getViews()) ? -1:
					   Integer.parseInt(arg0.getViews()) < Integer.parseInt(arg1.getViews())? 1 : 0;
			}
		});
		Log.d("Mrunal","Starting photoActivity from onPostExecute");
		Intent intent = new Intent(context, PhotosActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("Button", buttonClicked);
		intent.putParcelableArrayListExtra("THISDATA", result);
		context.startActivity(intent);
		
	}
	
}
	
	
	