//
//  YXManager+Helper.m
//  CoreDataPractise
//
//  Created by 中软mac028 on 15/9/1.
//  Copyright (c) 2015年 中软mac028. All rights reserved.
//

#import "YXManager+Helper.h"
#import "CompanyInfo.h"
#import "Company.h"

@implementation YXManager (Helper)

- (BOOL) addObjectToDb:(id)object
{
    CompanyInfo *companyInfo;
    if (object != nil &&[object isMemberOfClass:[NSArray class]] && [object count] > 0) {
        
        for (id obj in object) {
            if ([obj isMemberOfClass:[CompanyInfo class]]) {
                companyInfo = obj;
                Company *company = [NSEntityDescription insertNewObjectForEntityForName:@"Company" inManagedObjectContext:self.managedObjectContext];
                company.cpyid = companyInfo.cpyid;
                company.name = companyInfo.name;
                company.address = companyInfo.address;
                company.phone = companyInfo.phone;
                [self saveContext];
            }
        }
        return YES;
        
    } else if(object != nil && [object isMemberOfClass:[CompanyInfo class]]) {
        companyInfo = object;
        Company *company = [NSEntityDescription insertNewObjectForEntityForName:@"Company" inManagedObjectContext:self.managedObjectContext];
        company.cpyid = companyInfo.cpyid;
        company.name = companyInfo.name;
        company.address = companyInfo.address;
        company.phone = companyInfo.phone;
        company.city = companyInfo.city;
        company.version = @1;
        [self saveContext];
        return YES;
    }
    return NO;
}
- (BOOL) deleteObjectFromName:(NSString*)name
{
    BOOL flag = NO;
    if (name == nil || name.length <= 0) {
        return flag;
    }
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Company"];
    request.predicate = [NSPredicate predicateWithFormat:@"name=%@",name];
    //////////////
    NSArray *fetchArray = [self.managedObjectContext executeFetchRequest:request error:nil];
    if (fetchArray != nil && [fetchArray count] > 0) {
        flag = YES;
        for (Company *cpy in fetchArray) {
            [self.managedObjectContext deleteObject:cpy];
        }
        [self saveContext];
    }
    
    return flag;
}

- (BOOL) updateObjectToDb:(id)object
{
    BOOL flag = NO;
    CompanyInfo *info;
    if ([object isKindOfClass:[CompanyInfo class]]) {
        info = object;
        NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Company"];
        request.predicate = [NSPredicate predicateWithFormat:@"cpyid=%@",info.cpyid];
        NSArray *fetchArray = [self.managedObjectContext executeFetchRequest:request error:nil];
        if (fetchArray != nil && [fetchArray count] > 0) {
            flag = YES;
            for (id obj in fetchArray) {
                Company *company = obj;
                company.cpyid = info.cpyid;
                company.name = info.name;
                company.address = info.address;
                company.phone = info.phone;
            }
            [self saveContext];
        }
        
        

    }
   
    return flag;
}
- (NSArray*) queryObjectsFromDb
{
    
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Company"];
    NSSortDescriptor *sort = [NSSortDescriptor sortDescriptorWithKey:@"name" ascending:YES];
    request.sortDescriptors = @[sort];
    NSError *error = nil;
    NSArray *array = [self.managedObjectContext executeFetchRequest:request error:nil];
    NSMutableArray *infoArray = [NSMutableArray array];
    if (array != nil && array.count > 0 && error == nil) {
        for (Company *cpy in array) {
            CompanyInfo *cpyInfo = [[CompanyInfo alloc] init];
            cpyInfo.cpyid = cpy.cpyid;
            cpyInfo.name = cpy.name;
            cpyInfo.address = cpy.address;
            cpyInfo.phone = cpy.phone;
            cpyInfo.city = cpy.city;
            cpyInfo.version = cpy.version;
            [infoArray addObject:cpyInfo];
        }
        return infoArray;
    }

    return nil;
}
@end
