//
//  ViewController.m
//  PLPlayDemo
//
//  Created by 姜杉 on 16/5/10.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "ViewController.h"
#import "PlayViewController.h"
@interface ViewController ()

@end

@implementation ViewController
- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    self.title = @"First";
}
- (void)viewDidLoad {
    [super viewDidLoad];
    UIButton *btn = [UIButton buttonWithType:UIButtonTypeSystem];
    btn.frame = CGRectMake(200, 335, 100, 40);
    btn.backgroundColor = [UIColor blackColor];
    [btn setTitle:@"Play" forState:UIControlStateNormal];
    [btn addTarget:self action:@selector(btnclick) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:btn];
}
- (void)btnclick{
    PlayViewController *play = [[PlayViewController alloc]init];
    [self.navigationController pushViewController:play animated:YES];
}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
