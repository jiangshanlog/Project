//
//  ViewController.m
//  MD360PlayerDemo_test
//
//  Created by 姜杉 on 16/5/26.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "ViewController.h"
#import "VIMVideoPlayerView.h"
#import "VIMVideoPlayer.h"
#import "MDVRLibrary.h"
#import <PLPlayerKit/PLPlayer.h>

@interface ViewController ()<VIMVideoPlayerViewDelegate,PLPlayerDelegate>
@property (nonatomic, strong) VIMVideoPlayerView *videoPlayerView;
@property (nonatomic, strong) MDVRLibrary* vrLibrary;
@property (nonatomic, strong) NSURL* mURL;

@property (nonatomic ,strong) UIButton *mInteractiveBtn;
@property (nonatomic ,strong) UIButton *mDisplayBtn;
@property (nonatomic ,strong) UIButton *Close;

@property (nonatomic ,strong)PLPlayer *player;
@property (nonatomic ,strong)PLPlayerOption *option;
@property (nonatomic, assign) int reconnectCount;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
//        [self initPlayer];
    [self setupPLPlayer];
    [self.view addSubview:_player.playerView]; //视频
    [self.view addSubview:self.mInteractiveBtn];
    [self.view addSubview:self.mDisplayBtn];
    [self.view addSubview:self.Close];

    // Do any additional setup after loading the view, typically from a nib.
}

/**
 *  创建视频直播
 */
- (void)setupPLPlayer{
    //给AVAudio加category，判断是否音频播放及后台播放
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
    //直播参数设置
    _option = [PLPlayerOption defaultOption];
    //读取视频超流时间
    [_option setOptionValue:@10 forKey:PLPlayerOptionKeyTimeoutIntervalForMediaPackets];
    //播放地址
    NSString* play = @"http://d8d913s460fub.cloudfront.net/krpanocloud/video/airpano/video-1920x960a.mp4";
    /**
     *  This is test url
     */
    _player = [PLPlayer playerWithURL:[NSURL URLWithString:play] option:_option];
    //设置回调的delegate
    _player.delegate = self;
    self.player.delegateQueue = dispatch_get_main_queue();
    //是否开启后台音频播放
    self.player.backgroundPlayEnable = 1;
    //视频尺寸
    _player.playerView.frame = self.view.bounds;
    _player.playerView.backgroundColor = [UIColor clearColor];
    /////////////////////////////////////////////////////// MDVRLibrary
    MDVRConfiguration* config = [MDVRLibrary createConfig];
    AVPlayerItem *playerItem = [[AVPlayerItem alloc] initWithURL:[NSURL URLWithString:@"http://d8d913s460fub.cloudfront.net/krpanocloud/video/airpano/video-1920x960a.mp4"]];
    [config displayMode:MDModeDisplayGlass];
    [config interactiveMode:MDModeInteractiveMotion];
    [config asVideo:playerItem];
    [config setContainer:self view:self.view];
    [config pinchEnabled:true];
    
    self.vrLibrary = [config build];
    /////////////////////////////////////////////////////// MDVRLibrary
    
    //播放视频
    [self startPlayer];
}
/**
 *  视频开始
 */
- (void)startPlayer{
    [UIApplication sharedApplication].idleTimerDisabled = YES;
    [self.player play];
}
#pragma mark - <PLPlayerDelegate>

- (void)player:(nonnull PLPlayer *)player statusDidChange:(PLPlayerStatus)state {
    switch (state) {
            //正在播放的状态
        case PLPlayerStatusPlaying:{
            NSLog(@"PLPlayerStatusPlaying");
        }
            break;
        case PLPlayerStatusReady:{
            NSLog(@"PLPlayerStatusReady");
        }
            break;
            //正在准备中
        case PLPlayerStatusPreparing:{
            NSLog(@"PLPlayerStatusPreparing");
        }
            break;
            //正在缓存的状态
        case PLPlayerStatusCaching:{
            NSLog(@"PLPlayerStatusCaching");
        }
            break;
            //播放暂停的状态
        case PLPlayerStatusPaused:{
            NSLog(@"PLPlayerStatusPaused");
        }
            break;
            //播放结束或手动停止的状态
        case PLPlayerStatusStopped:{
            
        }
            break;
        default:
        {
        }
            break;
    }
    
}

- (void)player:(nonnull PLPlayer *)player stoppedWithError:(nullable NSError *)error {
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




- (void) initPlayer{
    // video player
    self.videoPlayerView = [[VIMVideoPlayerView alloc] initWithFrame:CGRectMake(0, 0, [[UIScreen mainScreen] bounds].size.width, [[UIScreen mainScreen] bounds].size.height)];
    self.videoPlayerView.translatesAutoresizingMaskIntoConstraints = NO;
    self.videoPlayerView.delegate = self;

    [self.videoPlayerView setVideoFillMode:AVLayerVideoGravityResizeAspect];
    [self.videoPlayerView.player enableTimeUpdates];
    [self.videoPlayerView.player enableAirplay];
    
    AVPlayerItem *playerItem = [[AVPlayerItem alloc] initWithURL:self.mURL];
    [self.videoPlayerView.player setPlayerItem:playerItem];
    [self.videoPlayerView.player play];
    
    
    /////////////////////////////////////////////////////// MDVRLibrary
    MDVRConfiguration* config = [MDVRLibrary createConfig];
    
    [config displayMode:MDModeDisplayGlass];
    [config interactiveMode:MDModeInteractiveMotion];
    [config asVideo:playerItem];
    [config setContainer:self view:self.view];
    [config pinchEnabled:true];
    
    self.vrLibrary = [config build];
    /////////////////////////////////////////////////////// MDVRLibrary
    
}
- (NSURL *)mURL{
    if (!_mURL) {
        _mURL = [NSURL URLWithString:@"http://d8d913s460fub.cloudfront.net/krpanocloud/video/airpano/video-1920x960a.mp4"];
    }
    return _mURL;
}
- (IBAction)onCloseBtnClicked:(id)sender {
    [self.videoPlayerView.player reset];
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
}
//NORMAL or GLASS
- (IBAction)onDisplayModeBtnClicked:(id)sender {
    [self.vrLibrary switchDisplayMode];
    [self syncDisplayLabel];
    
}
//MOTION
- (IBAction)onInteractiveModeBtnClicked:(id)sender {
    [self.vrLibrary switchInteractiveMode];
    [self syncInteractiveLabel];
}

-(void)syncDisplayLabel{
    int mode = [self.vrLibrary getDisplayMdoe];
    NSString* label;
    if (mode == MDModeDisplayNormal) {
        label = @"NORMAL";
    } else {
        label = @"GLASS";
    }
    [self.mDisplayBtn setTitle:label forState:UIControlStateNormal];
}


-(void)syncInteractiveLabel{
    int mode = [self.vrLibrary getInteractiveMdoe];
    NSString* label;
    if (mode == MDModeInteractiveTouch) {
        label = @"TOUCH";
    } else {
        label = @"MOTION";
    }
    [self.mInteractiveBtn setTitle:label forState:UIControlStateNormal];
}
- (UIButton *)mInteractiveBtn{
    if (!_mInteractiveBtn) {
        _mInteractiveBtn = [[UIButton alloc]initWithFrame:CGRectMake(20, 20, 100, 50)];
        _mInteractiveBtn.backgroundColor = [UIColor blackColor];
        [_mInteractiveBtn setTitle:@"TOUCH" forState:UIControlStateNormal];
        [_mInteractiveBtn addTarget:self action:@selector(onInteractiveModeBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _mInteractiveBtn;
}
- (UIButton *)mDisplayBtn{
    if (!_mDisplayBtn) {
        _mDisplayBtn = [[UIButton alloc]initWithFrame:CGRectMake(20, 100, 100, 50)];
        _mDisplayBtn.backgroundColor = [UIColor blackColor];
        [_mDisplayBtn setTitle:@"NORMAL" forState:UIControlStateNormal];
        [_mDisplayBtn addTarget:self action:@selector(onDisplayModeBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _mDisplayBtn;
}
- (UIButton *)Close{
    if (!_Close) {
        _Close = [[UIButton alloc]initWithFrame:CGRectMake(20, 200, 100, 50)];
        _Close.backgroundColor = [UIColor blackColor];
        [_Close setTitle:@"CLOSE" forState:UIControlStateNormal];
        [_Close addTarget:self action:@selector(onCloseBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _Close;
}
@end
