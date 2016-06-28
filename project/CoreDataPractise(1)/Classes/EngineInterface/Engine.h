//
//  Engine.h
//  CoreDataPractise
//
//  Created by 中软mac028 on 15/9/1.
//  Copyright (c) 2015年 中软mac028. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "YXManager+Helper.h"
@interface Engine : NSObject
- (void) addObject:(id) object;
- (NSArray*) queryObjects;
- (void) deleteObjectFromName:(NSString*)name;
- (void) updateObject:(id) object;
+ (id) shareInstance;
@end
