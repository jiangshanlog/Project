//
//  Model.h
//  UI封装demo
//
//  Created by 姜杉 on 16/5/5.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface Model : NSObject
@property (nonatomic, assign) BOOL isNight;

+ (instancetype)shareInstance;

- (void)saveAccountInfoToDisk;

- (void)loadAccountInfoFromDisk;



@end


@interface NightManager : NSObject

+ (void)setLabelColorWithLabel:(UILabel *)label;

+ (void)setBackgroundColorWithView:(UIView *)view;

+ (void)setButtonTitleColorWithButton:(UIButton *)button;
@end