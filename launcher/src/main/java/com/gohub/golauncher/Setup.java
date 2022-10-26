package com.gohub.golauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gohub.golauncher.activities.Preferences;

import static com.gohub.golauncher.activities.Preferences.PREFERENCE_DEFAULT_TRANSPARENCY;
import static com.gohub.golauncher.activities.Preferences.PREFERENCE_TRANSPARENCY;


public class Setup {
	private static final int DEFAULT_GRID_X = 3;
	private static final int DEFAULT_GRID_Y = 2;
	private static final int DEFAULT_MARGIN_X = 5;
	private static final int DEFAULT_MARGIN_Y = 5;

	private final Context mContext;
	private SharedPreferences mPreferences;

	public Setup(Context context) {
		mContext = context;
	}

	private SharedPreferences getPreferences() {
		if (mPreferences == null) {
			mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		}
		return (mPreferences);
	}

	private int getInt(String name, int defaultValue) {
		try {
			String value = getPreferences().getString(name, null);
			if (value != null)
				return (Integer.parseInt(value));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (defaultValue);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isDefaultTransparency() {
		try {
			return (getPreferences().getBoolean(PREFERENCE_DEFAULT_TRANSPARENCY, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (true);
	}

	public float getTransparency() {
		try {
			return (getPreferences().getFloat(PREFERENCE_TRANSPARENCY, 0.5F));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (0.5F);
	}

	public boolean keepScreenOn() {
		try {
			return (getPreferences().getBoolean(Preferences.PREFERENCE_SCREEN_ON, false));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (false);
	}

	public boolean iconsLocked() {
		try {
			return (getPreferences().getBoolean(Preferences.PREFERENCE_LOCKED, false));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (false);
	}

	public boolean showDate() {
		try {
			return (getPreferences().getBoolean(Preferences.PREFERENCE_SHOW_DATE, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (true);
	}

	public boolean showBattery() {
		try {
			return (getPreferences().getBoolean(Preferences.PREFERENCE_SHOW_BATTERY, false));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (false);
	}

	public boolean showNames() {
		try {
			return (getPreferences().getBoolean(Preferences.PREFERENCE_SHOW_NAME, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (true);
	}


	public int getGridX() {
		return getInt(Preferences.PREFERENCE_GRID_X, DEFAULT_GRID_X);
	}

	public int getGridY() {
		return getInt(Preferences.PREFERENCE_GRID_Y, DEFAULT_GRID_Y);
	}

	public int getMarginX() {
		return getInt(Preferences.PREFERENCE_MARGIN_X, DEFAULT_MARGIN_X);
	}

	public int getMarginY() {
		return getInt(Preferences.PREFERENCE_MARGIN_Y, DEFAULT_MARGIN_Y);
	}
}
