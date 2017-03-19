package me.saket.bettermovementmethod;

import android.app.Activity;
import android.graphics.RectF;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

/**
 * Handles URL clicks on TextViews. Unlike the default implementation, this:
 * <p>
 * <ul>
 * <li>Reliably applies a highlight color on links when they're touched.</li>
 * <li>Let's you handle URL clicks</li>
 * <li>Correctly identifies touched URLs (Unlike the default implementation where a click is registered even if it's
 * made outside of the URL's bounds if there is no more text in that direction.)</li>
 * </ul>
 */
public class BetterLinkMovementMethod extends LinkMovementMethod {

    private static final Class SPAN_CLASS = ClickableSpan.class;
    private static BetterLinkMovementMethod singleInstance;
    private static final int LINKIFY_NONE = -2;

    private OnLinkClickListener onLinkClickListener;
    private final RectF touchedLineBounds = new RectF();
    private boolean isUrlHighlighted;
    private boolean touchStartedOverLink;
    private int activeTextViewHashcode;

    public interface OnLinkClickListener {
        /**
         * @param textView The TextView on which the URL was clicked.
         * @param url      The clicked URL.
         * @return True if this click was handled. False to let Android handle the URL.
         */
        boolean onClick(TextView textView, String url);
    }

    /**
     * Return a new instance of BetterLinkMovementMethod.
     */
    public static BetterLinkMovementMethod newInstance() {
        return new BetterLinkMovementMethod();
    }

    /**
     * @param linkifyMask One of {@link Linkify#ALL}, {@link Linkify#PHONE_NUMBERS}, {@link Linkify#MAP_ADDRESSES},
     *                    {@link Linkify#WEB_URLS} and {@link Linkify#EMAIL_ADDRESSES}.
     * @param textViews   The TextViews on which a {@link BetterLinkMovementMethod} should be registered.
     * @return The registered {@link BetterLinkMovementMethod} on the TextViews.
     */
    public static BetterLinkMovementMethod linkify(int linkifyMask, TextView... textViews) {
        BetterLinkMovementMethod movementMethod = newInstance();
        for (TextView textView : textViews) {
            addLinks(linkifyMask, movementMethod, textView);
        }
        return movementMethod;
    }

    /**
     * Like {@link #linkify(int, TextView...)}, but can be used for TextViews with HTML links.
     *
     * @param textViews The TextViews on which a {@link BetterLinkMovementMethod} should be registered.
     * @return The registered {@link BetterLinkMovementMethod} on the TextViews.
     */
    public static BetterLinkMovementMethod linkifyHtml(TextView... textViews) {
        return linkify(LINKIFY_NONE, textViews);
    }

    /**
     * Recursively register a {@link BetterLinkMovementMethod} on every TextView inside a layout.
     *
     * @param linkifyMask One of {@link Linkify#ALL}, {@link Linkify#PHONE_NUMBERS}, {@link Linkify#MAP_ADDRESSES},
     *                    {@link Linkify#WEB_URLS} and {@link Linkify#EMAIL_ADDRESSES}.
     * @return The registered {@link BetterLinkMovementMethod} on the TextViews.
     */
    public static BetterLinkMovementMethod linkify(int linkifyMask, ViewGroup viewGroup) {
        BetterLinkMovementMethod movementMethod = newInstance();
        rAddLinks(linkifyMask, viewGroup, movementMethod);
        return movementMethod;
    }

    /**
     * Like {@link #linkify(int, TextView...)}, but can be used for TextViews with HTML links.
     *
     * @return The registered {@link BetterLinkMovementMethod} on the TextViews.
     */
    public static BetterLinkMovementMethod linkifyHtml(ViewGroup viewGroup) {
        return linkify(LINKIFY_NONE, viewGroup);
    }

    /**
     * Recursively register a {@link BetterLinkMovementMethod} on every TextView inside a layout.
     *
     * @param linkifyMask One of {@link Linkify#ALL}, {@link Linkify#PHONE_NUMBERS}, {@link Linkify#MAP_ADDRESSES},
     *                    {@link Linkify#WEB_URLS} and {@link Linkify#EMAIL_ADDRESSES}.
     * @return The registered {@link BetterLinkMovementMethod} on the TextViews.
     */
    public static BetterLinkMovementMethod linkify(int linkifyMask, Activity activity) {
        // Find the layout passed to setContentView().
        ViewGroup activityLayout = ((ViewGroup) ((ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT)).getChildAt(0));

        BetterLinkMovementMethod movementMethod = newInstance();
        rAddLinks(linkifyMask, activityLayout, movementMethod);
        return movementMethod;
    }

    /**
     * Like {@link #linkify(int, TextView...)}, but can be used for TextViews with HTML links.
     *
     * @return The registered {@link BetterLinkMovementMethod} on the TextViews.
     */
    public static BetterLinkMovementMethod linkifyHtml(Activity activity) {
        return linkify(LINKIFY_NONE, activity);
    }

    /**
     * Get a static instance of BetterLinkMovementMethod. Do note that registering a click listener on the returned
     * instance is not supported because it will potentially be shared on multiple TextViews.
     */
    public static BetterLinkMovementMethod getInstance() {
        if (singleInstance == null) {
            singleInstance = new BetterLinkMovementMethod();
        }
        return singleInstance;
    }

    protected BetterLinkMovementMethod() {
    }

    /**
     * Set a listener that will get called whenever any link is clicked on the TextView.
     */
    public BetterLinkMovementMethod setOnLinkClickListener(OnLinkClickListener onLinkClickListener) {
        if (this == singleInstance) {
            throw new UnsupportedOperationException("Setting a click listener on the instance returned by getInstance() is not supported. " +
                    "Please use newInstance() or any of the linkify() methods instead.");
        }

        this.onLinkClickListener = onLinkClickListener;
        return this;
    }

// ======== PUBLIC APIs END ======== //

    private static void rAddLinks(int linkifyMask, ViewGroup viewGroup, BetterLinkMovementMethod movementMethod) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof ViewGroup) {
                // Recursively find child TextViews.
                rAddLinks(linkifyMask, ((ViewGroup) child), movementMethod);

            } else if (child instanceof TextView) {
                TextView textView = (TextView) child;
                addLinks(linkifyMask, movementMethod, textView);
            }
        }
    }

    private static void addLinks(int linkifyMask, BetterLinkMovementMethod movementMethod, TextView textView) {
        textView.setMovementMethod(movementMethod);
        if (linkifyMask != LINKIFY_NONE) {
            Linkify.addLinks(textView, linkifyMask);
        }
    }

    @Override
    public boolean onTouchEvent(TextView view, Spannable text, MotionEvent event) {
        if (activeTextViewHashcode != view.hashCode()) {
            // Bug workaround: TextView stops calling onTouchEvent() once any URL is highlighted.
            // A hacky solution is to reset any "autoLink" property set in XML. But we also want
            // to do this once per TextView.
            activeTextViewHashcode = view.hashCode();
            view.setAutoLinkMask(0);
        }

        final ClickableSpanWithText touchedClickableSpan = findClickableSpanUnderTouch(view, text, event);

        // Toggle highlight
        if (touchedClickableSpan != null) {
            highlightUrl(view, touchedClickableSpan, text);
        } else {
            removeUrlHighlightColor(view);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartedOverLink = touchedClickableSpan != null;
                return touchStartedOverLink;

            case MotionEvent.ACTION_UP:
                // Register a click only if the touch started on an URL. That is, the touch did not start
                // elsewhere and ended up on an URL.
                if (touchedClickableSpan != null && touchStartedOverLink) {
                    dispatchUrlClick(view, touchedClickableSpan);
                    removeUrlHighlightColor(view);
                }
                boolean didTouchStartOverLink = touchStartedOverLink;
                touchStartedOverLink = false;

                // Consume this event even if we could not find any spans. Android's TextView implementation
                // has a bug where links get clicked even when there is no more text next to the link and the
                // touch lies outside its bounds in the same direction.
                return didTouchStartOverLink;

            case MotionEvent.ACTION_MOVE:
                return touchStartedOverLink;

            default:
                return false;
        }
    }

    /**
     * Determines the touched location inside the TextView's text and returns the ClickableSpan found under it (if any).
     *
     * @return The touched ClickableSpan or null.
     */
    protected ClickableSpanWithText findClickableSpanUnderTouch(TextView textView, Spannable text, MotionEvent event) {
        // So we need to find the location in text where touch was made, regardless of whether the TextView
        // has scrollable text. That is, not the entire text is currently visible.
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        // Ignore padding.
        touchX -= textView.getTotalPaddingLeft();
        touchY -= textView.getTotalPaddingTop();

        // Account for scrollable text.
        touchX += textView.getScrollX();
        touchY += textView.getScrollY();

        final Layout layout = textView.getLayout();
        final int touchedLine = layout.getLineForVertical(touchY);
        final int touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX);

        touchedLineBounds.left = layout.getLineLeft(touchedLine);
        touchedLineBounds.top = layout.getLineTop(touchedLine);
        touchedLineBounds.right = layout.getLineWidth(touchedLine) + touchedLineBounds.left;
        touchedLineBounds.bottom = layout.getLineBottom(touchedLine);

        if (touchedLineBounds.contains(touchX, touchY)) {
            // Find any ClickableSpan that lies under the touched area
            final Object[] spans = text.getSpans(touchOffset, touchOffset, SPAN_CLASS);
            for (final Object span : spans) {
                if (span instanceof ClickableSpan) {
                    return ClickableSpanWithText.ofSpan(textView, (ClickableSpan) span);
                }
            }
            // No ClickableSpan found under the touched location.
            return null;

        } else {
            // Touch lies outside the line's horizontal bounds where no spans should exist.
            return null;
        }
    }

    /**
     * Adds a highlight background color span to the TextView.
     */
    protected void highlightUrl(TextView textView, ClickableSpanWithText spanWithText, Spannable text) {
        if (isUrlHighlighted) {
            return;
        }
        isUrlHighlighted = true;

        final int spanStart = text.getSpanStart(spanWithText.span());
        final int spanEnd = text.getSpanEnd(spanWithText.span());
        text.setSpan(new BackgroundColorSpan(textView.getHighlightColor()), spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(text);

        Selection.setSelection(text, spanStart, spanEnd);
    }

    /**
     * Removes the highlight color under the Url.
     */
    protected void removeUrlHighlightColor(TextView textView) {
        if (!isUrlHighlighted) {
            return;
        }
        isUrlHighlighted = false;

        final Spannable text = (Spannable) textView.getText();

        BackgroundColorSpan[] highlightSpans = text.getSpans(0, text.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan highlightSpan : highlightSpans) {
            text.removeSpan(highlightSpan);
        }

        textView.setText(text);

        Selection.removeSelection(text);
    }

    protected void dispatchUrlClick(TextView textView, ClickableSpanWithText spanWithText) {
        final String spanUrl = spanWithText.text();
        boolean handled = onLinkClickListener != null && onLinkClickListener.onClick(textView, spanUrl);
        if (!handled) {
            // Let Android handle this click.
            spanWithText.span().onClick(textView);
        }
    }

    /**
     * A wrapper with a clickable span and its text.
     */
    protected static class ClickableSpanWithText {

        private ClickableSpan span;
        private String text;

        static ClickableSpanWithText ofSpan(TextView textView, ClickableSpan span) {
            Spanned s = (Spanned) textView.getText();
            String text;
            if (span instanceof URLSpan) {
                text = ((URLSpan) span).getURL();
            } else {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                text = s.subSequence(start, end).toString();
            }
            return new ClickableSpanWithText(span, text);
        }

        private ClickableSpanWithText(ClickableSpan span, String text) {
            this.span = span;
            this.text = text;
        }

        ClickableSpan span() {
            return span;
        }

        String text() {
            return text;
        }

    }

}
