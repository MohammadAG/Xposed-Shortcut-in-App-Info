package com.mohammadag.shortcutinappinfo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.android.settings"))
			return;

		XposedHelpers.findAndHookMethod("com.android.settings.applications.InstalledAppDetails",
				lpparam.classLoader, "setAppLabelAndIcon", PackageInfo.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				PackageInfo info = (PackageInfo) param.args[0];
				Object view;
				Resources mResources;
				View appSnippet;
				
				if (Build.VERSION.SDK_INT >= 23) {
				    view = XposedHelpers.getObjectField(param.thisObject, "mHeader");
				    mResources = (Resources) XposedHelpers.callMethod(param.thisObject, "getResources");
				} else {
				    view = XposedHelpers.getObjectField(param.thisObject, "mRootView");
				    mResources = ((View) view).getResources();
				}
				
				int appSnippetId = mResources.getIdentifier("app_snippet", "id", "com.android.settings");
				int iconId;
								
				if (Build.VERSION.SDK_INT >= 23) {
				    iconId = mResources.getIdentifier("icon", "id", "android");
				    appSnippet = (View) XposedHelpers.callMethod(view, "findViewById", appSnippetId);
				} else {
				    iconId = mResources.getIdentifier("app_icon", "id", "com.android.settings");
				    appSnippet = ((View) view).findViewById(appSnippetId);
				}

				ImageView icon = (ImageView) appSnippet.findViewById(iconId);

				final String packageName = info.packageName;

				icon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						PackageManager pm = v.getContext().getPackageManager();
						Intent intent = pm.getLaunchIntentForPackage(packageName);
						if (intent == null)
							return;
						v.getContext().startActivity(intent);
						v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
								HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
					}
				});
			}
		});
	}

}
