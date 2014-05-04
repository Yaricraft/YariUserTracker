package com.yaricraft.YariUserTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandHandler {
	public static void Process(CommandSender sender, Command cmd, String label, String[] args) {
		
		// Copy the args
    	List<String> lcargs = new ArrayList<String>();
    	for(String s : args) { lcargs.add(s.toLowerCase()); }
    	
    	// Check if command was sent by console.
    	Player player;
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run by a player.");
			return;
		} else {
			player = (Player) sender;
		}
		
		// Check permissions
		boolean isAdmin = player.hasPermission("usertracker.admin");
		
    	// Read the command. Arguments can be in any order.
    	int intCommandNumber = 0;
    	List<String> lstrCommandPlayers = new ArrayList<String>();
    	String strCommand = "";
    	
    	for(int i = 0; i < lcargs.size(); i++)
    	{
    		String arg = lcargs.get(i);
    		if(YariUserTracker.mapCommands.containsKey(arg))
    		{
    			if(strCommand.equals(""))
    			{
    				strCommand = arg;
    			}else{
    				player.sendMessage("Error: Cannot use more than one command. (Used "+strCommand+" and "+arg+")");
    				return;
    			}
    			if(YariUserTracker.mapCommands.get(strCommand)<100 && !isAdmin)
    			{
					player.sendMessage("Error: You don't have the required permission \"usertracker.admin\" for command \""+strCommand+"\".");
					return;
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
	    				return;
	    			}
	    			if(intCommandNumber <= 0)
	        		{
				    	player.sendMessage("Error: Number needs to be larger than 0.");
	    				return;
	        		}
	    			continue;
	    		}catch(Exception e){
	    			if(!YariUserTracker.mapPlayers.containsKey(arg))
	    			{
	    				if(isAdmin)
	    				{
	    					try
	    					{
	    						YariUserTracker.mapPlayers.put(arg, Integer.parseInt(YariUserTracker.getConfig("config", "repstart")));
	    					}catch(Exception e2){
	    						player.sendMessage("Error: Malformed config.yml");
	    						return;
	    					}
	    					player.sendMessage("Created player "+arg+" with "+YariUserTracker.getConfig("config", "repstart")+" Reputation Points");
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
			switch(YariUserTracker.mapCommands.get(strCommand))
			{
				case YariUserTracker.ADD:
					if(intCommandNumber==0)
					{
						player.sendMessage("Error: No number.");
						return;
					}
					for(String strCommandPlayer : lstrCommandPlayers)
					{
						int old = YariUserTracker.mapPlayers.get(strCommandPlayer);
						YariUserTracker.mapPlayers.put(strCommandPlayer, old+intCommandNumber);
					}
					player.sendMessage("Added "+intCommandNumber+" Reputation Points to "+Integer.toString(lstrCommandPlayers.size())+" users.");
	    	    	break;
				case YariUserTracker.REMOVE:
					if(intCommandNumber==0)
					{
						player.sendMessage("Error: No number.");
						return;
					}
					for(String strCommandPlayer : lstrCommandPlayers)
					{
						int old = YariUserTracker.mapPlayers.get(strCommandPlayer);
						YariUserTracker.mapPlayers.put(strCommandPlayer, old-intCommandNumber);
					}
					player.sendMessage("Removed "+intCommandNumber+" Reputation Points from "+Integer.toString(lstrCommandPlayers.size())+" users.");
	    	    	break;
				case YariUserTracker.PUNISH:
	    			break;
				case YariUserTracker.PRAISE:
	    	    	break;
				case YariUserTracker.SAVE:
					YariUserTracker.saveYamls();
		    		player.sendMessage("YariUserTracker saved to disk.");
	    	    	break;
				case YariUserTracker.LOAD:
					YariUserTracker.loadYamls();
		    		player.sendMessage("YariUserTracker loaded from disk.");
	    	    	break;
				case YariUserTracker.SETMAX:
	    	    	break;
				case YariUserTracker.SETSTART:
	    	    	break;
				case YariUserTracker.LIST:
					if(intCommandNumber==0)
					{
						player.sendMessage("Error: No number.");
						return;
					}
					int pages = YariUserTracker.mapPlayers.size()/5+1;
					if(intCommandNumber>pages)
					{
						player.sendMessage("Error: Page not found.");
						return;
					}
					player.sendMessage("Page "+Integer.toString(intCommandNumber)+"/"+Integer.toString(pages));
					for (Map.Entry<String, Integer> entry : YariUserTracker.mapPlayers.entrySet()) {
					    String key = entry.getKey();
					    Integer value = entry.getValue();
					    player.sendMessage(key+" has "+value+" Reputation Points.");
					}
	    	    	break;
				case YariUserTracker.HISTORY:
	    	    	break;
				case YariUserTracker.MAX:
					player.sendMessage("Maximum Reputation Points is "+YariUserTracker.getConfig("config","repmax")+".");
	    	    	break;
				case YariUserTracker.START:
					player.sendMessage("Starting Reputation Points is "+YariUserTracker.getConfig("config","repstart")+".");
	    	    	break;
	    	    default:
	    	    	break;
			}
    	}catch(Exception e){
    		if(!strCommand.equals(""))
    		{
    			player.sendMessage("Error: Malformed command "+strCommand+".");
    			return;
    		}
    		if(lstrCommandPlayers.isEmpty())
    		{
	    		try
	    		{
	    			player.sendMessage("You have "+YariUserTracker.mapPlayers.get(player.getName().toLowerCase())+" reputation points.");
				} catch(Exception e2) {
					player.sendMessage("You were not found in the usertracker. :(");
				}
    		}else{
    			for(String strCommandPlayer : lstrCommandPlayers)
				{
					player.sendMessage(strCommandPlayer+" has "+Integer.toString(YariUserTracker.mapPlayers.get(strCommandPlayer))+" Reputation Points.");
				}
    		}
    	}
    	return;
    }
    
    private void ChangeRep(List<String> lstrCommandPlayers, int intCommandNumber)
    {
    	
    }
}
