//
//  ViewController.m
//  BAddressPickerDemo
//
//  Created by 林洁 on 16/1/13.
//  Copyright © 2016年 onlylin. All rights reserved.
//

#import "ViewController.h"
#import "AddressPickerDemo.h"


@interface ViewController ()

@end

@implementation ViewController

#pragma mark - Life Cycle
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



- (IBAction)addressClickAction:(id)sender {
    AddressPickerDemo *addressPickerDemo = [[AddressPickerDemo alloc] init];
    UINavigationController *navigation = [[UINavigationController alloc] initWithRootViewController:addressPickerDemo];
    [self presentViewController:navigation animated:YES completion:nil];
    
}
@end
// 版权属于原作者
// http://code4app.com (cn) http://code4app.net (en)
// 发布代码于最专业的源码分享网站: Code4App.com