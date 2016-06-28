//
//  YXManager+Helper.h
//  CoreDataPractise
//
//  Created by 中软mac028 on 15/9/1.
//  Copyright (c) 2015年 中软mac028. All rights reserved.
//

#import "YXManager.h"

@interface YXManager (Helper)

- (BOOL) addObjectToDb:(id)object;
- (BOOL) deleteObjectFromName:(NSString*)name;
- (BOOL) updateObjectToDb:(id)object;
- (NSArray*) queryObjectsFromDb;
@end
