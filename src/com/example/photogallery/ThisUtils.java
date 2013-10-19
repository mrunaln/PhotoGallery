package com.example.photogallery;
/*
 * Mrunal Nargunde
 * Assignment 4
 * 
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;


public class ThisUtils {
static class mySAXParser extends DefaultHandler{
		static ArrayList<Data> thisData;
		static Data onedata;
		static ArrayList<Data> parseData(InputStream xml, String json) throws IOException, SAXException, XmlPullParserException, JSONException{
			thisData = new ArrayList<Data>();
			
		if(xml != null){	
			Log.d("Mrunal"," in utils - Will parse with xml");
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(xml,"UTF-8");
		    int event = parser.getEventType();
		    while (event != XmlPullParser.END_DOCUMENT){
		    	switch(event){
		    	case XmlPullParser.START_TAG:
		    		if(parser.getName().equals("photo")){
		    			onedata = new Data(null,null,null);
		    			onedata.setTitle(parser.getAttributeValue(null, "title"));
		    			onedata.setViews(parser.getAttributeValue(null, "views"));
		    			onedata.setImageurl(parser.getAttributeValue(null, "url_m"));
		    		}
		    		break;
		    	case XmlPullParser.END_TAG:
		    		//Log.d("Mrunal", "adding in the arraylist");
		    		thisData.add(onedata);
		    		break;
		    	default:
		    		break;
		    	}	
		    	event = parser.next();
		     }
		}else if (json != null ){
			Log.d("Mrunal"," in thisutils - Will parse with JSON");
			JSONObject jsonObject = new JSONObject(json);
			jsonObject = jsonObject.getJSONObject("photos");
			JSONArray jsonArray = jsonObject.getJSONArray("photo");
			
			for(int i= 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonPerson = jsonArray.getJSONObject(i);
				onedata = new Data(null,null,null);
				onedata.setTitle(jsonPerson.getString("title"));
				onedata.setViews(jsonPerson.getString("views"));
				onedata.setImageurl(jsonPerson.getString("url_m"));
				
				thisData.add(onedata);
			}
		}
			return thisData;
		  
}
}
}
