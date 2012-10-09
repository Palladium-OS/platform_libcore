/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.icu;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

public final class NativeBreakIterator implements Cloneable {
    // Acceptable values for the 'type' field.
    private static final int BI_CHAR_INSTANCE = 1;
    private static final int BI_WORD_INSTANCE = 2;
    private static final int BI_LINE_INSTANCE = 3;
    private static final int BI_SENT_INSTANCE = 4;

    private final int address;
    private final int type;
    private String string;
    private CharacterIterator charIterator;

    private NativeBreakIterator(int address, int type) {
        this.address = address;
        this.type = type;
        this.charIterator = new StringCharacterIterator("");
    }

    @Override
    public Object clone() {
        int cloneAddr = cloneImpl(this.address);
        NativeBreakIterator clone = new NativeBreakIterator(cloneAddr, this.type);
        clone.string = this.string;
        // The RI doesn't clone the CharacterIterator.
        clone.charIterator = this.charIterator;
        return clone;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof NativeBreakIterator)) {
            return false;
        }
        // TODO: is this sufficient? shouldn't we be checking the underlying rules?
        NativeBreakIterator rhs = (NativeBreakIterator) object;
        return type == rhs.type && charIterator.equals(rhs.charIterator);
    }

    @Override
    public int hashCode() {
        return 42; // No-one uses BreakIterator as a hash key.
    }

    @Override protected void finalize() throws Throwable {
        try {
            closeImpl(this.address);
        } finally {
            super.finalize();
        }
    }

    public int current() {
        return currentImpl(this.address, this.string);
    }

    public int first() {
        return firstImpl(this.address, this.string);
    }

    public int following(int offset) {
        return followingImpl(this.address, this.string, offset);
    }

    public CharacterIterator getText() {
        int newLocation = currentImpl(this.address, this.string);
        this.charIterator.setIndex(newLocation);
        return this.charIterator;
    }

    public int last() {
        return lastImpl(this.address, this.string);
    }

    public int next(int n) {
        return nextImpl(this.address, this.string, n);
    }

    public int next() {
        return nextImpl(this.address, this.string, 1);
    }

    public int previous() {
        return previousImpl(this.address, this.string);
    }

    public void setText(CharacterIterator newText) {
        StringBuilder sb = new StringBuilder();
        for (char c = newText.first(); c != CharacterIterator.DONE; c = newText.next()) {
            sb.append(c);
        }
        setText(sb.toString(), newText);
    }

    public void setText(String newText) {
        setText(newText, new StringCharacterIterator(newText));
    }

    private void setText(String s, CharacterIterator it) {
        this.string = s;
        this.charIterator = it;
        setTextImpl(this.address, this.string);
    }

    public boolean hasText() {
        return (string != null);
    }

    public boolean isBoundary(int offset) {
        return isBoundaryImpl(this.address, this.string, offset);
    }

    public int preceding(int offset) {
      return precedingImpl(this.address, this.string, offset);
    }

    public static NativeBreakIterator getCharacterInstance(Locale where) {
        return new NativeBreakIterator(getCharacterInstanceImpl(where.toString()), BI_CHAR_INSTANCE);
    }

    public static NativeBreakIterator getLineInstance(Locale where) {
        return new NativeBreakIterator(getLineInstanceImpl(where.toString()), BI_LINE_INSTANCE);
    }

    public static NativeBreakIterator getSentenceInstance(Locale where) {
        return new NativeBreakIterator(getSentenceInstanceImpl(where.toString()), BI_SENT_INSTANCE);
    }

    public static NativeBreakIterator getWordInstance(Locale where) {
        return new NativeBreakIterator(getWordInstanceImpl(where.toString()), BI_WORD_INSTANCE);
    }

    private static native int getCharacterInstanceImpl(String locale);
    private static native int getWordInstanceImpl(String locale);
    private static native int getLineInstanceImpl(String locale);
    private static native int getSentenceInstanceImpl(String locale);
    private static native int cloneImpl(int address);

    private static native void closeImpl(int address);

    private static native void setTextImpl(int address, String text);
    private static native int precedingImpl(int address, String text, int offset);
    private static native boolean isBoundaryImpl(int address, String text, int offset);
    private static native int nextImpl(int address, String text, int n);
    private static native int previousImpl(int address, String text);
    private static native int currentImpl(int address, String text);
    private static native int firstImpl(int address, String text);
    private static native int followingImpl(int address, String text, int offset);
    private static native int lastImpl(int address, String text);
}
