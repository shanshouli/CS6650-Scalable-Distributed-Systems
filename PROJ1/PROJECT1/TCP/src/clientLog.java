import java.io.IOException;
import java.util.logging.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class clientLog {
    private static Logger log = Logger.getLogger("UserActionsLog");
    private static FileHandler fileHandler;

    static {
        try {
            fileHandler = new FileHandler("UserActions.log", true);

            // use defined time stamp
            fileHandler.setFormatter(new CustomFormatter());

            // bind fileHandler with log file
            log.addHandler(fileHandler);

        } catch (IOException e) {
            System.out.println("Failure in initialization of log file" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        log.info(message);
        System.out.println(message);
    }

    // define the format of log file
    static class CustomFormatter extends Formatter {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        @Override
        public String format(LogRecord record) {
            return String.format("[%s]: %s%n",
                    dateFormat.format(new Date(record.getMillis())),
                    record.getMessage()); // get information of log
        }
    }
}
