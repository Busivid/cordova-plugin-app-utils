package au.id.ryanwilliams.cordova.apputils;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;

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
		} else if(action.equals("ComposeEmail")) {
			JSONObject jsonObject = args.getJSONObject(0);
			composeEmail(jsonObject.getString("body"), jsonObject.getString("subject"));
			callbackContext.success();	
		} else if(action.equals("ComposeSMS")) {
			JSONObject jsonObject = args.getJSONObject(0);
			composeSMS(jsonObject.getString("body"));
			callbackContext.success();	
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

	public void composeEmail(String body, String subject) {
		Activity activity = this.cordova.getActivity();
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/html");
		//i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		i.putExtra(Intent.EXTRA_TEXT, android.text.Html.fromHtml(body));
		activity.startActivity(Intent.createChooser(i, "Send mail..."));
	}

	public void composeSMS(String body) {
		Activity activity = this.cordova.getActivity();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(activity); //Need to change the build to API 19
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.putExtra(Intent.EXTRA_TEXT, body);

			if (defaultSmsPackageName != null) // if no default then Android will allow user to select from list
				sendIntent.setPackage(defaultSmsPackageName);

			activity.startActivity(sendIntent);
			return;
		}

		//pre-kitkat way of sending an sms
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.setData(Uri.parse("sms:"));
		sendIntent.putExtra("sms_body", body);
		activity.startActivity(sendIntent);
	}
}
