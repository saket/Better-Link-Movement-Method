# BetterLinkMovementMethod

![Better-Link-Movement-Method](https://github.com/Saketme/Better-Link-Movement-Method/blob/master/EXAMPLE.gif)

When `android:autoLink="all"` or `Linkify.addLinks(textView, Linkify.ALL)` is used to add links to web URLs, phone-numbers, map addresses or email addresses in a TextView, Android uses a class known as `LinkMovementMethod` that handles highlighting links when they're focused and dispatching an Intent when they're clicked.

BetterLinkMovementMethod improves over `LinkMovementMethod`, by fixing its flaws:

* No support for custom URL click listeners. For eg., phone numbers always show up in the dialer when clicked and there's no way to manually handle the click.
* Incorrect calculation of touch areas for links, resulting in ghost touch areas ([Example video](http://saket.me/wp-content/uploads/2016/09/Incorrect-touch-areas.mp4))
* Unreliable highlighting of links ([Example video](http://saket.me/wp-content/uploads/2016/09/Unreliable-highlighting.mp4))

A detailed explanation of why (and when) you should use `BetterLinkMovementMethod` can be read on my blog: http://saket.me/better-url-handler-textview-android/

Feel free to give a shoutout on Twitter [@Saketme](twitter.com/saketme) if you're using this in your app.

## Download

Add this to your module's `build.gradle`:

```gradle
repositories {
    jcenter()
}

dependencies {
    compile 'me.saket:better-link-movement-method:1.0'
}
```

## Usage

BetterLinkMovementMethod can be used in the same way as you’d use a normal LinkMovementMethod.

```java
TextView textView = (TextView) findViewById(R.id.text1);
textView.setMovementMethod(BetterLinkMovementMethod.newInstance());
Linkify.addLinks(textView, Linkify.PHONE_NUMBERS);
```

However, the easiest way to get started is by using one of its linkify() methods:

```java
BetterLinkMovementMethod.linkify(int linkifyMask, Activity);
BetterLinkMovementMethod.linkify(int linkifyMask, ViewGroup);
BetterLinkMovementMethod.linkify(int linkifyMask, TextView...);

// Where linkifyMask can be one of Linkify.ALL, Linkify.PHONE_NUMBERS, 
// Linkify.MAP_ADDRESSES, Linkify.WEB_URLS and Linkify.EMAIL_ADDRESSES.
```

## Examples

Registering a BetterLinkMovementMethod on a TextView:

`BetterLinkMovementMethod.linkify(Linkify.ALL, textView);`

or on infinite TextViews:

`BetterLinkMovementMethod.linkify(Linkify.ALL, textView1, textView2, textView3, ...);`

Adding a click listener:

```java
BetterLinkMovementMethod method = BetterLinkMovementMethod.linkify(Linkify.ALL, this);
method.setOnLinkClickListener((textView, url) -> {
    // Do something with the URL and return true to indicate that this URL was handled.
    // Otherwise, return false to let the framework handle the URL.
    return true;
});
 
// Or the less verbose way
BetterLinkMovementMethod
        .linkify(Linkify.ALL, this)
        .setOnLinkClickListener((textView, url) -> {
            // Do something.
            return true;
        });
```

You can also choose to go the shorter route of registering BetterLinkMovementMethod on all TextViews in your Activity’s layout in one go:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
 
    BetterLinkMovementMethod.linkify(Linkify.ALL, this);
}
```

When using in a non-Activity context (e.g., Fragments), you can also pass a ViewGroup as the 2nd param:

```java
@Nullable
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.your_fragment, container, false);
 
    BetterLinkMovementMethod.linkify(Linkify.ALL, ((ViewGroup) view));
 
    return view;
}
```

## Contributions

If you think that the APIs or the implementation can be improved, please feel free to raise a pull-request. 

## License

This library is licensed under GNU GPL v3, which can be [read here](https://github.com/Saketme/Better-Link-Movement-Method/blob/master/LICENSE.md). It's longer than the Bible so you'd probably never read it.
