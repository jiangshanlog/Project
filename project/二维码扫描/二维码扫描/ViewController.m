//
//  ViewController.m
//  二维码扫描
//
//  Created by 姜杉 on 16/3/31.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "ViewController.h"
#import "WebViewJavascriptBridge.h"
#import "CaremaViewController.h"
#import "Model.h"
@interface ViewController ()<UIWebViewDelegate>
@property WebViewJavascriptBridge* bridge;
@end

@implementation ViewController
- (void)viewWillAppear:(BOOL)animated {
    if (_bridge) {
        return;
    }
    
    UIWebView* webView = [[UIWebView alloc] initWithFrame:CGRectMake(0, 64, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height)];
    
    [self.view addSubview:webView];
    
    [WebViewJavascriptBridge enableLogging];
    _bridge = [WebViewJavascriptBridge bridgeForWebView:webView];
    
    
    
    [_bridge registerHandler:@"testObjcCallback" handler:^(id data, WVJBResponseCallback responseCallback) {

        if ([data  isEqual: @"begin"]) {
            CaremaViewController *carema = [[CaremaViewController alloc]init];
            [self presentViewController:carema animated:YES completion:^{}];
        }
        
        
    }];
    
    
    
        [self loadExamplePage:webView];
}
- (void)viewDidLoad {
    [super viewDidLoad];
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(getresult) name:@"passresult" object:nil];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)loadExamplePage:(UIWebView*)webView {
    NSString *html = @"http://192.168.2.127:86/androidviews/index.html";
    NSURL *url = [NSURL URLWithString:html];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
//    NSString* htmlPath = [[NSBundle mainBundle] pathForResource:@"index" ofType:@"html"];
//    NSString* appHtml = [NSString stringWithContentsOfFile:htmlPath encoding:NSUTF8StringEncoding error:nil];
//    NSURL *baseURL = [NSURL fileURLWithPath:htmlPath];
//    
//    [webView loadHTMLString:appHtml baseURL:baseURL];
    
    [webView loadRequest:request];
}
- (void)getresult{
    Model *model = [Model initWithnumber];
    id data = model.Result;
    [_bridge callHandler:@"testJavascriptHandler" data:data responseCallback:^(id response) {
        NSLog(@"success");
//        NSLog(@"testJavascriptHandler responded: %@", response);
    }];
}
@end
