package com.yaricraft.YariUserTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.OpCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.File;

public final class YariUserTracker extends JavaPlugin implements Listener {
	
	public Map<String, Integer> mapPlayers = new LinkedHashMap<String, Integer>();
	private Map<String, Integer> mapCommands = new HashMap<String, Integer>();
	private List<String> lstrOPCommands = new ArrayList<String>();
	
	File actionsFile;
    File configFile;
    File usersFile;
    File historyFile;

    FileConfiguration actions;
    FileConfiguration config;
    FileConfiguration users;
    FileConfiguration history;
    
    // Command Constants
    private static final int ADD = 1;
    private static final int REMOVE = 2;
    private static final int PUNISH = 3;
    private static final int PRAISE = 4;
    private static final int SAVE = 10;
    private static final int LOAD = 11;
    private static final int SETMAX = 15;
    private static final int SETSTART = 16;
    private static final int LIST = 100;
    private static final int HISTORY = 101;
    private static final int MAX = 105;
    private static final int START = 106;
	
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
        
        config = new YamlConfiguration();
        actions = new YamlConfiguration();
        users = new YamlConfiguration();
        history = new YamlConfiguration();
        
        loadYamls();
        
        getLogger().info("Points max: "+config.getString("repmax"));
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
        mapCommands.put("punish", 3);
        mapCommands.put("praise", 4);
        mapCommands.put("reward", 4);
        mapCommands.put("save", 10);
        mapCommands.put("load", 11);
        mapCommands.put("setmax", 15);
        mapCommands.put("setstart", 16);
        mapCommands.put("list", LIST);
        mapCommands.put("history", 101);
        mapCommands.put("max", 105);
        mapCommands.put("start", 106);
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
    
    public void loadYamls() {
        try {
            config.load(configFile); //loads the contents of the File to its FileConfiguration
            actions.load(actionsFile);
            users.load(usersFile);
            history.load(historyFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for(String i : users.getKeys(false))
        {
        	if(mapPlayers.containsKey(i))
        	{
        		// getLogger().info("Player "+i+" was already loaded, ignoring...");
        	}else{
        		mapPlayers.put(i, users.getInt(i));
        	}
        }
    }
    
    public void saveYamls() {
    	
    	for(String i : mapPlayers.keySet())
    	{
    		users.set(i, mapPlayers.get(i));
    	}
    	
        try {
            config.save(configFile); //saves the FileConfiguration to its File
            actions.save(actionsFile);
            users.save(usersFile);
            history.save(historyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // The only command is /yut. We don't need to check other commands.
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	
    	// Check if command was sent by console.
    	Player player;
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run by a player.");
			return true;
		} else {
			player = (Player) sender;
		}
		
		// Check permissions
		boolean isAdmin = player.hasPermission("usertracker.admin");
		
		// Copy the args
		List<String> lcargs = new ArrayList<String>();
		for(String s : args) { lcargs.add(s.toLowerCase()); }
		
    	// Read the command. Arguments can be in any order.
    	int intCommandNumber = 0;
    	List<String> lstrCommandPlayers = new ArrayList<String>();
    	String strCommand = "";
    	
    	for(int i = 0; i < lcargs.size(); i++)
    	{
    		String arg = lcargs.get(i);
    		if(mapCommands.containsKey(arg))
    		{
    			if(strCommand.equals(""))
    			{
    				strCommand = arg;
    			}else{
    				player.sendMessage("Error: Cannot use more than one command. (Used "+strCommand+" and "+arg+")");
    				return true;
    			}
    			if(mapCommands.get(strCommand)<100 && !isAdmin)
    			{
					player.sendMessage("Error: You don't have the required permission \"usertracker.admin\" for command \""+strCommand+"\".");
					return true;
    			}
    		}else{
	    		try
	    		{
	    			int intCommandInput = Integer.parseInt(arg);
	    			if(intCommandNumber == 0)
	    			{
	    				intCommandNumber = intCommandInput;
	    			}else{
	    				player.sendMessage("Error: Can't use more than one number.");
	    				return true;
	    			}
	    			if(intCommandNumber <= 0)
	        		{
				    	player.sendMessage("Error: Number needs to be larger than 0.");
	    				return true;
	        		}
	    			continue;
	    		}catch(Exception e){
	    			if(!mapPlayers.containsKey(arg))
	    			{
	    				if(isAdmin)
	    				{
	    					try
	    					{
	    						mapPlayers.put(arg, Integer.parseInt(config.getString("repstart")));
	    					}catch(Exception e2){
	    						player.sendMessage("Error: Malformed config.yml");
	    						return true;
	    					}
	    					player.sendMessage("Created player "+arg+" with "+config.getString("repstart")+" Reputation Points");
	    					lstrCommandPlayers.add(arg);
	    					continue;
	    				}else{
	    					player.sendMessage("Warning: Could not find player "+arg+", ignoring.");
	    					continue;
	    				}
	    			}else{
	    				lstrCommandPlayers.add(arg);
	    				continue;
	    			}
	    		}
    		}
    	}

    	// Process the command
    	try
    	{
			switch(mapCommands.get(strCommand))
			{
				case ADD:
					if(intCommandNumber==0)
					{
						player.sendMessage("Error: No number.");
						return true;
					}
					for(String strCommandPlayer : lstrCommandPlayers)
					{
						int old = mapPlayers.get(strCommandPlayer);
						mapPlayers.put(strCommandPlayer, old+intCommandNumber);
					}
					player.sendMessage("Added "+intCommandNumber+" Reputation Points to "+Integer.toString(lstrCommandPlayers.size())+" users.");
	    	    	break;
				case REMOVE:
					if(intCommandNumber==0)
					{
						player.sendMessage("Error: No number.");
						return true;
					}
					for(String strCommandPlayer : lstrCommandPlayers)
					{
						int old = mapPlayers.get(strCommandPlayer);
						mapPlayers.put(strCommandPlayer, old-intCommandNumber);
					}
					player.sendMessage("Removed "+intCommandNumber+" Reputation Points from "+Integer.toString(lstrCommandPlayers.size())+" users.");
	    	    	break;
				case PUNISH:
	    			break;
				case PRAISE:
	    	    	break;
				case SAVE:
					saveYamls();
		    		player.sendMessage("YariUserTracker saved to disk.");
	    	    	break;
				case LOAD:
					loadYamls();
		    		player.sendMessage("YariUserTracker loaded from disk.");
	    	    	break;
				case SETMAX:
	    	    	break;
				case SETSTART:
	    	    	break;
				case LIST:
					if(intCommandNumber==0)
					{
						player.sendMessage("Error: No number.");
						return true;
					}
					int pages = mapPlayers.size()/5+1;
					if(intCommandNumber>pages)
					{
						player.sendMessage("Error: Page not found.");
						return true;
					}
					player.sendMessage("Page "+Integer.toString(intCommandNumber)+"/"+Integer.toString(pages));
					for (Map.Entry<String, Integer> entry : mapPlayers.entrySet()) {
					    String key = entry.getKey();
					    Integer value = entry.getValue();
					    player.sendMessage(key+" has "+value+" Reputation Points.");
					}
	    	    	break;
				case HISTORY:
	    	    	break;
				case MAX:
					player.sendMessage("Maximum Reputation Points is "+config.getString("repmax")+".");
	    	    	break;
				case START:
					player.sendMessage("Starting Reputation Points is "+config.getString("repstart")+".");
	    	    	break;
	    	    default:
	    	    	break;
			}
    	}catch(Exception e){
    		if(!strCommand.equals(""))
    		{
    			player.sendMessage("Error: Malformed command "+strCommand+".");
    			return true;
    		}
    		if(lstrCommandPlayers.isEmpty())
    		{
	    		try
	    		{
	    			player.sendMessage("You have "+mapPlayers.get(player.getName().toLowerCase())+" reputation points.");
				} catch(Exception e2) {
					player.sendMessage("You were not found in the usertracker. :(");
				}
    		}else{
    			for(String strCommandPlayer : lstrCommandPlayers)
				{
					player.sendMessage(strCommandPlayer+" has "+Integer.toString(mapPlayers.get(strCommandPlayer))+" Reputation Points.");
				}
    		}
    	}
    	return true;
    }
    
    private void ChangeRep(List<String> lstrCommandPlayers, int intCommandNumber)
    {
    	
    }
    
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        String player = event.getPlayer().getName().toLowerCase();
        if(!mapPlayers.containsKey(player))
        {
        	event.getPlayer().sendMessage("You were added to the usertracker with the default "+String.valueOf(config.get("repstart"))+" points.");
        	mapPlayers.put(player, Integer.parseInt(config.getString("repstart")));
        }
    }
}
