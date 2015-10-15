//
//  AppUtils.m
//
//  The MIT License
//
//  Copyright (c) 2014 Paul Cervenka
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy of this
//  software and associated documentation files (the "Software"), to deal in the Software
//  without restriction, including without limitation the rights to use, copy, modify, merge,
//  publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to
//  whom the Software is furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all copies or
//  substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
//  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
//  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
//  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
//  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
//  IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//  SOFTWARE.
//

#import "AppUtils.h"

@implementation AppUtils

@synthesize controller;
@synthesize popover;

NSString* callbackId;

- (void)DeviceInfo:(CDVInvokedUrlCommand *)command
{
    UIDevice* device = [UIDevice currentDevice]; 
    NSMutableDictionary* deviceInfo = [NSMutableDictionary dictionaryWithCapacity:1];
    [deviceInfo setObject:[device name] forKey:@"name"];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:deviceInfo];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)IdleTimer:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* pluginResult = nil;
    NSMutableDictionary *options = [command.arguments objectAtIndex:0];
    UIApplication* app = [UIApplication sharedApplication];
    NSString *action = [options objectForKey:@"action"];
    
    if ([action isEqualToString: @"enable"]) {
        if( [app isIdleTimerDisabled] ) {
            [app setIdleTimerDisabled:false];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            // Error 1 - IdleTimer already enabled
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:
                                                                                                         [NSNumber numberWithInt:1], @"code",
                                                                                                         @"IdleTimer already enabled.", @"reason", nil]];
        }
    } else if ([action isEqualToString: @"disable"]) {
        if( ![app isIdleTimerDisabled] ) {
            [app setIdleTimerDisabled:true];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            // Error 1 - IdleTimer already disabled
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:
                                                                                                         [NSNumber numberWithInt:1], @"code",
                                                                                                         @"IdleTimer already disabled.", @"reason", nil]];
        }
    } else if ([action isEqualToString: @"status"]) {
        if( [app isIdleTimerDisabled] ) {
            // disabled
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
        } else {
            // enabled
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:1];
        }
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)BundleInfo:(CDVInvokedUrlCommand *)command
{
    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil;
        
        #ifdef DEBUG
            BOOL isDebug = YES;
        #else
            BOOL isDebug = NO;
        #endif
        
        NSDictionary *info = [NSDictionary dictionaryWithObjectsAndKeys:
                              [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"], @"bundleVersion",
                              [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"], @"bundleBuild",
                              [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleIdentifier"], @"bundleId",
                              [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleDisplayName"], @"bundleDisplayName",
                              [NSNumber numberWithBool:isDebug], @"bundleIsDebug",
                              [[NSLocale preferredLanguages] objectAtIndex:0], @"localeLanguage", nil];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:info];
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) ComposeEmail:(CDVInvokedUrlCommand *)command
{
	NSMutableDictionary* options = [command.arguments objectAtIndex:0];
	NSString* body = [options objectForKey:@"body"];
	NSArray* recipients = [options objectForKey:@"recipients"];
	NSString* subject = [options objectForKey:@"subject"];
	CDVPluginResult* pluginResult = nil;
	
	if (![MFMailComposeViewController canSendMail]) {
		pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:
																									 [NSNumber numberWithInt:2],
																									 @"code", @"Unavailable",
																									 @"reason", nil]];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
		return;
	}
	
	MFMailComposeViewController* mailController = [[MFMailComposeViewController alloc] init];
	
	callbackId = command.callbackId;
	self.mailController = mailController;
	mailController.mailComposeDelegate = self;
	
	[mailController setMessageBody:body isHTML:YES];
	[mailController setSubject:subject];
	[mailController setToRecipients:recipients];
	[self.viewController presentViewController:mailController animated:YES completion:nil];
}

- (void) ComposeSMS:(CDVInvokedUrlCommand *)command
{
	NSMutableDictionary* options = [command.arguments objectAtIndex:0];
	NSString* body = [options objectForKey:@"body"];
	NSArray* recipients = [options objectForKey:@"recipients"];
	NSString* subject = [options objectForKey:@"subject"];
	CDVPluginResult* pluginResult = nil;

	if (![MFMessageComposeViewController canSendText]) {
		pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:
																									 [NSNumber numberWithInt:2],
																									 @"code", @"Unavailable",
																									 @"reason", nil]];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
		return;
	}
	
	// Will display a dialog if text messaging is unavailable.
	MFMessageComposeViewController* messageController = [[MFMessageComposeViewController alloc] init];
	
	callbackId = command.callbackId;
	self.messageController = messageController;
	messageController.messageComposeDelegate = self;
	
	[messageController setBody:body];
	[messageController setRecipients:recipients];
	if ([MFMessageComposeViewController canSendSubject])
		[messageController setSubject:subject];
	[self.viewController presentViewController:messageController animated:YES completion:nil];
}

- (void)mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error {
	CDVPluginResult* pluginResult = nil;
	switch (result) {
		case MFMailComposeResultCancelled:
		case MFMailComposeResultSaved:
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:
																										 [NSNumber numberWithInt:2],
																										 @"code", @"Cancelled",
																										 @"reason", nil]];
			break;
			
		case MFMailComposeResultFailed:
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:
																										 [NSNumber numberWithInt:2],
																										 @"code", @"Failed",
																										 @"reason", nil]];
			break;

		case MFMailComposeResultSent:
			// The user successfully queued or sent the message.
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
			break;
			
		default:
			break;
	}
	
	[self.mailController dismissViewControllerAnimated:YES completion:nil];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}
- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult) result
{
	CDVPluginResult* pluginResult = nil;
	switch (result) {
		case MessageComposeResultCancelled:
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:
																										 [NSNumber numberWithInt:2],
																										 @"code", @"Cancelled",
																										 @"reason", nil]];
			break;
			
		case MessageComposeResultFailed:
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:
																										 [NSNumber numberWithInt:2],
																										 @"code", @"Failed",
																										 @"reason", nil]];
			break;
			
		case MessageComposeResultSent:
			// The user successfully queued or sent the message.
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
			break;
			
		default:
			break;
	}
	
	[self.messageController dismissViewControllerAnimated:YES completion:nil];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

@end
