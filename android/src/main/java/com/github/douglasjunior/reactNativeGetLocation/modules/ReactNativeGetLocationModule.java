/**
* MIT License
*
* Copyright (c) 2019 Douglas Nassif Roma Junior
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/

package com.github.douglasjunior.reactNativeGetLocation.modules;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationProvider;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.RequiresPermission;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.github.douglasjunior.reactNativeGetLocation.util.GetLocation;
import com.github.douglasjunior.reactNativeGetLocation.util.SettingsUtil;
import android.os.SystemClock;

import java.util.Timer;
import java.util.TimerTask;

public class ReactNativeGetLocationModule extends ReactContextBaseJavaModule {

    public static final String NAME = "ReactNativeGetLocation";

    private LocationManager locationManager;
    private GetLocation getLocation;
    Context ctx;
    String PROVIDER;
    Timer timer = null;
    TimerTask timerTask = null;

    public ReactNativeGetLocationModule(ReactApplicationContext ctx) {
        super(ctx);
        this.ctx = ctx;

        try {
            locationManager = (LocationManager) ctx.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void openWifiSettings(final Promise primise) {
        try {
            SettingsUtil.openWifiSettings(getReactApplicationContext());
            primise.resolve(null);
        } catch (Throwable ex) {
            primise.reject(ex);
        }
    }

    @ReactMethod
    public void openCelularSettings(final Promise primise) {
        try {
            SettingsUtil.openCelularSettings(getReactApplicationContext());
            primise.resolve(null);
        } catch (Throwable ex) {
            primise.reject(ex);
        }
    }

    @ReactMethod
    public void openGpsSettings(final Promise primise) {
        try {
            SettingsUtil.openGpsSettings(getReactApplicationContext());
            primise.resolve(null);
        } catch (Throwable ex) {
            primise.reject(ex);
        }
    }

    @ReactMethod
    public void openAppSettings(final Promise promise) {
        try {
            SettingsUtil.openAppSettings(getReactApplicationContext());
            promise.resolve(null);
        } catch (Throwable ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void getCurrentPosition(ReadableMap options, Promise promise) {
        if (getLocation != null) {
            getLocation.cancel();
        }
        getLocation = new GetLocation(locationManager);
        getLocation.get(options, promise);
    }

    @ReactMethod
    public void setCurrentPosition(final double latitude, final double longitude) {
        if (timer != null) {
            timer.cancel();
            timer = null;
            timerTask.cancel();
            timerTask = null;
        }
        if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        }
        locationManager.addTestProvider (
            LocationManager.GPS_PROVIDER,
            false, // requires network
            false, // requires satellite
            false, // requires cell
            false, // has monetary cost
            true, // supports altitude
            true, // supports speed
            true, // supports bearing
            android.location.Criteria.POWER_LOW,
            android.location.Criteria.ACCURACY_FINE
        );

        final Location newLocation = new Location(LocationManager.GPS_PROVIDER);

        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setAltitude(3F);
        newLocation.setSpeed(0.01F);
        newLocation.setBearing(1F);
        newLocation.setAccuracy(500);
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
              newLocation.setTime(System.currentTimeMillis());
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  newLocation.setBearingAccuracyDegrees(0.1F);
              }
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  newLocation.setVerticalAccuracyMeters(0.1F);
              }
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  newLocation.setSpeedAccuracyMetersPerSecond(0.01F);
              }
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                 newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
              }

              locationManager.setTestProviderStatus(
                  LocationManager.GPS_PROVIDER,
                  LocationProvider.AVAILABLE,
                  null,
                  System.currentTimeMillis()
              );
              locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
            }
        };
        timer.schedule(timerTask, 0, 200);
   }

    @ReactMethod
    public void stopMockLocation() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            timerTask.cancel();
            timerTask = null;
        }
        if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        }
    }

}
