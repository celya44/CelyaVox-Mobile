package fr.celya.celyavox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushMessagingService extends FirebaseMessagingService {
	private static final String TAG = "PushMessagingService";
	private static final String CHANNEL_ID = "CallsV2";
	private static final int NOTIFICATION_ID = 1001;

	@Override
	public void onNewToken(@NonNull String token) {
		super.onNewToken(token);
		// TODO: send the new token to your backend so it can target this device.
	}

	@Override
	public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);

		String type = null;
		String title = null;
		String body = null;

		// Extraire les données du message
		if (remoteMessage.getNotification() != null) {
			title = remoteMessage.getNotification().getTitle();
			body = remoteMessage.getNotification().getBody();
		}
		if (remoteMessage.getData() != null) {
			type = remoteMessage.getData().get("type");
			if (TextUtils.isEmpty(title)) {
				title = remoteMessage.getData().get("title");
			}
			if (TextUtils.isEmpty(body)) {
				body = remoteMessage.getData().get("body");
			}
		}

		// Si c'est un réveil/appel, utiliser ConnectionService
		if ("wake_up".equalsIgnoreCase(type) || "call".equalsIgnoreCase(type)) {
			Log.d(TAG, "Incoming call detected, using ConnectionService");
			showIncomingCall(title, body);
		} else {
			// Sinon, afficher une notification normale
			Log.d(TAG, "Regular notification");
			showNotification(title, body);
		}
	}

	private void showIncomingCall(String callerName, String callInfo) {
		TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
		if (telecomManager == null) {
			Log.e(TAG, "TelecomManager not available");
			showNotification(callerName, callInfo);
			return;
		}

		try {
			// Enregistrer le PhoneAccount (toujours, pour être sûr)
			PhoneAccountHandle phoneAccountHandle = getPhoneAccountHandle();
			registerPhoneAccount();

			// Vérifier si le PhoneAccount est activé
			if (!isPhoneAccountEnabled(telecomManager, phoneAccountHandle)) {
				Log.w(TAG, "PhoneAccount not enabled - opening settings");
				openPhoneAccountSettings();
				showNotification(callerName, callInfo);
				return;
			}

			// Créer le bundle d'extras
			Bundle extras = new Bundle();
			extras.putString("callId", String.valueOf(System.currentTimeMillis()));
			extras.putString("callerName", callerName != null ? callerName : "CelyaVox");
			extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
			extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false);

			// Ajouter l'incoming call
			telecomManager.addNewIncomingCall(phoneAccountHandle, extras);

			Log.d(TAG, "Incoming call added to TelecomManager");
		} catch (SecurityException e) {
			Log.e(TAG, "SecurityException when adding incoming call: " + e.getMessage());
			Log.e(TAG, "Falling back to notification");
			showNotification(callerName, callInfo);
		}
	}

	private boolean isPhoneAccountEnabled(TelecomManager telecomManager, PhoneAccountHandle handle) {
		try {
			PhoneAccount account = telecomManager.getPhoneAccount(handle);
			return account != null && account.isEnabled();
		} catch (Exception e) {
			Log.e(TAG, "Error checking if PhoneAccount is enabled: " + e.getMessage());
			return false;
		}
	}

	private void openPhoneAccountSettings() {
		try {
			Intent intent = new Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Failed to open phone account settings: " + e.getMessage());
		}
	}

	private void registerPhoneAccount() {
		TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
		if (telecomManager == null) {
			Log.e(TAG, "Cannot register phone account: TelecomManager unavailable");
			return;
		}

		PhoneAccountHandle phoneAccountHandle = getPhoneAccountHandle();
		PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, "CelyaVox")
				.setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER | PhoneAccount.CAPABILITY_CONNECTION_MANAGER)
				.build();

		telecomManager.registerPhoneAccount(phoneAccount);
		Log.d(TAG, "PhoneAccount registered");
	}

	private PhoneAccountHandle getPhoneAccountHandle() {
		ComponentName componentName = new ComponentName(this, TelecomConnectionService.class);
		return new PhoneAccountHandle(componentName, "CelyaVoxAccount");
	}

	private void showNotification(String title, String body) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(
			this,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		ensureChannel(manager);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(TextUtils.isEmpty(title) ? getString(R.string.app_name) : title)
				.setContentText(TextUtils.isEmpty(body) ? "" : body)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setCategory(NotificationCompat.CATEGORY_CALL)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setOngoing(true)
				.setTimeoutAfter(30000)
				.setAutoCancel(true)
				.setContentIntent(contentIntent)
				.setFullScreenIntent(contentIntent, true);

		manager.notify(NOTIFICATION_ID, builder.build());
	}

	private void ensureChannel(NotificationManager manager) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID,
					"Appels entrants (urgent)",
					NotificationManager.IMPORTANCE_HIGH
			);
			channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
			channel.enableVibration(true);
			channel.setBypassDnd(true);
			manager.createNotificationChannel(channel);
		}
	}
}
