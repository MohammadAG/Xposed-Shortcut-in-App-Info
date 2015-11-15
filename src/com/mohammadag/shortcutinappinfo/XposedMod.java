package com.mohammadag.shortcutinappinfo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
				Object mHeader = XposedHelpers.getObjectField(param.thisObject, "mHeader");
				Resources res = (Resources) XposedHelpers.callMethod(param.thisObject, "getResources");

				int appSnippetId = res.getIdentifier("app_snippet", "id", "com.android.settings");
				View appSnippet = (View) XposedHelpers.callMethod(mHeader, "findViewById", appSnippetId);

				int iconId = res.getIdentifier("icon", "id", "android");
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
