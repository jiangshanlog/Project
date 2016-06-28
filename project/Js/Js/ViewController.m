//
//  ViewController.m
//  Js
//
//  Created by 姜杉 on 16/3/28.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "ViewController.h"
#import <JavaScriptCore/JavaScriptCore.h>


@interface ViewController ()<UIWebViewDelegate>{
UIWebView *myWebView;
}



@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
//     Do any additional setup after loading the view, typically from a nib.
//    JSContext *context = [[JSContext alloc] init];
//    JSValue *jsVal = [context evaluateScript:@"21+7"];
//    int iVal = [jsVal toInt32];
//    
//    
//    
//    NSLog(@"JSValue: %@, int: %d", jsVal, iVal);
    

    

    
//
//    JSContext *context = [[JSContext alloc] init];
//    [context evaluateScript:@"var arr = [21, 7 , 'iderzheng.com'];"];
//    JSValue *jsArr = context[@"arr"]; // Get array from JSContext
//    
//    NSLog(@"JS Array: %@; Length: %@", jsArr, jsArr[@"length"]);
//    jsArr[1] = @"blog"; // Use JSValue as array
//    jsArr[7] = @7;
//    
//    NSLog(@"JS Array: %@; Length: %d", jsArr, [jsArr[@"length"] toInt32]);
//    
//    NSArray *nsArr = [jsArr toArray];
//    NSLog(@"NSArray: %@", nsArr);
    
    
    //初始化webview
    myWebView=[[UIWebView alloc]initWithFrame:CGRectMake(0, 22, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height-22)];
    myWebView.delegate=self;
    //添加webview到当前viewcontroller的view上
    [self.view addSubview:myWebView];
    
    //网址
    NSString *httpStr=@"http://www.baidu.com";
    NSURL *httpUrl=[NSURL URLWithString:httpStr];
    NSURLRequest *httpRequest=[NSURLRequest requestWithURL:httpUrl];
    [myWebView loadRequest:httpRequest];
    
}

-(void)webViewDidFinishLoad:(UIWebView *)webView
{
    //网页加载完成调用此方法
    
    //首先创建JSContext 对象（此处通过当前webView的键获取到jscontext）
    JSContext *context=[webView valueForKeyPath:@"documentView.webView.mainFrame.javaScriptContext"];
    NSString *alertJS=@"alert('test js OC')"; //准备执行的js代码
    [context evaluateScript:alertJS];//通过oc方法调用js的alert
    
}
@end
