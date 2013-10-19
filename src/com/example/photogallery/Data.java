package com.example.photogallery;

import android.os.Parcel;
import android.os.Parcelable;

public class Data implements Parcelable{

	String imageurl, title, views;

	public Data(String imageurl, String title, String views) {
		super();
		this.imageurl = imageurl;
		this.title = title;
		this.views = views;
	}

	public String getImageurl() {
		return imageurl;
	}

	public void setImageurl(String imageurl) {
		this.imageurl = imageurl;
	}

	@Override
	public String toString() {
		return "Data [imageurl=" + imageurl + ", title=" + title + ", views="
				+ views + "]";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getViews() {
		return views;
	}

	public void setViews(String views) {
		this.views = views;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(imageurl);
		dest.writeString(title);
		dest.writeString(views);
	}
	
	public static final Parcelable.Creator<Data> CREATOR= new Parcelable.Creator<Data>() {
		public Data createFromParcel(Parcel in) {
			return new Data(in);
}

		public Data[] newArray(int size) {
			return new Data[size];
		}
	};

	private Data(Parcel in) {
		this.imageurl = in.readString();
		this.title = in.readString();
		this.views = in.readString();
		
	}


	
	
	}
