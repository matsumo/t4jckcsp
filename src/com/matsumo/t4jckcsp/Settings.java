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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class Settings extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this is important because although the handler classes that read these settings
		// are in the same package, they are executed in the context of the hooked package
		getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
		addPreferencesFromResource(R.xml.settings);
		findPreference("targetApps").setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference pref) {
				showSelectAppsActivity(pref);
				return true;
			}}
		);
		findPreference("donation").setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.matsumo.donation"));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				return true;
			}}
		);
	}

	private void showSelectAppsActivity(Preference pref) {
		Intent intent = new Intent(Settings.this, AppListActivity.class);
		intent.putExtra(AppListActivity.EXTRA_PREFS_KEY_NAME, pref.getKey());
		startActivity(intent);
	}
}
