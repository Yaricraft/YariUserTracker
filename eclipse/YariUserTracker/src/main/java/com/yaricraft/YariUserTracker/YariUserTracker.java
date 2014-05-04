package com.yaricraft.YariUserTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class YariUserTracker extends JavaPlugin {
	
	// Private variables
	
	// Maps
	public static TreeMap<String, Integer> mapPlayers = new TreeMap<String, Integer>(new AlphaComparator());
	public static Map<String, Integer> mapCommands = new HashMap<String, Integer>();
	public static Map<String, FileConfiguration> mapConfigs = new HashMap<String, FileConfiguration>();
	
	// Files
	private static File actionsFile;
	private static File configFile;
	private static File usersFile;
	private static File historyFile;
    
    // Command Constants
	public static final int ADD = 1;
	public static final int REMOVE = 2;
	public static final int PUNISH = 3;
	public static final int PRAISE = 4;
	public static final int SAVE = 10;
	public static final int LOAD = 11;
	public static final int SETMAX = 15;
	public static final int SETSTART = 16;
	public static final int LIST = 100;
	public static final int HISTORY = 101;
	public static final int MAX = 105;
	public static final int START = 106;
    
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
    	getLogger().info("Loading configs...");
        configFile = new File(getDataFolder(), "config.yml");
        usersFile = new File(getDataFolder(), "users.yml");
        actionsFile = new File(getDataFolder(), "actions.yml");
        historyFile = new File(getDataFolder(), "history.yml");
        
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
        
        getLogger().info("Points max: "+mapConfigs.get("config").getString("repmax"));
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
        if(!configFile.exists()){                        // checks if the yaml does not exists
            configFile.getParentFile().mkdirs();         // creates the /plugins/<pluginName>/ directory if not found
            copy(getResource("config.yml"), configFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
        if(!actionsFile.exists()){
            actionsFile.getParentFile().mkdirs();
            copy(getResource("groups.yml"), actionsFile);
        }
        if(!usersFile.exists()){
            usersFile.getParentFile().mkdirs();
            copy(getResource("users.yml"), usersFile);
        }
        if(!historyFile.exists()){
            historyFile.getParentFile().mkdirs();
            copy(getResource("history.yml"), historyFile);
        }
        
        mapCommands.put("add", ADD);
        mapCommands.put("remove", REMOVE);
        mapCommands.put("punish", PUNISH);
        mapCommands.put("praise", PRAISE);
        mapCommands.put("reward", PRAISE); // Alias
        mapCommands.put("save", SAVE);
        mapCommands.put("load", LOAD);
        mapCommands.put("setmax", SETMAX);
        mapCommands.put("setstart", SETSTART);
        mapCommands.put("list", LIST);
        mapCommands.put("history", HISTORY);
        mapCommands.put("max", MAX);
        mapCommands.put("start", START);
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
            mapConfigs.get("config").load(configFile); 
            mapConfigs.get("actions").load(actionsFile);
    		mapConfigs.get("users").load(usersFile);
			mapConfigs.get("history").load(historyFile);
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
    	
    	for(String i : mapPlayers.keySet())
    	{
    		mapConfigs.get("users").set(i, mapPlayers.get(i));
    	}
    	
        try {
        	// Save the FileConfiguration to its File
        	mapConfigs.get("config").save(configFile);
			mapConfigs.get("actions").save(actionsFile);
			mapConfigs.get("users").save(usersFile);
			mapConfigs.get("history").save(historyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
