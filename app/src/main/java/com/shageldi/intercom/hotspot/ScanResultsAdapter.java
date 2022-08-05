/* 
 * Copyright (C) 2013-2014 www.Andbrain.com 
 * Faster and more easily to create android apps
 * 
 * */
package com.shageldi.intercom.hotspot;


import java.util.List;




import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ScanResultsAdapter extends BaseAdapter {

	private final Context mContext;
	private final List<ScanResult> mResults;
	public ScanResultsAdapter(Context context, List<ScanResult> results) {
		this.mContext = context;
		this.mResults = results;
	}

	@Override
	public int getCount() {
		return mResults.size();
	}
 
	@Override
	public Object getItem(int position) {
		return mResults.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {


		return convertView;
	}

}
