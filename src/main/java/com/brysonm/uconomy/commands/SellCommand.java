package com.brysonm.uconomy.commands;

import com.brysonm.uconomy.ItemUtils;
import com.brysonm.uconomy.SaleUtils;
import com.brysonm.uconomy.uConomy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SellCommand implements CommandExecutor {

    private static final String ITEMINHAND_KEYWORD = "iteminhand";

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {

            final Player player = (Player) sender;

            if(!player.hasPermission("uconomy." + cmd.getName().toLowerCase())) {

                player.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");

                return true;

            }

            if(args.length == 3) {

                try {

                    final int amount = Integer.parseInt(args[0]);

                    if(amount <= 0) {

                        player.sendMessage(ChatColor.RED + "You must use positive integers to sell items.");

                        return true;

                    }

                    final String item = args[1];
                    
                    final Material material = getMaterialToSell(player, item);

        		    if(material == null) {

        		        player.sendMessage(ChatColor.RED + "You cannot sell this item.");

        		        return true;

        		    }                     
                    
                    double price = Double.parseDouble(args[2]);

                    int count = 0;

                    for(ItemStack is : player.getInventory().getContents()) {

                        if(is != null && is.getType() == material && isSellable(is)) {

                            count += is.getAmount();

                        }

                    }

                    if(amount > count) {

                        player.sendMessage(ChatColor.RED + "You only have " + count + " " + ItemUtils.toFriendlyName(material) + ".");

                        return true;

                    }

                    final double unitPrice = price / amount;

                    new BukkitRunnable() {

                        public void run() {

                            for(int i = 0; i < amount; i++) {

                                SaleUtils.constructSale(player, material, unitPrice);

                            }

                        }

                    }.runTaskAsynchronously(uConomy.getInstance());

                    player.getInventory().removeItem(new ItemStack(material, amount));

                    player.sendMessage(ChatColor.GRAY + "You have put " + amount + " " + ItemUtils.toFriendlyName(material) + " on the market for " + price + " gold.");

                    return true;

                } catch(NumberFormatException ex) {

                    printSellCommandUsage(player);

                    return true;

                }

            } else {

                printSellCommandUsage(player);

                return true;

            }

        }
        return true;
    }

	private static void printSellCommandUsage(final Player player) {
		
		player.sendMessage(ChatColor.RED + "/sell <amount> <item> <price>");
		player.sendMessage(ChatColor.RED + "or");
		player.sendMessage(ChatColor.RED + "/sell <amount> " + ITEMINHAND_KEYWORD + " <price>");
		
	}

	private static Material getMaterialToSell(final Player player, final String item) {
		
		Material material = null;
		
		if(item.equalsIgnoreCase(ITEMINHAND_KEYWORD)) {
			
			if (isSellable(player.getItemInHand())) {
				material = player.getItemInHand().getType();
			}
			
		} else {
			
			material = Material.matchMaterial(item.toUpperCase());
			
		}
		
		return material;
		
	}

	private static boolean isSellable(ItemStack itemStack) {
		return itemStack.getData().getData() == 0x0 
				&& ! itemStack.hasItemMeta();
	}

}
