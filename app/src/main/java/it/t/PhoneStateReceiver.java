package it.t;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by ytlin on 2015/6/7.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    String state;

    public static String TAG="PhoneStateReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "PhoneStateReceiver**Call State=" + state);

            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d(TAG,"PhoneStateReceiver**Idle");
            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Incoming call
                String incomingNumber =
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG,"PhoneStateReceiver**Incoming call " + incomingNumber);



            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                // setResultExtras(null);
                Log.d(TAG,"before sleep ");
                SystemClock.sleep(13000);
                Log.d(TAG,"kill ");
                if (!killCall(context)) {
                    Log.d(TAG,"PhoneStateReceiver **Unable to kill Offhook call");
                    setResultData(null);
                }
                Log.d(TAG,context+"PhoneStateReceiver **Offhook 正在播話");
            }
        } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            // Outgoing call
           /* if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))*/
           /* if (!killCall(context)) {
                Log.d(TAG,"PhoneStateReceiver **Unable to kill outgoing call");
                setResultData(null);
            }*/

            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG,"PhoneStateReceiver **Outgoing call " + outgoingNumber);


            // Kills the outgoing call

        } else {
            Log.d(TAG,"PhoneStateReceiver **unexpected intent.action=" + intent.getAction());
        }
    }


    public boolean killCall(Context context) {
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);

            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);

        } catch (Exception ex) { // Many things can go wrong with reflection calls
            Log.d(TAG,"PhoneStateReceiver **" + ex.toString());
            return false;
        }
        return true;
    }

}
