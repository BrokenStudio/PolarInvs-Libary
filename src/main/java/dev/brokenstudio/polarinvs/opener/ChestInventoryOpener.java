package dev.brokenstudio.polarinvs.opener;

import com.google.common.base.Preconditions;
import dev.brokenstudio.polarinvs.InventoryManager;
import dev.brokenstudio.polarinvs.PolarInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class ChestInventoryOpener implements InventoryOpener {

    @Override
    public Inventory open(PolarInventory inventory, Player player) {
        Preconditions.checkArgument(inventory.getColumns() == 9,
                "The column count for the chest inventory must be 9, found: %s", inventory.getColumns());
        Preconditions.checkArgument(inventory.getRows() > 0 && inventory.getRows() < 7,
                "The row count for the chest inventory must be between 1 and 6, found: %s", inventory.getRows());
        InventoryManager manager = inventory.getInventoryManager();
        Inventory handle = Bukkit.createInventory(player, inventory.getRows() * inventory.getColumns(), inventory.getTitle());

        fill(handle, manager.getContents(player).get());

        player.openInventory(handle);
        return handle;
    }

    @Override
    public boolean supports(InventoryType type) {
        return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST || type == InventoryType.BARREL;
    }
}
