package com.yaricraft.YariUserTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

public final class YariUserTracker extends JavaPlugin {
	
	public Map<String, Integer> playerPoints = new HashMap<String, Integer>();
	
	File actionsFile;
    File configFile;
    File usersFile;
    File historyFile;

    FileConfiguration actions;
    FileConfiguration config;
    FileConfiguration users;
    FileConfiguration history;
    
	
	private enum eCommand {
		NONE, ADD, REMOVE, PUNISH
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
        	if(playerPoints.containsKey(i))
        	{
        		getLogger().info("Player "+i+" was already loaded, ignoring...");
        	}else{
        		playerPoints.put(i, users.getInt(i));
        	}
        }
    }
    
    public void saveYamls() {
    	
    	for(String i : playerPoints.keySet())
    	{
    		users.set(i, playerPoints.get(i));
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
    	
    	// See how the command was received. (Console or Player)
    	Player player = (Player) sender;
    	
    	// Read the command. Arguments can be in any order.
    	eCommand opCommand = eCommand.NONE;
    	int opNumber = 0;
    	List<String> opPlayers = new ArrayList<String>();
    	String strDiskCommand = "";
    	
    	for(int i = 0; i < args.length; i++)
    	{
    		if(args[i].equalsIgnoreCase("add"))
    		{
    			if(opCommand != eCommand.NONE)
    			{
    				player.sendMessage("Warning: Ignoring additional command ADD");
    				continue;
    			}
    			opCommand = eCommand.ADD;
    			continue;
    		}
    		if(args[i].equalsIgnoreCase("remove"))
    		{
    			if(opCommand != eCommand.NONE)
    			{
    				player.sendMessage("Warning: Ignoring additional command REMOVE");
    				continue;
    			}
    			opCommand = eCommand.REMOVE;
    			continue;
    		}
    		if(args[i].equalsIgnoreCase("punish"))
    		{
    			if(opCommand != eCommand.NONE)
    			{
    				player.sendMessage("Warning: Ignoring additional command PUNISH");
    				continue;
    			}
    			opCommand = eCommand.PUNISH;
    			
    			player.sendMessage("Error: No valid punishment found.");
    			return false;
    		}
    		if(args[i].equalsIgnoreCase("save"))
    		{
    			if(strDiskCommand.equals(""))
    			{
    				strDiskCommand = "save";
    				continue;
    			}else{
    				player.sendMessage("Error: Can't use multiple disk commands.");
        			return true;
    			}
    		}
    		if(args[i].equalsIgnoreCase("load"))
    		{
    			if(strDiskCommand.equals(""))
    			{
    				strDiskCommand = "load";
    				continue;
    			}else{
    				player.sendMessage("Error: Can't use multiple disk commands.");
        			return true;
    			}
    		}
    		try
    		{
    			opNumber = Integer.parseInt(args[i]);
    			if(opNumber <= 0)
        		{
			    	player.sendMessage("Error: Number needs to be larger than 0.");
    				return true;
        		}
    			continue;
    		}catch(Exception e)
    		{
    			opPlayers.add(args[i]);
    			continue;
    		}
    	}
    	
    	if(opPlayers.size() == 0)
    	{
    		if(opCommand != eCommand.NONE)
    		{
	    		player.sendMessage("Error: No players were found for command "+opCommand.toString());
	    		return true;
    		}else{
    			if(strDiskCommand.equals(""))
    			{
    				player.sendMessage("TODO: Add help.");
    				return true;
    			}
    		}
    	}
    	
    	if(opNumber != 0 && opCommand == eCommand.NONE)
    	{
    		player.sendMessage("Error: Numbers found but no command was used.");
    		return true;
    	}
    	
    	if(opNumber == 0 && (opCommand == eCommand.ADD || opCommand == eCommand.REMOVE))
    	{
    		player.sendMessage("Error: Number required for command "+opCommand.toString());
    		return true;
    	}
    	
    	// Done reading command, now process.
    	
    	// Load
    	if(strDiskCommand.equals("load"))
    	{
    		loadYamls();
    		player.sendMessage("YariUserTracker loaded from disk.");
    	}
    	
    	// Change rep.
    	String playername;
    	int playerpoints;
    	for(int i = 0; i < opPlayers.size(); i++)
    	{
    		playername = opPlayers.get(i);
    		if(playername.equals("save") || playername.equals("load") )
    		{
    			
    		}
    		if(playerPoints.containsKey(playername))
    		{
    			playerpoints = playerPoints.get(playername);
		    	switch (opCommand) {
		    		case ADD:
		    			playerPoints.put(playername, playerpoints+opNumber);
    	    			player.sendMessage(playername+" points increased from "+String.valueOf(playerpoints)+" to "+String.valueOf(playerpoints+opNumber));
		    			break;
		    		case REMOVE:
		    			playerPoints.put(playername, playerpoints-opNumber);
    	    			player.sendMessage(playername+" points decreased from "+String.valueOf(playerpoints)+" to "+String.valueOf(playerpoints-opNumber));
		    			break;
		    		case PUNISH:
		    			break;
		    		case NONE:
		    			player.sendMessage(playername+" has "+String.valueOf(playerpoints)+" points.");
		    	    	break;
		    	    default:
		    	    	break;
		    	}
	    	}else{
	    		switch (opCommand) {
		    		case ADD:
		    			playerPoints.put(playername, 10+opNumber);
    	    			player.sendMessage(playername+" created with "+String.valueOf(10+opNumber)+" points");
		    			break;
		    		case REMOVE:
		    			playerPoints.put(playername, 10-opNumber);
    	    			player.sendMessage(playername+" created with "+String.valueOf(10-opNumber)+" points");
		    			break;
		    		case PUNISH:
		    			break;
		    		case NONE:
		    			playerPoints.put(playername, 10+opNumber);
    	    			player.sendMessage(playername+" created with "+String.valueOf(10)+" points");
		    	    	break;
		    	    default:
		    	    	break;
		    	}
				continue;
	    	}
    	}
    	
    	// Save
    	if(strDiskCommand.equals("save"))
    	{
    		saveYamls();
    		player.sendMessage("YariUserTracker saved to disk.");
    	}

    	// If the command worked return true.
    	return true;
    }
}
