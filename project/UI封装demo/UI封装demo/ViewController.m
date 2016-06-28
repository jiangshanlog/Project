//
//  ViewController.m
//  UI封装demo
//
//  Created by 姜杉 on 16/5/4.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "ViewController.h"
#import "TextLabelView.h"
#import "Model.h"
#import "UIButton+Btn.h"

#define mScreenWidth   ([UIScreen mainScreen].bounds.size.width)
#define mScreenHeight  ([UIScreen mainScreen].bounds.size.height)
@interface ViewController ()
@property (nonatomic ,strong)TextLabelView *textview;
@property (nonatomic ,strong)UIButton *button;
@property (nonatomic,strong)UIButton *btntest;
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    _btntest = [[UIButton alloc]init];
    [self.view addSubview:[_btntest createButtonWithBtn:_btntest frame:CGRectMake(0, 0, 100, 100) title:@"title" tag:11 selector:@selector(buttonClick:) conrroller:self]];

    [self createButtonWithY:100 title:@"1.1 一行输入框  手机号" tag:0];
    [self createButtonWithY:160 title:@"1.2 两行输入框  测试的" tag:1];
    [self createButtonWithY:220 title:@"1.3 三行输入框  支付宝" tag:2];
        [self createButtonWithY:280 title:@"主题变化" tag:3];

}

/** 创建button的方法 */
- (void)createButtonWithY:(CGFloat)y title:(NSString *)title tag:(NSInteger)tag{
    _button = [UIButton buttonWithType:UIButtonTypeSystem];
    _button.frame = CGRectMake(50, y, mScreenWidth - 100, 60);
    [_button setTitle:title forState:UIControlStateNormal];
    [_button addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
    _button.tag = tag;
    [self.view addSubview:_button];
}

#pragma mark 点击事件

/** button的点击事件 */
- (void)buttonClick:(UIButton *)button {
    switch (button.tag) {
        case 0: // 一行输入框
            [self showInputViewType1];
            break;
        case 1: // 两行输入框
            [self showInputViewType2];
            break;
        case 2: // 三行输入框
            [self showInputViewType3];
            break;
            case 3:
            [self click];
            break;
        default:
            break;
    }
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    if (!_textview)  self.textview = [[TextLabelView alloc] init];
}


- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    if (_textview)  [self.textview removeFromSuperview];
}

/** 一行输入框 */
- (void)showInputViewType1 {
    // 1. 设置数据
    self.textview.titleLable.text = @"修改手机号";
    [self.textview setItems:@[@"输入新的手机号"]];
    // 2. show 显示
    [self.textview show];
    // 3. 自定义键盘
    self.textview.textFiled1.keyboardType = UIKeyboardTypeNumberPad;
    // 4. 定义回调block
    __weak typeof(self) weakSelf = self;
    self.textview.okButtonClickBolck = ^(NSMutableArray *arr){
        [weakSelf.textview hide];
    };
}

/** 两行输入框 */
- (void)showInputViewType2 {
    self.textview.titleLable.text = @"修改XXX";
    [self.textview setItems:@[@"测试一下",@"第二个输入框"]];
    [self.textview show];
    
    __weak typeof(self) weakSelf = self;
    self.textview.okButtonClickBolck = ^(NSMutableArray *arr){
        [weakSelf.textview hide];
    };
}

/** 三行输入框 */
- (void)showInputViewType3 {
    self.textview.titleLable.text = @"修改支付宝账号";
    [self.textview setItems:@[@"原支付宝信息",@"新支付宝账号",@"新收款人姓名"]];
//    [self.textview setTextFieldItems:@[[NSString stringWithFormat:@"%@     %@",@"alipayAccount",@"name"]]];
    [self.textview setPlaceholderItems:@[@"",@"手机号或邮箱",@""]];
    [self.textview show];
    self.textview.textFiled2.keyboardType = UIKeyboardTypeEmailAddress;
    
    __weak typeof(self) weakSelf = self;
    self.textview.okButtonClickBolck = ^(NSMutableArray *arr){
        [weakSelf.textview hide];
    };
}
/**
 *  切换夜间模式
 */
- (void)click{
    [Model shareInstance].isNight = ![Model shareInstance].isNight;
}
#pragma mark - 夜间模式
- (void)setNightDayModel {
    
    //某类(2个以上)设置 写到NightManager中
    [NightManager setBackgroundColorWithView:self.view];
    [NightManager setButtonTitleColorWithButton:_button];
    [NightManager setButtonTitleColorWithButton:_btntest];
    [NightManager setBackgroundColorWithView:_textview];


    // 个别特殊单独设置
}

- (void)setLightDayModel {
    
    //某类(2个以上)设置 写到NightManager中
    [NightManager setBackgroundColorWithView:self.view];
    [NightManager setButtonTitleColorWithButton:_button];
    [NightManager setButtonTitleColorWithButton:_btntest];
    [NightManager setBackgroundColorWithView:_textview];



    
    // 个别特殊单独设置
}
@end
