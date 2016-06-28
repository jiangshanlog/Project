//
//  CompleteViewController.h
//  UICEditSDK
//
//  Created by 姜杉 on 16/5/18.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CompleteViewController;
@protocol CompleteViewControllerDelegate <NSObject>
- (void)dgCompleteViewController_complete:(CompleteViewController*)vc;
@end

@interface CompleteViewController : UIViewController
+ (instancetype)sCreateSelf;
+ (instancetype)sCreateSelfWithDelegate:(id<CompleteViewControllerDelegate>)delegate;

@property (nonatomic,weak) id<CompleteViewControllerDelegate> dgDelegate;

- (void)pSetupOrigImage:(UIImage*)oImage withPreImage:(UIImage*)pImage;
@end
