//
//  CDVMWBarcodeScanner.h
//  CameraDemo
//
//  Created by vladimir zivkovic on 5/8/13.
//
//

#import "MWScannerViewController.h"
#import <Cordova/CDV.h>
#import "MWResult.h"

@interface CDVMWBarcodeScanner : CDVPlugin <ScanningFinishedDelegate>

- (void)scanningFinished:(NSString *)result withType:(NSString *)lastFormat isGS1: (bool) isGS1 andRawResult: (NSData *) rawResult locationPoints:(MWLocation *)locationPoints imageWidth:(int)imageWidth imageHeight:(int)imageHeight;
- (void)initDecoder:(CDVInvokedUrlCommand*)command;
- (void)startScanner:(CDVInvokedUrlCommand*)command;
- (void)setActiveCodes:(CDVInvokedUrlCommand*)command;
- (void)setActiveSubcodes:(CDVInvokedUrlCommand*)command;
- (void)setFlags:(CDVInvokedUrlCommand*)command;
- (void)setDirection:(CDVInvokedUrlCommand*)command;
- (void)setScanningRect:(CDVInvokedUrlCommand*)command;
- (void)setLevel:(CDVInvokedUrlCommand*)command;
- (void)registerCode:(CDVInvokedUrlCommand*)command;
- (int)getLastType:(CDVInvokedUrlCommand*)command;

- (void)setInterfaceOrientation:(CDVInvokedUrlCommand*)command;
- (void)setOverlayMode:(CDVInvokedUrlCommand*)command;
- (void)enableHiRes:(CDVInvokedUrlCommand*)command;
- (void)enableFlash:(CDVInvokedUrlCommand*)command;
- (void)enableZoom:(CDVInvokedUrlCommand*)command;
- (void)turnFlashOn:(CDVInvokedUrlCommand*)command;
- (void)setZoomLevels:(CDVInvokedUrlCommand*)command;
- (void)setMaxThreads:(CDVInvokedUrlCommand*)command;
- (void)setCustomParam:(CDVInvokedUrlCommand*)command;
- (void)scanImage:(CDVInvokedUrlCommand*)command;
- (void)setParam:(CDVInvokedUrlCommand*)command;

@end

