cordova-plugin-app-utils
========================

Cordova AppUtils Plugin for Apache Cordova >= 3.0.0

## Installation

    cordova plugin add https://github.com/ryanwilliams83/cordova-plugin-app-utils.git
    
## AppUtils

### IdleTimer

Enable / Disable the device sleep mode.

		// Enable the IdleTimer
		apputils.IdleTimer.enable(onSuccess, onError);
		/*
			onSuccess:
				"OK"
			onError:
				{ code: 1, reason: "IdleTimer already enabled." }
		*/

		// Disable the IdleTimer
		apputils.IdleTimer.disable(onSuccess, onError);
		/*
			onSuccess:
				"OK"
			onError:
				{ code: 1, reason: "IdleTimer already disabled." }
		*/

		// Get the IdleTimer Status
		apputils.IdleTimer.getStatus(onSuccess);
		/*
			onSuccess:
				0 = disabled
				1 = enabled
		*/

### BundleInfo

Get the App Bundle Info.

		apputils.BundleInfo(onSuccess);
		/*
			onSuccess:
				{
					"localeLanguage": <STRING>,
					"bundleBuild": <STRING>,
					"bundleVersion": <STRING>,
					"bundleId": <STRING>,
					"bundleDisplayName": <STRING>,
					"bundleIsDebug": <STRING>
				}
				automatic available under "apputils.info"
		*/

## Supported Platforms
Android
iOS

## Version History

### 0.1.0 (2014-06)
* initial version
