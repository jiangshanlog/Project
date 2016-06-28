//
//  UIButton+Btn.m
//  UI封装demo
//
//  Created by 姜杉 on 16/5/5.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "UIButton+Btn.h"
#import <Masonry.h>
#define mScreenWidth   ([UIScreen mainScreen].bounds.size.width)
#define mScreenHeight  ([UIScreen mainScreen].bounds.size.height)
@implementation UIButton (Btn)
- (UIButton *)createButtonWithBtn:(UIButton *)button frame:(CGRect)frame title:(NSString *)title tag:(NSInteger)tag selector:(SEL)selector conrroller:(UIViewController *)controller{
    button.frame = frame;
    [button setTitle:title forState:UIControlStateNormal];
    [button addTarget:self action:selector forControlEvents:UIControlEventTouchUpInside];
    button.tag = tag;
    return button;
}
@end
