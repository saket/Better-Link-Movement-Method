# BetterLinkMovementMethod

![Better-Link-Movement-Method](https://github.com/Saketme/Better-Link-Movement-Method/blob/master/EXAMPLE.gif)

When `android:autoLink="all"` or `Linkify.addLinks(textView, Linkify.ALL)` is used to add links to web URLs, phone-numbers, map addresses or email addresses in a TextView, Android uses a class known as `LinkMovementMethod` for highlighting links when they're focused and dispatching an Intent when they're clicked.

BetterLinkMovementMethod improves over `LinkMovementMethod`, by fixing some of its flaws:

* No support for custom click listeners. For eg., phone numbers always show up in the dialer when clicked and there's no way to change that.
* Incorrect calculation of touch areas for links, resulting in ghost touch areas ([video](http://saket.me/wp-content/uploads/2016/09/Incorrect-touch-areas.mp4)).
* Unreliable highlighting of links ([video](http://saket.me/wp-content/uploads/2016/09/Unreliable-highlighting.mp4)).

A detailed explanation of why (and when) you should use `BetterLinkMovementMethod` can be read on my blog: http://saket.me/better-url-handler-textview-android/

## Usage
`BetterLinkMovementMethod` is designed to be a drop-in replacement for `LinkMovementMethod`:
```gradle
dependencies {
  implementation 'me.saket:better-link-movement-method:2.2.0'
}
```

```kotlin
val textView = findViewById(...)
Linkify.addLinks(textView, Linkify.ALL)
textView.movementMethod = BetterLinkMovementMethod.getInstance()
```

Click listeners can be registered by creating a unique instance of `BetterLinkMovementMethod` for each `TextView`:

```kotlin
textView.movementMethod = BetterLinkMovementMethod.newInstance().apply {
  setOnLinkClickListener { textView, url ->
    // Handle click or return false to let the framework handle this link.
    true
  }
  setOnLinkLongClickListener { textView, url ->
    // Handle long-click or return false to let the framework handle this link.
    true
  }
}
```

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
