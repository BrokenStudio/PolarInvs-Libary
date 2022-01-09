package dev.brokenstudio.polarinvs.opener;

import dev.brokenstudio.polarinvs.ClickableItem;
import dev.brokenstudio.polarinvs.PolarInventory;
import dev.brokenstudio.polarinvs.content.InventoryContents;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public interface InventoryOpener {

    Inventory open(PolarInventory inventory, Player player);
    boolean supports(InventoryType type);

    default void fill(Inventory handle, InventoryContents contents){
        ClickableItem[] items = contents.all();
        for(int i = 0; i < items.length; i++){
            if(items[i] == null)
                continue;
            handle.setItem(i, items[i].getItemStack());
        }
    }

}
