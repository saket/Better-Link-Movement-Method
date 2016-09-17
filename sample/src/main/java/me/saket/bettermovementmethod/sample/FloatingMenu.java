package me.saket.bettermovementmethod.sample;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * Similar to Marshmallow's floating contextual menu.
 */
public class FloatingMenu extends PopupWindow implements View.OnClickListener {

    private final Context context;
    private final View anchorView;
    private final String phoneAddress;

    public FloatingMenu(Context context, View anchorView, String phoneAddress) {
        super(context);
        this.context = context;
        this.anchorView = anchorView;
        this.phoneAddress = phoneAddress;
    }

    public static void show(Context context, View anchorView, String phoneAddress) {
        FloatingMenu floatingMenu = new FloatingMenu(context, anchorView, phoneAddress);
        floatingMenu.show();
    }

    /**
     * Shows this menu popup anchored on the View set using {@link FloatingMenu#show(Context, View, String)}.
     */
    @SuppressLint("InflateParams")
    public void show() {
        final View menuLayout = LayoutInflater.from(context).inflate(R.layout.phone_number_floating_menu, null);
        registerButtonClickListeners((ViewGroup) menuLayout);
        setContentView(menuLayout);

        // Close on outside touch
        setTouchable(true);
        setFocusable(true);

        // Do not go beyond window bounds
        setClippingEnabled(true);

        // Set dimensions
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        // Show below the anchor View, centered to its dimensions
        final Point showPoint = determineShowPoint(menuLayout);
        showAtLocation(anchorView, Gravity.NO_GRAVITY, showPoint.x, showPoint.y);
    }

    private void registerButtonClickListeners(ViewGroup menuLayout) {
        // Find all buttons in the layout and register their click listeners.
        for (int i = 0; i < menuLayout.getChildCount(); i++) {
            final View view = menuLayout.getChildAt(i);
            if (view instanceof Button) {
                view.setOnClickListener(this);
            }
        }
    }

    private Point determineShowPoint(View menuLayout) {
        final int[] absLocation = new int[2];
        anchorView.getLocationInWindow(absLocation);

        menuLayout.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        final int menuWidth = menuLayout.getMeasuredWidth();

        int x = absLocation[0] + anchorView.getWidth() / 2 - menuWidth / 2;
        int y = absLocation[1] + anchorView.getHeight() + ((ViewGroup.MarginLayoutParams) anchorView.getLayoutParams()).bottomMargin;
        return new Point(x, y);
    }

// ======== CLICK LISTENERS ======== //

    @Override
    public void onClick(View menuButton) {
        switch (menuButton.getId()) {
            case R.id.btn_call:
                onCallAddressClick();
                break;

            case R.id.btn_compose_sms:
                onComposeNewSmsClick();
                break;

            case R.id.btn_copy:
                onCopyAddressClick();
                break;

            case R.id.btn_save:
                onAddToContactsClick();
                break;
        }
        dismiss();
    }

    private void onCallAddressClick() {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneAddress)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(dialIntent);
    }

    private void onComposeNewSmsClick() {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneAddress));
        context.startActivity(smsIntent);
    }

    private void onCopyAddressClick() {
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(context.getPackageName(), phoneAddress);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void onAddToContactsClick() {
        final Intent addContactIntent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
        addContactIntent.putExtra("finishActivityOnSaveCompleted", true);
        addContactIntent.setData(Uri.fromParts("tel", phoneAddress, null));
        addContactIntent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
        context.startActivity(addContactIntent);
    }

}
