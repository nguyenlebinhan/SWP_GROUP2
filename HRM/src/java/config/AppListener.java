



package config;

import dal.DBInitializer;
import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.*;





@WebListener
public class AppListener implements ServletContextListener{
    private static final Logger LOGGER = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.log(Level.INFO,"Ứng dụng web đang khởi tạo. Bắt đầu khởi tạo SQl Server...");

        CommandMap.setDefaultCommandMap(new MailcapCommandMap());

        DBInitializer init = new DBInitializer();
        init.initializeDatabase(false);
        LOGGER.info("Init database successfully");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.INFO,"WEB IS TRYING TO SHUT DOWN.CLEAN THE RESOURCES ....");
    }


}
