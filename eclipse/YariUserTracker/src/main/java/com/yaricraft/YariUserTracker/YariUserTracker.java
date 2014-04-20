package com.yaricraft.YariUserTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
import com.google.api.services.drive.model.File;

public final class YariUserTracker extends JavaPlugin {
	
	public Map<String, Integer> playerPoints = new HashMap<String, Integer>();
	
	private enum eCommand {
		NONE, ADD, REMOVE, PUNISH
	}
	
    @Override
    public void onEnable() {
    	getLogger().info("Loading config...");
    }
 
    @Override
    public void onDisable() {
        // TODO Insert logic to be performed when the plugin is disabled
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
    			player.sendMessage("TODO: Add help.");
    			return true;
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
    	
    	String playername;
    	int playerpoints;
    	for(int i = 0; i < opPlayers.size(); i++)
    	{
    		playername = opPlayers.get(i);
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

    	// If the command worked return true.
    	return true;
    }
}
