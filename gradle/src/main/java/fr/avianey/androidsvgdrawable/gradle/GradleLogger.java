package fr.avianey.androidsvgdrawable.gradle;

import org.slf4j.Logger;

import fr.avianey.androidsvgdrawable.util.Log;

public class GradleLogger implements Log {
    
    public final Logger log;
    
    public GradleLogger(Logger log) {
        this.log = log;
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(CharSequence content) {
        log.debug(String.valueOf(content));
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        log.debug(String.valueOf(content), error);
    }

    @Override
    public void debug(Throwable error) {
        log.debug("", error);
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(CharSequence content) {
        log.info(String.valueOf(content));
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        log.info(String.valueOf(content), error);
    }

    @Override
    public void info(Throwable error) {
        log.info("", error);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(CharSequence content) {
        log.warn(String.valueOf(content));
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        log.warn(String.valueOf(content), error);
    }

    @Override
    public void warn(Throwable error) {
        log.warn("", error);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void error(CharSequence content) {
        log.error(String.valueOf(content));
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        log.error(String.valueOf(content), error);
    }

    @Override
    public void error(Throwable error) {
        log.error("", error);
    }

}
