//
//  Model.m
//  UI封装demo
//
//  Created by 姜杉 on 16/5/5.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "Model.h"
#define kLightDayModelNotification @"LightDayModelNotification"
#define kNightDayModelNotification @"NightDayModelNotification"

@implementation Model

- (void)setIsNight:(BOOL)isNight {
    _isNight = isNight;
    
    if (isNight) {
        [[NSNotificationCenter defaultCenter] postNotificationName:kNightDayModelNotification object:nil];
    } else {
        [[NSNotificationCenter defaultCenter] postNotificationName:kLightDayModelNotification object:nil];
    }
}


+ (instancetype)shareInstance {
    static dispatch_once_t onceToken;
    static Model *model = nil;
    dispatch_once(&onceToken, ^{
        model = [[Model alloc]init];
    });
    return model;
}

- (void)saveAccountInfoToDisk {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    
    NSString  *isNigthStr;
    if (self.isNight) {
        isNigthStr = @"yes";
    } else {
        isNigthStr = @"no";
    }
    [ud setObject:isNigthStr forKey:@"isNight"];
    
    [ud synchronize];
    
}

- (void)loadAccountInfoFromDisk {
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    
    NSString  *isNigthStr = [ud objectForKey:@"isNight"];
    self.isNight = [isNigthStr isEqualToString:@"yes"];
    
}

@end
@implementation NightManager

+ (void)setLabelColorWithLabel:(UILabel *)label {
    
    if ([Model shareInstance].isNight) {
        label.textColor = [UIColor blackColor];
    } else {
                label.textColor = [UIColor whiteColor];

    }
    
}

+ (void)setBackgroundColorWithView:(UIView *)view {
    if ([Model shareInstance].isNight) {
        view.backgroundColor = [UIColor whiteColor];
    } else {
                view.backgroundColor = [UIColor blackColor];

    }
}

+ (void)setButtonTitleColorWithButton:(UIButton *)button {
    if ([Model shareInstance].isNight) {
        [button setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    } else {
        [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    }
}
@end
