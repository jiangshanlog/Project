//
//  ViewController.m
//  直播结束view
//
//  Created by 姜杉 on 16/3/18.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "ViewController.h"
#import "PopUpEndView.h"
@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    PopUpEndView *view = [[PopUpEndView alloc]initWithFrame:[UIScreen mainScreen].bounds];
    [self.view addSubview:view];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
