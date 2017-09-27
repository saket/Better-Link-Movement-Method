package me.saket.bettermovementmethod.sample;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;
import android.widget.Toast;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import me.saket.bettermovementmethod.BetterLinkMovementMethod.OnLinkLongClickListener;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Add links to all TextViews.
    BetterLinkMovementMethod.linkify(Linkify.ALL, this)
        .setOnLinkClickListener(urlClickListener)
        .setOnLinkLongClickListener(longClickListener);

    TextView wayneTowerIntroView = findViewById(R.id.wayne_tower_intro);
    wayneTowerIntroView.setText(Html.fromHtml(getString(R.string.bettermovementmethod_dummy_text_long)));
    BetterLinkMovementMethod.linkifyHtml(wayneTowerIntroView)
        .setOnLinkClickListener(urlClickListener)
        .setOnLinkLongClickListener(longClickListener);
  }

  private final BetterLinkMovementMethod.OnLinkClickListener urlClickListener = (view, url) -> {
    if (isPhoneNumber(url)) {
      PhoneLinkPopupMenu phonePopupMenu = new PhoneLinkPopupMenu(this, view, url);
      phonePopupMenu.show();

    } else if (isEmailAddress(url)) {
      EmailLinkPopupMenu emailPopupMenu = new EmailLinkPopupMenu(this, view);
      emailPopupMenu.show();

    } else if (isMapAddress(url)) {
      MapLinkPopupMenu mapPopupMenu = new MapLinkPopupMenu(this, view);
      mapPopupMenu.show();

    } else {
      Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
    }

    return true;
  };

  private final OnLinkLongClickListener longClickListener = (textView, url) -> {
    Toast.makeText(this, "Long-click: " + url, Toast.LENGTH_SHORT).show();
    return true;
  };

  private boolean isPhoneNumber(String url) {
    return url.startsWith("tel:");
  }

  private boolean isEmailAddress(String url) {
    return url.contains("@");
  }

  private boolean isMapAddress(String url) {
    return url.contains("goo.gl/maps");
  }
}
