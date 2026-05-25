/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.*;
/**
 *
 * @author ADMIN
 */
public class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static ConfigManager instance;
    private Properties properties;
    
    private ConfigManager(){
        loadProperties();
    }
    
    public static synchronized ConfigManager getInstance(){
        if(instance == null){
            instance = new ConfigManager();
        }
        return instance;
    }
    
    private void loadProperties(){
        properties = new Properties();
        try(InputStream input = openConfigStream()){
            if(input == null){
                LOGGER.log(Level.WARNING,"Cannot find database config file in config repository");
                return;
            }
            properties.load(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8));
            LOGGER.log(Level.INFO,"Successfully load .env configuration file");
        }catch(IOException e){
            LOGGER.log(Level.SEVERE,"Error loading .env file",e);
        }
    }
        
    private InputStream openConfigStream(){
        ClassLoader classLoader = ConfigManager.class.getClassLoader();
        InputStream input = classLoader.getResourceAsStream("config/.properties");
        return (input != null) ? input : classLoader.getResourceAsStream("config/.env");
    }
    
    public String getProperty(String key){
        String envKey = key.toUpperCase().replace('.', '-');
        String envValue = System.getenv(envKey);
        if(envValue != null){
            LOGGER.log(Level.INFO,"Loaded from ENV: {0}={1}",new Object[]{envKey,envValue});
            return envValue;
        }
        String fileValue = properties.getProperty(key);
        if(fileValue == null){
            LOGGER.log(Level.WARNING,"Configuration key not found: {0}",key);
        }
        return fileValue;
    }
    
    
    public String getProperty(String key, String defaultValue){
        String value = getProperty(key);
        return (value != null)?value : defaultValue;
    }
    
    public int getIntProperty(String key, int defaultValue){
        String value = getProperty(key);
        
        try{
            return (value != null) ? Integer.parseInt(value): defaultValue;
        }catch(NumberFormatException e){
            LOGGER.log(Level.WARNING,"Cannot parse integer for key: "+ key,e);
            return defaultValue;
        }
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue){
        String value = getProperty(key);
        try{
            return (value != null) ? Boolean.parseBoolean(value):defaultValue;
        }catch(NumberFormatException e){
            LOGGER.log(Level.WARNING,"Cannot parse boolean for key: "+key,e);
            return defaultValue;
        }
    }
    
    public boolean containsKey(String key){
        return System.getenv(key) != null || properties.containsKey(key);
    }
    
    
    public Properties getAllProperties(){
        Properties combined = new Properties();
        combined.putAll(properties);
        System.getenv().forEach((k,v) -> combined.setProperty(k, v));
        return combined;
    }
    
    
    public String maskIfSenstive(String key, String value){
        if(key.toLowerCase().contains("password") || key.toLowerCase().contains("secret")){
            return "*********";
        }
        return value;
    }    
    
    
    
    
}
