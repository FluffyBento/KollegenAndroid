package net.kdt.pojavlaunch;

import androidx.annotation.Keep;

import java.util.concurrent.CopyOnWriteArrayList;


@Keep
public class Logger {
    private static final CopyOnWriteArrayList<eventLogListener> logListeners = new CopyOnWriteArrayList<>();

    
    public static native void appendToLog(String text);

    
    public static native void begin(String logFilePath);

    
    public static void addLogListener(eventLogListener logListeners) {
        boolean wasEmpty = Logger.logListeners.isEmpty();
        Logger.logListeners.add(logListeners);
        if (wasEmpty) setLogListener(Logger::onEventLogged);
    }

    
    public static void removeLogListener(eventLogListener logListener) {
        Logger.logListeners.remove(logListener);
        if (Logger.logListeners.isEmpty()){
            
            
            setLogListener(null);
        }
    }

    private static void onEventLogged(String text) {
        for (eventLogListener logListener: Logger.logListeners) {
            logListener.onEventLogged(text);
        }
    }

    
    @Keep
    public interface eventLogListener {
        void onEventLogged(String text);
    }

    
    private static native void setLogListener(eventLogListener logListener);
}
