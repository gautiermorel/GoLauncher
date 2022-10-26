package com.gohub.golauncher.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gohub.golauncher.AppInfo;
import com.gohub.golauncher.R;

public class ApplicationAdapter extends ArrayAdapter<AppInfo> {
	private final int mResource;

	public ApplicationAdapter(Context context, int resId, AppInfo[] items) {
		super(context, R.layout.list_item, items);
		mResource = resId;
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		View view;

		if (convertView == null) {
			view = View.inflate(getContext(), mResource, null);
		} else {
			view = convertView;
		}
		ImageView packageImage = (ImageView) view.findViewById(R.id.application_icon);
		TextView packageName = (TextView) view.findViewById(R.id.application_name);
		AppInfo appInfo = getItem(position);

		if (appInfo != null) {
			view.setTag(appInfo);
			packageName.setText(appInfo.getName());
			if (appInfo.getIcon() != null)
				packageImage.setImageDrawable(appInfo.getIcon());
		}
		return (view);
	}
}
