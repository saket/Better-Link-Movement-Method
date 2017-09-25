package me.saket.bettermovementmethod.sample;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;
import android.widget.Toast;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    BetterLinkMovementMethod.OnLinkClickListener urlClickListener = (view, url) -> {
      if (isPhoneNumber(url)) {
        FloatingMenuPhone.show(this, view, url);

      } else if (isEmailAddress(url)) {
        EmailFloatingMenu.show(this, view, url);

      } else if (isMapAddress(url)) {
        MapFloatingMenu.show(this, view, url);

      } else {
        Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
      }

      return true;
    };
    BetterLinkMovementMethod.linkify(Linkify.ALL, this).setOnLinkClickListener(urlClickListener);

    TextView gothamTextView = (TextView) findViewById(android.R.id.text1);
    gothamTextView.setText(Html.fromHtml(getString(R.string.bettermovementmethod_dummy_text_long)));
    BetterLinkMovementMethod.linkifyHtml(gothamTextView).setOnLinkClickListener(urlClickListener);
  }

  private boolean isPhoneNumber(String url) {
    return url.endsWith(getString(R.string.bettermovementmethod_dummy_number));
  }

  private boolean isEmailAddress(String url) {
    return url.contains("@");
  }

  private boolean isMapAddress(String url) {
    return url.contains("goo.gl/maps");
  }

}
