package me.saket.bettermovementmethod.sample;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;

public class EmailLinkPopupMenu extends PopupMenu {

  public EmailLinkPopupMenu(Context context, View anchor) {
    super(context, anchor);
    getMenuInflater().inflate(R.menu.email_link_menu, getMenu());
  }
}
