package me.saket.bettermovementmethod.sample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

public class PhoneLinkPopupMenu extends PopupMenu {

  public PhoneLinkPopupMenu(Context context, View anchor, String phoneUrl) {
    super(context, anchor);

    getMenuInflater().inflate(R.menu.phone_link_menu, getMenu());
    setOnMenuItemClickListener(item -> {
      switch (item.getItemId()) {
        case R.id.action_call:
          callNumber(context, phoneUrl);
          return true;

        case R.id.action_sms:
          onComposeNewSmsClick(context, phoneUrl);
          return true;

        case R.id.action_copy:
          onCopyAddressClick(context, phoneUrl);
          return true;

        case R.id.action_save:
          onAddToContactsClick(context, phoneUrl);
          return true;

        default:
          throw new UnsupportedOperationException();
      }
    });
  }

  private void callNumber(Context context, String phoneUrl) {
    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(dialIntent);
  }

  private void onComposeNewSmsClick(Context context, String phoneUrl) {
    Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneUrl));
    context.startActivity(smsIntent);
  }

  private void onCopyAddressClick(Context context, String phoneUrl) {
    final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    final ClipData clip = ClipData.newPlainText(context.getPackageName(), phoneUrl);
    //noinspection ConstantConditions
    clipboard.setPrimaryClip(clip);
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
  }

  private void onAddToContactsClick(Context context, String phoneUrl) {
    final Intent addContactIntent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
    addContactIntent.putExtra("finishActivityOnSaveCompleted", true);
    addContactIntent.setData(Uri.fromParts("tel", phoneUrl, null));
    addContactIntent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
    context.startActivity(addContactIntent);
  }
}
