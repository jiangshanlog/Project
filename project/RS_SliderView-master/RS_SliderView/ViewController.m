//
//  ViewController.m
//  RS_SliderView
//
//  Created by Roman Simenok on 16.02.15.
//  Copyright (c) 2015 Roman Simenok. All rights reserved.
//

#import "ViewController.h"
#import "RS_SliderView.h"

@interface ViewController () <RSliderViewDelegate>

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    RS_SliderView *horSlider = [[RS_SliderView alloc] initWithFrame:CGRectMake(20, 40, 280, 70) andOrientation:Horizontal];
    horSlider.delegate = self;
    [horSlider setColorsForBackground:[UIColor colorWithRed:27.0/255.0 green:28.0/255.0 blue:37.0/255.0 alpha:1.0]
                           foreground:[UIColor colorWithRed:0.0 green:106.0/255.0 blue:95.0/255.0 alpha:1.0]
                               handle:[UIColor colorWithRed:0.0 green:205.0/255.0 blue:184.0/255.0 alpha:1.0]
                               border:[UIColor colorWithRed:0.0 green:205.0/255.0 blue:184.0/255.0 alpha:1.0]];
    horSlider.label.text = @"Horizontal slider";
    // default font is Helvetica, size 24, so set font only if you need to change it.
    horSlider.label.font = [UIFont fontWithName:@"Helvetica" size:25];
    horSlider.label.textColor = [UIColor colorWithRed:0.0 green:205.0/255.0 blue:184.0/255.0 alpha:1.0];
    [self.view addSubview:horSlider];
    
    RS_SliderView *vertSlider = [[RS_SliderView alloc] initWithFrame:CGRectMake(150, 200, 80, 300) andOrientation:Vertical];
    vertSlider.delegate = self;
    [vertSlider setColorsForBackground:[UIColor colorWithRed:37.0/255.0 green:46.0/255.0 blue:38.0/255.0 alpha:1.0]
                            foreground:[UIColor colorWithRed:32.0/255.0 green:86.0/255.0 blue:0.0 alpha:1.0]
                                handle:[UIColor colorWithRed:128.0/255.0 green:209.0/255.0 blue:79.0/255.0 alpha:1.0]
                                border:[UIColor colorWithRed:128.0/255.0 green:209.0/255.0 blue:79.0/255.0 alpha:1.0]];
    // If you don't need handle
//    [vertSlider hideHandle];
    // if you don't like round corners or border
    [vertSlider removeRoundCorners:YES removeBorder:YES];
    // Coment code below if you donn't need text on you slider
    vertSlider.label.text = @"Vertical slider";
    vertSlider.label.font = [UIFont fontWithName:@"Helvetica" size:25];
    vertSlider.label.textColor = [UIColor colorWithRed:128.0/255.0 green:209.0/255.0 blue:79.0/255.0 alpha:1.0];
    [self.view addSubview:vertSlider];
}

-(void)sliderValueChanged:(RS_SliderView *)sender {
    NSLog(@"Value Changed: %f", sender.value);
}

-(void)sliderValueChangeEnded:(RS_SliderView *)sender {
    NSLog(@"Touсh ended: %f", sender.value);
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
// 版权属于原作者
// http://code4app.com (cn) http://code4app.net (en)
// 发布代码于最专业的源码分享网站: Code4App.com