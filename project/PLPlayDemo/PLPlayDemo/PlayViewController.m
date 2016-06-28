//
//  PlayViewController.m
//  PLPlayDemo
//
//  Created by 姜杉 on 16/5/10.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "PlayViewController.h"
#import <PLPlayerKit/PLPlayer.h>

#define enableBackgroundPlay  1
@interface PlayViewController ()<PLPlayerDelegate>
@property (nonatomic ,strong)PLPlayer *player;
@property (nonatomic ,strong)PLPlayerOption *option;
@property (nonatomic, weak) UIActivityIndicatorView *activityIndicatorView;
@property (nonatomic, assign) int reconnectCount;
@end

@implementation PlayViewController
- (void)viewWillAppear:(BOOL)animated{
[super viewWillAppear:animated];
    self.view.backgroundColor = [UIColor whiteColor];
    
}
- (void)viewDidLoad {
    [super viewDidLoad];
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:nil];
    _option = [PLPlayerOption defaultOption];
    [_option setOptionValue:@10 forKey:PLPlayerOptionKeyTimeoutIntervalForMediaPackets];
    _player = [PLPlayer playerWithURL:[NSURL URLWithString:@"rtmp://live.hkstv.hk.lxdns.com/live/hks"] option:_option];
    _player.playerView.contentMode = UIViewContentModeScaleAspectFit;
    _player.playerView.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleWidth;

    _player.delegate = self;
    self.player.delegateQueue = dispatch_get_main_queue();
    self.player.backgroundPlayEnable = enableBackgroundPlay;
#if !enableBackgroundPlay
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(startPlayer) name:UIApplicationWillEnterForegroundNotification object:nil];
        [self setupUI];
#endif
    [self setupUI];
    
    [self startPlayer];


    
}
- (void)setupUI{
    [self.view addSubview:_player.playerView];
}
- (void)startPlayer{
    [self addActivityIndicatorView];
    [UIApplication sharedApplication].idleTimerDisabled = YES;
    [self.player play];
}
- (void)addActivityIndicatorView {
    if (self.activityIndicatorView) {
        return;
    }
    UIActivityIndicatorView *activityIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    activityIndicatorView.center = CGPointMake(CGRectGetMidX(self.view.bounds), CGRectGetMidY(self.view.bounds));
    [self.view addSubview:activityIndicatorView];
    [activityIndicatorView stopAnimating];
    
    self.activityIndicatorView = activityIndicatorView;
}


#pragma mark - <PLPlayerDelegate>

- (void)player:(nonnull PLPlayer *)player statusDidChange:(PLPlayerStatus)state {
    if (PLPlayerStatusCaching == state) {
        [self.activityIndicatorView startAnimating];
    } else {
        [self.activityIndicatorView stopAnimating];
    }
}

- (void)player:(nonnull PLPlayer *)player stoppedWithError:(nullable NSError *)error {
    [self.activityIndicatorView stopAnimating];
    [self tryReconnect:error];
}

- (void)tryReconnect:(nullable NSError *)error {
    if (_reconnectCount < 3) {
        _reconnectCount ++;
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * pow(2, self.reconnectCount) * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self.player play];
        });
        
    }

}


@end
