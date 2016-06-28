//
//  Model.m
//  二维码扫描
//
//  Created by 姜杉 on 16/3/31.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "Model.h"

@implementation Model
+(id)initWithnumber{
    static Model *model;
    if (model==nil) {
        //只初始化一次
        model = [[Model alloc]init];
    }
    return model;
}
@end
