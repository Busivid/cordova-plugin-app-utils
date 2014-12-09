var DEVICE_PLATFORM_ANDROID = 'Android';
var DEVICE_PLATFORM_IOS = 'iOS';

var exec = require('cordova/exec'),
	channel = require('cordova/channel'),
	utils = require('cordova/utils');

channel.createSticky('onAppUtilsReady');

// tell cordova channel to wait on the AppUtilsReady event
channel.waitForInitialization('onAppUtilsReady');

var AppUtils = function () {
	var me = this;

	me.name = 'AppUtils';
	me.version = '0.1.0';

	channel.onCordovaReady.subscribe(function () {
		me.BundleInfo(function (info) {
			me.info = info;
			channel.onAppUtilsReady.fire();
		}, function (e) {
			me.info = false;
			utils.alert('[ERROR] Error initializing Cordova: ' + e);
		});
	});

};

// BundleInfo
AppUtils.prototype.BundleInfo = function (onSuccess) {
	exec(onSuccess, null, 'AppUtils', 'BundleInfo', []);
};
		
// ComposeEmail
AppUtils.prototype.ComposeEmail = function (onSuccess, onError, options) {
	if (typeof options == 'undefined' || options == null)
		options = {};

	if (typeof options.recipients == 'string')
		options.recipients = new Array(options.recipients);

	if (typeof options.recipients == 'undefined' || !Array.isArray(options.recipients))
		options.recipients = new Array();

	exec(onSuccess, onError, 'AppUtils', 'ComposeEmail', [options]);
};

// ComposeSMS
AppUtils.prototype.ComposeSMS = function (onSuccess, onError, options) {
	if (typeof options == 'undefined' || options == null)
		options = {};

	if (typeof options.recipients == 'string')
		options.recipients = new Array(options.recipients);

	if (typeof options.recipients == 'undefined' || !Array.isArray(options.recipients))
		options.recipients = new Array();
	
	exec(onSuccess, onError, 'AppUtils', 'ComposeSMS', [options]);
};

// DeviceInfo
AppUtils.prototype.DeviceInfo = function(onSuccess) {
	exec(onSuccess, null, 'AppUtils', 'DeviceInfo', []);
};

// IdleTimer
AppUtils.prototype.IdleTimer = {
	enable: function (onSuccess, onError) {
		if (device.platform == DEVICE_PLATFORM_ANDROID)
			return;

		var options = {
			'action': 'enable'
		};
		exec(onSuccess, onError, 'AppUtils', 'IdleTimer', [options]);
	},
	disable: function (onSuccess, onError) {
		if (device.platform == DEVICE_PLATFORM_ANDROID)
			return;

		var options = {
			'action': 'disable'
		};
		exec(onSuccess, onError, 'AppUtils', 'IdleTimer', [options]);
	},
	getStatus: function (onSuccess) {
		if (device.platform == DEVICE_PLATFORM_ANDROID)
			return;

		var options = {
			'action': 'status'
		};
		exec(onSuccess, null, 'AppUtils', 'IdleTimer', [options]);
	}
};

// OpenWith
AppUtils.prototype.OpenWith = function (onSuccess, onError, options) {
	exec(onSuccess, onError, 'AppUtils', 'OpenWith', [options]);
};

// SocialShare
AppUtils.prototype.SocialShare = function (onSuccess, onError, options) {
	exec(onSuccess, onError, 'AppUtils', 'SocialShare', [options]);
};

module.exports = new AppUtils();

