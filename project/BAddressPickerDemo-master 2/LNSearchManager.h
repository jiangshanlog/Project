//
//  LNSearchManager.h
//  Bee
//
//  Created by 林洁 on 16/1/14.
//  Copyright © 2016年 Lin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import "LNLocationGeocoder.h"

@interface LNSearchManager : NSObject

@property (nonatomic, strong) CLGeocoder *gecoder;

@property (nonatomic, copy) void (^completionBlock)(LNLocationGeocoder *locationGeocoder,NSError *error);

- (void)startReverseGeocode:(CLLocation*)location completeionBlock:(void(^)(LNLocationGeocoder *locationGeocoder,NSError *error))completeion;

@end
// 版权属于原作者
// http://code4app.com (cn) http://code4app.net (en)
// 发布代码于最专业的源码分享网站: Code4App.com