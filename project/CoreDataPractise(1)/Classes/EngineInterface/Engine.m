//
//  Engine.m
//  CoreDataPractise
//
//  Created by 中软mac028 on 15/9/1.
//  Copyright (c) 2015年 中软mac028. All rights reserved.
//

#import "Engine.h"
#import "YXManager+Helper.h"
static Engine *instance;
@interface Engine ()
{
    NSArray *array;
    YXManager *manager;
}
@end
@implementation Engine
+ (id) shareInstance
{
     @synchronized(self){
         if (instance == nil) { 
             instance = [[Engine alloc] init];
            
         }
     }
    return instance;
}
- (void) addObject:(id)object
{
    manager = [YXManager shareInstance];
    BOOL isSuccess = [manager addObjectToDb:object];
    if (isSuccess) {
        NSLog(@"添加成功");
    } else {
        NSLog(@"添加失败");
    }
}
- (NSArray*) queryObjects
{
    if (array == nil) {
        array = [[YXManager shareInstance] queryObjectsFromDb];
    }
    return array;
}
- (void) deleteObjectFromName:(NSString *)name
{
    manager = [YXManager shareInstance];
    BOOL isSuccess =[manager deleteObjectFromName:name];
    if (isSuccess) {
        NSLog(@"删除成功");
    } else {
        NSLog(@"删除失败");
    }
}
- (void) updateObject:(id)object
{
    manager = [YXManager shareInstance];
    BOOL isSuccess =[manager updateObjectToDb:object];
    if (isSuccess) {
        NSLog(@"更新成功");
    } else {
        NSLog(@"更新失败");
    }
}
@end
