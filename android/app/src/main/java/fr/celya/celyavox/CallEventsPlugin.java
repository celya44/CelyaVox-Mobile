package fr.celya.celyavox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CallEvents")
public class CallEventsPlugin extends Plugin {
    private static final String TAG = "CallEventsPlugin";
    private BroadcastReceiver callReceiver;

    @Override
    public void load() {
        super.load();
        registerCallReceiver();
        Log.d(TAG, "CallEventsPlugin loaded");
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();
        if (callReceiver != null) {
            try {
                getContext().unregisterReceiver(callReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
            }
        }
    }

    private void registerCallReceiver() {
        callReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String callId = intent.getStringExtra("callId");
                
                JSObject data = new JSObject();
                data.put("callId", callId);
                
                if ("fr.celya.celyavox.CALL_ANSWERED".equals(action)) {
                    Log.d(TAG, "Call answered event - notifying JavaScript");
                    data.put("action", "answered");
                    notifyListeners("callAnswered", data);
                } else if ("fr.celya.celyavox.CALL_REJECTED".equals(action)) {
                    Log.d(TAG, "Call rejected event - notifying JavaScript");
                    data.put("action", "rejected");
                    notifyListeners("callRejected", data);
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("fr.celya.celyavox.CALL_ANSWERED");
        filter.addAction("fr.celya.celyavox.CALL_REJECTED");
        
        getContext().registerReceiver(callReceiver, filter, Context.RECEIVER_EXPORTED);
        Log.d(TAG, "Call receiver registered");
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        JSObject ret = new JSObject();
        ret.put("value", value);
        call.resolve(ret);
    }
}
