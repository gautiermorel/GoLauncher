package com.gohub.golauncher.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gohub.golauncher.AppInfo;
import com.gohub.golauncher.R;
import com.gohub.golauncher.Setup;
import com.gohub.golauncher.Utils;
import com.gohub.golauncher.activities.ApplicationList;
import com.gohub.golauncher.activities.Preferences;
import com.gohub.golauncher.views.ApplicationView;

import java.text.DateFormat;
import java.util.Date;

@SuppressWarnings("PointlessBooleanExpression")
public class ApplicationFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
	public static final String TAG = "ApplicationFragment";
	private static final String PREFERENCES_NAME = "applications";
	private static final int REQUEST_CODE_APPLICATION_LIST = 0x1E;
	private static final int REQUEST_CODE_WALLPAPER = 0x1F;
	private static final int REQUEST_CODE_APPLICATION_START = 0x20;
	private static final int REQUEST_CODE_PREFERENCES = 0x21;

	private TextView mClock;
	private TextView mDate;
	private DateFormat mTimeFormat;
	private DateFormat mDateFormat;
	private TextView mBatteryLevel;
	private ImageView mBatteryIcon;
	private BroadcastReceiver mBatteryChangedReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			mBatteryLevel.setText(
					String.format(getResources().getString(R.string.battery_level_text), level)
			);
			final int batteryIconId = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
			mBatteryIcon.setImageDrawable(getResources().getDrawable(batteryIconId));
		}
	};
	private boolean mBatteryChangedReceiverRegistered = false;

	private final Handler mHandler = new Handler();
	private final Runnable mTimerTick = new Runnable() {
		@Override
		public void run() {
			setClock();
		}
	};

	private int mGridX = 3;
	private int mGridY = 2;
	private LinearLayout mContainer;
	private ApplicationView[][] mApplications = null;
	private View mSettings;
	private View mSystemSettings;
	private View mSystemWifi;
	private View mSystemActivity;
	private View mGridView;
	private Setup mSetup;


	public ApplicationFragment() {
		// Required empty public constructor
	}

	public static ApplicationFragment newInstance() {
		return new ApplicationFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_application, container, false);

		mSetup = new Setup(getContext());
		mContainer = (LinearLayout) view.findViewById(R.id.container);
		mSettings = view.findViewById(R.id.settings);
		mSystemSettings = view.findViewById(R.id.system_settings);
		mSystemWifi = view.findViewById(R.id.system_wifi);
		mSystemActivity = view.findViewById(R.id.system_activity);
		mGridView = view.findViewById(R.id.application_grid);
		mClock = (TextView) view.findViewById(R.id.clock);
		mDate = (TextView) view.findViewById(R.id.date);
		final LinearLayout batteryLayout = (LinearLayout) view.findViewById(R.id.battery_layout);
		mBatteryLevel = (TextView) view.findViewById(R.id.battery_level);
		mBatteryIcon = (ImageView) view.findViewById(R.id.battery_icon);

		mTimeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
		mDateFormat = android.text.format.DateFormat.getLongDateFormat(getActivity());

		if (mSetup.keepScreenOn())
			mContainer.setKeepScreenOn(true);

		if (mSetup.showDate() == false)
			mDate.setVisibility(View.GONE);

		if (mSetup.showBattery()) {
			batteryLayout.setVisibility(View.VISIBLE);
			getActivity().registerReceiver(this.mBatteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			mBatteryChangedReceiverRegistered = true;
		} else {
			batteryLayout.setVisibility(View.INVISIBLE);
			if (mBatteryChangedReceiverRegistered) {
				getActivity().unregisterReceiver(this.mBatteryChangedReceiver);
				mBatteryChangedReceiverRegistered = false;
			}
		}

		mSettings.setOnClickListener(this);
		mSystemSettings.setOnClickListener(this);
		mSystemWifi.setOnClickListener(this);
		mSystemActivity.setOnClickListener(this);
		mGridView.setOnClickListener(this);

		createApplications();

		return view;
	}

	private void createApplications() {
		mContainer.removeAllViews();

		mGridX = mSetup.getGridX();
		mGridY = mSetup.getGridY();

		if (mGridX < 2)
			mGridX = 2;
		if (mGridY < 1)
			mGridY = 1;

		int marginX = Utils.getPixelFromDp(getContext(), mSetup.getMarginX());
		int marginY = Utils.getPixelFromDp(getContext(), mSetup.getMarginY());

		boolean showNames = mSetup.showNames();

		mApplications = new ApplicationView[mGridY][mGridX];

		int position = 0;
		for (int y = 0; y < mGridY; y++) {
			LinearLayout ll = new LinearLayout(getContext());
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ll.setGravity(Gravity.CENTER_VERTICAL);
			ll.setFocusable(false);
			ll.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, 0, 1
			));

			for (int x = 0; x < mGridX; x++) {
				ApplicationView av = new ApplicationView(getContext());
				av.setOnClickListener(this);
				av.setOnLongClickListener(this);
				av.setOnMenuOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onLongClick(v);
					}
				});
				av.setPosition(position++);
				av.showName(showNames);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					av.setId(0x00FFFFFF + position);
				} else {
					av.setId(View.generateViewId());
				}
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
				lp.setMargins(marginX, marginY, marginX, marginY);
				av.setLayoutParams(lp);
				ll.addView(av);
				mApplications[y][x] = av;
			}
			mContainer.addView(ll);
		}

		updateApplications();
		setApplicationOrder();
	}

	private void setApplicationOrder() {
		for (int y = 0; y < mGridY; y++) {
			for (int x = 0; x < mGridX; x++) {
				int upId = R.id.application_grid;
				int downId = R.id.settings;
				int leftId = R.id.application_grid;
				int rightId = R.id.settings;

				if (y > 0)
					upId = mApplications[y - 1][x].getId();

				if (y + 1 < mGridY)
					downId = mApplications[y + 1][x].getId();

				if (x > 0)
					leftId = mApplications[y][x - 1].getId();
				else if (y > 0)
					leftId = mApplications[y - 1][mGridX - 1].getId();

				if (x + 1 < mGridX)
					rightId = mApplications[y][x + 1].getId();
				else if (y + 1 < mGridY)
					rightId = mApplications[y + 1][0].getId();

				mApplications[y][x].setNextFocusLeftId(leftId);
				mApplications[y][x].setNextFocusRightId(rightId);
				mApplications[y][x].setNextFocusUpId(upId);
				mApplications[y][x].setNextFocusDownId(downId);
			}
		}

		mGridView.setNextFocusLeftId(R.id.settings);
		mGridView.setNextFocusRightId(mApplications[0][0].getId());
		mGridView.setNextFocusUpId(R.id.settings);
		mGridView.setNextFocusDownId(mApplications[0][0].getId());

		mSettings.setNextFocusLeftId(mApplications[mGridY - 1][mGridX - 1].getId());
		mSettings.setNextFocusRightId(R.id.system_settings);
		mSettings.setNextFocusUpId(mApplications[mGridY - 1][mGridX - 1].getId());
		mSettings.setNextFocusDownId(R.id.system_settings);

		mSystemSettings.setNextFocusLeftId(R.id.settings);
		mSystemSettings.setNextFocusRightId(R.id.system_wifi);
		mSystemSettings.setNextFocusUpId(mApplications[mGridY - 1][mGridX - 1].getId());
		mSystemSettings.setNextFocusDownId(R.id.system_wifi);

		mSystemWifi.setNextFocusLeftId(R.id.system_settings);
		mSystemWifi.setNextFocusRightId(R.id.system_activity);
		mSystemWifi.setNextFocusUpId(mApplications[mGridY - 1][mGridX - 1].getId());
		mSystemWifi.setNextFocusDownId(R.id.system_activity);

		mSystemActivity.setNextFocusLeftId(R.id.system_wifi);
		mSystemActivity.setNextFocusRightId(R.id.application_grid);
		mSystemActivity.setNextFocusUpId(mApplications[mGridY - 1][mGridX - 1].getId());
		mSystemActivity.setNextFocusDownId(R.id.application_grid);
	}


	private void updateApplications() {
		PackageManager pm = getActivity().getPackageManager();
		SharedPreferences prefs = getActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

		for (int y = 0; y < mGridY; y++) {
			for (int x = 0; x < mGridX; x++) {
				ApplicationView app = mApplications[y][x];
				setApplication(pm, app, prefs.getString(app.getPreferenceKey(), null));
			}
		}
	}


	private void restartActivity() {
		if (mBatteryChangedReceiverRegistered) {
			getActivity().unregisterReceiver(mBatteryChangedReceiver);
			mBatteryChangedReceiverRegistered = false;
		}
		Intent intent = getActivity().getIntent();
		getActivity().finish();
		startActivity(intent);
	}


	private void writePreferences(int appNum, String packageName) {
		SharedPreferences prefs = getActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		String key = ApplicationView.getPreferenceKey(appNum);

		if (TextUtils.isEmpty(packageName))
			editor.remove(key);
		else
			editor.putString(key, packageName);

		editor.apply();
	}

	private void setApplication(PackageManager pm, ApplicationView app, String packageName) {
		try {

			if (TextUtils.isEmpty(packageName) == false) {
				PackageInfo pi = pm.getPackageInfo(packageName, 0);
				if (pi != null) {
					AppInfo appInfo = new AppInfo(pm, pi.applicationInfo);

					if (appInfo.getName().equalsIgnoreCase("SmartTube")) {
						app.setImageDrawable(getResources().getDrawable(R.drawable.ic_youtube))
								.setText("Youtube")
								.setPackageName(appInfo.getPackageName());
					} else if (appInfo.getName().equalsIgnoreCase("SIM Toolkit")) {
						app.setImageDrawable(getResources().getDrawable(R.drawable.ic_france_tv))
								.setText("MyTF1")
								.setPackageName(appInfo.getPackageName());
					} else if (appInfo.getName().equalsIgnoreCase("OpenVPN for Android")) {
						app.setImageDrawable(getResources().getDrawable(R.drawable.ic_vpn))
								.setText("VPN")
								.setPackageName(appInfo.getPackageName());
					} else if (appInfo.getName().equalsIgnoreCase("MYTF1")) {
						app.setImageDrawable(getResources().getDrawable(R.drawable.ic_mytf1))
								.setText("MYTF1")
								.setPackageName(appInfo.getPackageName());
					} else if (appInfo.getName().equalsIgnoreCase("File Explorer")) {
						app.setImageDrawable(getResources().getDrawable(R.drawable.ic_explorer))
								.setText("File Explorer")
								.setPackageName(appInfo.getPackageName());
					} else if (appInfo.getName().equalsIgnoreCase("Auvio")) {
						app.setImageDrawable(getResources().getDrawable(R.drawable.ic_auvio))
								.setText("Auvio")
								.setPackageName(appInfo.getPackageName());
					} else if (appInfo.getName().equalsIgnoreCase("France tv")) {
						app.setImageDrawable(getResources().getDrawable(R.drawable.ic_france_tv))
								.setText("France tv")
								.setPackageName(appInfo.getPackageName());
					} else {
						app.setImageDrawable(appInfo.getIcon())
								.setText(appInfo.getName())
								.setPackageName(appInfo.getPackageName());
					}
				}
			} else {
				app.setImageResource(R.drawable.ic_add)
						.setText("")
						.setPackageName(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		setClock();
		if (mSetup.showBattery() && !mBatteryChangedReceiverRegistered) {
			getActivity().registerReceiver(this.mBatteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			mBatteryChangedReceiverRegistered = true;
		}
		mHandler.postDelayed(mTimerTick, 1000);
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mTimerTick);
		if (mBatteryChangedReceiverRegistered) {
			getActivity().unregisterReceiver(this.mBatteryChangedReceiver);
		}
	}

	private void setClock() {
		Date date = new Date(System.currentTimeMillis());
		mClock.setText(mTimeFormat.format(date));
		mDate.setText(mDateFormat.format(date));
		mHandler.postDelayed(mTimerTick, 1000);
	}

	@Override
	public boolean onLongClick(View v) {
		if (v instanceof ApplicationView) {
			ApplicationView appView = (ApplicationView) v;
			if (appView.hasPackage() && mSetup.iconsLocked()) {
				Toast.makeText(getActivity(), R.string.home_locked, Toast.LENGTH_SHORT).show();
			} else {
				openApplicationList(ApplicationList.VIEW_LIST, appView.getPosition(), appView.hasPackage(), REQUEST_CODE_APPLICATION_LIST);
			}
			return (true);
		}
		return (false);
	}

	@Override
	public void onClick(View v) {
		if (v instanceof ApplicationView) {
			openApplication((ApplicationView) v);
			return;
		}

		switch (v.getId()) {
			case R.id.application_grid: {
				openApplicationList(ApplicationList.VIEW_GRID, 0, false, REQUEST_CODE_APPLICATION_START);
			}
			break;

			case R.id.settings:
				startActivityForResult(new Intent(getContext(), Preferences.class), REQUEST_CODE_PREFERENCES);
				break;

			case R.id.system_settings:
				startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
				break;

			case R.id.system_wifi:
				startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
				break;

			case R.id.system_activity:
				startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS), 0);
				break;
		}

	}

	private void openApplication(ApplicationView v) {
		if (v.hasPackage() == false) {
			openApplicationList(ApplicationList.VIEW_LIST, v.getPosition(), false, REQUEST_CODE_APPLICATION_LIST);
			return;
		}

		try {
			Toast.makeText(getActivity(), v.getName(), Toast.LENGTH_SHORT).show();
			startActivity(getLaunchIntentForPackage(v.getPackageName()));
		} catch (Exception e) {
			Toast.makeText(getActivity(), v.getName() + " : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void openApplication(String packageName) {
		try {
			Intent startApp = getLaunchIntentForPackage(packageName);
			Toast.makeText(getActivity(), packageName, Toast.LENGTH_SHORT).show();
			startActivity(startApp);
		} catch (Exception e) {
			Toast.makeText(getActivity(), packageName + " : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void openApplicationList(int viewType, int appNum, boolean showDelete, int requestCode) {
		Intent intent = new Intent(getActivity(), ApplicationList.class);
		intent.putExtra(ApplicationList.APPLICATION_NUMBER, appNum);
		intent.putExtra(ApplicationList.VIEW_TYPE, viewType);
		intent.putExtra(ApplicationList.SHOW_DELETE, showDelete);
		startActivityForResult(intent, requestCode);
	}
	
	private Intent getLaunchIntentForPackage(String packageName) {
		PackageManager pm = getActivity().getPackageManager();
		Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
		
		if(launchIntent == null) {
			launchIntent = pm.getLeanbackLaunchIntentForPackage(packageName);
		}
		
		return launchIntent;			
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
			case REQUEST_CODE_WALLPAPER:
				break;
			case REQUEST_CODE_PREFERENCES:
				restartActivity();
				break;
			case REQUEST_CODE_APPLICATION_START:
				if (intent != null)
					openApplication(intent.getExtras().getString(ApplicationList.PACKAGE_NAME));
				break;
			case REQUEST_CODE_APPLICATION_LIST:
				if (resultCode == Activity.RESULT_OK) {
					Bundle extra = intent.getExtras();
					int appNum = intent.getExtras().getInt(ApplicationList.APPLICATION_NUMBER);

					if (extra.containsKey(ApplicationList.DELETE) && extra.getBoolean(ApplicationList.DELETE)) {
						writePreferences(appNum, null);
					} else {
						writePreferences(appNum,
								intent.getExtras().getString(ApplicationList.PACKAGE_NAME)
						);
					}
					updateApplications();
				}
				break;
		}
	}


}
