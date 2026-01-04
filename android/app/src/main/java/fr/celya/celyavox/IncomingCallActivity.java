package fr.celya.celyavox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IncomingCallActivity extends AppCompatActivity {
    private static final String TAG = "IncomingCallActivity";
    private String callId;
    private BroadcastReceiver callReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Afficher par-dessus l'écran de verrouillage
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        
        setContentView(R.layout.activity_incoming_call);
        
        // Récupérer les informations de l'appel
        Intent intent = getIntent();
        callId = intent.getStringExtra("callId");
        String callerName = intent.getStringExtra("callerName");
        
        TextView callerNameView = findViewById(R.id.caller_name);
        if (callerName != null && !callerName.isEmpty()) {
            callerNameView.setText(callerName);
        }
        
        // Bouton Accepter
        findViewById(R.id.btn_accept).setOnClickListener(v -> acceptCall());
        
        // Bouton Rejeter
        findViewById(R.id.btn_reject).setOnClickListener(v -> rejectCall());
        
        // Écouter les événements de l'appel
        registerCallReceiver();
        
        Log.d(TAG, "IncomingCallActivity created for call: " + callId);
    }

    private void registerCallReceiver() {
        callReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("fr.celya.celyavox.CALL_ANSWERED".equals(action)) {
                    Log.d(TAG, "Call answered via system UI");
                    onCallAccepted();
                } else if ("fr.celya.celyavox.CALL_REJECTED".equals(action)) {
                    Log.d(TAG, "Call rejected via system UI");
                    finish();
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("fr.celya.celyavox.CALL_ANSWERED");
        filter.addAction("fr.celya.celyavox.CALL_REJECTED");
        registerReceiver(callReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    private void acceptCall() {
        Log.d(TAG, "User accepted call from UI");
        
        CallConnection connection = TelecomConnectionService.getCurrentConnection();
        if (connection != null) {
            connection.onAnswer();
        }
        
        onCallAccepted();
    }

    private void onCallAccepted() {
        // Mettre à jour l'UI
        TextView statusView = findViewById(R.id.call_status);
        statusView.setText("Connexion en cours...");
        
        // Ouvrir MainActivity après un court délai
        findViewById(R.id.btn_accept).postDelayed(() -> {
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            finish();
        }, 500);
    }

    private void rejectCall() {
        Log.d(TAG, "User rejected call from UI");
        
        CallConnection connection = TelecomConnectionService.getCurrentConnection();
        if (connection != null) {
            connection.onReject();
        }
        
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
        }
    }
}
