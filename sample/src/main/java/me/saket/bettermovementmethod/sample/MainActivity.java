package me.saket.bettermovementmethod.sample;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView1 = (TextView) findViewById(R.id.text1);

        BetterLinkMovementMethod
                .linkify(Linkify.PHONE_NUMBERS, textView1)
                .setOnLinkClickListener((view, url) -> {
                    FloatingMenu.show(this, view, url);
                    return true;
                });
    }

}
