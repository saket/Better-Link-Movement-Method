package me.saket.bettermovementmethod.sample;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;

public class MapLinkPopupMenu extends PopupMenu {

  public MapLinkPopupMenu(Context context, View anchor) {
    super(context, anchor);
    getMenuInflater().inflate(R.menu.map_link_menu, getMenu());
  }
}
