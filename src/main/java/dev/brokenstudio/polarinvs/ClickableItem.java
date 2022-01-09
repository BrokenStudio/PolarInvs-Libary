package dev.brokenstudio.polarinvs;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClickableItem {

    private ItemStack itemStack;
    private Consumer<InventoryClickEvent> clickConsumer;

    public ClickableItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.clickConsumer = inventoryClickEvent -> {

        };
    }

    public ClickableItem(ItemStack itemStack, Consumer<InventoryClickEvent> consumer){
        this.itemStack = itemStack;
        this.clickConsumer = consumer;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void run(InventoryClickEvent e){
        clickConsumer.accept(e);
    }

}
