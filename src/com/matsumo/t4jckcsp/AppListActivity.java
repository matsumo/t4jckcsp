/**
 * t4jckcs patcher Copyright (C) 2014 matsumo All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.matsumo.t4jckcsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.PathClassLoader;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;

public class AppListActivity extends ListActivity {
	public static final String EXTRA_PREFS_KEY_NAME = "EXTRA_PREFS_KEY_NAME";

	private SharedPreferences pref;
	private String pref_key;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		if (intent != null) {
			pref_key = intent.getStringExtra(EXTRA_PREFS_KEY_NAME);
		}
		if (pref_key == null) {
			Toast.makeText(this, "bad param!", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.list);

		((Button) findViewById(R.id.button1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						saveItems();
						finish();
					}
				});

		new GetAppListTask().execute();
	}

	class PackageInfo {
		public String pkg, name;
		public boolean checked;
	}

	class BooleanListAdapter extends ArrayAdapter<PackageInfo> {
		protected LayoutInflater inflater;
		private List<PackageInfo> items;

		public BooleanListAdapter(Context context, int rowLayoutResourceId,
				List<PackageInfo> items) {
			super(context, rowLayoutResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = inflater.inflate(R.layout.list_row1, null);
			}
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox1);
			final int p = position;
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					items.get(p).checked = isChecked;
				}
			});
			checkBox.setChecked(items.get(position).checked);
			checkBox.setText(items.get(position).name);
			return view;
		}
	}

	class GetAppListTask extends AsyncTask<Integer, Integer, Boolean> {
		ArrayList<PackageInfo> appInfo = new ArrayList<PackageInfo>();
		protected boolean isStop = false;

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			PackageManager pm = AppListActivity.this.getPackageManager();
			Intent intent = new Intent(Intent.ACTION_MAIN, null);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			if (isStop) return false;
			Collections.sort(list, new ResolveInfo.DisplayNameComparator(pm));

			Set<String> items = loadItems(pref, pref_key, new HashSet<String>());
			appInfo.clear();
			for (ResolveInfo info : list) {
				if (isStop) return false;
				if (!isUseTwitter4j(info.activityInfo.packageName)) continue;
				PackageInfo p = new PackageInfo();
				p.pkg = info.activityInfo.packageName;
				p.name = info.loadLabel(pm).toString();
				p.checked = items.contains(p.pkg);
				appInfo.add(p);
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			setProgressBarIndeterminateVisibility(false);
			if (result) {
				setListAdapter(new BooleanListAdapter(AppListActivity.this,
						R.layout.list_row1, appInfo));
			}
		}

		@Override
		protected void onCancelled() {
			isStop = true;
		}
	}

	private void saveItems() {
		Set<String> items = new HashSet<String>();
		if (getListAdapter() == null || getListAdapter().getCount() == 0) return;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			PackageInfo info = (PackageInfo) getListAdapter().getItem(i);
			if (info.checked) items.add(info.pkg);
		}
		saveItems(pref, pref_key, items);
		Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
	}

	private static Set<String> loadItems(SharedPreferences prefs,
			String prefKey, Set<String> defaultItems) {
		if (prefs == null)
			return defaultItems;
		Set<String> items = prefs.getStringSet(prefKey, defaultItems);
		return items;
	}

	private static void saveItems(SharedPreferences prefs, String prefKey, Set<String> items) {
		if (prefs == null || items == null) return;
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(prefKey, items);
		editor.commit();
	}

	private boolean isUseTwitter4j(String pkg) {
		boolean r = false;
		// http://noxi515.blogspot.jp/2011/12/androidapkclassandroid237.html
		try {
			ApplicationInfo info = getPackageManager().getApplicationInfo(pkg, 0);
			ClassLoader loader = new PathClassLoader(info.sourceDir + ":",
					getClassLoader().getParent());
			Class.forName("twitter4j.conf.ConfigurationBase", true, loader);
			r = true;
		} catch (Exception e) {
		}
		return r;
	}
}