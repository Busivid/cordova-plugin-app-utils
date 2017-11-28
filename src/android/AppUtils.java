package com.busivid.cordova.apputils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class AppUtils extends CordovaPlugin {
	private static final int EMAIL_INTENT_RESULT = 1000;
	private static final int SMS_INTENT_RESULT = 1001;

	private CallbackContext callbackContext; // The callback context from which we were invoked.

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;

		if (action.equals("BundleInfo")) {
			try {
				JSONObject bundleInfo = this.bundleInfo();
				callbackContext.success(bundleInfo);
			} catch (NameNotFoundException exception) {
				callbackContext.error(exception.getMessage());
			}
		} else if(action.equals("ComposeEmail")) {
			JSONObject jsonObject = args.getJSONObject(0);

			String body = jsonObject.getString("body");
			JSONArray recipientsJson = jsonObject.getJSONArray("recipients");
			String subject = jsonObject.getString("subject");

			String[] recipients = convertJsonArrayToStringArray(recipientsJson);

			composeEmail(body, subject, recipients);
		} else if(action.equals("ComposeSMS")) {
			JSONObject jsonObject = args.getJSONObject(0);

			String body = jsonObject.getString("body");
			JSONArray recipientsJson = jsonObject.getJSONArray("recipients");

			String[] recipients = convertJsonArrayToStringArray(recipientsJson);

			composeSMS(body, recipients);
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

	public static String[] convertJsonArrayToStringArray(JSONArray jsonArray) {
		try {
			ArrayList<String> list = new ArrayList<String>();
			for(int i = 0; i < jsonArray.length(); i++) {
				list.add((String)jsonArray.get(i));
			}

			return list.toArray(new String[list.size()]);
		} catch (JSONException exception) {
			return new String[0];
		}
	}

	public static String joinArrayToString(String[] input, String seperator) {
		String output = "";

		for (String s : input) {
			if(output == "") {
				output += s;
			} else {
				output += seperator + s;
			}
		}

		return output;
	}

	public void composeEmail(final String body, final String subject, final String[] recipients) {
		final CordovaPlugin plugin = (CordovaPlugin) this;
		Runnable worker = new Runnable() {
			public void run() {
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.setData(Uri.parse("mailto:"));
				emailIntent.setType("message/rfc822");
				//emailIntent.setType("text/html");
				emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
				emailIntent.putExtra(Intent.EXTRA_TEXT, android.text.Html.fromHtml(body));
				emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, android.text.Html.fromHtml(body));
				plugin.cordova.startActivityForResult(plugin, emailIntent, EMAIL_INTENT_RESULT);
			}
		};
		this.cordova.getThreadPool().execute(worker);
	}

	public void composeSMS(final String body, final String[] recipients) {
		final CordovaPlugin plugin = (CordovaPlugin) this;
		final String recipientsAsString = joinArrayToString(recipients, ",");

		Runnable worker = new Runnable() {
			public void run() {
				Intent smsIntent = new Intent();
				smsIntent.setData(Uri.parse("sms:" + recipientsAsString));
				smsIntent.putExtra("address", recipientsAsString);
				smsIntent.putExtra(Intent.EXTRA_TEXT, body);
				smsIntent.putExtra("exit_on_sent", true);
				smsIntent.putExtra("sms_body", body);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					smsIntent.setAction(Intent.ACTION_SENDTO);
					String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(plugin.cordova.getActivity());
					if (defaultSmsPackageName != null)
						smsIntent.setPackage(defaultSmsPackageName);
				} else {
					smsIntent.setAction(Intent.ACTION_VIEW);
				}

				plugin.cordova.startActivityForResult(plugin, smsIntent, SMS_INTENT_RESULT);
			}
		};
		this.cordova.getThreadPool().execute(worker);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == EMAIL_INTENT_RESULT || requestCode == SMS_INTENT_RESULT) {
			this.callbackContext.success();
		}
	}
}
