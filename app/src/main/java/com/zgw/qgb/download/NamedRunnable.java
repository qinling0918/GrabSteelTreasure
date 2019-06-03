package com.zgw.qgb.download;

import java.util.Locale;

public abstract class NamedRunnable implements Runnable {
    protected final String name;

    public static String format(String format, Object... args) {
        return String.format(Locale.CHINA, format, args);
    }

    public NamedRunnable(String format, Object... args) {
        this.name = format(format, args);
    }

    @Override
    public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    protected abstract void execute();
}