package fr.celya.celyavox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

public class TelecomConnectionService extends ConnectionService {
    private static final String TAG = "TelecomConnectionService";
    private static Context appContext;
    private static CallConnection currentConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        Log.d(TAG, "TelecomConnectionService created");
    }

    public static Context getContext() {
        return appContext;
    }

    public static CallConnection getCurrentConnection() {
        return currentConnection;
    }

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "Creating incoming connection");
        
        Bundle extras = request.getExtras();
        String callId = extras != null ? extras.getString("callId", "unknown") : "unknown";
        String callerName = extras != null ? extras.getString("callerName", "CelyaVox") : "CelyaVox";
        
        currentConnection = new CallConnection(callId);
        currentConnection.setCallerDisplayName(callerName, TelecomManager.PRESENTATION_ALLOWED);
        currentConnection.setAddress(request.getAddress(), TelecomManager.PRESENTATION_ALLOWED);
        currentConnection.setRinging();
        
        return currentConnection;
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "Outgoing connection not supported");
        return Connection.createFailedConnection(new android.telecom.DisconnectCause(android.telecom.DisconnectCause.ERROR));
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e(TAG, "Failed to create incoming connection");
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e(TAG, "Failed to create outgoing connection");
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
    }
}
