//
//  ViewController.m
//  Utovr_use_sdk_test
//
//  Created by 姜杉 on 16/4/15.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "ViewController.h"
#import <UtoVRPlayer/UtoVRPlayer.h>
@interface ViewController ()
@property (nonatomic ,strong) UVPlayer *player;
@property (nonatomic ,strong) UIView   *colorview;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    _colorview = [[UIView alloc]initWithFrame:self.view.bounds];
    _colorview.backgroundColor = [UIColor orangeColor];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(simpleclick:)];
    tap.numberOfTapsRequired = 2;
    UITapGestureRecognizer *tap2 = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(simpleclick2:)];
    tap2.numberOfTapsRequired = 2;
    
    UIButton *btn = [[UIButton alloc]init];
    btn.frame = CGRectMake(self.view.bounds.size.width-50, 20,50 , 30);
    btn.backgroundColor = [UIColor whiteColor];
    [btn addTarget:self action:@selector(click) forControlEvents:UIControlEventTouchUpInside];
    self.player = [[UVPlayer alloc] initWithConfiguration:nil];
    [self.view addSubview:self.colorview];
    [self.view addSubview:self.player.playerView];
    self.player.playerView.frame = CGRectMake(200, 0, self.view.bounds.size.width-200, 200);
            self.player.viewStyle = UVPlayerViewStyleNone;
//    UVPlayerItem *local4k = [[UVPlayerItem alloc] initWithPath:[[NSBundle mainBundle] pathForResource:@"wu" ofType:@"mp4"] type:UVPlayerItemTypeLocalVideo];
//    [self.player appendItem:local4k];
    NSMutableArray *items = [[NSMutableArray alloc]init];
//    UVPlayerItem *playitem = [[UVPlayerItem alloc]initWithPath:@"http://cache.utovr.com/201508270529022474.mp4" type:UVPlayerItemTypeOnline];
    UVPlayerItem *playitem = [[UVPlayerItem alloc]initWithPath:@"http://10962.hlsplay.aodianyun.com/eggvr/stream.m3u8" type:UVPlayerItemTypeOnline];
    [items addObject:playitem];
    [self.player appendItems:items];
    
    
    [self.player.playerView addSubview:btn];
    [_colorview addGestureRecognizer:tap2];
    [self.player.playerView addGestureRecognizer:tap];
//    UVPlayerItem *local4k = [[UVPlayerItem alloc] initWithPath:[[NSBundle mainBundle] pathForResource:@"wu" ofType:@"mp4"] type:UVPlayerItemTypeLocalVideo];
//    [self.player appendItem:local4k];
    /**
     *  退出播放器时释放该资源
     */
//    [self.player prepareToRelease];
    // Do any additional setup after loading the view, typically from a nib.
}
//-(void)viewDidLayoutSubviews {
//    [super viewDidLayoutSubviews];
//    //调整frame。你可以使用任何其它布局方式保证播放视图是你期望的大小
//    CGRect frame;
//    if (self.traitCollection.verticalSizeClass == UIUserInterfaceSizeClassCompact) {
////                   self.player.viewStyle = UVPlayerViewStyleDefault;
//        self.player.viewStyle = UVPlayerViewStyleNone;
//
//
////        frame = CGRectMake(0, 20, self.view.bounds.size.width, self.view.bounds.size.height - 20);
//        frame = self.view.bounds;
//        
//        
//        
//        
//        
//    } else {
//        
//        frame = CGRectMake(200, 0, self.view.bounds.size.width-200, 200);
//           self.player.viewStyle = UVPlayerViewStyleNone;
//    }
//    self.player.playerView.frame = frame;
//    
//}

-(void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    //退出时不要忘记调用prepareToRelease
    [self.player prepareToRelease];
}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (void)click{
    
    self.player.gyroscopeEnabled = YES;
}


- (void)simpleclick:(UIGestureRecognizer *)ta{
    NSLog(@"123");
    [UIView animateWithDuration:0.3 animations:^{
//                self.player.viewStyle = UVPlayerViewStyleDefault;
        self.player.playerView.frame = self.view.bounds;
        self.colorview.frame = CGRectMake(200, 0, self.view.bounds.size.width-200, 200);
        [self.view bringSubviewToFront:self.colorview];
        

    }];

}
- (void)simpleclick2:(UIGestureRecognizer *)ta{
    NSLog(@"1234");
    [UIView animateWithDuration:0.3 animations:^{
        //                self.player.viewStyle = UVPlayerViewStyleDefault;
        self.player.playerView.frame = CGRectMake(200, 0, self.view.bounds.size.width-200, 200);
        self.colorview.frame = self.view.bounds;
                [self.view bringSubviewToFront:self.player.playerView];
    }];
    
}
@end
