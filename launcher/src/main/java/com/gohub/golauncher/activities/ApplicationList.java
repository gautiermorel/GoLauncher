package com.gohub.golauncher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.gohub.golauncher.AppInfo;
import com.gohub.golauncher.R;
import com.gohub.golauncher.Utils;
import com.gohub.golauncher.views.ApplicationAdapter;


public class ApplicationList extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {
	public static final String PACKAGE_NAME = "package_name";
	public static final String APPLICATION_NUMBER = "application";
	public static final String VIEW_TYPE = "view_type";
	public static final String DELETE = "delete";
	public static final String SHOW_DELETE = "show_delete";
	//
	public static final int VIEW_GRID = 0;
	public static final int VIEW_LIST = 1;
	//
	private int mApplication = -1;
	private int mViewType = 0;
	private AbsListView mListView;
	private final AsyncTask<Void, Void, AppInfo[]> mApplicationLoader = new AsyncTask<Void, Void, AppInfo[]>() {
		@Override
		protected AppInfo[] doInBackground(Void... params) {
			return Utils.loadApplications(ApplicationList.this).toArray(new AppInfo[0]);
		}

		@Override
		protected void onPostExecute(AppInfo[] apps) {
			getListView().setOnItemClickListener(ApplicationList.this);
			getListView().setAdapter(
					new ApplicationAdapter(ApplicationList.this,
							mViewType == VIEW_LIST ? R.layout.list_item : R.layout.grid_item,
							apps));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle args = intent.getExtras();

		if (args != null) {
			if (args.containsKey(APPLICATION_NUMBER))
				mApplication = args.getInt(APPLICATION_NUMBER);
			if (args.containsKey(VIEW_TYPE))
				mViewType = args.getInt(VIEW_TYPE);
		}

		setContentView(mViewType == VIEW_LIST ?
				R.layout.listview :
				R.layout.gridview);

		mListView = (AbsListView) findViewById(R.id.list);
		mApplicationLoader.execute();

		View v;
		if ((args != null) && (args.containsKey(SHOW_DELETE))) {
			if (!args.getBoolean(SHOW_DELETE)) {
				if ((v = findViewById(R.id.bottom_panel)) != null)
					v.setVisibility(View.GONE);
			}
		}
		if ((v = findViewById(R.id.delete)) != null)
			v.setOnClickListener(this);
		if ((v = findViewById(R.id.cancel)) != null)
			v.setOnClickListener(this);
	}

	private AbsListView getListView() {
		return mListView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AppInfo appInfo = (AppInfo) view.getTag();
		Intent data = new Intent();

		data.putExtra(PACKAGE_NAME, appInfo.getPackageName());
		data.putExtra(APPLICATION_NUMBER, mApplication);

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, data);
		} else {
			getParent().setResult(Activity.RESULT_OK, data);
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.delete:
				Intent data = new Intent();

				data.putExtra(DELETE, true);
				data.putExtra(APPLICATION_NUMBER, mApplication);

				if (getParent() == null)
					setResult(Activity.RESULT_OK, data);
				else
					getParent().setResult(Activity.RESULT_OK, data);
				finish();
				break;

			case R.id.cancel:
				if (getParent() == null)
					setResult(Activity.RESULT_CANCELED);
				else
					getParent().setResult(Activity.RESULT_CANCELED);
				finish();
				break;
		}
	}
}
