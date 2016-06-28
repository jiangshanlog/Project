//
//  EndLiveViewController.m
//  直播结束view
//
//  Created by 姜杉 on 16/3/18.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "EndLiveViewController.h"
#import <Masonry.h>
#define kscreenW [UIScreen mainScreen].bounds.size.width
#define kscreenH [UIScreen mainScreen].bounds.size.height
@interface EndLiveViewController ()

@end

@implementation EndLiveViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    UIImageView *BackgroundImage = [[UIImageView alloc]initWithFrame:[UIScreen mainScreen].bounds];
    BackgroundImage.image = [UIImage imageNamed:@"直播结束-正在保存中-背景"];
    BackgroundImage.userInteractionEnabled = YES;
    [self.view addSubview:BackgroundImage];
    
    
    UIButton *RightCancelButton = [[UIButton alloc]init];
    [RightCancelButton setImage:[UIImage imageNamed:@"iconClose"] forState:UIControlStateNormal];
    [RightCancelButton addTarget:self action:@selector(RightCancelButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        [BackgroundImage addSubview:RightCancelButton];
    [RightCancelButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo (BackgroundImage).with.offset (30);
        make.right.equalTo (BackgroundImage).with.offset (-20);
        make.width.mas_equalTo (@40);
        make.height.mas_equalTo (@40);
    }];
    RightCancelButton.imageEdgeInsets = UIEdgeInsetsMake(10, 10, 10, 10);

    
    UILabel *SubTitle = [[UILabel alloc]init];
    SubTitle.text = @"本次直播获得了534个金币";
    SubTitle.textColor = [UIColor whiteColor];
    [SubTitle sizeToFit];
    [BackgroundImage addSubview:SubTitle];
    [SubTitle mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo (BackgroundImage);
        make.centerY.equalTo (BackgroundImage).with.offset (-38);
        
    }];
    
    
    UILabel *MainTitle = [[UILabel alloc]init];
    MainTitle.text = @"直播已结束";
    MainTitle.textColor = [UIColor whiteColor];
    MainTitle.font = [UIFont systemFontOfSize:40 weight:15];
    [MainTitle sizeToFit];
    [BackgroundImage addSubview:MainTitle];
    [MainTitle mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo (SubTitle.mas_top).with.offset (-21);
        make.centerX.equalTo (BackgroundImage);
    }];
    
    

    
    UIButton *WeiboBtn = [[UIButton alloc]init];
    [WeiboBtn setImage:[UIImage imageNamed:@"btn_sharelist_weibo"] forState:UIControlStateNormal];
    [WeiboBtn addTarget:self action:@selector(WeiboBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    [BackgroundImage addSubview:WeiboBtn];
    [WeiboBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo (BackgroundImage);
        make.width.mas_equalTo (@65);
        make.height.mas_equalTo (@65);
        make.bottom.equalTo (BackgroundImage).with.offset (-72*(kscreenW/375));
    }];
    
    
    UIButton *WeixinBtn = [[UIButton alloc]init];
    [WeixinBtn setImage:[UIImage imageNamed:@"btn_sharelist_wx"] forState:UIControlStateNormal];
    [WeixinBtn addTarget:self action:@selector(WeixinClick:) forControlEvents:UIControlEventTouchUpInside];
    [BackgroundImage addSubview:WeixinBtn];
    [WeixinBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo (@65);
        make.height.mas_equalTo (@65);
        make.bottom.equalTo (BackgroundImage).with.offset (-72*(kscreenW/375));
        make.right.equalTo (WeiboBtn.mas_left).with.offset (-33);
        
    }];
    
    UIButton *QQBtn = [[UIButton alloc]init];
    [QQBtn setImage:[UIImage imageNamed:@"btn_sharelist_qq"] forState:UIControlStateNormal];
    [QQBtn addTarget:self action:@selector(QQBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    [BackgroundImage addSubview:QQBtn];
    [QQBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo (@65);
        make.height.mas_equalTo (@65);
        make.bottom.equalTo (BackgroundImage).with.offset (-72*(kscreenW/375));
        make.left.equalTo (WeiboBtn.mas_right).with.offset (33);
    }];
    
    
    UILabel *ShareLabel = [[UILabel alloc]init];
    ShareLabel.text = @"分享到";
    ShareLabel.font = [UIFont systemFontOfSize:20];
    ShareLabel.textColor = [UIColor whiteColor];
    [ShareLabel sizeToFit];
    [BackgroundImage addSubview:ShareLabel];
    [ShareLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo (BackgroundImage);
        make.bottom.equalTo (WeiboBtn.mas_top).with.offset (-10);
    }];
    
    UILabel *WeiboLabel = [[UILabel alloc]init];
    WeiboLabel.text = @"微博";
    WeiboLabel.font = [UIFont systemFontOfSize:16];
    WeiboLabel.textColor = [UIColor whiteColor];
    [BackgroundImage addSubview:WeiboLabel];
    [WeiboLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo (BackgroundImage);
        make.top.equalTo (WeiboBtn.mas_bottom).with.offset (10);
    }];
    
    UILabel *WeixinLabel = [[UILabel alloc]init];
    WeixinLabel.text = @"微信";
    WeixinLabel.textColor = [UIColor whiteColor];
    WeixinLabel.font = [UIFont systemFontOfSize:16];
    [BackgroundImage addSubview:WeixinLabel];
    [WeixinLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo (WeixinBtn);
        make.top.equalTo (WeixinBtn.mas_bottom).with.offset (10);
    }];
    
    UILabel *QQLabel = [[UILabel alloc]init];
    QQLabel.text = @"QQ";
    QQLabel.textColor = [UIColor whiteColor];
    QQLabel.font = [UIFont systemFontOfSize:16];
    [BackgroundImage addSubview:QQLabel];
    [QQLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo (QQBtn);
        make.top.equalTo (QQBtn.mas_bottom).with.offset (10);
    }];

    UILabel *SaveTitle = [[UILabel alloc]init];
    SaveTitle.text = @"正在保存中...";
    SaveTitle.textColor = [UIColor whiteColor];
    [SaveTitle sizeToFit];
    [BackgroundImage addSubview:SaveTitle];
    [SaveTitle mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo (ShareLabel.mas_top).with.offset (-45);
        make.centerX.equalTo (BackgroundImage);
    }];
    
    
}

- (void)RightCancelButtonClick:(UIButton *)sender{
    NSLog(@"点击取消按钮");
}
- (void)WeiboBtnClick:(UIButton *)sender{
    NSLog(@"点击微博");
}
- (void)WeixinClick:(UIButton *)sender{
    NSLog(@"点击微信");
}
- (void)QQBtnClick:(UIButton *)sender{
    NSLog(@"点击QQ");
}

@end
