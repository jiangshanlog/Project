//
//  BHotCityCell.h
//  Bee
//
//  Created by 林洁 on 16/1/13.
//  Copyright © 2016年 Lin. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface BHotCityCell : UITableViewCell

@property (nonatomic,copy) void (^buttonClickBlock)(UIButton *);

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier array:(NSArray*)cities;

- (void)buttonWhenClick:(void(^)(UIButton *button))block;

@end
// 版权属于原作者
// http://code4app.com (cn) http://code4app.net (en)
// 发布代码于最专业的源码分享网站: Code4App.com