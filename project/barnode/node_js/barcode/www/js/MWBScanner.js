/*
    Version 1.8.8
   
    - Added support for android API 23 app permissions
    - Bugfixes
    
    Version 1.8.7
   
    - Added option to set scanning rectangle for partial view scanning. To use it just add the following line to the scanner configuration:

        mwbs['MWBuseAutoRect'](false);

    - Added new feature that makes possible scanning in view:

        scanner.startScanning(x, y, width, height); 
        //all parameters represent percentages relative to the screen size
        
    - Other methods for partial screen scanning control:

        scanner.togglePauseResume() - toggle pause resume scanning
        scanner.closeScanner()       - stop and remove scanner view
        scanner.toggleFlash()       - toggle flash on/off
        scanner.toggleZoom()        - toggle zoom in/out


    Version 1.7

    - Added scanImage(URI) which can be used for image scanning. Optionally, the method can be used with custom init and callback - scanImage(MWBSInitSpace.init,MWBSInitSpace.callback,URI);

        URI                     - the path to the image
        MWBSInitSpace.init      - scanner initialisation
        MWBSInitSpace.callback  - result callback


    Version 1.6 

    - Added continuous scanning functionality:

    - Added support for 64bit android devices.
    - Camera overlay bug fixes.
    Decoder updates:
        - DM non-centric scanning upgrade (still with fixed number of testing locations)
        - GS1 updates: 
        - Code 128 FNC1 not included in result, only GS1 compliance is set
        - DM - proper handling of FNC1 and ECI support added
        - Fixed non-symetric viewfinder (for all orientations) in ios/android native and phonegap
        - Fixed PDF trialing characters on all platforms for specific samples


    Version 1.5

    - Added multi-threading support. By default decoder will use all available CPU cores on device. To limit the number 
    of used threads, use new function:  MWBsetMaxThreads: function (maxThreads)
    
    Version 1.4.1

    - Structure of plugin reorganized to fit plugman specifications
    
    Version 1.4

    - Added support for custom init and callback functions. All init and callback function can still be declared here, 
    but users can now use an outside Javascript file that they can maintain during updates, so that they don't lose 
    their changes when they update.
    To use the custom script, they only need to include it in the index file like so:
     <script type="text/javascript" src="js/MWBConfig.js"></script>
    To call the scanner with the custom init and callback you use scanner.startScanning(MWBSInitSpace.init,MWBSInitSpace.callback);

    - Added MWBsetCustomParam: function - allows user to put some custom key/value pair which can be used later from native code
                                                                                                
    - Added ITF-14 support
                                                                                                
    - Added Code 11 support
                                                                                                
    - Added MSI Plessey support
                                                                                                
    - Added GS1 support
                                                                                                
    
    Version 1.3
    Copyright (c) 2014 Manatee Works. All rights reserved.
    
    Changes in 1.3:
    
    - Zoom feature added for iOS and Android. It's not supported on WP8 due to API limitation.
    
    - Added function to turn Flash ON by default
    
    - Fixed 'frameworks was not added to the references' on WP8
    
    - Fixed freezing if missing org.apache.cordova.device plugin
    
    - Added x86 lib for Android
    
    
    
    Changes in 1.2:
    
    - Registering calls moved from native code to MWBScanner.js
    
    You can now enter your licensing info without changing the native code of plugin;
    
    - Import package_name.R manually after adding Android plugin is not necessary anymore
    
    - Decoding library updated to 2.9.31
    
    
    
    Changes in 1.1:
    
    - Advanced Overlay (MWBsetOverlayMode: function(overlayMode)
    
    You can now choose between Simple Image Overlay and MW Dynamic Overlay, which shows the actual
    viewfinder, depending on selected barcode types and their respective scanning rectangles;
    
    - Orientation parameter (MWBsetInterfaceOrientation: function(interfaceOrientation))
    
    Now there's only a single function for supplying orientation parameters which makes tweaking the
    controller for changing scanner orientation no longer needed;
    
    - Enable or disable high resolution scanning (MWBenableHiRes: function(enableHiRes))
    
    Added option to choose between high or normal resolution scanning to better match user
    application requirements;
    
    - Flash handling (MWBenableFlash: function(enableFlash))
    
    Added option to enable or disable the flash toggle button;
    
    
    */

 var CONSTANTS = {
      /**
        * @name Basic return values for API functions
        * @{
        */
     MWB_RT_OK :                     0,
     MWB_RT_FAIL :                  -1,
     MWB_RT_NOT_SUPPORTED :         -2,
     MWB_RT_BAD_PARAM :               -3,
     
     
     
     /** @brief  Code39 decoder flags value: require checksum check
        */
     MWB_CFG_CODE39_REQUIRE_CHECKSUM :  0x2,
     /**/
     
     /** @brief  Code39 decoder flags value: don't require stop symbol - can lead to false results
        */
     MWB_CFG_CODE39_DONT_REQUIRE_STOP : 0x4,
     /**/
     
     /** @brief  Code39 decoder flags value: decode full ASCII
        */
     MWB_CFG_CODE39_EXTENDED_MODE :      0x8,
     /**/
     
     /** @brief  Code93 decoder flags value: decode full ASCII
        */
     MWB_CFG_CODE93_EXTENDED_MODE :      0x8,
     /**/
     
     
     /** @brief  Code25 decoder flags value: require checksum check
        */
     MWB_CFG_CODE25_REQ_CHKSUM :        0x1,
     /**/
               
     /** @brief  Code11 decoder flags value: require checksum check
      *  MWB_CFG_CODE11_REQ_SINGLE_CHKSUM is set by default
      */
     MWB_CFG_CODE11_REQ_SINGLE_CHKSUM:         0x1,
     MWB_CFG_CODE11_REQ_DOUBLE_CHKSUM:         0x2,
     /**/
                   
     /** @brief  MSI Plessey decoder flags value: require checksum check
      *  MWB_CFG_MSI_REQ_10_CHKSUM is set by default
      */
     MWB_CFG_MSI_REQ_10_CHKSUM :                 0x01,
     MWB_CFG_MSI_REQ_1010_CHKSUM :               0x02,
     MWB_CFG_MSI_REQ_11_IBM_CHKSUM :             0x04,
     MWB_CFG_MSI_REQ_11_NCR_CHKSUM :             0x08,
     MWB_CFG_MSI_REQ_1110_IBM_CHKSUM :           0x10,
     MWB_CFG_MSI_REQ_1110_NCR_CHKSUM :           0x20,
     /**/
     
     /** @brief  Codabar decoder flags value: include start/stop symbols in result
        */
     MWB_CFG_CODABAR_INCLUDE_STARTSTOP :        0x1,
     /**/


   
     /** @brief  Barcode decoder param types
     */
       MWB_PAR_ID_RESULT_PREFIX :       0x10,
       MWB_PAR_ID_ECI_MODE :            0x08,
    /**/

    /** @brief  Barcode param values
     */
        
       MWB_PAR_VALUE_ECI_DISABLED :    0x00, //default
       MWB_PAR_VALUE_ECI_ENABLED :     0x01,

       MWB_PAR_VALUE_RESULT_PREFIX_NEVER :   0x00, // default
       MWB_PAR_VALUE_RESULT_PREFIX_ALWAYS :  0x01,
       MWB_PAR_VALUE_RESULT_PREFIX_DEFAULT : 0x02,
    /**/


    /** @brief  UPC/EAN decoder disable addons detection
     */
     MWB_CFG_EANUPC_DISABLE_ADDON :  0x1,
    /**/
     
     /** @brief  Global decoder flags value: apply sharpening on input image
        */
     MWB_CFG_GLOBAL_HORIZONTAL_SHARPENING :          0x01,
     MWB_CFG_GLOBAL_VERTICAL_SHARPENING :            0x02,
     MWB_CFG_GLOBAL_SHARPENING :                     0x03,
     
     /** @brief  Global decoder flags value: apply rotation on input image
        */
     MWB_CFG_GLOBAL_ROTATE90 :                       0x04,
     
     /**
        * @name Bit mask identifiers for supported decoder types
        * @{ */
     MWB_CODE_MASK_NONE :             0x00000000,
     MWB_CODE_MASK_QR :               0x00000001,
     MWB_CODE_MASK_DM :               0x00000002,
     MWB_CODE_MASK_RSS :              0x00000004,
     MWB_CODE_MASK_39 :               0x00000008,
     MWB_CODE_MASK_EANUPC :           0x00000010,
     MWB_CODE_MASK_128 :             0x00000020,
     MWB_CODE_MASK_PDF :             0x00000040,
     MWB_CODE_MASK_AZTEC :             0x00000080,
     MWB_CODE_MASK_25 :            0x00000100,
     MWB_CODE_MASK_93 :               0x00000200,
     MWB_CODE_MASK_CODABAR :          0x00000400,
     MWB_CODE_MASK_DOTCODE :          0x00000800,
     MWB_CODE_MASK_11 :               0x00001000,
     MWB_CODE_MASK_MSI :              0x00002000,
     MWB_CODE_MASK_ALL :              0xffffffff,
     /** @} */
     
     
     /**
        * @name Bit mask identifiers for RSS decoder types
        * @{ */
     MWB_SUBC_MASK_RSS_14 :           0x00000001,
     MWB_SUBC_MASK_RSS_LIM :          0x00000004,
     MWB_SUBC_MASK_RSS_EXP :          0x00000008,
     /** @} */
     
     /**
        * @name Bit mask identifiers for Code 2 of 5 decoder types
        * @{ */
     MWB_SUBC_MASK_C25_INTERLEAVED :  0x00000001,
     MWB_SUBC_MASK_C25_STANDARD :     0x00000002,
     MWB_SUBC_MASK_C25_ITF14 :        0x00000004,
     /** @} */
     
     /**
        * @name Bit mask identifiers for UPC/EAN decoder types
        * @{ */
     MWB_SUBC_MASK_EANUPC_EAN_13 :    0x00000001,
     MWB_SUBC_MASK_EANUPC_EAN_8 :     0x00000002,
     MWB_SUBC_MASK_EANUPC_UPC_A :     0x00000004,
     MWB_SUBC_MASK_EANUPC_UPC_E :     0x00000008,
     /** @} */
     
     /**
        * @name Bit mask identifiers for 1D scanning direction
        * @{ */
     MWB_SCANDIRECTION_HORIZONTAL :   0x00000001,
     MWB_SCANDIRECTION_VERTICAL :     0x00000002,
     MWB_SCANDIRECTION_OMNI :         0x00000004,
     MWB_SCANDIRECTION_AUTODETECT :   0x00000008,
     /** @} */
     
     FOUND_NONE :       0,
     FOUND_DM :       1,
     FOUND_39 :       2,
     FOUND_RSS_14 :     3,
     FOUND_RSS_14_STACK :   4,
     FOUND_RSS_LIM :    5,
     FOUND_RSS_EXP :    6,
     FOUND_EAN_13 :     7,
     FOUND_EAN_8 :    8,
     FOUND_UPC_A :    9,
     FOUND_UPC_E :    10,
     FOUND_128 :      11,
     FOUND_PDF :      12,
     FOUND_QR :       13,
     FOUND_AZTEC :       14,
     FOUND_25_INTERLEAVED :15,
     FOUND_25_STANDARD :   16,
     FOUND_93 :       17,
     FOUND_CODABAR :    18,
     FOUND_DOTCODE :    19,
     FOUND_128_GS1 :    20,
     FOUND_ITF14 :    21,
     FOUND_11 :    22,
     FOUND_MSI :    23,
     FOUND_25_IATA :    24,
     OrientationPortrait :         'Portrait',
     OrientationLandscapeLeft :    'LandscapeLeft',
     OrientationLandscapeRight :   'LandscapeRight',
     OrientationAll :              'All',
     OverlayModeNone :    0,
     OverlayModeMW :      1,
     OverlayModeImage :   2
 };
 
 
 var BarcodeScanner = {
 
 /**
    * Init decoder with default params.
    */
 MWBinitDecoder: function(callback) {
    cordova.exec(callback, function(){}, "MWBarcodeScanner", "initDecoder", []);
 },
 
 /**
      * result.code - string representation of barcode result
      * result.type - type of barcode detected or 'Cancel' if scanning is canceled
      * result.bytes - bytes array of raw barcode result
      * result.isGS1 - (boolean) barcode is GS1 compliant
      * result.location - contains rectangle points p1,p2,p3,p4 with the corresponding x,y
      * result.imageWidth - Width of the scanned image
      * result.imageHeight - Height of the scanned image
      */
 MWBstartScanning: function(callback,x,y,width,height) {
       var args = Array.prototype.slice.call(arguments);
       if(args.length>1){
       cordova.exec(callback, function(err) {
                    callback('Error: ' + err);
                    }, "MWBarcodeScanner", "startScannerView", [x,y,width,height]);
       
       }else{
       cordova.exec(callback, function(err) {
                    callback('Error: ' + err);
                    }, "MWBarcodeScanner", "startScanner", []);
       
       }
 },
 
 /**
    * Registers licensing information with single selected decoder type.
    * If registering information is correct, enables full support for selected
    * decoder type.
    * It should be called once per decoder type.
    *
    * @param[in]   codeMask                Single decoder type selector (MWB_CODE_MASK_...)
    * @param[in]   userName                User name string
    * @param[in]   key                     License key string
    *
    * @retval      MWB_RT_OK               Registration successful
    * @retval      MWB_RT_FAIL             Registration failed
    * @retval      MWB_RT_BAD_PARAM        More than one decoder flag selected
    * @retval      MWB_RT_NOT_SUPPORTED    Selected decoder type or its registration
    *                                      is not supported
    */
 MWBregisterCode: function(codeMask, userName, key) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "registerCode", [codeMask, userName, key]);
 },
 
 /**
    * Sets active or inactive status of decoder types
    *
    * @param[in]       activeCodes             ORed bit flags (MWB_CODE_MASK_...) of decoder types
    *                                          to be activated.
    */
 MWBsetActiveCodes: function(activeCodes) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setActiveCodes", [activeCodes]);
 },
 
 /**
    * Set active subcodes for given code group flag.
    * Subcodes under some decoder type are all activated by default.
    *
    * @param[in]       codeMask                Single decoder type/group (MWB_CODE_MASK_...)
    * @param[in]       subMask                 ORed bit flags of requested decoder subtypes (MWB_SUBC_MASK_)
    */
 MWBsetActiveSubcodes: function(codeMask, activeSubcodes) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setActiveSubcodes", [codeMask, activeSubcodes]);
 },
 
 /**
    * MWBsetFlags configures options (if any) for decoder type specified in codeMask.
    * Options are given in  flags as bitwise OR of option bits. Available options depend on selected decoder type.
    *
    * @param[in]   codeMask                Single decoder type (MWB_CODE_MASK_...)
    * @param[in]   flags                   ORed bit mask of selected decoder type options (MWB_FLAG_...)
    */
 MWBsetFlags: function(codeMask, flags) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setFlags", [codeMask, flags]);
 },

 /**
    * MWBsetMinLength configures minimum result length for decoder type specified in codeMask.
    *
    * @param[in]   codeMask                Single decoder type (MWB_CODE_MASK_...)
    * @param[in]   minLength               Minimum result length for selected decoder type 
    */
 MWBsetMinLength: function(codeMask, minLength) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setMinLength", [codeMask, minLength]);
 },
 
 /**
    * This function enables some control over scanning lines choice for 1D barcodes. By ORing
    * available bit-masks user can add one or more direction options to scanning lines set.
    * @n           - MWB_SCANDIRECTION_HORIZONTAL - horizontal lines
    * @n           - MWB_SCANDIRECTION_VERTICAL - vertical lines
    * @n           - MWB_SCANDIRECTION_OMNI - omnidirectional lines
    * @n           - MWB_SCANDIRECTION_AUTODETECT - enables BarcodeScanner's
    *                autodetection of barcode direction
    *
    * @param[in]   direction               ORed bit mask of direction modes given with
    *                                      MWB_SCANDIRECTION_... bit-masks
    */
 MWBsetDirection: function(direction) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setDirection", [direction]);
 },
 
 /**
    * Sets rectangular area for barcode scanning with selected single decoder type.
    * After MWBsetScanningRect() call, all subseqent scans will be restricted
    * to this region. If rectangle is not set, whole image is scanned.
    * Also, if width or height is zero, whole image is scanned.
    *
    * Parameters are interpreted as percentage of image dimensions, i.e. ranges are
    * 0 - 100 for all parameters.
    *
    * @param[in]   codeMask            Single decoder type selector (MWB_CODE_MASK_...)
    * @param[in]   left                X coordinate of left edge (percentage)
    * @param[in]   top                 Y coordinate of top edge (percentage)
    * @param[in]   width               Rectangle witdh (x axis) (percentage)
    * @param[in]   height              Rectangle height (y axis) (percentage)
    */
 MWBsetScanningRect: function(codeMask, left, top, width, height) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setScanningRect", [codeMask, left, top, width, height]);
 },
 
 /**
    * Barcode detector relies on image processing and geometry inerpolation for
    * extracting optimal data for decoding. Higher effort level involves more processing
    * and intermediate parameter values, thus increasing probability of successful
    * detection with low quality images, but also consuming more CPU time.
    *
    * @param[in]   level                   Effort level - available values are 1, 2, 3, 4 and 5.
    *                                      Levels greater than 3 are not suitable fro real-time decoding
    */
 MWBsetLevel: function(level) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setLevel", [level]);
 },
 
 /**
    * Sets prefered User Interface orientation of scanner screen
    * Choose one fo the available values:
    * OrientationPortrait
    * OrientationLandscapeLeft
    * OrientationLandscapeRight
    *
    * Default value is OrientationLandscapeLeft
    */
 MWBsetInterfaceOrientation: function(interfaceOrientation) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setInterfaceOrientation", [interfaceOrientation]);
 },
 
 /**
    * Choose overlay graphics type for scanning screen:
    * OverlayModeNone   - No overlay is displayed
    * OverlayModeMW     - Use MW Dynamic Viewfinder with blinking line (you can customize display options
    *                     in native class by changing defaults)
    * OverlayModeImage  - Show image on top of camera preview
    *
    * Default value is OverlayModeMW
    */
 MWBsetOverlayMode: function(overlayMode) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setOverlayMode", [overlayMode]);
 },
 
 /**
    * Enable or disable high resolution scanning. It's recommended to enable it when target barcodes
    * are of high density or small footprint. If device doesn't support high resolution param will be ignored
    *
    * Default value is true (enabled)
    */
 MWBenableHiRes: function(enableHiRes) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "enableHiRes", [enableHiRes]);
 },
 
 /**
    * Enable or disable flash toggle button on scanning screen. If device doesn't support flash mode
    * button will be hidden regardles of param
    *
    * Default value is true (enabled)
    */
 MWBenableFlash: function(enableFlash) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "enableFlash", [enableFlash]);
 },
 
 /**
    * Set default state of flash (torch) when scanner activity is started
    *
    * Default value is false (disabled)
    */
 MWBturnFlashOn: function(flashOn) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "turnFlashOn", [flashOn]);
 },
/**
* Toggle on/off flash state
*
*/
MWBtoggleFlash: function() {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "toggleFlash", []);
},
 
 /**
    * Enable or disable zoom button on scanning screen. If device doesn't support zoom,
    * button will be hidden regardles of param. Zoom is not supported on Windows Phone 8
    * as there's no zooming api available!
    *
    * Default value is true (enabled)
    */
 MWBenableZoom: function(enableZoom) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "enableZoom", [enableZoom]);
 },
 
 /**
    * Set two desired zoom levels in percentage and initial level. Set first two params to zero for default
    * levels. On iOS, first zoom level is set to maximum non-interpolated level available on device, and
    * second is double of first level. On Android, default first zoom is 150% and second is 300%. Zoom is
    * not supported on Windows Phone 8 as there's no zooming api available!
    * Initial zoom level can be 0 (100% - non zoomed), 1 (zoomLevel1) or 2 (zoomLevel2). Default is 0.
    *
    */
 MWBsetZoomLevels: function(zoomLevel1, zoomLevel2, initialZoomLevel) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setZoomLevels", [zoomLevel1, zoomLevel2, initialZoomLevel]);
 },
/**
 * Toggle on/off zoom state
 *
 */
MWBtoggleZoom: function() {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "toggleZoom", []);
},
 
    /**
 * Set maximum threads to be used for decoding. Value will be limited to maximum available CPU cores.
    * Default is 4 (will trim to max available value). Set to 1 to disable multi-threaded decoding
    */
 MWBsetMaxThreads: function (maxThreads) {
     cordova.exec(function () { }, function () { }, "MWBarcodeScanner", "setMaxThreads", [maxThreads]);
 },

 
    /**
     * Set custom key:value pair which is accesible from native code.
     */
 MWBsetCustomParam: function(key, value) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setCustomParam", [key, value]);
               },
               
               
    /**
    * Enable/disable continuous scanning. If 'shouldClose' is 'false', result callback will be performed and
    * scanner will be paused. The User can call 'resumeScanning' to continue scanning, or 'closeScanner'
    * for closing the scanner. Default is 'true'.
    * Function is not available on WP8 due to the technical limitations.
    */
    MWBcloseScannerOnDecode: function(shouldClose) {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "closeScannerOnDecode", [shouldClose]);
    },
    /**
    * Resume scanning. Use this method if already using MWBcloseScannerOnDecode(false).
    * Function is not available on WP8 due to the technical limitations.
    */
    MWBresumeScanning: function() {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "resumeScanning", []);
    },
    /**
    * Close scanner. Use this method if already using MWBcloseScannerOnDecode(false).
    * Function is not available on WP8 due to the technical limitations.
    */
    MWBcloseScanner: function() {
    cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "closeScanner", []);
    },
   /**
    * Use 60 fps when available.
    * Function is only available on iOS.
    * Default is 'false'
    */
   MWBuse60fps: function(use) {
   cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "use60fps", [use]);
   },
   /**
    * Scan image.
    * imageURI - path to the image to be scanned.
    */
   MWBscanImage: function(imageURI, callback) {
   cordova.exec(callback, function(){}, "MWBarcodeScanner", "scanImage", [imageURI]);
   },
   /**
    * Set custom decoder param.
    * MWB_setParam set custom decoder param id/value pair for decoder type specified in \a codeMask.
    * codeMask                Single decoder type (MWB_CODE_MASK_...)
    * paramId                 ID of param
    * paramValue              Integer value of param
    */
   MWBsetParam: function(codeMask, paramId, paramValue) {
       cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setParam", [codeMask, paramId, paramValue]);
   },
   /**
    * Pause scanner view
    */
   MWBtogglePauseResume: function() {
       cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "togglePauseResume", []);
   },
   /**
    *  Ignore result if scanned the same code in continuous scanning mode
    *  
    *  delay         Time interval between 2 scan results with the same result.code in milliseconds
    */
   MWBduplicateCodeDelay: function(delay) {
       cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "duplicateCodeDelay", [delay]);
   },
   /**
    *  Use auto generated full screen scanning rectangle, or use user defined scanning rectangles
    *
    *  useAutoRect   Whether or not to use auto generated full screen scanning rectangle, or use user defined scanning rectangles [true, false]; default: true
    */
   MWBuseAutoRect: function(useAutoRect) {
       cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setUseAutorect", [useAutoRect]);
   },
   /**
    *  Use front facing camera
    *
    *  useFrontCamera   Whether or not to use front facing camera [true, false]; default: false
    */
   MWBuseFrontCamera: function(useFrontCamera) {
        cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "useFrontCamera", [useFrontCamera]);
   }
 
 };
 
//change these registration settings to match your licence keys
/* BEGIN Registration settings */
//if your app doesn't work after setting license keys, try to uncomment the try-catch, and see what the error is

//    try{
    var mwregister = {
    'Android' : {
        'MWB_CODE_MASK_25' : {'username' : '', 'key' : ''},
        'MWB_CODE_MASK_39' : {'username':'','key':''},
        'MWB_CODE_MASK_93' : {'username':'','key':''},
        'MWB_CODE_MASK_128' : {'username':'','key':''},
        'MWB_CODE_MASK_AZTEC' : {'username':'','key':''},
        'MWB_CODE_MASK_DM' : {'username':'','key':''},
        'MWB_CODE_MASK_PDF' : {'username':'','key':''},
        'MWB_CODE_MASK_QR' : {'username':'','key':''},
        'MWB_CODE_MASK_RSS' : {'username':'','key':''},
        'MWB_CODE_MASK_CODABAR' : {'username':'','key':''},
        'MWB_CODE_MASK_11' : {'username':'','key':''},
        'MWB_CODE_MASK_MSI' : {'username':'','key':''},
        'MWB_CODE_MASK_DOTCODE' : {'username':'','key':''}
        },
    'iOS' :{
        'MWB_CODE_MASK_25' : {'username' : '', 'key' : ''},
        'MWB_CODE_MASK_39' : {'username':'','key':''},
        'MWB_CODE_MASK_93' : {'username':'','key':''},
        'MWB_CODE_MASK_128' : {'username':'','key':''},
        'MWB_CODE_MASK_AZTEC' : {'username':'','key':''},
        'MWB_CODE_MASK_DM' : {'username':'','key':''},
        'MWB_CODE_MASK_PDF' : {'username':'','key':''},
        'MWB_CODE_MASK_QR' : {'username':'','key':''},
        'MWB_CODE_MASK_RSS' : {'username':'','key':''},
        'MWB_CODE_MASK_CODABAR' : {'username':'','key':''},
        'MWB_CODE_MASK_11' : {'username':'','key':''},
        'MWB_CODE_MASK_MSI' : {'username':'','key':''},
        'MWB_CODE_MASK_DOTCODE' : {'username':'','key':''}
        },
    'Win32NT' : {
        'MWB_CODE_MASK_25' : {'username' : '', 'key' : ''},
        'MWB_CODE_MASK_39' : {'username':'','key':''},
        'MWB_CODE_MASK_93' : {'username':'','key':''},
        'MWB_CODE_MASK_128' : {'username':'','key':''},
        'MWB_CODE_MASK_AZTEC' : {'username':'','key':''},
        'MWB_CODE_MASK_DM' : {'username':'','key':''},
        'MWB_CODE_MASK_PDF' : {'username':'','key':''},
        'MWB_CODE_MASK_QR' : {'username':'','key':''},
        'MWB_CODE_MASK_RSS' : {'username':'','key':''},
        'MWB_CODE_MASK_CODABAR' : {'username':'','key':''},
        'MWB_CODE_MASK_11' : {'username':'','key':''},
        'MWB_CODE_MASK_MSI' : {'username':'','key':''},
        'MWB_CODE_MASK_DOTCODE' : {'username':'','key':''}
        }
    }
//    }
//    catch(e){
//        console.log(e);
//    }
/* END registration settings */
 scanner = {};

scanner.closeScanner = function(){
    BarcodeScanner.MWBcloseScanner();
}
scanner.togglePauseResume= function(){
       BarcodeScanner.MWBtogglePauseResume();
}
scanner.toggleFlash = function(){
    BarcodeScanner.MWBtoggleFlash();
}
scanner.toggleZoom = function(){
    BarcodeScanner.MWBtoggleZoom();
}
scanner.resumeScanning = function(){
    BarcodeScanner.MWBresumeScanning();
}

scanner.scanImage =function(initMWBS,callbackMWBS,imageURI){


                                    var args = Array.prototype.slice.call(arguments);

                                    // console.log(arguments);
                                    // console.log('--------');
                                    // console.log(args);

                                    var initMWBS = (args.length>1)?args[0]:false,
                                        callbackMWBS = (args.length>1)?args[1]:false,
                                        imageURI = (args.length>1)?args[2]:args[0];


   
                                      //Initialize decoder with default params
                                      BarcodeScanner.MWBinitDecoder(function(){
                                      var initFunc = (typeof initMWBS === 'function')?initMWBS:function(mwbs,constants,dvc){
                                      
                                      console.log('Init function defined in MWBScanner.js invoked');
                                    
                                      var platform = mwregister[dvc.platform];
                                      
                                      Object.keys(platform).forEach(function(reg_codes){
                                                                    mwbs['MWBregisterCode'](constants[reg_codes],platform[reg_codes]['username'],platform[reg_codes]['key']);
                                                                    });
                                      
                                      
                                      // console.log('JS registration ends: '+ (new Date()).getTime());
                                      // console.log('JS Settings starts: '+ (new Date()).getTime());
                                      //settings portion, disable those that are not needed
                                      
                                      /* BEGIN settings CALLS */
                                      //if your code doesn't work after changing a few parameters, and there is no error output, uncomment the try-catch, the error will be output in your console
                                      //    try{
                                      //  mwbs['MWBsetActiveCodes'](constants.MWB_CODE_MASK_128 | constants.MWB_CODE_MASK_QR);
                                      //  mwbs['MWBsetFlags'](constants.MWB_CODE_MASK_39, constants.MWB_CFG_CODE39_EXTENDED_MODE);
                                      //  mwbs['MWBsetDirection'](constants.MWB_SCANDIRECTION_VERTICAL | constants.MWB_SCANDIRECTION_HORIZONTAL);
                                      //  mwbs['MWBsetScanningRect'](constants.MWB_CODE_MASK_39, 20,20,60,60);
                                      //  mwbs['MWBsetMinLength'](constants.MWB_CODE_MASK_39, 4);
                                      //  mwbs['MWBsetParam'](constants.MWB_CODE_MASK_DM, constants.MWB_PAR_ID_RESULT_PREFIX, constants.MWB_PAR_VALUE_RESULT_PREFIX_ALWAYS);
                                      
                                      
                                      // console.log('JS Settings ends: '+ (new Date()).getTime());
                                      //    }
                                      //    catch(e){
                                      //        console.log(e);
                                      //    }
                                      
                                      /* END settings CALLS */
                                      
                                      /* CUSTOM JAVASCRIPT CALLS */
                                      

                                      };
                                      //call the init function
                                      initFunc(BarcodeScanner,CONSTANTS,device);
                                      
                                              var callFunc = (typeof callbackMWBS === 'function')?callbackMWBS:function(result){
                                              
                                                  console.log('MWBScanner Defined callback Invoked');
                                                  
                                                  /**
                                                   * result.code - string representation of barcode result
                                                   * result.type - type of barcode detected or 'Cancel' if scanning is canceled
                                                   * result.bytes - bytes array of raw barcode result
                                                   * result.isGS1 - (boolean) barcode is GS1 compliant
                                                   * result.location - contains rectangle points p1,p2,p3,p4 with the corresponding x,y
                                                   * result.imageWidth - Width of the scanned image
                                                   * result.imageHeight - Height of the scanned image
                                                   */
                                                  
                                                  if (result.type == 'NoResult'){
                                                     //Perform some action on scanning canceled if needed
                                                  }
                                                  else if (result && result.code){
                                                  
                                                    navigator.notification.alert(result.code, function(){}, result.type + (result.isGS1?" (GS1)":""), 'Close');
                                              
                                                  }
                                             }
                                              

                                              BarcodeScanner['MWBscanImage'](imageURI ,callFunc);
                                                                    
                                    });
                                      
       }

 var x,y,width,height;

 scanner.startScanning = function(initMWBS,callbackMWBS) {
    var args = Array.prototype.slice.call(arguments);



    var initMWBS = (args.length == 2 || args.length == 6)?args[0]:false,
    callbackMWBS = (args.length == 2 || args.length == 6)?args[1]:false;

    x=y=width=height =false;

    if(args.length == 4){
       x = args[0],
       y = args[1],
       width = args[2],
       height = args[3];
    }else if(args.length == 6){
       x = args[2],
       y = args[3],
       width = args[4],
       height = args[5];
    }
          
    //Initialize decoder with default params
    BarcodeScanner.MWBinitDecoder(function(){     

        var initFunc = (typeof initMWBS === 'function')?initMWBS:function(mwbs,constants,dvc){

                console.log('Init function defined in MWBScanner.js invoked');
        
                /* END registration settings */
                var platform = mwregister[dvc.platform];

                Object.keys(platform).forEach(function(reg_codes){
                    mwbs['MWBregisterCode'](constants[reg_codes],platform[reg_codes]['username'],platform[reg_codes]['key']);
                });


                // console.log('JS registration ends: '+ (new Date()).getTime());
                // console.log('JS Settings starts: '+ (new Date()).getTime());
                //settings portion, disable those that are not needed

                /* BEGIN settings CALLS */
                    //if your code doesn't work after changing a few parameters, and there is no error output, uncomment the try-catch, the error will be output in your console
            //    try{
                  //  mwbs['MWBsetInterfaceOrientation'] (constants.OrientationPortrait);
                  //  mwbs['MWBsetOverlayMode'](constants.OverlayModeImage);
                  //  mwbs['MWBenableHiRes'](true);
                  //  mwbs['MWBenableFlash'](true);
                  //  mwbs['MWBsetActiveCodes'](constants.MWB_CODE_MASK_128 | constants.MWB_CODE_MASK_39);
                  //  mwbs['MWBsetLevel'](2);
                  //  mwbs['MWBsetFlags'](constants.MWB_CODE_MASK_39, constants.MWB_CFG_CODE39_EXTENDED_MODE);
                  //  mwbs['MWBsetDirection'](constants.MWB_SCANDIRECTION_VERTICAL | constants.MWB_SCANDIRECTION_HORIZONTAL);
                  //  mwbs['MWBsetScanningRect'](constants.MWB_CODE_MASK_39, 20,20,60,60);
                  //  mwbs['MWBenableZoom'](true);
                  //  mwbs['MWBsetZoomLevels'](200, 400, 0);
                  //  mwbs['MWBsetMinLength'](constants.MWB_CODE_MASK_39, 4);
                  //  mwbs['MWBsetMaxThreads'](1);
                  //  mwbs['MWBcloseScannerOnDecode'](false);
                  //  mwbs['MWBuse60fps'](true);      
                  //  mwbs['MWBsetParam'](constants.MWB_CODE_MASK_DM, constants.MWB_PAR_ID_RESULT_PREFIX, constants.MWB_PAR_VALUE_RESULT_PREFIX_ALWAYS);
                  //  mwbs['MWBduplicateCodeDelay'](1000);    
                  //  mwbs['MWBuseAutoRect'](false);      
                  //  mwbs['MWBuseFrontCamera'](true);

                                  

                    // console.log('JS Settings ends: '+ (new Date()).getTime());
            //    }
            //    catch(e){
            //        console.log(e);
            //    }

                /* END settings CALLS */
                
                /* CUSTOM JAVASCRIPT CALLS */

            };            
        //call the init function
        initFunc(BarcodeScanner,CONSTANTS,device);
        
        var callFunc = (typeof callbackMWBS === 'function')?callbackMWBS:function(result){

            console.log('MWBScanner Defined callback Invoked');

         /**
          * result.code - string representation of barcode result
          * result.type - type of barcode detected or 'Cancel' if scanning is canceled
          * result.bytes - bytes array of raw barcode result
          * result.isGS1 - (boolean) barcode is GS1 compliant
          * result.location - contains rectangle points p1,p2,p3,p4 with the corresponding x,y
          * result.imageWidth - Width of the scanned image
          * result.imageHeight - Height of the scanned image
          */

            if (result.type == 'Cancel'){
            //Perform some action on scanning canceled if needed
            } 
            else if (result && result.code){
               
                /*
                *  Use this sample if scanning in view 
                */
                /*
                var para = document.createElement("li");
                var node = document.createTextNode(result.code+" : "+result.type);
                para.appendChild(node);
                              
                var element = document.getElementById("mwb_list");
                element.appendChild(para);
                */          


                /*
                *  Use this sample when using mwbs['MWBcloseScannerOnDecode'](false);
                */
                /*
                 setTimeout(function(){                  
                    scanner.resumeScanning();  
                 },2000);                                
                */

               navigator.notification.alert(result.code, function(){}, result.type + (result.isGS1?" (GS1)":""), 'Close');

            }
        }

        console.log('JS Starting Scanner: '+ (new Date()).getTime());
        // Call the barcode scanner screen
        if(x === false){
            BarcodeScanner.MWBstartScanning(callFunc);  // Scan using full screen
        }else{
            BarcodeScanner.MWBstartScanning(callFunc, x,y,width,height); // Scan using view with: x, y, width, height (percentage of screen size)
        }
    });
 }
    module.exports = scanner;