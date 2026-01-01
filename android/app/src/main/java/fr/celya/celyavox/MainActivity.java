package fr.celya.celyavox;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.getcapacitor.BridgeActivity;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends BridgeActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Ensure IME is shown and layout resizes properly on newer SDKs
		getWindow().setSoftInputMode(
			WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
		);
		enableImmersiveMode();
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
}
