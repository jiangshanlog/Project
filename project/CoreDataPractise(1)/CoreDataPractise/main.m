//
//  main.m
//  CoreDataPractise
//
//  Created by 中软mac028 on 15/9/1.
//  Copyright (c) 2015年 中软mac028. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Engine.h"
#import "CompanyInfo.h"
#import "Company.h"
int main(int argc, const char * argv[]) {
    @autoreleasepool {
        
        Engine *engine = [[Engine alloc]init];
        CompanyInfo *companyInfo = [[CompanyInfo alloc] init];
        companyInfo.cpyid = @1;
        companyInfo.name = @"et2";
        companyInfo.address = @"天地软件2";
        companyInfo.phone = @"411350452";
        companyInfo.city = @"大连";
       [engine addObject:companyInfo];
//        [engine deleteObjectFromName:@"et2"];
      //  [engine updateObject:companyInfo];
        NSArray* array = [engine queryObjects];
//        Company *company = [[Company alloc] init];
//        company.cpyid = @1;
        
      //  CompanyInfo *com1 = (CompanyInfo*)[array objectAtIndex:0];
       // company.cpyid = com1.cpyid;
        
      //  [engine addObject:companyInfo];
        
        for (CompanyInfo *com in array) {
            NSLog(@"%@",[com cpyid]);
            NSLog(@"%@", [com name]);
            NSLog(@"%@",[com address]);
            NSLog(@"%@",[com phone]);
             NSLog(@"%@",[com version]);
            NSLog(@"%@", [com city]);
        }
        NSLog(@"-------------------");
        NSArray*  array1 = [engine queryObjects];
        for (CompanyInfo *com1 in array1) {
            NSLog(@"%@",[com1 cpyid]);
            NSLog(@"%@", [com1 name]);
            NSLog(@"%@",[com1 address]);
            NSLog(@"%@",[com1 phone]);
        }
        
        
    }
    return 0;
}
