//
//  LNLocationManager.h
//  BAddressPickerDemo
//
//  Created by 林洁 on 16/1/14.
//  Copyright © 2016年 onlylin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>

@interface LNLocationManager : UIView<CLLocationManagerDelegate>

@property (nonatomic, strong) CLLocationManager *loactionManager;

@property (nonatomic, copy) void (^startBlock)(void);

@property (nonatomic, copy) void (^successCompletionBlock)(CLLocation *location);

@property (nonatomic, copy) void (^failureCompletionBlock)(CLLocation *location,NSError *error);


- (void)startWithBlock:(void(^)(void))start
       completionBlock:(void(^)(CLLocation *location))success
               failure:(void(^)(CLLocation *location, NSError *error))failure;

@end
// 版权属于原作者
// http://code4app.com (cn) http://code4app.net (en)
// 发布代码于最专业的源码分享网站: Code4App.com