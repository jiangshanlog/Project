//
//  BaseViewController.h
//  UI封装demo
//
//  Created by 姜杉 on 16/5/5.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface BaseViewController : UIViewController
/**
 *  设置白天模式(公共接口)
 */
- (void)setLightDayModel;

/**
 *  设置夜间模式(公共接口)
 */
- (void)setNightDayModel;
@end
