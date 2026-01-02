package fr.celya.celyavox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushMessagingService extends FirebaseMessagingService {
	private static final String CHANNEL_ID = "Push";
	private static final int NOTIFICATION_ID = 1001;

	@Override
	public void onNewToken(@NonNull String token) {
		super.onNewToken(token);
		// TODO: send the new token to your backend so it can target this device.
	}

	@Override
	public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);

		// Build an intent that will launch (or bring to front) the main activity.
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(
			this,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		// Derive title/body from notification or data payloads.
		String title = null;
		String body = null;
		if (remoteMessage.getNotification() != null) {
			title = remoteMessage.getNotification().getTitle();
			body = remoteMessage.getNotification().getBody();
		}
		if (remoteMessage.getData() != null) {
			if (TextUtils.isEmpty(title)) {
				title = remoteMessage.getData().get("title");
			}
			if (TextUtils.isEmpty(body)) {
				body = remoteMessage.getData().get("body");
			}
		}

		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		ensureChannel(manager);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(TextUtils.isEmpty(title) ? getString(R.string.app_name) : title)
				.setContentText(TextUtils.isEmpty(body) ? "" : body)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setCategory(NotificationCompat.CATEGORY_ALARM)
				.setAutoCancel(true)
				.setContentIntent(contentIntent)
				.setFullScreenIntent(contentIntent, true);

		manager.notify(NOTIFICATION_ID, builder.build());
	}

	private void ensureChannel(NotificationManager manager) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID,
					"Push",
					NotificationManager.IMPORTANCE_HIGH
			);
			channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
			channel.enableVibration(true);
			channel.setBypassDnd(true);
			manager.createNotificationChannel(channel);
		}
	}
}
