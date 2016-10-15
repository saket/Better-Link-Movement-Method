package me.saket.bettermovementmethod.sample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

public class EmailFloatingMenu extends BaseFloatingMenu {

    public static void show(Context context, View anchorView, String emailAddress) {
        new EmailFloatingMenu(context, anchorView, emailAddress).show();
    }

    private EmailFloatingMenu(Context context, View anchorView, String phoneAddress) {
        super(context, anchorView, phoneAddress);
    }

    @Override
    protected int getMenuLayout() {
        return R.layout.floating_menu_email;
    }

    @Override
    protected void handleMenuClick(int menuItemId) {
        switch (menuItemId) {
            case R.id.btn_email:
                onEmailAddressClick();
                break;

            case R.id.btn_copy:
                onCopyAddressClick();
                break;
        }
    }

    private void onEmailAddressClick() {
        Intent dialIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(getURL()));
        getContext().startActivity(dialIntent);
    }

    private void onCopyAddressClick() {
        final ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(getContext().getPackageName(), getURL());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

}
