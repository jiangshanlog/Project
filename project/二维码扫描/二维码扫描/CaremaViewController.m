//
//  CaremaViewController.m
//  二维码扫描
//
//  Created by 姜杉 on 16/3/31.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "CaremaViewController.h"
#import <AVFoundation/AVFoundation.h>
#import "Model.h"
static const char *kScanQRCodeQueueName = "ScanQRCodeQueue";
@interface CaremaViewController ()<AVCaptureMetadataOutputObjectsDelegate>
@property (nonatomic)UIView *sanFrameView;
@property (nonatomic) AVCaptureSession *captureSession;
@property (nonatomic) AVCaptureVideoPreviewLayer *videoPreviewLayer;
@property (nonatomic) BOOL lastResut;
@end

@implementation CaremaViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _sanFrameView = [[UIView alloc]initWithFrame:[UIScreen mainScreen].bounds];
    [self.view addSubview:_sanFrameView];
        _lastResut = YES;
    
    
    [self startReading];
}

-(void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects
      fromConnection:(AVCaptureConnection *)connection
{
    if (metadataObjects != nil && [metadataObjects count] > 0) {
        AVMetadataMachineReadableCodeObject *metadataObj = [metadataObjects objectAtIndex:0];
        NSString *result;
        if ([[metadataObj type] isEqualToString:AVMetadataObjectTypeQRCode]) {
            result = metadataObj.stringValue;
        } else {
            NSLog(@"不是二维码");
        }
        [self performSelectorOnMainThread:@selector(reportScanResult:) withObject:result waitUntilDone:NO];
    }
}
- (void)reportScanResult:(NSString *)result
{
    [self stopReading];
    if (!_lastResut) {
        return;
    }
    _lastResut = NO;
//    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"二维码扫描"
//                                                    message:result
//                                                   delegate:nil
//                                          cancelButtonTitle:@"取消"
//                                          otherButtonTitles: nil];
//    [alert show];
    Model *model = [Model initWithnumber];
    model.Result = result;
    [[NSNotificationCenter defaultCenter]postNotificationName:@"passresult" object:nil];
    [self dismissViewControllerAnimated:YES completion:^{}];
    
    // 以及处理了结果，下次扫描
    _lastResut = YES;
}
- (void)stopReading
{
    // 停止会话
    [_captureSession stopRunning];
    _captureSession = nil;
}



- (BOOL)startReading
{
    // 获取 AVCaptureDevice 实例
    NSError * error;
    AVCaptureDevice *captureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    // 初始化输入流
    AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];
    if (!input) {
        NSLog(@"%@", [error localizedDescription]);
        return NO;
    }
    // 创建会话
    _captureSession = [[AVCaptureSession alloc] init];
    // 添加输入流
    [_captureSession addInput:input];
    // 初始化输出流
    AVCaptureMetadataOutput *captureMetadataOutput = [[AVCaptureMetadataOutput alloc] init];
    // 添加输出流
    [_captureSession addOutput:captureMetadataOutput];
    
    // 创建dispatch queue.
    dispatch_queue_t dispatchQueue;
    dispatchQueue = dispatch_queue_create(kScanQRCodeQueueName, NULL);
    [captureMetadataOutput setMetadataObjectsDelegate:self queue:dispatchQueue];
    // 设置元数据类型 AVMetadataObjectTypeQRCode
    [captureMetadataOutput setMetadataObjectTypes:[NSArray arrayWithObject:AVMetadataObjectTypeQRCode]];
    
    // 创建输出对象
    _videoPreviewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:_captureSession];
    [_videoPreviewLayer setVideoGravity:AVLayerVideoGravityResizeAspectFill];
    [_videoPreviewLayer setFrame:_sanFrameView.layer.bounds];
    [_sanFrameView.layer addSublayer:_videoPreviewLayer];
    // 开始会话
    [_captureSession startRunning];
    
    return YES;
}

@end
