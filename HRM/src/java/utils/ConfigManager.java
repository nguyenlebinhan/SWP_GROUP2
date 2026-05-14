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
        
        try(InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config/.env")){
            if(input == null){
                LOGGER.log(Level.WARNING, "Cannot find .env file in config directory");
                return;
            }
            
            properties.load(new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8));
            LOGGER.log(Level.INFO,"Successfully loade .env cofiguration file");
        }catch(IOException e){
            LOGGER.log(Level.SEVERE,"Error reading .env file", e);
        }
    }
    
    public String getProperty(String key){
        String envKey = key.toUpperCase().replace('.','_');
        String envValue = System.getenv(envKey);
        
        if(envValue != null){
            LOGGER.log(Level.INFO,"Loaded from ENV: {0} = {1}", new Object[]{envKey,maskIfSenstive(envKey,envValue)});
            return envValue;
        }
        
        String fileValue = properties.getProperty(key);
        if(fileValue != null){
            
        }else{
            LOGGER.log(Level.WARNING,"Configuration key not found: {0}", key);
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
