//
//  AppDelegate.m
//  TuTestDemo
//
//  Created by 姜杉 on 16/3/23.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "AppDelegate.h"
#import <TuSDKGeeV1/TuSDKGeeV1.h>
#import "NaviViewController.h"
#import "CameraViewController.h"

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    [TuSDK initSdkWithAppKey:@"096fc9a52d20ea91-00-jjr0p1"];
    [TuSDK setLogLevel:lsqLogLevelDEBUG];
    self.window = [[UIWindow alloc]initWithFrame:[UIScreen mainScreen].bounds];
//    NaviViewController *na = [[NaviViewController alloc]init];
    CameraViewController *na = [[CameraViewController alloc]init];
    UINavigationController *navi = [[UINavigationController alloc]initWithRootViewController:na];
    self.window.rootViewController = navi;
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
