package com.breadwallet;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.tools.listeners.SyncReceiver;
import com.breadwallet.tools.util.Utils;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


/**
************************************************************************
// Copyright (C) Sigma Systems Inc - All Rights Reserved
// Unauthorized copying of this file, via any medium is strictly prohibited
// Proprietary and confidential
// Written by BreadWallet LLC 2015
// Modified and maintained by Crypto Cloud Inc, Sigma Systems Inc, SumcoinIndex.com <support@sumcoinindex.com>, 
// 2017 
***********************************************************************
 * SliceWallet
 * <p/>
 * Created by Sigma Systems Inc <support@sumcoinindex.com> 2017.
 * Copyright (c) 2017 Sigma Systems Inc
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class BreadApp extends Application {
    private static final String TAG = BreadApp.class.getName();
    public static int DISPLAY_HEIGHT_PX;
    FingerprintManager mFingerprintManager;
    // host is the server(s) on which the API is hosted
    public static String HOST = "api.breadwallet.com";
    private static List<OnAppBackgrounded> listeners;
    private static Timer isBackgroundChecker;
    public static AtomicInteger activityCounter = new AtomicInteger();
    public static long backgroundedTime;
    public static boolean appInBackground;

    private static Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Utils.isEmulatorOrDebug(this)) {
//            BRKeyStore.putFailCount(0, this);
            HOST = "stage2.breadwallet.com";
            FirebaseCrash.setCrashCollectionEnabled(false);
//            FirebaseCrash.report(new RuntimeException("test with new json file"));
        }

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int DISPLAY_WIDTH_PX = size.x;
        DISPLAY_HEIGHT_PX = size.y;
        mFingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

//        addOnBackgroundedListener(new OnAppBackgrounded() {
//            @Override
//            public void onBackgrounded() {
//
//            }
//        });

    }


    public static Context getBreadContext() {
        return currentActivity == null ? SyncReceiver.app : currentActivity;
    }

    public static void setBreadContext(Activity app) {
        currentActivity = app;
    }

    public static void fireListeners() {
        if (listeners == null) return;
        for (OnAppBackgrounded lis : listeners) lis.onBackgrounded();
    }

    public static void addOnBackgroundedListener(OnAppBackgrounded listener) {
        if (listeners == null) listeners = new ArrayList<>();
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public static boolean isAppInBackground(final Context context) {
        return context == null || activityCounter.get() <= 0;
    }

    //call onStop on evert activity so
    public static void onStop(final BRActivity app) {
        if (isBackgroundChecker != null) isBackgroundChecker.cancel();
        isBackgroundChecker = new Timer();
        TimerTask backgroundCheck = new TimerTask() {
            @Override
            public void run() {
                if (isAppInBackground(app)) {
                    backgroundedTime = System.currentTimeMillis();
                    Log.e(TAG, "App went in background!");
                    // APP in background, do something
                    isBackgroundChecker.cancel();
                    fireListeners();
                }
                // APP in foreground, do something else
            }
        };

        isBackgroundChecker.schedule(backgroundCheck, 500, 500);
    }

    public interface OnAppBackgrounded {
        void onBackgrounded();
    }
}
