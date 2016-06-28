//
//  UIViewController+Navigation.m
//  UI封装demo
//
//  Created by 姜杉 on 16/5/5.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "UIViewController+Navigation.h"

@implementation UIViewController (Navigation)
- (void)initWithTitle:(NSString *)title leftImg:(NSString *)leftimg rightImg:(NSString *)rightimg{
    self.navigationController.navigationBar.hidden = NO;
    [self.navigationController.navigationBar setBarTintColor:[UIColor blackColor]];
    self.navigationController.navigationBar.titleTextAttributes = @{NSForegroundColorAttributeName : [UIColor whiteColor], NSFontAttributeName : [UIFont systemFontOfSize:20]};
    self.navigationController.navigationBar.tintColor = [UIColor clearColor];
    
    if (title) {
        self.title = title;
    }
    
    if (leftimg) {
        UIButton* btn = [UIButton buttonWithType:UIButtonTypeCustom];
        UIImage* image = [UIImage imageNamed:leftimg];
        [btn setImage:image forState:UIControlStateNormal];
        [btn setFrame:CGRectMake(0, 0, image.size.width, image.size.height)];
        [btn addTarget:self action:@selector(leftAction) forControlEvents:UIControlEventTouchUpInside];
        UIBarButtonItem* item = [[UIBarButtonItem alloc] initWithCustomView:btn];
        self.navigationItem.leftBarButtonItems = @[item];
    }
    
    if (rightimg) {
        UIButton* btn = [UIButton buttonWithType:UIButtonTypeCustom];
        UIImage* rImage = [UIImage imageNamed:rightimg];
        [btn setImage:rImage forState:UIControlStateNormal];
        [btn setFrame:CGRectMake(0, 0, rImage.size.width, rImage.size.height)];
        
        UIBarButtonItem* sItem = [[UIBarButtonItem alloc] initWithCustomView:btn];
        self.navigationItem.rightBarButtonItem = sItem;
    }
}
@end
