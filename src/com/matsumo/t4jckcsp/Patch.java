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

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Patch implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {
	//TODO set package name for target applications
	private static String[] TARGET_PKG = {"your.app.package.name1", "your.app.package.name2"/* ... */};
	//TODO set consumer key
	private static String REPLACE_CK = "YOUR CONSUMER KEY";
	//TODO set consumer secret
	private static String REPLACE_CS = "YOUR CONSUMER SECRET";

	@Override
	public void initZygote(StartupParam startupParam) {
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		boolean found = false;
		for(int i=0; i<TARGET_PKG.length; i++){
			if (lpparam.packageName.compareTo(TARGET_PKG[i]) == 0){
				found = true;
				break;
			}
		}
		if(found){
			XposedBridge.log("!!found pkg="+lpparam.packageName);
			findAndHookMethod("twitter4j.conf.ConfigurationBase",
				lpparam.classLoader,
				"setOAuthConsumerKey",
				String.class,
				new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if(param.args[0] != null){
						param.args[0] = REPLACE_CK;
						XposedBridge.log("!!replace setOAuthConsumerKey");
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
						param.args[0] = REPLACE_CS;
						XposedBridge.log("!!replace setOAuthConsumerSecret");
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
						param.args[0] = REPLACE_CK;
						param.args[1] = REPLACE_CS;
						XposedBridge.log("!!replace OAuthAuthorization");
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
						param.args[0] = REPLACE_CK;
						param.args[1] = REPLACE_CS;
						XposedBridge.log("!!replace OAuth2Authorization");
					}
				}
			});
		}
	}
}
