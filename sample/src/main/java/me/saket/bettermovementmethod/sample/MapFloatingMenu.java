package me.saket.bettermovementmethod.sample;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class MapFloatingMenu extends BaseFloatingMenu {

  public static void show(Context context, View anchorView, String mapAddress) {
    new MapFloatingMenu(context, anchorView, mapAddress).show();
  }

  private MapFloatingMenu(Context context, View anchorView, String phoneAddress) {
    super(context, anchorView, phoneAddress);
  }

  @Override
  protected int getMenuLayout() {
    return R.layout.floating_menu_map;
  }

  @Override
  protected void handleMenuClick(int menuItemId) {
    Toast.makeText(getContext(), "Just kidding", Toast.LENGTH_SHORT).show();
  }

}
