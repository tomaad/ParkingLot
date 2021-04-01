package factory;

import org.slf4j.Logger;

public class AppLogger {
    private Logger logger;

    public AppLogger(Logger logger) {
        this.logger = logger;
    }

    public void debug(String msg) {
        logger.debug(msg);
        System.out.println(msg);
    }

    public void error(String msg) {
        logger.error(msg);
        System.out.println(msg);
    }
}
