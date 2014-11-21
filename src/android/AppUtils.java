package au.id.ryanwilliams.cordova.apputils;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.Method;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppUtils extends CordovaPlugin {

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("BundleInfo")) {
			try {
 				JSONObject bundleInfo = this.bundleInfo();
				callbackContext.success(bundleInfo);
			} catch (NameNotFoundException exception) {
				callbackContext.error(exception.getMessage());	
			}
		} else if(action.equals("SocialShare")) {
			//will put some logic here soon
			JSONObject socialShareResults = new JSONObject();
			callbackContext.success(socialShareResults);	
		} else if (action.equals("DeviceInfo")) {
			JSONObject deviceInfo = new JSONObject();
			deviceInfo.put("name", this.getHostname());
			callbackContext.success(deviceInfo);
		} else {
			return false;
		}
		return true;
	}

	public JSONObject bundleInfo() throws JSONException, NameNotFoundException {
		JSONObject results = new JSONObject();

		Activity cordovaActivity = this.cordova.getActivity();
		String packageName = cordovaActivity.getPackageName();
		
		PackageInfo packageInfo = cordovaActivity.getPackageManager().getPackageInfo(packageName, 0);

		boolean isDebug = (cordovaActivity.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

		results.put("bundleVersion", packageInfo.versionCode);
		results.put("bundleId", packageName);
		results.put("bundleIsDebug", isDebug);
		return results;	
	}

	public static String getHostname() {
		try {
			Method getString = Build.class.getDeclaredMethod("getString", String.class);
			getString.setAccessible(true);
			return getString.invoke(null, "net.hostname").toString();
		} catch (Exception ex) {
			return null;
		}
	}
}
