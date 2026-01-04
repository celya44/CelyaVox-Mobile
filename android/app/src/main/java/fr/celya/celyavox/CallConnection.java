package fr.celya.celyavox;

import android.content.Intent;
import android.net.Uri;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;

public class CallConnection extends Connection {
    private static final String TAG = "CallConnection";
    
    private final String callId;
    
    public CallConnection(String callId) {
        this.callId = callId;
        setConnectionCapabilities(
            CAPABILITY_SUPPORT_HOLD |
            CAPABILITY_MUTE
        );
        setAudioModeIsVoip(true);
        Log.d(TAG, "CallConnection created for call: " + callId);
    }

    @Override
    public void onShowIncomingCallUi() {
        Log.d(TAG, "onShowIncomingCallUi - Launching IncomingCallActivity");
        super.onShowIncomingCallUi();
        
        // Lancer l'activité d'appel entrant
        Intent intent = new Intent(TelecomConnectionService.getContext(), IncomingCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("callId", callId);
        TelecomConnectionService.getContext().startActivity(intent);
    }

    @Override
    public void onAnswer() {
        Log.d(TAG, "Call answered: " + callId);
        setActive();
        
        // Notifier l'activité que l'appel est accepté
        Intent intent = new Intent("fr.celya.celyavox.CALL_ANSWERED");
        intent.putExtra("callId", callId);
        TelecomConnectionService.getContext().sendBroadcast(intent);
    }

    @Override
    public void onReject() {
        Log.d(TAG, "Call rejected: " + callId);
        setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
        destroy();
        
        // Notifier l'activité que l'appel est rejeté
        Intent intent = new Intent("fr.celya.celyavox.CALL_REJECTED");
        intent.putExtra("callId", callId);
        TelecomConnectionService.getContext().sendBroadcast(intent);
    }

    @Override
    public void onDisconnect() {
        Log.d(TAG, "Call disconnected: " + callId);
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        destroy();
    }

    @Override
    public void onAbort() {
        Log.d(TAG, "Call aborted: " + callId);
        setDisconnected(new DisconnectCause(DisconnectCause.CANCELED));
        destroy();
    }

    @Override
    public void onHold() {
        Log.d(TAG, "Call held: " + callId);
        setOnHold();
    }

    @Override
    public void onUnhold() {
        Log.d(TAG, "Call unheld: " + callId);
        setActive();
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        Log.d(TAG, "Audio state changed: " + state);
    }
}
