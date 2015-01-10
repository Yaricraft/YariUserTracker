package com.yaricraft.YariUserTracker;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class YariUserTracker extends JavaPlugin {
	
	// Private variables
	
	// Maps
	public static TreeMap<String, Integer> mapPlayers = new TreeMap<String, Integer>(new AlphaComparator());
	public static Map<String, FileConfiguration> mapConfigs = new HashMap<String, FileConfiguration>();
	private static Map<String, File> mapFiles = new HashMap<String, File>();
    
    // Getters and setters
    
    public static String getConfig(String _strConfig, String _strKey )
    {
    	String value = mapConfigs.get(_strConfig).getString(_strKey);
    	return value;
    }
    
    public static void setConfig(String _strConfig, String _strKey, String _strValue )
    {
    	mapConfigs.get(_strConfig).set(_strKey, _strValue);
    }
	
    @Override
    public void onEnable() {
    	
        mapFiles.put("config", new File(getDataFolder(), "config.yml"));
        mapFiles.put("users", new File(getDataFolder(), "users.yml"));
		mapFiles.put("actions", new File(getDataFolder(), "actions.yml"));
		mapFiles.put("history", new File(getDataFolder(), "history.yml"));
        
        try {
            firstRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mapConfigs.put("config",  new YamlConfiguration());
        mapConfigs.put("actions",  new YamlConfiguration());
		mapConfigs.put("users",  new YamlConfiguration());
		mapConfigs.put("history",  new YamlConfiguration());
        
        loadYamls();
        
        getServer().getPluginManager().registerEvents(new Listeners(), this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	CommandHandler.Process(sender, cmd, label, args);
    	return true;
    }
 
    // We need to save when the server exits.
    @Override
    public void onDisable() {
    	saveYamls();
    }
    
    private void firstRun() throws Exception {
        if(!mapFiles.get("config").exists()){                        // checks if the yaml does not exists
        	mapFiles.get("config").getParentFile().mkdirs();         // creates the /plugins/<pluginName>/ directory if not found
            copy(getResource("config.yml"), mapFiles.get("config")); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
        if(!mapFiles.get("actions").exists()){
        	mapFiles.get("actions").getParentFile().mkdirs();
            copy(getResource("groups.yml"), mapFiles.get("actions"));
        }
        if(!mapFiles.get("users").exists()){
        	mapFiles.get("users").getParentFile().mkdirs();
            copy(getResource("users.yml"), mapFiles.get("users"));
        }
        if(!mapFiles.get("history").exists()){
        	mapFiles.get("history").getParentFile().mkdirs();
            copy(getResource("history.yml"), mapFiles.get("history"));
        }
    }
    
    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void loadYamls() {
        try {
        	
        	// Load the contents of the File to its FileConfiguration
        	for(String s : mapConfigs.keySet() )
        	{
        		mapConfigs.get(s).load(mapFiles.get(s));
        	}
        	
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Store player names in our ordered map.
        for(String i : mapConfigs.get("users").getKeys(false))
        {
        	if(mapPlayers.containsKey(i))
        	{
        		// getLogger().info("Player "+i+" was already loaded, ignoring...");
        	}else{
        		mapPlayers.put(i, mapConfigs.get("users").getInt(i));
        	}
        }
    }
    
    public static void saveYamls() {
    	
    	// Clear the users.
    	mapConfigs.put("users", new YamlConfiguration());
    	
    	// Add the users.
    	for(String i : mapPlayers.keySet())
    	{
    		mapConfigs.get("users").set(i, mapPlayers.get(i));
    	}
    	
    	// Save the yamls.
        try {
        	// Save the FileConfiguration to its File
        	for(String s : mapConfigs.keySet() )
        	{
        		mapConfigs.get(s).save(mapFiles.get(s));
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
