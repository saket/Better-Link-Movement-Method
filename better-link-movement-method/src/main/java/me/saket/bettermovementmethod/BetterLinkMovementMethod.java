package me.saket.bettermovementmethod;

import android.app.Activity;
import android.graphics.RectF;
import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

/**
 * Handles URL clicks on TextViews. Unlike the default implementation, this:
 * <p>
 * <ul>
 * <li>Reliably applies a highlight color on links when they're touched.</li>
 * <li>Let's you handle single and long clicks on URLs</li>
 * <li>Correctly identifies focused URLs (Unlike the default implementation where a click is registered even if it's
 * made outside of the URL's bounds if there is no more text in that direction.)</li>
 * </ul>
 */
public class BetterLinkMovementMethod extends LinkMovementMethod {

    private static BetterLinkMovementMethod singleInstance;
    private static final int LINKIFY_NONE = -2;

    private OnLinkClickListener onLinkClickListener;
    private OnLinkLongClickListener onLinkLongClickListener;
    private final RectF touchedLineBounds = new RectF();
    private boolean isUrlHighlighted;
    private ClickableSpan clickableSpanUnderTouchOnActionDown;
    private int activeTextViewHashcode;
    private LongPressTimer ongoingLongPressTimer;
    private boolean wasLongPressRegistered;

    private static final int CLICK = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;

    private static final Object FROM_BELOW = new NoCopySpan.Concrete();

    public interface OnLinkClickListener {
        /**
         * @param textView The TextView on which a click was registered.
         * @param url      The clicked URL.
         * @return True if this click was handled. False to let Android handle the URL.
         */
        boolean onClick(TextView textView, String url);
    }

    public interface OnLinkLongClickListener {
        /**
         * @param textView The TextView on which a long-click was registered.
         * @param url      The long-clicked URL.
         * @return True if this long-click was handled. False to let Android handle the URL (as a short-click).
         */
        boolean onLongClick(TextView textView, String url);
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public static BetterLinkMovementMethod linkifyHtml(Activity activity) {
        return linkify(LINKIFY_NONE, activity);
    }

    /**
     * Get a static instance of BetterLinkMovementMethod. Do note that registering a click listener on the returned
     * instance is not supported because it will potentially be shared on multiple TextViews.
     */
    @SuppressWarnings("unused")
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
    public BetterLinkMovementMethod setOnLinkClickListener(OnLinkClickListener clickListener) {
        if (this == singleInstance) {
            throw new UnsupportedOperationException("Setting a click listener on the instance returned by getInstance() is not supported to avoid memory " +
                    "leaks. Please use newInstance() or any of the linkify() methods instead.");
        }

        this.onLinkClickListener = clickListener;
        return this;
    }

    /**
     * Set a listener that will get called whenever any link is clicked on the TextView.
     */
    public BetterLinkMovementMethod setOnLinkLongClickListener(OnLinkLongClickListener longClickListener) {
        if (this == singleInstance) {
            throw new UnsupportedOperationException("Setting a long-click listener on the instance returned by getInstance() is not supported to avoid " +
                    "memory leaks. Please use newInstance() or any of the linkify() methods instead.");
        }

        this.onLinkLongClickListener = longClickListener;
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
    public boolean canSelectArbitrarily() {
        return true;
    }

    @Override
    protected boolean handleMovementKey(TextView widget, Spannable buffer, int keyCode, int movementMetaState, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (KeyEvent.metaStateHasNoModifiers(movementMetaState)) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getRepeatCount() == 0 && action(CLICK, widget, buffer)) {
                        return true;
                    }
                }
                break;
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event);
    }

    @Override
    protected boolean up(TextView widget, Spannable buffer) {
        if (action(UP, widget, buffer)) {
            return true;
        }

        return super.up(widget, buffer);
    }

    @Override
    protected boolean down(TextView widget, Spannable buffer) {
        if (action(DOWN, widget, buffer)) {
            return true;
        }

        return super.down(widget, buffer);
    }

    @Override
    protected boolean left(TextView widget, Spannable buffer) {
        if (action(UP, widget, buffer)) {
            return true;
        }

        return super.left(widget, buffer);
    }

    @Override
    protected boolean right(TextView widget, Spannable buffer) {
        if (action(DOWN, widget, buffer)) {
            return true;
        }

        return super.right(widget, buffer);
    }

    private boolean action(int what, TextView widget, Spannable buffer) {
        if (activeTextViewHashcode != widget.hashCode()) {
            // Bug workaround: TextView stops calling onTouchEvent() once any URL is highlighted.
            // A hacky solution is to reset any "autoLink" property set in XML. But we also want
            // to do this once per TextView.
            activeTextViewHashcode = widget.hashCode();
            widget.setAutoLinkMask(0);
        }

        Layout layout = widget.getLayout();

        int padding = widget.getTotalPaddingTop() + widget.getTotalPaddingBottom();
        int areatop = widget.getScrollY();
        int areabot = areatop + widget.getHeight() - padding;

        int linetop = layout.getLineForVertical(areatop);
        int linebot = layout.getLineForVertical(areabot);

        int first = layout.getLineStart(linetop);
        int last = layout.getLineEnd(linebot);

        ClickableSpan[] candidates = buffer.getSpans(first, last, ClickableSpan.class);

        int a = Selection.getSelectionStart(buffer);
        int b = Selection.getSelectionEnd(buffer);

        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);

        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                selStart = selEnd = buffer.length();
            }
        }

        if (selStart > last)
            selStart = selEnd = Integer.MAX_VALUE;
        if (selEnd < first)
            selStart = selEnd = -1;

        int beststart, bestend;
        ClickableSpan[] link;

        switch (what) {
            case CLICK:
                if (selStart == selEnd) {
                    return false;
                }

                link = buffer.getSpans(selStart, selEnd, ClickableSpan.class);

                if (link.length != 1)
                    return false;

                dispatchUrlClick(widget, link[0]);
                return true;

            case UP:
                beststart = -1;
                bestend = -1;

                for (ClickableSpan span : candidates) {
                    int end = buffer.getSpanEnd(span);

                    if (end < selEnd || selStart == selEnd) {
                        if (end > bestend) {
                            beststart = buffer.getSpanStart(span);
                            bestend = end;
                        }
                    }
                }

                if (beststart >= 0) {
                    Selection.setSelection(buffer, bestend, beststart);
                    return true;
                }

                break;

            case DOWN:
                beststart = Integer.MAX_VALUE;
                bestend = Integer.MAX_VALUE;

                for (ClickableSpan span : candidates) {
                    int start = buffer.getSpanStart(span);

                    if (start > selStart || selStart == selEnd) {
                        if (start < beststart) {
                            beststart = start;
                            bestend = buffer.getSpanEnd(span);
                        }
                    }
                }

                if (bestend < Integer.MAX_VALUE) {
                    Selection.setSelection(buffer, beststart, bestend);
                    return true;
                }

                break;
        }

        return false;
    }


    @Override
    public void initialize(TextView widget, Spannable text) {
        Selection.removeSelection(text);
        text.removeSpan(FROM_BELOW);
    }

    @Override
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        Selection.removeSelection(text);

        if ((dir & View.FOCUS_BACKWARD) != 0) {
            text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT);
        } else {
            text.removeSpan(FROM_BELOW);
        }
    }

    @Override
    public boolean onTouchEvent(final TextView textView, Spannable text, MotionEvent event) {
        if (activeTextViewHashcode != textView.hashCode()) {
            // Bug workaround: TextView stops calling onTouchEvent() once any URL is highlighted.
            // A hacky solution is to reset any "autoLink" property set in XML. But we also want
            // to do this once per TextView.
            activeTextViewHashcode = textView.hashCode();
            textView.setAutoLinkMask(0);
        }

        final ClickableSpan clickableSpanUnderTouch = findClickableSpanUnderTouch(textView, text, event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clickableSpanUnderTouchOnActionDown = clickableSpanUnderTouch;
        }
        final boolean touchStartedOverAClickableSpan = clickableSpanUnderTouchOnActionDown != null;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (clickableSpanUnderTouch != null) {
                    highlightUrl(textView, clickableSpanUnderTouch, text);
                }

                if (touchStartedOverAClickableSpan && onLinkLongClickListener != null) {
                    LongPressTimer.OnTimerReachedListener longClickListener = () -> {
                        wasLongPressRegistered = true;
                        textView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        removeUrlHighlightColor(textView);
                        dispatchUrlLongClick(textView, clickableSpanUnderTouch);
                    };
                    startTimerForRegisteringLongClick(textView, longClickListener);
                }
                return touchStartedOverAClickableSpan;

            case MotionEvent.ACTION_UP:
                // Register a click only if the touch started and ended on the same URL.
                if (!wasLongPressRegistered && touchStartedOverAClickableSpan && clickableSpanUnderTouch == clickableSpanUnderTouchOnActionDown) {
                    dispatchUrlClick(textView, clickableSpanUnderTouch);
                }
                cleanupOnTouchUp(textView);

                // Consume this event even if we could not find any spans to avoid letting Android handle this event.
                // Android's TextView implementation has a bug where links get clicked even when there is no more text
                // next to the link and the touch lies outside its bounds in the same direction.
                return touchStartedOverAClickableSpan;

            case MotionEvent.ACTION_CANCEL:
                cleanupOnTouchUp(textView);
                return false;

            case MotionEvent.ACTION_MOVE:
                // Stop listening for a long-press as soon as the user wanders off to unknown lands.
                if (clickableSpanUnderTouch != clickableSpanUnderTouchOnActionDown) {
                    removeLongPressCallback(textView);
                }

                if (!wasLongPressRegistered) {
                    // Toggle highlight.
                    if (clickableSpanUnderTouch != null) {
                        highlightUrl(textView, clickableSpanUnderTouch, text);
                    } else {
                        removeUrlHighlightColor(textView);
                    }
                }

                return touchStartedOverAClickableSpan;

            default:
                return false;
        }
    }

    private void cleanupOnTouchUp(TextView textView) {
        wasLongPressRegistered = false;
        clickableSpanUnderTouchOnActionDown = null;
        removeUrlHighlightColor(textView);
        removeLongPressCallback(textView);
    }

    /**
     * Determines the touched location inside the TextView's text and returns the ClickableSpan found under it (if any).
     *
     * @return The touched ClickableSpan or null.
     */
    protected ClickableSpan findClickableSpanUnderTouch(TextView textView, Spannable text, MotionEvent event) {
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
            // Find a ClickableSpan that lies under the touched area.
            final Object[] spans = text.getSpans(touchOffset, touchOffset, ClickableSpan.class);
            for (final Object span : spans) {
                if (span instanceof ClickableSpan) {
                    return (ClickableSpan) span;
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
     * Adds a background color span at <var>clickableSpan</var>'s location.
     */
    protected void highlightUrl(TextView textView, ClickableSpan clickableSpan, Spannable text) {
        if (isUrlHighlighted) {
            return;
        }
        isUrlHighlighted = true;

        int spanStart = text.getSpanStart(clickableSpan);
        int spanEnd = text.getSpanEnd(clickableSpan);
        BackgroundColorSpan highlightSpan = new BackgroundColorSpan(textView.getHighlightColor());
        text.setSpan(highlightSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        textView.setTag(R.id.bettermovementmethod_highlight_background_span, highlightSpan);
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

        Spannable text = (Spannable) textView.getText();
        BackgroundColorSpan highlightSpan = (BackgroundColorSpan) textView.getTag(R.id.bettermovementmethod_highlight_background_span);
        text.removeSpan(highlightSpan);

        Selection.removeSelection(text);
    }

    protected void startTimerForRegisteringLongClick(TextView textView, LongPressTimer.OnTimerReachedListener longClickListener) {
        ongoingLongPressTimer = new LongPressTimer();
        ongoingLongPressTimer.setOnTimerReachedListener(longClickListener);
        textView.postDelayed(ongoingLongPressTimer, ViewConfiguration.getLongPressTimeout());
    }

    /**
     * Remove the long-press detection timer.
     */
    protected void removeLongPressCallback(TextView textView) {
        if (ongoingLongPressTimer != null) {
            textView.removeCallbacks(ongoingLongPressTimer);
            ongoingLongPressTimer = null;
        }
    }

    protected void dispatchUrlClick(TextView textView, ClickableSpan clickableSpan) {
        ClickableSpanWithText clickableSpanWithText = ClickableSpanWithText.ofSpan(textView, clickableSpan);
        boolean handled = onLinkClickListener != null && onLinkClickListener.onClick(textView, clickableSpanWithText.text());

        if (!handled) {
            // Let Android handle this click.
            clickableSpanWithText.span().onClick(textView);
        }
    }

    protected void dispatchUrlLongClick(TextView textView, ClickableSpan clickableSpan) {
        ClickableSpanWithText clickableSpanWithText = ClickableSpanWithText.ofSpan(textView, clickableSpan);
        boolean handled = onLinkLongClickListener != null && onLinkLongClickListener.onLongClick(textView, clickableSpanWithText.text());

        if (!handled) {
            // Let Android handle this long click as a short-click.
            clickableSpanWithText.span().onClick(textView);
        }
    }

    protected static final class LongPressTimer implements Runnable {
        private OnTimerReachedListener onTimerReachedListener;

        protected interface OnTimerReachedListener {
            void onTimerReached();
        }

        @Override
        public void run() {
            onTimerReachedListener.onTimerReached();
        }

        public void setOnTimerReachedListener(OnTimerReachedListener listener) {
            onTimerReachedListener = listener;
        }
    }

    /**
     * A wrapper to support all {@link ClickableSpan}s that may or may not provide URLs.
     */
    protected static class ClickableSpanWithText {
        private final ClickableSpan span;
        private final String text;

        protected static ClickableSpanWithText ofSpan(TextView textView, ClickableSpan span) {
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

        protected ClickableSpanWithText(ClickableSpan span, String text) {
            this.span = span;
            this.text = text;
        }

        protected ClickableSpan span() {
            return span;
        }

        protected String text() {
            return text;
        }
    }
}
