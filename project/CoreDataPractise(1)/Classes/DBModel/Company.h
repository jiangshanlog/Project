//
//  Company.h
//  CoreDataPractise
//
//  Created by 中软mac028 on 15/9/2.
//  Copyright (c) 2015年 中软mac028. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Company : NSManagedObject

@property (nonatomic, retain) NSString * address;
@property (nonatomic, retain) NSString * city;
@property (nonatomic, retain) NSNumber * cpyid;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSString * phone;
@property (nonatomic, retain) NSNumber * version;

@end
