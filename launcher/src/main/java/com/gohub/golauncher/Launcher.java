package com.gohub.golauncher;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;

import com.gohub.golauncher.fragments.ApplicationFragment;

public class Launcher extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setFullScreen();
		setContentView(R.layout.activity_launcher);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, ApplicationFragment.newInstance(), ApplicationFragment.TAG)
				.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFullScreen();
	}

	private void setFullScreen() {
		try {
			if (Build.VERSION.SDK_INT < 19) {
				getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
						WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
				);
			} else {
				View decorView = getWindow().getDecorView();

				int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
						| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
						| View.SYSTEM_UI_FLAG_IMMERSIVE;

				decorView.setSystemUiVisibility(uiOptions);

				getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
						WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
