package me.saket.bettermovementmethod.sample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

public class FloatingMenuPhone extends BaseFloatingMenu {

  public static void show(Context context, View anchorView, String phoneAddress) {
    new FloatingMenuPhone(context, anchorView, phoneAddress).show();
  }

  private FloatingMenuPhone(Context context, View anchorView, String phoneAddress) {
    super(context, anchorView, phoneAddress);
  }

  @Override
  protected int getMenuLayout() {
    return R.layout.floating_menu_phone_number;
  }

  @Override
  protected void handleMenuClick(int menuItemId) {
    switch (menuItemId) {
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
  }

  private void onCallAddressClick() {
    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(getURL())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getContext().startActivity(dialIntent);
  }

  private void onComposeNewSmsClick() {
    Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + getURL()));
    getContext().startActivity(smsIntent);
  }

  private void onCopyAddressClick() {
    final ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    final ClipData clip = ClipData.newPlainText(getContext().getPackageName(), getURL());
    clipboard.setPrimaryClip(clip);
    Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
  }

  private void onAddToContactsClick() {
    final Intent addContactIntent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
    addContactIntent.putExtra("finishActivityOnSaveCompleted", true);
    addContactIntent.setData(Uri.fromParts("tel", getURL(), null));
    addContactIntent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
    getContext().startActivity(addContactIntent);
  }

}
