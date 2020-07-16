package com.github.douglasjunior.reactNativeGetLocation.modules;

import android.telephony.TelephonyManager;
import java.lang.reflect.Method;
import java.util.Objects;
import java.lang.reflect.InvocationTargetException;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationProvider;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class MobileDataMgrModule extends ReactContextBaseJavaModule {

    public static final String NAME = "MobileDataMgr";

    Context ctx;

    public MobileDataMgrModule(ReactApplicationContext ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void getMobileDataState(final Promise promise) {
      try {
         TelephonyManager telephonyService = (TelephonyManager) ctx.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
         Method getMobileDataEnabledMethod = Objects.requireNonNull(telephonyService).getClass().getDeclaredMethod("getDataEnabled");
         promise.resolve((boolean) (Boolean) getMobileDataEnabledMethod.invoke(telephonyService));
      } catch (Exception ex) {
         promise.reject(ex);
      }
   }

   @ReactMethod
   public void setMobileDataState(boolean mobileDataEnabled, final Promise promise) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        TelephonyManager telephonyService = (TelephonyManager) ctx.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        Method setMobileDataEnabledMethod = Objects.requireNonNull(telephonyService).getClass().getDeclaredMethod("setDataEnabled", boolean.class);
        setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
        promise.resolve(true);
  }

}
