package dev.brokenstudio.polarinvs;

import dev.brokenstudio.polarinvs.content.InventoryContents;
import dev.brokenstudio.polarinvs.content.InventoryProvider;
import dev.brokenstudio.polarinvs.opener.InventoryOpener;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class PolarInventory {

    private String id, title;
    private final InventoryType type;
    private final int rows;
    private final int columns;
    private boolean closeable;

    private final InventoryProvider provider;
    private PolarInventory parent;

    private List<InventoryListener<? extends Event>> listeners;
    private final InventoryManager inventoryManager;

    public PolarInventory(InventoryType type, int rows, int columns, InventoryProvider provider, InventoryManager inventoryManager) {
        this.type = type;
        this.rows = rows;
        this.columns = columns;
        this.provider = provider;
        this.inventoryManager = inventoryManager;
    }

    public Inventory open(Player player){
        return open(player,1);
    }
    public Inventory open(Player player, int page){
        Optional<PolarInventory> oldInv = this.inventoryManager.getInventory(player);

        oldInv.ifPresent(inv -> {
            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryCloseEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener)
                            .accept(new InventoryCloseEvent(player.getOpenInventory())));
            this.inventoryManager.setInventory(player, null);
        });
        InventoryContents contents = new InventoryContents(this, player);
        if(page > 1)
            contents.pagination().pages(page);
        contents.pagination().page(page);
        this.inventoryManager.setContents(player, contents);

        try {
            this.provider.init(player, contents);

            if(!this.inventoryManager.getContents(player).equals(Optional.of(contents)))
                return null;

            InventoryOpener opener = this.inventoryManager.findOpener(type)
                    .orElseThrow(() -> new IllegalStateException("No opener found for the inventory type " + type.name()));
            Inventory handle = opener.open(this, player);
            this.inventoryManager.setInventory(player, this);

            return handle;
        }catch (Exception ex){
            this.inventoryManager.handleInventoryOpenError(this, player, ex);
            return null;
        }
    }

    public void close(Player player){
        listeners.stream()
                .filter(listener -> listener.getType() == InventoryCloseEvent.class)
                .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener)
                        .accept(new InventoryCloseEvent(player.getOpenInventory())));

        this.inventoryManager.setInventory(player, null);
        player.closeInventory();
        this.inventoryManager.setContents(player, null);
    }

    public int getRows() {
        return rows;
    }

    public InventoryProvider getProvider() {
        return provider;
    }

    public int getColumns() {
        return columns;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public InventoryType getType() {
        return type;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    public Optional<PolarInventory> getParent() {
        return Optional.ofNullable(parent);
    }

    public void setParent(PolarInventory parent) {
        this.parent = parent;
    }

    public List<InventoryListener<? extends Event>> getListeners() {
        return listeners;
    }

    public void setListeners(List<InventoryListener<? extends Event>> listeners) {
        this.listeners = listeners;
    }

    public static Builder builder(){ return new Builder(); }

    public static final class Builder{

        private String id = "unknown";
        private String title = "";
        private InventoryType inventoryType = InventoryType.CHEST;
        private int rows = 6;
        private int columns = 9;
        private boolean closeable = true;

        private InventoryManager inventoryManager;
        private InventoryProvider provider;
        private PolarInventory parent;

        private List<InventoryListener<? extends Event>> listeners = new ArrayList<>();

        private Builder(){}

        public Builder id(String id){
            this.id = id;
            return this;
        }

        public Builder title(String title){
            this.title = title;
            return this;
        }

        public Builder type(InventoryType type){
            this.inventoryType = type;
            return this;
        }

        public Builder size(int rows, int columns){
            this.rows = rows;
            this.columns = columns;
            return this;
        }

        public Builder closeable(boolean closeable){
            this.closeable = closeable;
            return this;
        }

        public Builder provider(InventoryProvider provider){
            this.provider = provider;
            return this;
        }

        public Builder listener(InventoryListener<? extends Event> listener) {
            this.listeners.add(listener);
            return this;
        }

        public Builder parent(PolarInventory parent){
            this.parent = parent;
            return this;
        }

        public Builder manager(InventoryManager manager) {
            this.inventoryManager = manager;
            return this;
        }

        public PolarInventory build(){
            if(this.provider == null)
                throw new IllegalStateException("The provider of the PolarInventory.Builder must be set.");
            InventoryManager manager = this.inventoryManager != null ? this.inventoryManager : InventoryManager.getDefaultManager();
            if(manager == null)
                throw new IllegalStateException("There was no InventoryManager for PolarInventory.Builder provided. And the default InventoryManager wasn't set.");
            PolarInventory inv = new PolarInventory(this.inventoryType, this.rows, this.columns, this.provider, manager);
            inv.id = this.id;
            inv.title = this.title;
            inv.closeable = this.closeable;
            inv.parent = this.parent;
            inv.listeners = this.listeners;

            return inv;
        }

    }

}
