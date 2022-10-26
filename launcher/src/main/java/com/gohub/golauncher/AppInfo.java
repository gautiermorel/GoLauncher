package com.gohub.golauncher;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;


public class AppInfo {
	private final Drawable mIcon;
	private String mName;
	private final String mPackageName;

	AppInfo(PackageManager packageManager, ResolveInfo resolveInfo) {
		mPackageName = resolveInfo.activityInfo.packageName;
		mIcon = resolveInfo.loadIcon(packageManager);
		try {
			mName = resolveInfo.loadLabel(packageManager).toString();
		} catch (Exception e) {
			mName = mPackageName;
		}
	}

	public AppInfo(PackageManager packageManager, ApplicationInfo applicationInfo) {
		mPackageName = applicationInfo.packageName;
		mIcon = applicationInfo.loadIcon(packageManager);
		try {
			mName = applicationInfo.loadLabel(packageManager).toString();
		} catch (Exception e) {
			mName = mPackageName;
		}
	}


	@NonNull
	public String getName() {
		if (mName != null)
			return mName;
		return ("");
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public String getPackageName() {
		return mPackageName;
	}
}
