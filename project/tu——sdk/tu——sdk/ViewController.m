//
//  ViewController.m
//  tu——sdk
//
//  Created by 姜杉 on 16/3/22.
//  Copyright © 2016年 姜杉. All rights reserved.
//

#import "ViewController.h"
#import <TuSDKGeeV1/TuSDKGeeV1.h>

@interface ViewController (){
    // *****  这里很重要  *****
    // 照片美化组件
    TuSDKCPPhotoEditMultipleComponent *_photoEditMultipleComponent;
}

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // 启动GPS
    [[TuSDKTKLocation shared] requireAuthorWithController:self];
    // Do any additional setup after loading the view, typically from a nib.
    UIButton *btn = [[UIButton alloc]initWithFrame:CGRectMake(200, 100, 100, 100)];
    [btn setBackgroundColor:[UIColor blackColor]];
    [btn setTitle:@"start" forState:UIControlStateNormal];
    
    [btn addTarget:self action:@selector(click) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:btn];
    
}
- (void)click{
    _photoEditMultipleComponent =
    [TuSDKGeeV1 photoEditMultipleWithController:self
                                  callbackBlock:^(TuSDKResult *result, NSError *error, UIViewController *controller)
     {
         // 获取图片失败
         if (error) {
             lsqLError(@"editMultiple error: %@", error.userInfo);
             return;
         }
         [result logInfo];
         //
         // ~~~~ 设置图片 ~~~~  这里很重要，一定要选择一种图片传输方式，将图片传给组件
         //
         // UIImage 类型，图片
         _photoEditMultipleComponent.inputImage = result.image;
         // NSString 类型，文件保存路径
         _photoEditMultipleComponent.inputTempFilePath = result.imagePath;
         // TuSDKTSAssetInterface 类型，相册地址对象
         _photoEditMultipleComponent.inputAsset = result.imageAsset;
         
         // 是否在组件执行完成后自动关闭组件 (默认:NO)
         _photoEditMultipleComponent.autoDismissWhenCompelted = YES;
         // 编辑动作完成传出参数时，如果使用 UImage 对象，请注意组件关闭，控制器被销毁 UIImage 会被释放，持有方法参考上文。
         
         [_photoEditMultipleComponent showComponent];
         [_photoEditMultipleComponent.options.editMultipleOptions disableModule:lsqTuSDKCPEditActionCuter];
         
         [_photoEditMultipleComponent.options.editMultipleOptions disableModule:lsqTuSDKCPEditActionFilter];
         
         [_photoEditMultipleComponent.options.editMultipleOptions disableModule:lsqTuSDKCPEditActionSkin];
         _photoEditMultipleComponent.options.editMultipleOptions.saveToAlbum = YES;
         _photoEditMultipleComponent.options.editMultipleOptions.saveToAlbumName = @"TuSDK";
             _photoEditMultipleComponent.options.editMultipleOptions.saveToTemp = YES;
          _photoEditMultipleComponent.options.editMultipleOptions.isAutoRemoveTemp = YES;
         _photoEditMultipleComponent.options.editMultipleOptions.disableStepsSave = YES;
          _photoEditMultipleComponent.options.editMultipleOptions.showResultPreview = NO;
         _photoEditMultipleComponent.options.editMultipleOptions.limitForScreen = YES;
             _photoEditMultipleComponent.options.editMultipleOptions.limitSideSize = 800;
         _photoEditMultipleComponent.options.editMultipleOptions.componentClazz = [TuSDKPFEditMultipleController class];
          _photoEditMultipleComponent.options.editMultipleOptions.viewClazz = [TuSDKPFEditMultipleView class];
           _photoEditMultipleComponent.options.editFilterOptions.filterGroup = @[@"SkinNature", @"SkinPink", @"SkinJelly", @"SkinNoir", @"SkinRuddy", @"SkinPowder", @"SkinSugar"];
           _photoEditMultipleComponent.options.editFilterOptions.outputCompress = 0.95f;
         _photoEditMultipleComponent.options.editFilterOptions.filterBarCellWidth = 75;
   _photoEditMultipleComponent.options.editFilterOptions.filterBarHeight = 100;
         _photoEditMultipleComponent.options.editFilterOptions.displayFilterSubtitles = YES;
         _photoEditMultipleComponent.options.editFilterOptions.isRenderFilterThumb = YES;
         _photoEditMultipleComponent.options.editCuterOptions.enableTrun = YES;
         _photoEditMultipleComponent.options.editCuterOptions.enableMirror = YES;
          _photoEditMultipleComponent.options.editCuterOptions.ratioType = lsqRatioAll;
         _photoEditMultipleComponent.options.editCuterOptions.ratioTypeList = @[@(lsqRatioOrgin), @(lsqRatio_1_1), @(lsqRatio_2_3)];
             _photoEditMultipleComponent.options.editSmudgeOptions.defaultBrushSize = lsqMediumBrush;
           _photoEditMultipleComponent.options.editSmudgeOptions.saveLastBrush = YES;
             _photoEditMultipleComponent.options.editSmudgeOptions.maxUndoCount = 5;
         
         
         //
         // 可在此添加自定义方法，在编辑完成时进行页面跳转操，例如 ：
         // [controller presentViewController:[[UIViewController alloc] init] animated:YES completion:nil];
         
         // 图片处理结果 TuSDKResult *result 具有三种属性，分别是 ：
         // result.image 是 UIImage 类型
         // result.imagePath 是 NSString 类型
         // result.imageAsset 是 TuSDKTSAssetInterface 类型
         
         // 下面以 result.image 举例如何将图片编辑结果持有并进行其他操作
         // 可在此添加自定义方法，将 result 结果传出，例如 ：  [self openEditorWithImage:result.image];
         // 并在外部使用方法接收 result 结果，例如 ： -(void)openEditorWithImage:(UIImage *)image;
         // 用户也可以在 result 结果的外部接受的方法中实现页面的跳转操作，用户可根据自身需求使用。
         
         // 用户在获取到 result.image 结果并跳转到其他页面进行操作的时候可能会出现无法持有对象的情况
         // 此时用户可以将 result.image 对象转换成 NSData 类型的对象，然后再进行操作，例如 ：
         // NSData *imageData = UIImageJPEGRepresentation(result.image, 1.0);
         // ViewController *viewController = [[ViewController alloc]init];
         // [self.controller pushViewController:viewController animated:YES];
         // viewController.currentImage = [UIImage imageWithData:imageData];
         
         // 获取 result 对象的不同属性，需要对 option 选项中的保存到相册和保存到临时文件相关项进行设置。
         // 
         
     }];
    
}


@end
