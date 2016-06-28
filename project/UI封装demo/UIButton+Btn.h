//
//  UIButton+Btn.h
//  UI封装demo
//
//  Created by 姜杉 on 16/5/5.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIButton (Btn)
- (UIButton *)createButtonWithBtn:(UIButton *)button frame:(CGRect)frame title:(NSString *)title tag:(NSInteger)tag selector:(SEL)selector conrroller:(UIViewController *)controller;
@end
