package fr.celya.celyavox;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.getcapacitor.BridgeActivity;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends BridgeActivity {

	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Enregistrer le plugin CallEvents
		registerPlugin(CallEventsPlugin.class);
		
		// Ensure IME is shown and layout resizes properly on newer SDKs
		getWindow().setSoftInputMode(
			WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
		);
		enableImmersiveMode();
		
		// Vérifier si le PhoneAccount est activé
		checkPhoneAccountEnabled();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			enableImmersiveMode();
		}
	}

	private void enableImmersiveMode() {
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		View decorView = getWindow().getDecorView();
		WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(decorView);
		if (controller != null) {
			controller.hide(WindowInsetsCompat.Type.systemBars());
			controller.setSystemBarsBehavior(
				WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
			);
		}
	}
	
	private void checkPhoneAccountEnabled() {
		try {
			TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
			if (telecomManager == null) {
				return;
			}
			
			ComponentName componentName = new ComponentName(this, TelecomConnectionService.class);
			PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(componentName, "CelyaVoxAccount");
			
			// Enregistrer le PhoneAccount
			PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, "CelyaVox")
					.setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER | PhoneAccount.CAPABILITY_CONNECTION_MANAGER)
					.build();
			telecomManager.registerPhoneAccount(phoneAccount);
			
			// Vérifier s'il est activé
			PhoneAccount account = telecomManager.getPhoneAccount(phoneAccountHandle);
			if (account != null && !account.isEnabled()) {
				Log.w(TAG, "PhoneAccount not enabled - showing dialog");
				showEnablePhoneAccountDialog();
			} else {
				Log.d(TAG, "PhoneAccount is enabled");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error checking PhoneAccount: " + e.getMessage());
		}
	}
	
	private void showEnablePhoneAccountDialog() {
		new AlertDialog.Builder(this)
			.setTitle("Activation requise")
			.setMessage("Pour recevoir les appels entrants, vous devez activer CelyaVox dans les paramètres d'appel.\n\nVoulez-vous ouvrir les paramètres maintenant ?")
			.setPositiveButton("Ouvrir les paramètres", (dialog, which) -> {
				try {
					Intent intent = new Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS);
					startActivity(intent);
				} catch (Exception e) {
					Log.e(TAG, "Failed to open phone account settings: " + e.getMessage());
				}
			})
			.setNegativeButton("Plus tard", null)
			.setCancelable(true)
			.show();
	}
}
