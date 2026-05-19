/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import dal.DBInitializer;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.*;

/**
 *
 * @author ADMIN
 */
@WebListener
public class AppListener implements ServletContextListener{
    private static final Logger LOGGER = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.log(Level.INFO,"Ứng dụng web đang khởi tạo. Bắt đầu khởi tạo SQl Server...");
        DBInitializer init = new DBInitializer();
        init.initializeDatabase(false);
        LOGGER.info("Init database successfully");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.INFO,"WEB IS TRYING TO SHUT DOWN.CLEAN THE RESOURCES ....");
    }
    
    
}
