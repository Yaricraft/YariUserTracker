package com.yaricraft.YariUserTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandHandler {
	
	private enum yutCommand {
		ADD, REMOVE, PUNISH, PRAISE, SAVE, LOAD, PURGE, SETMAX, SETSTART, HELP, HISTORY, LIST, MAX, START;
	}
	
	private static class Commands {
		
		private static Map<String, yutCommand> mapCommands;
		static {
			mapCommands = new HashMap<String, yutCommand>();
			mapCommands.put("add", yutCommand.ADD);
			mapCommands.put("remove", yutCommand.REMOVE);
		    mapCommands.put("punish", yutCommand.PUNISH);
		    mapCommands.put("praise", yutCommand.PRAISE);
		    mapCommands.put("save", yutCommand.SAVE);
		    mapCommands.put("load", yutCommand.LOAD);
		    mapCommands.put("purge", yutCommand.PURGE);
		    mapCommands.put("setmax", yutCommand.SETMAX);
		    mapCommands.put("setstart", yutCommand.SETSTART);
		    mapCommands.put("help", yutCommand.HELP);
		    mapCommands.put("history", yutCommand.HISTORY);
		    mapCommands.put("list", yutCommand.LIST);
		    mapCommands.put("max", yutCommand.MAX);
		    mapCommands.put("start", yutCommand.START);
		}
		
		public static void addAlias(String _alias, String _command) {
			if(!mapCommands.containsKey(_command)) return;
			mapCommands.put(_alias, mapCommands.get(_command) );
		}
		
		public static boolean isCommand(String _command) {
			if(mapCommands.containsKey(_command)) {
				return true;
			}else{
				return false;
			}
		}
		
		public static yutCommand get(String _command) {
			return mapCommands.get(_command);
		}
	}
	
	private static final Set<String> setOPCommands;
	static {
		setOPCommands = new HashSet<String>();
		setOPCommands.add("add");
		setOPCommands.add("remove");
		setOPCommands.add("punish");
		setOPCommands.add("praise");
		setOPCommands.add("save");
		setOPCommands.add("load");
		setOPCommands.add("purge");
		setOPCommands.add("setmax");
		setOPCommands.add("setstart");
	}
	
	public void addAlias(String _alias, String _command)
	{
		Commands.addAlias(_alias, _command);
	}
	
	public static void Process(CommandSender sender, Command cmd, String label, String[] args) {
		
    	// Check if command was sent by console.
    	Player player;
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run by a player.");
			return;
		} else {
			player = (Player) sender;
		}
		
		// Copy the arguments as lower-case.
    	List<String> lcargs = new ArrayList<String>();
    	for(String s : args) { lcargs.add(s.toLowerCase()); }
		
		// Check permissions
		boolean isAdmin = player.hasPermission("usertracker.admin");
		boolean isOp = player.isOp();
		
    	// Read the command. Arguments can be in any order.
    	int intCommandNumber = 0;
    	List<String> lstrCommandPlayers = new ArrayList<String>();
    	String strCommand = "";
    	
    	for(String arg : lcargs)
    	{
    		if(Commands.isCommand(arg))
    		{
    			if(strCommand.equals(""))
    			{
    				strCommand = arg;
    			}else{
    				player.sendMessage("Error: Cannot use more than one command. (Used "+strCommand+" and "+arg+")");
    				return;
    			}
    			if(setOPCommands.contains(arg) && !(isAdmin || isOp))
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
		switch(Commands.get(strCommand)) {
			case ADD:
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
			case REMOVE:
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
			case PUNISH:
    			break;
			case PRAISE:
    	    	break;
			case SAVE:
				YariUserTracker.saveYamls();
	    		player.sendMessage("YariUserTracker saved to disk.");
    	    	break;
			case LOAD:
				YariUserTracker.loadYamls();
	    		player.sendMessage("YariUserTracker loaded from disk.");
    	    	break;
			case PURGE:
				for(String strCommandPlayer : lstrCommandPlayers)
				{
					YariUserTracker.mapPlayers.remove(strCommandPlayer);
					player.sendMessage("Purged player "+strCommandPlayer+" from the tracker.");
				}
    	    	break;
			case SETMAX:
				YariUserTracker.setConfig("config", "repmax", Integer.toString(intCommandNumber));
				player.sendMessage("Set Reputation maximum to "+Integer.toString(intCommandNumber)+".");
    	    	break;
			case SETSTART:
				YariUserTracker.setConfig("config", "repstart", Integer.toString(intCommandNumber));
				player.sendMessage("Set Reputation start to "+Integer.toString(intCommandNumber)+".");
    	    	break;
			case HELP:
				player.sendMessage(ChatColor.GOLD+"=============================");
				player.sendMessage(ChatColor.GOLD+"       YariUserTracker");
				player.sendMessage(ChatColor.GOLD+"=============================");
				player.sendMessage(ChatColor.AQUA+"Arguments can be in any order. Multiple players can be used in any command with <player>.");
				player.sendMessage(ChatColor.GOLD+"/yut: "+ChatColor.WHITE+"Shows you your reputation points.");
				player.sendMessage(ChatColor.GOLD+"/yut list: "+ChatColor.WHITE+"Shows the reputation points of all users.");
				player.sendMessage(ChatColor.GOLD+"/yut <player>: "+ChatColor.WHITE+"Shows you the reputation points of player.");
				player.sendMessage(ChatColor.GOLD+"/yut max: "+ChatColor.WHITE+"Shows you the maximum reputation points.");
				player.sendMessage(ChatColor.GOLD+"/yut start: "+ChatColor.WHITE+"Shows you the starting reputation points for new players.");
				if(isAdmin || isOp)
				{
					player.sendMessage(ChatColor.RED+"ADMIN COMMANDS:");
					player.sendMessage(ChatColor.GOLD+"/yut add <number> <player>: "+ChatColor.WHITE+"Adds <number> reputation points to <player>.");
					player.sendMessage(ChatColor.GOLD+"/yut remove <number> <player>: "+ChatColor.WHITE+"Removes <number> reputation points from <player>.");
					player.sendMessage(ChatColor.GOLD+"/yut save: "+ChatColor.WHITE+"Saves all data to disk.");
					player.sendMessage(ChatColor.GOLD+"/yut load: "+ChatColor.WHITE+"Loads any data added to disk after the server started.");
					player.sendMessage(ChatColor.GOLD+"/yut setmax <number>: "+ChatColor.WHITE+"Set the maximum reputation points.");
					player.sendMessage(ChatColor.GOLD+"/yut setstart <number>: "+ChatColor.WHITE+"Set the starting reputation points for new players.");
				}
    	    	break;
			case HISTORY:
    	    	break;
			case LIST:
				for (Map.Entry<String, Integer> entry : YariUserTracker.mapPlayers.entrySet()) {
				    String key = entry.getKey();
				    Integer value = entry.getValue();
				    player.sendMessage(key+" has "+value+" Reputation Points.");
				}
    	    	break;
			case MAX:
				player.sendMessage("Maximum Reputation Points is "+YariUserTracker.getConfig("config","repmax")+".");
    	    	break;
			case START:
				player.sendMessage("Starting Reputation Points is "+YariUserTracker.getConfig("config","repstart")+".");
    	    	break;
    	    default:
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
    	    	break;
		}
    	return;
    }
}
