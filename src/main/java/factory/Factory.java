package factory;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Factory {

    public static AppLogger getAppLogger(Logger logger) {
        return new AppLogger(logger);
    }

    public static Logger getSlf4jLogger(Class<?> c) {
        return LoggerFactory.getLogger(c);
    }
}
