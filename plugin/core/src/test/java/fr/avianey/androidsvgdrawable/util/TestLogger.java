package fr.avianey.androidsvgdrawable.util;

import java.io.PrintStream;

public class TestLogger implements Log {

    private final PrintStream out;

    public TestLogger() {
        this.out = null;
    }

    public TestLogger(PrintStream out) {
        this.out = out;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(CharSequence content) {
        if (out != null) {
            out.println(content);
        }
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        if (out != null) {
            out.println(content);
            error.printStackTrace(out);
        }
    }

    @Override
    public void debug(Throwable error) {}

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(CharSequence content) {
        if (out != null) {
            out.println(content);
        }
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        if (out != null) {
            out.println(content);
            error.printStackTrace(out);
        }
    }

    @Override
    public void info(Throwable error) {}

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(CharSequence content) {
        if (out != null) {
            out.println(content);
        }
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        if (out != null) {
            out.println(content);
            error.printStackTrace(out);
        }
    }

    @Override
    public void warn(Throwable error) {}

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(CharSequence content) {
        if (out != null) {
            out.println(content);
        }
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        if (out != null) {
            out.println(content);
            error.printStackTrace(out);
        }
    }

    @Override
    public void error(Throwable error) {}

}
