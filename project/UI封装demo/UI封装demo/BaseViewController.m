//
//  BaseViewController.m
//  UI封装demo
//
//  Created by 姜杉 on 16/5/5.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "BaseViewController.h"
#import "Model.h"
#define kLightDayModelNotification @"LightDayModelNotification"
#define kNightDayModelNotification @"NightDayModelNotification"
@implementation BaseViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    if ([Model shareInstance].isNight) {
        [self setNightDayModel];
    } else {
        [self setLightDayModel];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(setLightDayModel) name:kLightDayModelNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(setNightDayModel) name:kNightDayModelNotification object:nil];
    
}

- (void)setLightDayModel {
    NSLog(@"-------设置白天模式");
}

- (void)setNightDayModel {
    NSLog(@"-------设置夜晚模式");
}

- (void)dealloc {
    
    // 必须在dealloc方法中移除观察
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
