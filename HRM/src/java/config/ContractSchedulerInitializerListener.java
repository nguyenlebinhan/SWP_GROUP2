package config;

import dal.DBContext;
import dao.EmploymentContractDAO;
import dao.EmployeeDAO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.EmploymentContractService;

public class ContractSchedulerInitializerListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(ContractSchedulerInitializerListener.class.getName());

    private volatile ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        LOGGER.log(Level.INFO, "=== Contract Scheduler Initializer Starting ===");
        LOGGER.log(Level.INFO, "Application context path: {0}", context.getContextPath());

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Contract-Daily-Batch-Scheduler");
            t.setDaemon(true); 
            return t;
        });

        long initialDelay = calculateInitialDelayToMidnight();
        long period = TimeUnit.HOURS.toSeconds(24); 

        LOGGER.log(Level.INFO, "Scheduling daily contract batch job. Initial delay: {0} seconds, Period: 24 hours", initialDelay);

        scheduler.scheduleAtFixedRate(
            this::runDailyContractUpdates,
            initialDelay,
            period,
            TimeUnit.SECONDS
        );

        context.setAttribute("contractDailyScheduler", scheduler);
        
        LOGGER.log(Level.INFO, "=== Contract Scheduler Initialized Successfully ===");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "=== Contract Scheduler Shutdown Initiated ===");

        ScheduledExecutorService localScheduler = this.scheduler;
        
        if (localScheduler != null && !localScheduler.isShutdown()) {
            localScheduler.shutdown();
            
            try {
                if (!localScheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.log(Level.WARNING, "Scheduler did not terminate gracefully within 60s. Forcing shutdown.");
                    localScheduler.shutdownNow();
                    
                    if (!localScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                        LOGGER.log(Level.SEVERE, "Scheduler failed to terminate even after forced shutdown.");
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Scheduler shutdown interrupted. Forcing immediate termination.", e);
                localScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        sce.getServletContext().removeAttribute("contractDailyScheduler");
        
        LOGGER.log(Level.INFO, "=== Contract Scheduler Shutdown Completed ===");
    }

    private void runDailyContractUpdates() {
        LOGGER.log(Level.INFO, "=== Daily Contract Batch Job STARTED ===");

        try {
            EmploymentContractDAO contractDAO = new EmploymentContractDAO();
            EmployeeDAO employeeDAO = new EmployeeDAO();
            DBContext dbContext = new DBContext();
            
            EmploymentContractService contractService = new EmploymentContractService(contractDAO, employeeDAO, dbContext);

            contractService.processDailyContractUpdates();

            LOGGER.log(Level.INFO, "=== Daily Contract Batch Job COMPLETED Successfully ===");

        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, "=== Daily Contract Batch Job FAILED with unexpected error ===", e);
        }
    }

    private long calculateInitialDelayToMidnight() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        
        long secondsUntilMidnight = java.time.Duration.between(now, nextMidnight).getSeconds();
        
        if (secondsUntilMidnight <= 0) {
            secondsUntilMidnight = 86400; 
        }
        
        LOGGER.log(Level.INFO, "Current time: {0}, Next midnight: {1}, Delay: {2} seconds", 
                   new Object[]{now, nextMidnight, secondsUntilMidnight});
        
        return secondsUntilMidnight;
    }
}
