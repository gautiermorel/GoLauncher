package com.gohub.golauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Utils {
	public static List<AppInfo> loadApplications(Context context) {
		PackageManager packageManager = context.getPackageManager();
		
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> intentActivities = packageManager.queryIntentActivities(mainIntent, 0);
		
		mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
		intentActivities.addAll(packageManager.queryIntentActivities(mainIntent, 0));
				
		Set<String> knownPackages = new HashSet<>();
		List<AppInfo> entries = new ArrayList<>();

		if (intentActivities != null) {
			for (ResolveInfo resolveInfo : intentActivities) {
				if (!context.getPackageName().equals(resolveInfo.activityInfo.packageName) && 
					!knownPackages.contains(resolveInfo.activityInfo.packageName)) {
					entries.add(new AppInfo(packageManager, resolveInfo));
					knownPackages.add(resolveInfo.activityInfo.packageName);
				}
			}
		}

		Collections.sort(entries, new Comparator<AppInfo>() {
			@Override
			public int compare(AppInfo lhs, AppInfo rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});
		return entries;
	}

	public static int getPixelFromDp(Context context, int dp) {
		Resources r = context.getResources();
		return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
	}
}
