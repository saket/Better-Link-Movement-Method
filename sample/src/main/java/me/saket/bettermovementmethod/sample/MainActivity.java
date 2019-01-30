package me.saket.bettermovementmethod.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import me.saket.bettermovementmethod.BetterLinkMovementMethod.OnLinkLongClickListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setSupportActionBar(findViewById(R.id.toolbar));
        // Add links to all TextViews.
        BetterLinkMovementMethod.linkify(Linkify.ALL, this)
                .setOnLinkClickListener(urlClickListener)
                .setOnLinkLongClickListener(longClickListener);

        TextView wayneTowerIntroView = findViewById(R.id.wayne_tower_intro);
        wayneTowerIntroView.setText(Html.fromHtml(getString(R.string.bettermovementmethod_dummy_text_long)));
        BetterLinkMovementMethod.linkifyHtml(wayneTowerIntroView)
                .setOnLinkClickListener(urlClickListener)
                .setOnLinkLongClickListener(longClickListener);

        // https://github.com/Saketme/Better-Link-Movement-Method/issues/8
        Spannable introductionText = (Spannable) wayneTowerIntroView.getText();
        int start = introductionText.toString().indexOf("Wayne tower");
        int end = start + "Wayne tower".length();
        introductionText.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.wayneTower)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        String url = null;
        switch (item.getItemId()) {
            case R.id.menuItem_all_my_apps:
                url = "https://play.google.com/store/apps/developer?id=AndroidDeveloperLB";
                break;
            case R.id.menuItem_all_my_repositories:
                url = "https://github.com/AndroidDeveloperLB";
                break;
            case R.id.menuItem_current_repository_website:
                url = "https://github.com/AndroidDeveloperLB/Better-Link-Movement-Method";
                break;
        }
        if (url == null)
            return true;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
        return true;
    }
}
