//
//  EditViewController.h
//  UICEditSDK
//
//  Created by 姜杉 on 16/5/18.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <PhotoEditFramework/PhotoEditFramework.h>

typedef NS_ENUM(NSUInteger, eMainViewControllerState) {
    eMainViewControllerState_init,
    eMainViewControllerState_choosed,
    eMainViewControllerState_complate,
};
@interface EditViewController : UIViewController<pg_edit_sdk_controller_delegate>
@property (nonatomic,assign,readonly) eMainViewControllerState mState;
@end
