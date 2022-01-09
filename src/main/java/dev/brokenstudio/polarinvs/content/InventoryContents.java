package dev.brokenstudio.polarinvs.content;

import dev.brokenstudio.polarinvs.ClickableItem;
import dev.brokenstudio.polarinvs.PolarInventory;
import dev.brokenstudio.polarinvs.opener.InventoryOpener;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class InventoryContents {

    private final PolarInventory inv;
    private Player player;
    private Pagination pagination;


    public InventoryContents(PolarInventory inv, Player player) {
        this.inv = inv;
        this.player = player;
        pagination = new Pagination(inv.getRows() * inv.getColumns());
    }

    public PolarInventory inventory(){
        return inv;
    }

    public Pagination pagination(){
        return this.pagination;
    }

    public ClickableItem[] all(){
        return this.pagination.getPageItems();
    }

    public Optional<SlotPos> firstEmpty(){
        for(int row = 1; row <= this.inv.getRows(); row++){
            for(int column = 1; column <= this.inv.getColumns(); column++){
                if(!this.get(row, column).isPresent()){
                    return Optional.of(SlotPos.of(row, column));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<ClickableItem> get(int row, int column){
        return this.pagination.get(row,column);
    }

    public Optional<ClickableItem> get(SlotPos pos){
        return get(pos.getRow(), pos.getColumn());
    }

    public InventoryContents set(SlotPos pos, ItemStack itemStack){
        return set(pos, new ClickableItem(itemStack));
    }

    public InventoryContents set(int row, int column, ItemStack itemStack){
        return set(row, column, new ClickableItem(itemStack));
    }

    public InventoryContents set(int row, int column, ClickableItem item){
        this.pagination.set(item, row, column);
        return this;
    }

    public InventoryContents set(SlotPos pos, ClickableItem item){
        return set(pos.getRow(), pos.getColumn(), item);
    }

    public InventoryContents fill(ClickableItem item){
        for(int row = 1; row <= this.inv.getRows(); row++){
            for(int column = 1; column <= this.inv.getColumns(); column++){
                set(row, column, item);
            }
        }
        return this;
    }

    public InventoryContents fill(ItemStack itemStack){
        return fill(new ClickableItem(itemStack));
    }

    public InventoryContents fillRow(int row, ClickableItem item){
        for(int i = 1; i <= this.inv.getColumns(); i++){
            set(row, i, item);
        }
        return this;
    }

    public InventoryContents fillRow(int row, ItemStack itemStack){
        return fillRow(row, new ClickableItem(itemStack));
    }

    public InventoryContents fillColumn(int column, ClickableItem item){
        for(int i = 1; i <= this.inv.getRows(); i++){
            set(i, column, item);
        }
        return this;
    }


    public InventoryContents fillBorder(ClickableItem clickableItem){
        fillRow(1, clickableItem);
        fillColumn(1, clickableItem);
        fillRow(this.inv.getRows(), clickableItem);
        fillColumn(this.inv.getColumns(), clickableItem)           ;
        return this;
    }

    public InventoryContents fillBorder(ItemStack itemStack){
        return fillBorder(new ClickableItem(itemStack));
    }

    public void nextPage(){
        if(this.pagination.isLast()){
            return;
        }
        this.pagination.next();
        Inventory handle = player.getOpenInventory().getTopInventory();
        handle.clear();
        ClickableItem[] items = this.all();
        for(int i = 0; i < items.length; i++){
            if(items[i] == null)
                continue;
            handle.setItem(i, items[i].getItemStack());
        }
        player.updateInventory();
    }

    public void previousPage(){
        if(this.pagination.isFirst()){
            return;
        }
        this.pagination.previous();
        Inventory handle = player.getOpenInventory().getTopInventory();
        handle.clear();
        ClickableItem[] items = this.all();
        for(int i = 0; i < items.length; i++){
            if(items[i] == null)
                continue;
            handle.setItem(i, items[i].getItemStack());
        }
        player.updateInventory();
    }

    public void page(int page){
        if(this.pagination.getCurrentPage() == page)
            return;
        this.pagination.page(page);
        Inventory handle = player.getOpenInventory().getTopInventory();
        handle.clear();
        ClickableItem[] items = this.all();
        for(int i = 0; i < items.length; i++){
            if(items[i] == null)
                continue;
            handle.setItem(i, items[i].getItemStack());
        }
        player.updateInventory();
    }



}
