package com.zgw.qgb.helper;

import android.text.SpannableStringBuilder;

import java.util.ArrayDeque;
import java.util.Deque;

import static android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE;

/** A more elegant {@link SpannableStringBuilder} wrapper */
public class Truss {
  private final SpannableStringBuilder builder;
  private final Deque<Span> stack;

  public Truss() {
    builder = new SpannableStringBuilder();
    stack = new ArrayDeque<>();
  }

  public Truss append(String string) {
    builder.append(string);
    return this;
  }

  public Truss append(CharSequence charSequence) {
    builder.append(charSequence);
    return this;
  }

  public Truss append(char c) {
    builder.append(c);
    return this;
  }

  public Truss append(int number) {
    builder.append(String.valueOf(number));
    return this;
  }

  /** Starts {@code span} at the current position in the builder. */
  public Truss pushSpan(Object... spans) {
    stack.addLast(new Span(builder.length(), spans));

    return this;
  }

  /** End the most recently pushed span at the current position in the builder. */
  public Truss popSpan() {
    Span span = stack.removeLast();
    if (null != span) setSpan(span);

    return this;
  }

  /** End all pushed span at the current position in the builder. */
  public Truss popSpans() {
    Span span = stack.pollLast();
    while (null != span) {
      setSpan(span);
      span = stack.pollLast();
    }
    return this;
  }

  /**  pushed span in the builder. */
  private void setSpan(Span span) {
    for (Object spanObject : span.spans) {
      builder.setSpan(spanObject, span.start, builder.length(), SPAN_INCLUSIVE_EXCLUSIVE);
    }
  }

  /** Create the final {@link CharSequence}, popping any remaining spans. */
  public CharSequence build() {
    while (!stack.isEmpty()) {
      popSpans();
    }
    return builder;
  }

  private static final class Span {
    final int start;
    final Object[] spans;

    private Span(int start, Object... spans) {
      this.start = start;
      this.spans = spans;
    }
  }
}
