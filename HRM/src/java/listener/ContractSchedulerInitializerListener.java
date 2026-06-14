package listener;

import dal.DBContext;
import dao.EmploymentContractDAO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.EmploymentContractService;

/**
 * ServletContextListener for System Automated Scheduler Flow (MVC2 Background Engine).
 * 
 * This listener initializes and manages the lifecycle of the background
 * contract processing engine that runs daily batch updates for:
 * - PENDING_ACTIVATION → ACTIVE (auto-activation)
 * - ACTIVE → EXPIRED (auto-expiration)
 * 
 * Architecture:
 * - Native ServletContextListener (no annotations, registered in web.xml)
 * - Uses ScheduledExecutorService for thread-safe, single-threaded scheduling
 * - Graceful shutdown on context destruction to prevent Tomcat thread/memory leaks
 * - Isolates scheduler logic entirely from manual HR HTTP endpoints
 * - Each scheduled run creates its own service instance with fresh DB connections
 * 
 * Thread Safety:
 * - Single-threaded executor ensures sequential batch execution (no concurrent runs)
 * - Fresh DAO/Service/DBContext per run prevents cross-run contamination
 * - Exception catch-all in runDailyContractUpdates() prevents scheduler thread death
 */
public class ContractSchedulerInitializerListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(ContractSchedulerInitializerListener.class.getName());

    // The single-threaded scheduled executor for the cron-like batch job
    // Volatile for safe publication across threads (servlet init vs destroy)
    private volatile ScheduledExecutorService scheduler;

    /**
     * Called when the web application is deployed and started.
     * Initializes the scheduler and registers the daily batch task targeting midnight.
     * 
     * @param sce the ServletContextEvent containing the application context
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        LOGGER.log(Level.INFO, "=== Contract Scheduler Initializer Starting ===");
        LOGGER.log(Level.INFO, "Application context path: {0}", context.getContextPath());

        // Create a single-threaded scheduled executor
        // Using single thread ensures sequential execution (no concurrent batch runs)
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Contract-Daily-Batch-Scheduler");
            t.setDaemon(true); // Allow JVM/Tomcat to exit even if this thread is running
            return t;
        });

        // Calculate initial delay to run at next midnight (00:00:00)
        long initialDelay = calculateInitialDelayToMidnight();
        long period = TimeUnit.HOURS.toSeconds(24); // 24 hours = 86400 seconds

        LOGGER.log(Level.INFO, "Scheduling daily contract batch job. Initial delay: {0} seconds, Period: 24 hours", initialDelay);

        // Schedule the daily contract processing task
        // Using scheduleAtFixedRate ensures consistent 24h intervals
        scheduler.scheduleAtFixedRate(
            this::runDailyContractUpdates,
            initialDelay,
            period,
            TimeUnit.SECONDS
        );

        // Store scheduler reference in ServletContext for potential admin inspection/monitoring
        context.setAttribute("contractDailyScheduler", scheduler);
        
        LOGGER.log(Level.INFO, "=== Contract Scheduler Initialized Successfully ===");
    }

    /**
     * Called when the web application is shutting down or undeployed.
     * Performs graceful shutdown of the scheduler to prevent resource leaks.
     * 
     * Strict shutdown sequence:
     * 1. shutdown() - disable new tasks
     * 2. awaitTermination(60s) - wait for running task
     * 3. shutdownNow() - force cancel if still running
     * 4. awaitTermination(10s) - final wait
     * 
     * This guarantees no thread leaks in Tomcat container.
     * 
     * @param sce the ServletContextEvent containing the application context
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "=== Contract Scheduler Shutdown Initiated ===");

        ScheduledExecutorService localScheduler = this.scheduler;
        
        if (localScheduler != null && !localScheduler.isShutdown()) {
            // Step 1: Disable new tasks from being submitted
            localScheduler.shutdown();
            
            try {
                // Step 2: Wait for existing tasks to terminate (max 60 seconds)
                if (!localScheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    // Step 3: Force shutdown if tasks didn't complete in time
                    LOGGER.log(Level.WARNING, "Scheduler did not terminate gracefully within 60s. Forcing shutdown.");
                    localScheduler.shutdownNow();
                    
                    // Step 4: Final wait after forced shutdown
                    if (!localScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                        LOGGER.log(Level.SEVERE, "Scheduler failed to terminate even after forced shutdown.");
                    }
                }
            } catch (InterruptedException e) {
                // Re-interrupt current thread and force shutdown
                LOGGER.log(Level.SEVERE, "Scheduler shutdown interrupted. Forcing immediate termination.", e);
                localScheduler.shutdownNow();
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }

        // Clean up context attribute
        sce.getServletContext().removeAttribute("contractDailyScheduler");
        
        LOGGER.log(Level.INFO, "=== Contract Scheduler Shutdown Completed ===");
    }

    /**
     * Executes the daily contract processing batch job.
     * 
     * This method is invoked by the ScheduledExecutorService on the configured schedule.
     * Creates a fresh service instance with its own DB connections for each run
     * to ensure isolation and prevent connection leak across runs.
     * 
     * CRITICAL: All exceptions caught to prevent scheduler thread death.
     * If this thread dies, no future scheduled runs will execute.
     */
    private void runDailyContractUpdates() {
        LOGGER.log(Level.INFO, "=== Daily Contract Batch Job STARTED ===");

        try {
            // Create fresh DAO and DBContext for this batch run
            // This ensures clean connection state and no cross-run contamination
            EmploymentContractDAO contractDAO = new EmploymentContractDAO();
            DBContext dbContext = new DBContext();
            
            // Initialize service with fresh dependencies
            EmploymentContractService contractService = new EmploymentContractService(contractDAO, dbContext);

            // Execute the scheduler flow (processes both activations and expirations)
            // This method handles its own per-contract transactions internally
            contractService.processDailyContractUpdates();

            LOGGER.log(Level.INFO, "=== Daily Contract Batch Job COMPLETED Successfully ===");

        } catch (Exception e) {
            // Catch-all to prevent scheduler thread death
            // If this thread dies, no future scheduled runs will execute
            LOGGER.log(Level.SEVERE, "=== Daily Contract Batch Job FAILED with unexpected error ===", e);
        }
    }

    /**
     * Calculates the initial delay (in seconds) until the next midnight (00:00:00).
     * 
     * For production deployment, this targets exact midnight daily execution.
     * 
     * @return seconds until next midnight (always > 0 and <= 86400)
     */
    private long calculateInitialDelayToMidnight() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        
        long secondsUntilMidnight = java.time.Duration.between(now, nextMidnight).getSeconds();
        
        // Ensure positive delay (should always be > 0 and <= 86400)
        if (secondsUntilMidnight <= 0) {
            secondsUntilMidnight = 86400; // Fallback to 24 hours
        }
        
        LOGGER.log(Level.INFO, "Current time: {0}, Next midnight: {1}, Delay: {2} seconds", 
                   new Object[]{now, nextMidnight, secondsUntilMidnight});
        
        return secondsUntilMidnight;
    }
}
