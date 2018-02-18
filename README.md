# BetterLinkMovementMethod

![Better-Link-Movement-Method](https://github.com/Saketme/Better-Link-Movement-Method/blob/master/EXAMPLE.gif)

When `android:autoLink="all"` or `Linkify.addLinks(textView, Linkify.ALL)` is used to add links to web URLs, phone-numbers, map addresses or email addresses in a TextView, Android uses a class known as `LinkMovementMethod` that handles highlighting links when they're focused and dispatching an Intent when they're clicked.

BetterLinkMovementMethod improves over `LinkMovementMethod`, by fixing its flaws:

* No support for custom URL click listeners. For eg., phone numbers always show up in the dialer when clicked and there's no way to manually handle the click.
* Incorrect calculation of touch areas for links, resulting in ghost touch areas ([Example video](http://saket.me/wp-content/uploads/2016/09/Incorrect-touch-areas.mp4))
* Unreliable highlighting of links ([Example video](http://saket.me/wp-content/uploads/2016/09/Unreliable-highlighting.mp4))

A detailed explanation of why (and when) you should use `BetterLinkMovementMethod` can be read on my blog: http://saket.me/better-url-handler-textview-android/

Feel free to give a shoutout on Twitter [@Saketme](https://twitter.com/saketme) if you're using this in your app.

## Download

Add this to your module's `build.gradle`:

```gradle
repositories {
    jcenter()
}

dependencies {
    implementation 'me.saket:better-link-movement-method:2.1.0'
}
```

## Sample

You can find sample APKs on the [releases page](https://github.com/Saketme/Better-Link-Movement-Method/releases) to see `BetterLinkMovementMethod` in action.

## Usage

BetterLinkMovementMethod is designed to be a drop-in replacement for LinkMovementMethod.

```java
TextView textView = (TextView) findViewById(R.id.text1);
textView.setMovementMethod(BetterLinkMovementMethod.newInstance());
Linkify.addLinks(textView, Linkify.PHONE_NUMBERS);
```

However, the easiest way to get started is by using one of its linkify() methods:

```java
BetterLinkMovementMethod.linkify(int linkifyMask, TextView...);
BetterLinkMovementMethod.linkify(int linkifyMask, ViewGroup);
BetterLinkMovementMethod.linkify(int linkifyMask, Activity);

// Where linkifyMask can be one of Linkify.ALL, Linkify.PHONE_NUMBERS,
// Linkify.MAP_ADDRESSES, Linkify.WEB_URLS and Linkify.EMAIL_ADDRESSES.
```

## Examples

```java
BetterLinkMovementMethod
    .linkify(Linkify.ALL, textView)
    .setOnLinkClickListener((textView, url) -> {
      // Handle clicks.
      return true;
    })
    .setOnLinkLongClickListener((textView, url) -> {
      // Handle long-clicks.
      return true;
    });
```

You can also choose to go the shorter route of registering BetterLinkMovementMethod on all TextViews in your Activity’s layout in one go:

```java
@Override
void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_main);

  BetterLinkMovementMethod.linkify(Linkify.ALL, this);
}
```

When using in a non-Activity context (e.g., Fragments), you can also pass a ViewGroup as the 2nd param:

```java
@Nullable
@Override
View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  View view = inflater.inflate(R.layout.your_fragment, container, false);

  BetterLinkMovementMethod.linkify(Linkify.ALL, ((ViewGroup) view));

  return view;
}
```

## Contributions

If you think that the APIs or the implementation can be improved, please feel free to raise a pull-request.

## License

```
Copyright 2018 Saket Narayan.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
