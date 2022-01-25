package dev.brokenstudio.polarinvs;

import dev.brokenstudio.polarinvs.content.InventoryContents;
import dev.brokenstudio.polarinvs.opener.ChestInventoryOpener;
import dev.brokenstudio.polarinvs.opener.InventoryOpener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

public class InventoryManager {

    private static InventoryManager defaultManager;

    private final JavaPlugin plugin;
    private final PluginManager pluginManager;

    private Map<Player, PolarInventory> inventories;
    private Map<Player, InventoryContents> contents;

    private List<InventoryOpener> defaultOpeners;
    private List<InventoryOpener> openers;

    public InventoryManager(JavaPlugin plugin){
        this.plugin = plugin;
        this.pluginManager = this.plugin.getServer().getPluginManager();
        _initialize();
    }

    private void _initialize(){
        this.inventories = new HashMap<>();
        this.contents = new HashMap<>();
        this.defaultOpeners = new ArrayList<>();
        this.defaultOpeners.add(new ChestInventoryOpener());
        this.openers = new ArrayList<>();
        this.plugin.getServer().getPluginManager().registerEvents(new InvListener(), this.plugin);
        new InvTask().runTaskTimer(plugin,1,1);
    }

    public Optional<InventoryOpener> findOpener(InventoryType type){
        Optional<InventoryOpener> opInv = this.openers.stream()
                .filter(opener -> opener.supports(type))
                .findAny();
        if(!opInv.isPresent()){
            opInv = this.defaultOpeners.stream()
                    .filter(opener -> opener.supports(type))
                    .findAny();
        }

        return opInv;
    }

    public void registerOpener(InventoryOpener... openers){
        this.openers.addAll(Arrays.asList(openers));
    }

    public List<Player> getOpenedPlayers(PolarInventory inventory){
        List<Player> list = new ArrayList<>();

        this.inventories.forEach((player, inv)->{
            if(inventory.equals(inv))
                list.add(player);
        });
        return list;
    }

    public Optional<PolarInventory> getInventory(Player player){
        return Optional.ofNullable(this.inventories.get(player));
    }

    protected void setInventory(Player player, PolarInventory inventory){
        if(inventory == null)
            this.inventories.remove(player);
        else
            this.inventories.put(player, inventory);
    }

    public Optional<InventoryContents> getContents(Player player){
        return Optional.ofNullable(this.contents.get(player));
    }

    protected void setContents(Player player, InventoryContents inventoryContents){
        if(inventoryContents == null)
            this.contents.remove(player);
        else
            this.contents.put(player, inventoryContents);
    }

    public void handleInventoryOpenError(PolarInventory inventory, Player player, Exception ex){
        inventory.close(player);

        Bukkit.getLogger().log(Level.SEVERE, "Error while opening PolarInventory:", ex);
    }

    public void handleInventoryUpdateError(PolarInventory inventory, Player player, Exception ex){
        inventory.close(player);

        Bukkit.getLogger().log(Level.SEVERE, "Error while updating PolarInventory:", ex);
    }



    class InvListener implements Listener {

        @EventHandler(priority = EventPriority.LOW)
        public void onClick(InventoryClickEvent e){

            if(!(e.getWhoClicked() instanceof Player))
                return;

            Player p = (Player) e.getWhoClicked();

            if(!inventories.containsKey(p))
                return;

            // Restrict putting items from the bottom inventory into the top inventory
            Inventory clickedInventory = e.getClickedInventory();
            if (clickedInventory == p.getOpenInventory().getBottomInventory()) {
                if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    e.setCancelled(true);
                    return;
                }

                if (e.getAction() == InventoryAction.NOTHING && e.getClick() != ClickType.MIDDLE) {
                    e.setCancelled(true);
                    return;
                }
            }

            if(clickedInventory == p.getOpenInventory().getTopInventory()){
                e.setCancelled(true);
                int row = (e.getSlot() / 9) + 1;
                int column = (e.getSlot() % 9) + 1;

                if(row < 1 || column < 1){
                    return;
                }

                PolarInventory inv = inventories.get(p);

                if(row > inv.getRows() || column > inv.getColumns()){
                    return;
                }
                inv.getListeners().stream()
                        .filter(listener -> listener.getType() == InventoryClickEvent.class)
                        .forEach(listener -> ((InventoryListener<InventoryClickEvent>) listener).accept(e));
                contents.get(p).get(row, column).ifPresent(item -> item.run(e));

                p.updateInventory();
            }

        }

        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryDrag(InventoryDragEvent e) {
            Player p = (Player) e.getWhoClicked();

            if (!inventories.containsKey(p))
                return;

            PolarInventory inv = inventories.get(p);

            for (int slot : e.getRawSlots()) {
                if (slot >= p.getOpenInventory().getTopInventory().getSize())
                    continue;

                e.setCancelled(true);
                break;
            }

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryDragEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryDragEvent>) listener).accept(e));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryOpen(InventoryOpenEvent e) {
            Player p = (Player) e.getPlayer();

            if (!inventories.containsKey(p))
                return;

            PolarInventory inv = inventories.get(p);

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryOpenEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryOpenEvent>) listener).accept(e));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryClose(InventoryCloseEvent e) {
            Player p = (Player) e.getPlayer();

            if (!inventories.containsKey(p))
                return;

            PolarInventory inv = inventories.get(p);

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryCloseEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener).accept(e));

            if (inv.isCloseable()) {
                e.getInventory().clear();

                if(inv.getParent().isPresent()){
                    inv.getParent().get().open(p);
                }else{
                    inventories.remove(p);
                    contents.remove(p);
                }


            } else
                Bukkit.getScheduler().runTask(plugin, () -> p.openInventory(e.getInventory()));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onPlayerQuit(PlayerQuitEvent e) {
            Player p = e.getPlayer();

            if (!inventories.containsKey(p))
                return;

            PolarInventory inv = inventories.get(p);

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == PlayerQuitEvent.class)
                    .forEach(listener -> ((InventoryListener<PlayerQuitEvent>) listener).accept(e));

            inventories.remove(p);
            contents.remove(p);
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onPluginDisable(PluginDisableEvent e) {
            new HashMap<>(inventories).forEach((player, inv) -> {
                inv.getListeners().stream()
                        .filter(listener -> listener.getType() == PluginDisableEvent.class)
                        .forEach(listener -> ((InventoryListener<PluginDisableEvent>) listener).accept(e));

                inv.close(player);
            });

            inventories.clear();
            contents.clear();
        }

    }



    class InvTask extends BukkitRunnable{
        @Override
        public void run() {
            new HashMap<>(inventories).forEach((player, inv)->{
                try {
                    inv.getProvider().update(player, contents.get(player));
                }catch (Exception e){
                    handleInventoryUpdateError(inv, player, e);
                }
            });
        }
    }

    public static InventoryManager getDefaultManager() {
        return defaultManager;
    }

    public static void setDefaultManager(InventoryManager defaultManager) {
        InventoryManager.defaultManager = defaultManager;
    }
}
