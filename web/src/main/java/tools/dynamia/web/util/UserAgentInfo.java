/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.web.util;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;

/**
 * The DetectSmartPhone class encapsulates information about a browser's
 * connection to your web site. You can use it to find out whether the browser
 * asking for your site's content is probably running on a mobile device. The
 * methods were written so you can be as granular as you want. For example,
 * enquiring whether it's as specific as an iPod Touch or as general as a
 * smartphone class device. The object's methods return true, or false.
 */
public class UserAgentInfo implements Serializable {
    // User-Agent and Accept HTTP request headers

    private String userAgent = "";
    private String httpAccept = "";

    // Let's store values for quickly accessing the same info multiple times.
    private boolean initCompleted = false;
    private boolean isWebkit = false; // Stores the result of DetectWebkit()
    private boolean isMobilePhone = false; // Stores the result of
    // DetectMobileQuick()
    private boolean isIphone = false; // Stores the result of DetectIphone()
    private boolean isAndroid = false; // Stores the result of DetectAndroid()
    private boolean isAndroidPhone = false; // Stores the result of
    // DetectAndroidPhone()
    private boolean isTierTablet = false; // Stores the result of
    // DetectTierTablet()
    private boolean isTierIphone = false; // Stores the result of
    // DetectTierIphone()
    private boolean isTierRichCss = false; // Stores the result of
    // DetectTierRichCss()
    private boolean isTierGenericMobile = false; // Stores the result of
    // DetectTierOtherPhones()
    private final boolean isGalaxyTab = false;

    // Initialize some initial smartphone string variables.
    public static final String engineWebKit = "webkit";

    public static final String deviceIphone = "iphone";
    public static final String deviceIpod = "ipod";
    public static final String deviceIpad = "ipad";
    public static final String deviceMacPpc = "macintosh"; // Used for
    // disambiguation

    public static final String deviceAndroid = "android";
    public static final String deviceGoogleTV = "googletv";
    public static final String deviceHtcFlyer = "htc_flyer"; // HTC Flyer

    public static final String deviceWinPhone7 = "windows phone os 7";
    public static final String deviceWinPhone8 = "windows phone 8";
    public static final String deviceWinMob = "windows ce";
    public static final String deviceWindows = "windows";
    public static final String deviceIeMob = "iemobile";
    public static final String devicePpc = "ppc"; // Stands for PocketPC
    public static final String enginePie = "wm5 pie"; // An old Windows Mobile

    public static final String deviceBB = "blackberry";
    public static final String deviceBB10 = "bb10"; // For the new BB 10 OS
    public static final String vndRIM = "vnd.rim"; // Detectable when BB devices
    // emulate IE or Firefox
    public static final String deviceBBStorm = "blackberry95"; // Storm 1 and 2
    public static final String deviceBBBold = "blackberry97"; // Bold 97x0
    // (non-touch)
    public static final String deviceBBBoldTouch = "blackberry 99"; // Bold 99x0
    // (touchscreen)
    public static final String deviceBBTour = "blackberry96"; // Tour
    public static final String deviceBBCurve = "blackberry89"; // Curve 2
    public static final String deviceBBCurveTouch = "blackberry 938"; // Curve
    // Touch
    // 9380
    public static final String deviceBBTorch = "blackberry 98"; // Torch
    public static final String deviceBBPlaybook = "playbook"; // PlayBook tablet

    public static final String deviceSymbian = "symbian";
    public static final String deviceS60 = "series60";
    public static final String deviceS70 = "series70";
    public static final String deviceS80 = "series80";
    public static final String deviceS90 = "series90";

    public static final String devicePalm = "palm";
    public static final String deviceWebOS = "webos"; // For Palm's line of
    // WebOS devices
    public static final String deviceWebOShp = "hpwos"; // For HP's line of
    // WebOS devices
    public static final String engineBlazer = "blazer"; // Old Palm
    public static final String engineXiino = "xiino"; // Another old Palm

    public static final String deviceNuvifone = "nuvifone"; // Garmin Nuvifone
    public static final String deviceBada = "bada"; // Samsung's Bada OS
    public static final String deviceTizen = "tizen"; // Tizen OS
    public static final String deviceMeego = "meego"; // Meego OS

    public static final String deviceKindle = "kindle"; // Amazon Kindle, eInk
    // one
    public static final String engineSilk = "silk-accelerated"; // Amazon's
    // accelerated
    // Silk browser
    // for Kindle
    // Fire

    // Initialize variables for mobile-specific content.
    public static final String vndwap = "vnd.wap";
    public static final String wml = "wml";

    // Initialize variables for other random devices and mobile browsers.
    public static final String deviceTablet = "tablet"; // Generic term for
    // slate and tablet
    // devices
    public static final String deviceBrew = "brew";
    public static final String deviceDanger = "danger";
    public static final String deviceHiptop = "hiptop";
    public static final String devicePlaystation = "playstation";
    public static final String devicePlaystationVita = "vita";
    public static final String deviceNintendoDs = "nitro";
    public static final String deviceNintendo = "nintendo";
    public static final String deviceWii = "wii";
    public static final String deviceXbox = "xbox";
    public static final String deviceArchos = "archos";

    public static final String engineOpera = "opera"; // Popular browser
    public static final String engineNetfront = "netfront"; // Common embedded
    // OS browser
    public static final String engineUpBrowser = "up.browser"; // common on some
    // phones
    public static final String engineOpenWeb = "openweb"; // Transcoding by
    // OpenWave server
    public static final String deviceMidp = "midp"; // a mobile Java technology
    public static final String uplink = "up.link";
    public static final String engineTelecaQ = "teleca q"; // a modern feature
    // phone browser
    public static final String engineObigo = "obigo"; // W 10 is a modern
    // feature phone browser

    public static final String devicePda = "pda"; // some devices report
    // themselves as PDAs
    public static final String mini = "mini"; // Some mobile browsers put "mini"
    // in their names.
    public static final String mobile = "mobile"; // Some mobile browsers put
    // "mobile" in their user
    // agent strings.
    public static final String mobi = "mobi"; // Some mobile browsers put "mobi"
    // in their user agent strings.

    // Use Maemo, Tablet, and Linux to test for Nokia"s Internet Tablets.
    public static final String maemo = "maemo";
    public static final String linux = "linux";
    public static final String qtembedded = "qt embedded"; // for Sony Mylo
    public static final String mylocom2 = "com2"; // for Sony Mylo also

    // In some UserAgents, the only clue is the manufacturer.
    public static final String manuSonyEricsson = "sonyericsson";
    public static final String manuericsson = "ericsson";
    public static final String manuSamsung1 = "sec-sgh";
    public static final String manuSony = "sony";
    public static final String manuHtc = "htc";

    // In some UserAgents, the only clue is the operator.
    public static final String svcDocomo = "docomo";
    public static final String svcKddi = "kddi";
    public static final String svcVodafone = "vodafone";

    // Disambiguation strings.
    public static final String disUpdate = "update"; // pda vs. update

    // Galaxy Tabs
    public static final String samgsungGalaxys = "SCH-I800,GT-P7510,GT-P1000,SC-01C,SGH-T849,SHW-M180L,SHW-M180S,SPH-P100";

    /**
     * Initialize the userAgent and httpAccept variables
     *
     * @param userAgent  the User-Agent header
     * @param httpAccept the Accept header
     */
    public UserAgentInfo(String userAgent, String httpAccept) {
        if (userAgent != null) {
            this.userAgent = userAgent.toLowerCase();
        }
        if (httpAccept != null) {
            this.httpAccept = httpAccept.toLowerCase();
        }

        // Intialize key stored values.
        initDeviceScan();
    }

    public UserAgentInfo(HttpServletRequest request) {
        this(request.getHeader("User-Agent"), request.getHeader("Accept"));
    }

    /**
     * Return the lower case HTTP_USER_AGENT
     *
     * @return userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Return the lower case HTTP_ACCEPT
     *
     * @return httpAccept
     */
    public String getHttpAccept() {
        return httpAccept;
    }

    /**
     * Return whether the device is an Iphone or iPod Touch
     *
     * @return isIphone
     */
    public boolean isIphone() {
        return isIphone;
    }

    /**
     * Return whether the device is in the Tablet Tier.
     *
     * @return isTierTablet
     */
    public boolean isTierTablet() {
        return isTierTablet;
    }

    /**
     * Return whether the device is in the Iphone Tier.
     *
     * @return isTierIphone
     */
    public boolean isTierIphone() {
        return isTierIphone;
    }

    /**
     * Return whether the device is in the 'Rich CSS' tier of mobile devices.
     *
     * @return isTierRichCss
     */
    public boolean isTierRichCss() {
        return isTierRichCss;
    }

    /**
     * Return whether the device is a generic, less-capable mobile device.
     *
     * @return isTierGenericMobile
     */
    public boolean IsTierGenericMobile() {
        return isTierGenericMobile;
    }

    public boolean isGalaxyTab() {
        return isGalaxyTab;
    }

    /**
     * Initialize Key Stored Values.
     */
    public void initDeviceScan() {
        // Save these properties to speed processing
        this.isWebkit = detectWebkit();
        this.isIphone = detectIphone();
        this.isAndroid = detectAndroid();
        this.isAndroidPhone = detectAndroidPhone();

        // Generally, these tiers are the most useful for web development
        this.isMobilePhone = detectMobileQuick();
        this.isTierTablet = detectTierTablet();
        this.isTierIphone = detectTierIphone();

        // Optional: Comment these out if you NEVER use them
        this.isTierRichCss = detectTierRichCss();
        this.isTierGenericMobile = detectTierOtherPhones();

        this.initCompleted = true;
    }

    public boolean detectGalaxyTablet() {
        if ((this.initCompleted)
                || (this.isGalaxyTab)) {
            return this.isGalaxyTab;
        }

        String[] galaxyRef = samgsungGalaxys.split(",");
        String userAgentUpperCase = userAgent.toUpperCase();
        for (String ref : galaxyRef) {
            if (userAgentUpperCase.contains(ref)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Detects if the current device is an iPhone.
     *
     * @return detection of an iPhone
     */
    public boolean detectIphone() {
        if ((this.initCompleted)
                || (this.isIphone)) {
            return this.isIphone;
        }

        // The iPad and iPod touch say they're an iPhone! So let's disambiguate.
        return userAgent.contains(deviceIphone)
                && !detectIpad()
                && !detectIpod();
    }

    /**
     * Detects if the current device is an iPod Touch.
     *
     * @return detection of an iPod Touch
     */
    public boolean detectIpod() {
        return userAgent.contains(deviceIpod);
    }

    /**
     * Detects if the current device is an iPad tablet.
     *
     * @return detection of an iPad
     */
    public boolean detectIpad() {
        return userAgent.contains(deviceIpad)
                && detectWebkit();
    }

    /**
     * Detects if the current device is an iPhone or iPod Touch.
     *
     * @return detection of an iPhone or iPod Touch
     */
    public boolean detectIphoneOrIpod() {
        // We repeat the searches here because some iPods may report themselves
        // as an iPhone, which would be okay.
        return userAgent.contains(deviceIphone)
                || userAgent.contains(deviceIpod);
    }

    /**
     * Detects *any* iOS device: iPhone, iPod Touch, iPad.
     *
     * @return detection of an Apple iOS device
     */
    public boolean detectIos() {
        return detectIphoneOrIpod() || detectIpad();
    }

    /**
     * Detects *any* Android OS-based device: phone, tablet, and multi-media
     * player. Also detects Google TV.
     *
     * @return detection of an Android device
     */
    public boolean detectAndroid() {
        if ((this.initCompleted)
                || (this.isAndroid)) {
            return this.isAndroid;
        }

        if ((userAgent.contains(deviceAndroid))
                || detectGoogleTV()) {
            return true;
        }
        // Special check for the HTC Flyer 7" tablet. It should report here.
        return userAgent.contains(deviceHtcFlyer);
    }

    /**
     * Detects if the current device is a (small-ish) Android OS-based device
     * used for calling and/or multi-media (like a Samsung Galaxy Player).
     * Google says these devices will have 'Android' AND 'mobile' in user agent.
     * Ignores tablets (Honeycomb and later).
     *
     * @return detection of an Android phone
     */
    public boolean detectAndroidPhone() {
        if ((this.initCompleted)
                || (this.isAndroidPhone)) {
            return this.isAndroidPhone;
        }

        if (detectAndroid() && (userAgent.contains(mobile))) {
            return true;
        }
        // Special check for Android phones with Opera Mobile. They should
        // report here.
        if (detectOperaAndroidPhone()) {
            return true;
        }
        // Special check for the HTC Flyer 7" tablet. It should report here.
        return userAgent.contains(deviceHtcFlyer);
    }

    /**
     * Detects if the current device is a (self-reported) Android tablet. Google
     * says these devices will have 'Android' and NOT 'mobile' in their user
     * agent.
     *
     * @return detection of an Android tablet
     */
    public boolean detectAndroidTablet() {
        // First, let's make sure we're on an Android device.
        if (!detectAndroid()) {
            return false;
        }

        // Special check for Opera Android Phones. They should NOT report here.
        if (detectOperaMobile()) {
            return false;
        }
        // Special check for the HTC Flyer 7" tablet. It should NOT report here.
        if (userAgent.contains(deviceHtcFlyer)) {
            return false;
        }

        // Otherwise, if it's Android and does NOT have 'mobile' in it, Google
        // says it's a tablet.
        return (!userAgent.contains(mobile));
    }

    /**
     * Detects if the current device is an Android OS-based device and the
     * browser is based on WebKit.
     *
     * @return detection of an Android WebKit browser
     */
    public boolean detectAndroidWebKit() {
        return detectAndroid() && detectWebkit();
    }

    /**
     * Detects if the current device is a GoogleTV.
     *
     * @return detection of GoogleTV
     */
    public boolean detectGoogleTV() {
        return userAgent.contains(deviceGoogleTV);
    }

    /**
     * Detects if the current browser is based on WebKit.
     *
     * @return detection of a WebKit browser
     */
    public boolean detectWebkit() {
        if ((this.initCompleted)
                || (this.isWebkit)) {
            return this.isWebkit;
        }

        return userAgent.contains(engineWebKit);
    }

    /**
     * Detects if the current browser is EITHER a Windows Phone 7.x OR 8 device
     *
     * @return detection of Windows Phone 7.x OR 8
     */
    public boolean detectWindowsPhone() {
        return detectWindowsPhone7() || detectWindowsPhone8();
    }

    /**
     * Detects a Windows Phone 7.x device (in mobile browsing mode).
     *
     * @return detection of Windows Phone 7
     */
    public boolean detectWindowsPhone7() {
        return userAgent.contains(deviceWinPhone7);
    }

    /**
     * Detects a Windows Phone 8 device (in mobile browsing mode).
     *
     * @return detection of Windows Phone 8
     */
    public boolean detectWindowsPhone8() {
        return userAgent.contains(deviceWinPhone8);
    }

    /**
     * Detects if the current browser is a Windows Mobile device. Excludes
     * Windows Phone 7.x and 8 devices. Focuses on Windows Mobile 6.xx and
     * earlier.
     *
     * @return detection of Windows Mobile
     */
    public boolean detectWindowsMobile() {
        if (detectWindowsPhone()) {
            return false;
        }
        // Most devices use 'Windows CE', but some report 'iemobile'
        // and some older ones report as 'PIE' for Pocket IE.
        // We also look for instances of HTC and Windows for many of their WinMo
        // devices.
        if (userAgent.contains(deviceWinMob)
                || userAgent.contains(deviceIeMob)
                || userAgent.contains(enginePie)
                || (userAgent.contains(manuHtc) && userAgent.contains(deviceWindows))
                || (detectWapWml() && userAgent.contains(deviceWindows))) {
            return true;
        }

        // Test for Windows Mobile PPC but not old Macintosh PowerPC.
        return userAgent.contains(devicePpc)
                && !(userAgent.contains(deviceMacPpc));

    }

    /**
     * Detects if the current browser is any BlackBerry. Includes BB10 OS, but
     * excludes the PlayBook.
     *
     * @return detection of Blackberry
     */
    public boolean detectBlackBerry() {
        if (userAgent.contains(deviceBB)
                || httpAccept.contains(vndRIM)) {
            return true;
        }

        return detectBlackBerry10Phone();

    }

    /**
     * Detects if the current browser is a BlackBerry 10 OS phone. Excludes
     * tablets.
     *
     * @return detection of a Blackberry 10 device
     */
    public boolean detectBlackBerry10Phone() {
        return userAgent.contains(deviceBB10)
                && userAgent.contains(mobile);
    }

    /**
     * Detects if the current browser is on a BlackBerry tablet device. Example:
     * PlayBook
     *
     * @return detection of a Blackberry Tablet
     */
    public boolean detectBlackBerryTablet() {
        return userAgent.contains(deviceBBPlaybook);
    }

    /**
     * Detects if the current browser is a BlackBerry device AND uses a
     * WebKit-based browser. These are signatures for the new BlackBerry OS 6.
     * Examples: Torch. Includes the Playbook.
     *
     * @return detection of a Blackberry device with WebKit browser
     */
    public boolean detectBlackBerryWebKit() {
        return detectBlackBerry() && detectWebkit();
    }

    /**
     * Detects if the current browser is a BlackBerry Touch device, such as the
     * Storm, Torch, and Bold Touch. Excludes the Playbook.
     *
     * @return detection of a Blackberry touchscreen device
     */
    public boolean detectBlackBerryTouch() {
        return detectBlackBerry()
                && (userAgent.contains(deviceBBStorm)
                || userAgent.contains(deviceBBTorch)
                || userAgent.contains(deviceBBBoldTouch)
                || userAgent.contains(deviceBBCurveTouch));
    }

    /**
     * Detects if the current browser is a BlackBerry device AND has a more
     * capable recent browser. Excludes the Playbook. Examples, Storm, Bold,
     * Tour, Curve2 Excludes the new BlackBerry OS 6 and 7 browser!!
     *
     * @return detection of a Blackberry device with a better browser
     */
    public boolean detectBlackBerryHigh() {
        // Disambiguate for BlackBerry OS 6 or 7 (WebKit) browser
        if (detectBlackBerryWebKit()) {
            return false;
        }
        if (detectBlackBerry()) {
            return detectBlackBerryTouch()
                    || userAgent.contains(deviceBBBold)
                    || userAgent.contains(deviceBBTour)
                    || userAgent.contains(deviceBBCurve);
        } else {
            return false;
        }
    }

    /**
     * Detects if the current browser is a BlackBerry device AND has an older,
     * less capable browser. Examples: Pearl, 8800, Curve1
     *
     * @return detection of a Blackberry device with a poorer browser
     */
    public boolean detectBlackBerryLow() {
        if (detectBlackBerry()) {
            // Assume that if it's not in the High tier, then it's Low
            return !detectBlackBerryHigh()
                    && !detectBlackBerryWebKit();
        } else {
            return false;
        }
    }

    /**
     * Detects if the current browser is the Symbian S60 Open Source Browser.
     *
     * @return detection of Symbian S60 Browser
     */
    public boolean detectS60OssBrowser() {
        // First, test for WebKit, then make sure it's either Symbian or S60.
        return detectWebkit()
                && (userAgent.contains(deviceSymbian)
                || userAgent.contains(deviceS60));
    }

    /**
     * Detects if the current device is any Symbian OS-based device, including
     * older S60, Series 70, Series 80, Series 90, and UIQ, or other browsers
     * running on these devices.
     *
     * @return detection of SymbianOS
     */
    public boolean detectSymbianOS() {
        return userAgent.contains(deviceSymbian)
                || userAgent.contains(deviceS60)
                || userAgent.contains(deviceS70)
                || userAgent.contains(deviceS80)
                || userAgent.contains(deviceS90);
    }

    /**
     * Detects if the current browser is on a PalmOS device.
     *
     * @return detection of a PalmOS device
     */
    public boolean detectPalmOS() {
        // Make sure it's not WebOS first
        if (detectPalmWebOS()) {
            return false;
        }

        // Most devices nowadays report as 'Palm', but some older ones reported
        // as Blazer or Xiino.
        return userAgent.contains(devicePalm)
                || userAgent.contains(engineBlazer)
                || userAgent.contains(engineXiino);
    }

    /**
     * Detects if the current browser is on a Palm device running the new WebOS.
     *
     * @return detection of a Palm WebOS device
     */
    public boolean detectPalmWebOS() {
        return userAgent.contains(deviceWebOS);
    }

    /**
     * Detects if the current browser is on an HP tablet running WebOS.
     *
     * @return detection of an HP WebOS tablet
     */
    public boolean detectWebOSTablet() {
        return userAgent.contains(deviceWebOShp)
                && userAgent.contains(deviceTablet);
    }

    /**
     * Detects Opera Mobile or Opera Mini.
     *
     * @return detection of an Opera browser for a mobile device
     */
    public boolean detectOperaMobile() {
        return userAgent.contains(engineOpera)
                && (userAgent.contains(mini)
                || userAgent.contains(mobi));
    }

    /**
     * Detects Opera Mobile on an Android phone.
     *
     * @return detection of an Opera browser on an Android phone
     */
    public boolean detectOperaAndroidPhone() {
        return userAgent.contains(engineOpera)
                && (userAgent.contains(deviceAndroid)
                && userAgent.contains(mobi));
    }

    /**
     * Detects Opera Mobile on an Android tablet.
     *
     * @return detection of an Opera browser on an Android tablet
     */
    public boolean detectOperaAndroidTablet() {
        return userAgent.contains(engineOpera)
                && (userAgent.contains(deviceAndroid)
                && userAgent.contains(deviceTablet));
    }

    /**
     * Detects if the current device is an Amazon Kindle (eInk devices only).
     * Note: For the Kindle Fire, use the normal Android methods.
     *
     * @return detection of a Kindle
     */
    public boolean detectKindle() {
        return userAgent.contains(deviceKindle)
                && !detectAndroid();
    }

    /**
     * Detects if the current Amazon device is using the Silk Browser. Note:
     * Typically used by the the Kindle Fire.
     *
     * @return detection of an Amazon Kindle Fire in Silk mode.
     */
    public boolean detectAmazonSilk() {
        return userAgent.contains(engineSilk);
    }

    /**
     * Detects if the current browser is a Garmin Nuvifone.
     *
     * @return detection of a Garmin Nuvifone
     */
    public boolean detectGarminNuvifone() {
        return userAgent.contains(deviceNuvifone);
    }

    /**
     * Detects a device running the Bada smartphone OS from Samsung.
     *
     * @return detection of a Bada device
     */
    public boolean detectBada() {
        return userAgent.contains(deviceBada);
    }

    /**
     * Detects a device running the Tizen smartphone OS.
     *
     * @return detection of a Tizen device
     */
    public boolean detectTizen() {
        return userAgent.contains(deviceTizen);
    }

    /**
     * Detects a device running the Meego OS.
     *
     * @return detection of a Meego device
     */
    public boolean detectMeego() {
        return userAgent.contains(deviceMeego);
    }

    /**
     * Detects the Danger Hiptop device.
     *
     * @return detection of a Danger Hiptop
     */
    public boolean detectDangerHiptop() {
        return userAgent.contains(deviceDanger)
                || userAgent.contains(deviceHiptop);
    }

    /**
     * Detects if the current browser is a Sony Mylo device.
     *
     * @return detection of a Sony Mylo device
     */
    public boolean detectSonyMylo() {
        return userAgent.contains(manuSony)
                && (userAgent.contains(qtembedded)
                || userAgent.contains(mylocom2));
    }

    /**
     * Detects if the current device is on one of the Maemo-based Nokia Internet
     * Tablets.
     *
     * @return detection of a Maemo OS tablet
     */
    public boolean detectMaemoTablet() {
        if (userAgent.contains(maemo)) {
            return true;
        } else return userAgent.contains(linux)
                && userAgent.contains(deviceTablet)
                && !detectWebOSTablet()
                && !detectAndroid();
    }

    /**
     * Detects if the current device is an Archos media player/Internet tablet.
     *
     * @return detection of an Archos media player
     */
    public boolean detectArchos() {
        return userAgent.contains(deviceArchos);
    }

    /**
     * Detects if the current device is an Internet-capable game console.
     * Includes many handheld consoles.
     *
     * @return detection of any Game Console
     */
    public boolean detectGameConsole() {
        return detectSonyPlaystation()
                || detectNintendo()
                || detectXbox();
    }

    /**
     * Detects if the current device is a Sony Playstation.
     *
     * @return detection of Sony Playstation
     */
    public boolean detectSonyPlaystation() {
        return userAgent.contains(devicePlaystation);
    }

    /**
     * Detects if the current device is a handheld gaming device with a
     * touchscreen and modern iPhone-class browser. Includes the Playstation
     * Vita.
     *
     * @return detection of a handheld gaming device
     */
    public boolean detectGamingHandheld() {
        return (userAgent.contains(devicePlaystation))
                && (userAgent.contains(devicePlaystationVita));
    }

    /**
     * Detects if the current device is a Nintendo game device.
     *
     * @return detection of Nintendo
     */
    public boolean detectNintendo() {
        return userAgent.contains(deviceNintendo)
                || userAgent.contains(deviceWii)
                || userAgent.contains(deviceNintendoDs);
    }

    /**
     * Detects if the current device is a Microsoft Xbox.
     *
     * @return detection of Xbox
     */
    public boolean detectXbox() {
        return userAgent.contains(deviceXbox);
    }

    /**
     * Detects whether the device is a Brew-powered device.
     *
     * @return detection of a Brew device
     */
    public boolean detectBrewDevice() {
        return userAgent.contains(deviceBrew);
    }

    /**
     * Detects whether the device supports WAP or WML.
     *
     * @return detection of a WAP- or WML-capable device
     */
    public boolean detectWapWml() {
        return httpAccept.contains(vndwap)
                || httpAccept.contains(wml);
    }

    /**
     * Detects if the current device supports MIDP, a mobile Java technology.
     *
     * @return detection of a MIDP mobile Java-capable device
     */
    public boolean detectMidpCapable() {
        return userAgent.contains(deviceMidp)
                || httpAccept.contains(deviceMidp);
    }

    // *****************************
    // Device Classes
    // *****************************

    /**
     * Check to see whether the device is any device in the 'smartphone'
     * category.
     *
     * @return detection of a general smartphone device
     */
    public boolean detectSmartphone() {
        // Exclude duplicates from TierIphone
        return (detectTierIphone()
                || detectS60OssBrowser()
                || detectSymbianOS()
                || detectWindowsMobile()
                || detectBlackBerry()
                || detectPalmOS()) && !detectGalaxyTablet();
    }

    /**
     * Detects if the current device is a mobile device. This method catches
     * most of the popular modern devices. Excludes Apple iPads and other modern
     * tablets.
     *
     * @return detection of any mobile device using the quicker method
     */
    public boolean detectMobileQuick() {
        // Let's exclude tablets
        if (detectTierTablet()) {
            return false;
        }

        if ((initCompleted)
                || (isMobilePhone)) {
            return isMobilePhone;
        }

        // Most mobile browsing is done on smartphones
        if (detectSmartphone()) {
            return true;
        }

        if (detectWapWml()
                || detectBrewDevice()
                || detectOperaMobile()) {
            return true;
        }

        if ((userAgent.contains(engineObigo))
                || (userAgent.contains(engineNetfront))
                || (userAgent.contains(engineUpBrowser))
                || (userAgent.contains(engineOpenWeb))) {
            return true;
        }

        if (detectDangerHiptop()
                || detectMidpCapable()
                || detectMaemoTablet()
                || detectArchos()) {
            return true;
        }

        if ((userAgent.contains(devicePda))
                && (!userAgent.contains(disUpdate))) // no index found
        {
            return true;
        }

        if (userAgent.contains(mobile)) {
            return true;
        }

        // We also look for Kindle devices
        return detectKindle()
                || detectAmazonSilk();

    }

    /**
     * The longer and more thorough way to detect for a mobile device. Will
     * probably detect most feature phones, smartphone-class devices, Internet
     * Tablets, Internet-enabled game consoles, etc. This ought to catch a lot
     * of the more obscure and older devices, also -- but no promises on
     * thoroughness!
     *
     * @return detection of any mobile device using the more thorough method
     */
    public boolean detectMobileLong() {
        if (detectMobileQuick()
                || detectGameConsole()
                || detectSonyMylo()) {
            return true;
        }

        // detect older phones from certain manufacturers and operators.
        if (userAgent.contains(uplink)) {
            return true;
        }
        if (userAgent.contains(manuSonyEricsson)) {
            return true;
        }
        if (userAgent.contains(manuericsson)) {
            return true;
        }
        if (userAgent.contains(manuSamsung1)) {
            return true;
        }

        if (userAgent.contains(svcDocomo)) {
            return true;
        }
        if (userAgent.contains(svcKddi)) {
            return true;
        }
        return userAgent.contains(svcVodafone);

    }

    // *****************************
    // For Mobile Web Site Design
    // *****************************

    /**
     * The quick way to detect for a tier of devices. This method detects for
     * the new generation of HTML 5 capable, larger screen tablets. Includes
     * iPad, Android (e.g., Xoom), BB Playbook, WebOS, etc.
     *
     * @return detection of any device in the Tablet Tier
     */
    public boolean detectTierTablet() {
        if ((this.initCompleted)
                || (this.isTierTablet)) {
            return this.isTierTablet;
        }

        return detectIpad()
                || detectAndroidTablet()
                || detectBlackBerryTablet()
                || detectWebOSTablet()
                || detectGalaxyTablet();
    }

    /**
     * The quick way to detect for a tier of devices. This method detects for
     * devices which can display iPhone-optimized web content. Includes iPhone,
     * iPod Touch, Android, Windows Phone 7 and 8, BB10, WebOS, Playstation
     * Vita, etc.
     *
     * @return detection of any device in the iPhone/Android/Windows
     * Phone/BlackBerry/WebOS Tier
     */
    public boolean detectTierIphone() {
        if ((this.initCompleted)
                || (this.isTierIphone)) {
            return this.isTierIphone;
        }

        return detectIphoneOrIpod()
                || detectAndroidPhone()
                || detectWindowsPhone()
                || detectBlackBerry10Phone()
                || (detectBlackBerryWebKit()
                && detectBlackBerryTouch())
                || detectPalmWebOS()
                || detectBada()
                || detectTizen()
                || detectGamingHandheld();
    }

    /**
     * The quick way to detect for a tier of devices. This method detects for
     * devices which are likely to be capable of viewing CSS content optimized
     * for the iPhone, but may not necessarily support JavaScript. Excludes all
     * iPhone Tier devices.
     *
     * @return detection of any device in the 'Rich CSS' Tier
     */
    public boolean detectTierRichCss() {
        if ((this.initCompleted)
                || (this.isTierRichCss)) {
            return this.isTierRichCss;
        }

        boolean result = false;

        // The following devices are explicitly ok.
        // Note: 'High' BlackBerry devices ONLY
        if (detectMobileQuick()) {

            // Exclude iPhone Tier and e-Ink Kindle devices.
            if (!detectTierIphone() && !detectKindle()) {

                // The following devices are explicitly ok.
                // Note: 'High' BlackBerry devices ONLY
                // Older Windows 'Mobile' isn't good enough for iPhone Tier.
                if (detectWebkit()
                        || detectS60OssBrowser()
                        || detectBlackBerryHigh()
                        || detectWindowsMobile()
                        || userAgent.contains(engineTelecaQ)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * The quick way to detect for a tier of devices. This method detects for
     * all other types of phones, but excludes the iPhone and RichCSS Tier
     * devices.
     *
     * @return detection of a mobile device in the less capable tier
     */
    public boolean detectTierOtherPhones() {
        if ((this.initCompleted)
                || (this.isTierGenericMobile)) {
            return this.isTierGenericMobile;
        }

        // Exclude devices in the other 2 categories
        return detectMobileLong()
                && !detectTierIphone()
                && !detectTierRichCss();

    }
}
