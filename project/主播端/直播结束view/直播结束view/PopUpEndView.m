//
//  PopUpEndView.m
//  直播结束view
//
//  Created by 姜杉 on 16/3/18.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "PopUpEndView.h"
#import <Masonry.h>
#define kscreenW [UIScreen mainScreen].bounds.size.width
#define kscreenH [UIScreen mainScreen].bounds.size.height

@implementation PopUpEndView

-(id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self) {
        [self creatEndView];
    }
    return self;
}
- (void)creatEndView{
    self.backgroundColor = [UIColor whiteColor];
    UIImageView *EndViewBackgroundView = [[UIImageView alloc]initWithFrame:[UIScreen mainScreen].bounds];
    [self addSubview:EndViewBackgroundView];
    EndViewBackgroundView.userInteractionEnabled = YES;
    EndViewBackgroundView.image = [UIImage imageNamed:@"矩形-1"];
    
    
    UIImageView *Endview = [[UIImageView alloc]init];
    Endview.userInteractionEnabled = YES;
    Endview.image = [UIImage imageNamed:@"圆角矩形-2"];
    [EndViewBackgroundView addSubview:Endview];
    

    [Endview mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo (@249);
        make.height.mas_equalTo (@165);
        make.centerX.equalTo (EndViewBackgroundView);
        make.centerY.equalTo (EndViewBackgroundView).with.offset (-40);
    }];
    
    UILabel *EndTitle = [[UILabel alloc]initWithFrame:CGRectMake(42, 37, 180, 30)];
    EndTitle.text = @"是否结束直播";
    EndTitle.textColor = [UIColor whiteColor];
    EndTitle.font = [UIFont systemFontOfSize:27];
    [Endview addSubview:EndTitle];
    
    UIButton *SureButton = [[UIButton alloc]initWithFrame:CGRectMake(42, 101, 68, 30)];
    [SureButton setImage:[UIImage imageNamed:@"btn_confirm_normal"] forState:UIControlStateNormal];
    [SureButton addTarget:self action:@selector(SureButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    
    
    
    [Endview addSubview:SureButton];
    
    UIButton *CancelButton = [[UIButton alloc]initWithFrame:CGRectMake(138, 101, 68, 30)];
    [CancelButton setImage:[UIImage imageNamed:@"btn_cancel_normal"] forState:UIControlStateNormal];
    [CancelButton addTarget:self action:@selector(CancelButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    
    [Endview addSubview:CancelButton];
    

}
- (void)SureButtonClick:(UIButton *)sender{
    NSLog(@"确认");
    
}
- (void)CancelButtonClick:(UIButton *)sender{
    NSLog(@"取消");
}
@end
