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

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.HashSet;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Patch implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {
	private static String[] targetPkg = {};
	private static String replaceCK = null;
	private static String replaceCS = null;
	private static XSharedPreferences pref;

	@Override
	public void initZygote(StartupParam startupParam) {
		pref = new XSharedPreferences(Patch.class.getPackage().getName());
		replaceCK = pref.getString("defaultCK", null);
		replaceCS = pref.getString("defaultCS", null);
		targetPkg = pref.getStringSet("targetApps", new HashSet<String>()).toArray(new String[0]);
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		boolean found = false;
		for(int i=0; i<targetPkg.length; i++){
			if (lpparam.packageName.compareTo(targetPkg[i]) == 0){
				found = true;
				break;
			}
		}
		if(found && replaceCK != null && replaceCS != null){
//			XposedBridge.log("!!found pkg="+lpparam.packageName);
			findAndHookMethod("twitter4j.conf.ConfigurationBase",
				lpparam.classLoader,
				"setOAuthConsumerKey",
				String.class,
				new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if(param.args[0] != null){
						param.args[0] = replaceCK;
//						XposedBridge.log("!!replace setOAuthConsumerKey");
					}
				}
			});
			findAndHookMethod("twitter4j.conf.ConfigurationBase",
				lpparam.classLoader,
				"setOAuthConsumerSecret",
				String.class,
				new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if(param.args[0] != null){
						param.args[0] = replaceCS;
//						XposedBridge.log("!!replace setOAuthConsumerSecret");
					}
				}
			});
			findAndHookMethod("twitter4j.auth.OAuthAuthorization",
				lpparam.classLoader,
				"setOAuthConsumer",
				String.class,
				String.class,
				new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if(param.args[0] != null && param.args[1] != null){
						param.args[0] = replaceCK;
						param.args[1] = replaceCS;
//						XposedBridge.log("!!replace OAuthAuthorization");
					}
				}
			});
			findAndHookMethod("twitter4j.auth.OAuth2Authorization",
				lpparam.classLoader,
				"setOAuthConsumer",
				String.class,
				String.class,
				new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if(param.args[0] != null && param.args[1] != null){
						param.args[0] = replaceCK;
						param.args[1] = replaceCS;
//						XposedBridge.log("!!replace OAuth2Authorization");
					}
				}
			});
		}
	}
}
