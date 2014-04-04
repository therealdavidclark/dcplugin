package com.gmail.therealdavidclark.dcplugin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class dcplugin extends JavaPlugin {

	public Map<String, Double[]> saved_locations = new HashMap<>();
	public String saved_locations_file = ".\\plugins\\saved_locations.bin";

	// getDataFolder() + File.separator +

	@Override
	public void onEnable() {
		try {
			saved_locations = SLAPI.load(saved_locations_file);
			getLogger().info("****************************************");
			getLogger().info("Locations loaded");
			getLogger().info("****************************************");
		} catch (Exception e) {
			// handle the exception
			// e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		try {
			SLAPI.save(saved_locations, saved_locations_file);
			getLogger().info("****************************************");
			getLogger().info("Locations saved");
			getLogger().info("****************************************");
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;

		try {
			// Get the player's location.
			Location loc = ((Player) sender).getLocation();
			World world = loc.getWorld();
			
			/*
			getLogger().info("cmd: " + cmd.getName());
			getLogger().info("label: " + label);
			getLogger().info("args: " + args.length);
			*/	

			switch (cmd.getName().toLowerCase()) {
			case "dchelp":
			case "dc":
				sender.sendMessage("/dchouse - Build a house");
				sender.sendMessage("/dcgetpos - Returns current location");
				sender.sendMessage("/dcgoto x y z - Sends the player to location x y z");
				return true;

			case "dchouse":
				int x1 = loc.getBlockX();
				int y1 = loc.getBlockY();
				int z1 = loc.getBlockZ();
				
				int hSize = 3;
				int hHeight = 3;
				
				//input custom size
				if(args.length>=1){
					if(isInt(args[0])){
						hSize = Math.abs(Integer.parseInt(args[0]));
						if(hSize<3)hSize=3;
					}
				}
				//input custom height
				if(args.length>=2){
					if(isInt(args[1])){
						hHeight = Math.abs(Integer.parseInt(args[1]));
						if(hHeight<3)hHeight=3;
					}
				}
				
				//getLogger().info("house size: " + hSize);
				//getLogger().info("house height: " + hHeight);
				
				// Clear space
				for (int yPoint = y1 - 1; yPoint <= y1 + hHeight + 2; yPoint++) {
					for (int xPoint = x1 - hSize - 2; xPoint <= x1 + hSize + 2; xPoint++) {
						for (int zPoint = z1 - hSize - 2; zPoint <= z1 + hSize + 2; zPoint++) {
							makeBlock(world, Material.AIR, xPoint, yPoint, zPoint);
						}
					}
				}

				// Walls
				for(int y = y1; y <= y1 + hHeight - 1; y++){
					for(int x = x1 - hSize; x <= x1 + hSize; x++){
						for(int z = z1 - hSize; z <= z1 + hSize; z++){
							if(Math.abs(x1-x)==hSize || Math.abs(z1-z)==hSize){
								makeBlock(world, Material.BRICK, x, y, z);
							}
						}
					}
				}
				
				// Roof
				int offset = hSize;
				for(int y = y1 + hHeight; y <= y1 + hHeight + hSize; y++){
					for(int x = x1 - offset; x <= x1 + offset; x++){
						for(int z = z1 - offset; z <= z1 + offset; z++){
							if(Math.abs(x1-x)==offset || Math.abs(z1-z)==offset){
								if(offset<=1){
									makeBlock(world, Material.GLASS, x, y, z);
								}else{
									makeBlock(world, Material.SMOOTH_BRICK, x, y, z);
								}
							}
						}
					}
					offset--;
				}

/*				
				for (int xPoint = x1 - hSize; xPoint <= x1 + hSize; xPoint++) {
					for (int zPoint = z1 - hSize; zPoint <= z1 + hSize; zPoint++) {
						int offset = hHeight + hSize - Math.abs(x1 - xPoint) - Math.abs(z1 - zPoint);
						getLogger().info("roof offset: " + offset);
						//makeBlock(world, Material.COBBLESTONE, xPoint, y1 + hHeight, zPoint);
						makeBlock(world, Material.COBBLESTONE, xPoint, y1 + offset, zPoint);
					}
				}
*/
				// floor and fence and outer torch
				for (int xPoint = x1 - hSize - 2; xPoint <= x1 + hSize + 2; xPoint++) {
					for (int zPoint = z1 - hSize - 2; zPoint <= z1 + hSize + 2; zPoint++) {
						if (zPoint == z1 || xPoint == x1) {
							makeBlock(world, Material.SMOOTH_BRICK, xPoint, y1 - 1, zPoint, (byte) 3);
						} else {
							makeBlock(world, Material.SMOOTH_BRICK, xPoint, y1 - 1, zPoint);
						}
						if (zPoint == z1 - hSize - 2 || zPoint == z1 + hSize + 2 || xPoint == x1 - hSize - 2 || xPoint == x1 + hSize + 2) {
							makeBlock(world, Material.FENCE, xPoint, y1, zPoint);
						}
						if (zPoint == z1 - hSize - 1 || zPoint == z1 + hSize + 1 || xPoint == x1 - hSize - 1 || xPoint == x1 + hSize + 1) {
							makeBlock(world, Material.TORCH, xPoint, y1 + hHeight, zPoint);
						}
					}
				}
				makeBlock(world, Material.FENCE_GATE, x1 - hSize - 2, y1, z1, (byte) 3);

				// inner torches
				makeBlock(world, Material.TORCH, x1 + hSize - 1, y1 + hHeight - 1, z1 + hSize - 1);
				makeBlock(world, Material.TORCH, x1 - hSize + 1, y1 + hHeight - 1, z1 + hSize - 1);
				makeBlock(world, Material.TORCH, x1 - hSize + 1, y1 + hHeight - 1, z1 - hSize + 1);
				makeBlock(world, Material.TORCH, x1 + hSize - 1, y1 + hHeight - 1, z1 - hSize + 1);

				// house windows
				for (int a = -1; a <= 1; a++) {
					makeBlock(world, Material.GLASS, x1 + a, y1 + 1, z1 - hSize);
					makeBlock(world, Material.GLASS, x1 + a, y1 + 1, z1 + hSize);
					makeBlock(world, Material.GLASS, x1 - hSize, y1 + 1, z1 + a);
					makeBlock(world, Material.GLASS, x1 + hSize, y1 + 1, z1 + a);
				}

				// door
				Block doorTop = world.getBlockAt(x1 - hSize, y1 + 1, z1);
				Block doorBottom = world.getBlockAt(x1 - hSize, y1, z1);

				doorTop.setData((byte) 8);
				doorBottom.setData((byte) 0);

				doorBottom.setTypeId(64);
				doorTop.setTypeId(64);

				// house stuff
				makeBlock(world, Material.FURNACE, x1 + hSize - 1, y1, z1 - 1);
				makeBlock(world, Material.WORKBENCH, x1 + hSize - 1, y1, z1 + 1);
				makeBlock(world, Material.CHEST, x1 + hSize - 1, y1, z1);
				sender.sendMessage("That's a house!");
				return true;

			case "dcsaveloc":
				if (args.length < 1) {
					sender.sendMessage("Missing argument(s)");
					return false;
				}
				Double[] myLoc = { loc.getX(), loc.getY()+1, loc.getZ() };
				saved_locations.put(args[0], myLoc);
				sender.sendMessage("Location saved");
				return true;

			case "dclistloc":
				sender.sendMessage("Locations:");
				// for (String key : saved_locations.keySet())
				// sender.sendMessage(key);

				Double mx1 = loc.getX();
				Double my1 = loc.getY();
				Double mz1 = loc.getZ();

				for (Map.Entry<String, Double[]> entry : saved_locations.entrySet()) {
					String key = entry.getKey();
					Double[] value = entry.getValue();

					Double mx2 = value[0];
					Double my2 = value[1];
					Double mz2 = value[2];

					Double dist = Math.floor(Math.sqrt(Math.pow(mx2 - mx1, 2) + Math.pow(my2 - my1, 2) + Math.pow(mz2 - mz1, 2)));
					sender.sendMessage(key + " (" + String.valueOf(dist) + " blocks away)");

				}

				return true;

			case "dcgoloc":
				if (args.length != 1) {
					sender.sendMessage("Missing argument(s)");
					return false;
				}

				for (Map.Entry<String, Double[]> entry : saved_locations
						.entrySet()) {
					String key = entry.getKey();
					Double[] value = entry.getValue();
					if (key.equalsIgnoreCase(args[0])) {
						Player player = (Player) sender;
						try {
							player.teleport(new Location(player.getWorld(),
									value[0], value[1], value[2]));
							break;
						} catch (NumberFormatException ex) {
							player.sendMessage("Given location is invalid");
						}

					}
				}
				return true;

			case "dcbang":
				Player me = (Player) sender;
				world.strikeLightning( me.getTargetBlock(null, 200).getLocation() );
				return true;
			}

		} catch (Exception ex) {
			sender.sendMessage("Something went wrong: " + ex.getMessage());
			return false;
		}

		return false;
	}

	public Block makeBlock(World world, Material m, int x, int y, int z) {
		try {
			Block currentBlock = world.getBlockAt(x, y, z);
			currentBlock.setType(m);
			return currentBlock;
		} catch (Exception ex) {
			getLogger().info("Makeblock error: " + ex.getMessage());
			return world.getBlockAt(x, y, z);
		}
	}

	@SuppressWarnings("deprecation")
	public Block makeBlock(World world, Material m, int x, int y, int z,
			byte data) {
		try {
			Block currentBlock = world.getBlockAt(x, y, z);
			currentBlock.setData(data);
			currentBlock.setType(m);
			return currentBlock;
		} catch (Exception ex) {
			getLogger().info("Makeblock error: " + ex.getMessage());
			return world.getBlockAt(x, y, z);
		}
	}

	public boolean addStack(Chest chest, Material material) {
		ItemStack itemstack = new ItemStack(material, 64);
		chest.getBlockInventory().addItem(itemstack);
		return true;
	}
	
	public static boolean isInt(String strNum) {
	    boolean ret = true;
	    try {
	    	Integer.parseInt(strNum);
	    }catch (NumberFormatException e) {
	        ret = false;
	    }
	    return ret;
	}
}
