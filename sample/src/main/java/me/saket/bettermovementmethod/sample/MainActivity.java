package me.saket.bettermovementmethod.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.widget.TextView;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView1 = (TextView) findViewById(R.id.text2);
        Linkify.addLinks(textView1, Linkify.ALL);
        BetterLinkMovementMethod method = BetterLinkMovementMethod.newInstance();
        method.setOnLinkClickListener((view, url) -> {
            FloatingMenu.show(this, view, url);
            return true;
        });
        textView1.setMovementMethod(method);

        TextView textView2 = (TextView) findViewById(R.id.text1);
        Linkify.addLinks(textView2, Linkify.ALL);
    }

}
