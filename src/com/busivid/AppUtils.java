package com.busivid.AppUtils;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppUtils extends CordovaPlugin {

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        	if (action.equals("BundleInfo")) {
 			Map<string, string> bundleInfo = this.bundleInfo();
			callbackContext.success(bundleInfo);
			return true;
            	} 
		return false;
	}

	public Map<string, string> bundleInfo() {
		Map<String, String> results = new Map<String, String>();

		PackageInfo packageInfo = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), 0);

		results.put("bundleVersion", packageInfo.versionCode);
		results.put("bundleId", this.cordova.getActivity().getPackageName());
                results.put("isDebug", BuildConfig.DEBUG);
		return results;	
	}

}
