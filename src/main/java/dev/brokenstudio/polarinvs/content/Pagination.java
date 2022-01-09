package dev.brokenstudio.polarinvs.content;

import dev.brokenstudio.polarinvs.ClickableItem;

import java.util.Arrays;
import java.util.Optional;

public class Pagination {

    private  int pages;
    private final int itemsPerPage;
    private ClickableItem[] items;
    private int currentPage;

    public Pagination(int itemsPerPage){
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 1;
        pages(1);
    }

    public ClickableItem[] getPageItems(){
        return Arrays.copyOfRange(items, (this.currentPage-1) * this.itemsPerPage,(this.currentPage * itemsPerPage));
    }

    public ClickableItem[] getPageItems(int page){
        return Arrays.copyOfRange(items, (page-1) * this.itemsPerPage,(page * itemsPerPage));
    }

    public int getPages() {
        return pages;
    }

    public Pagination page(int page){
        this.currentPage = page;
        return this;
    }

    public boolean isFirst(){
        return this.currentPage == 1;
    }

    public boolean isLast(){
        return this.currentPage == this.pages;
    }

    public Pagination first(){
        this.currentPage = 1;
        return this;
    }

    public Pagination previous(){
        if(!isFirst())
            this.currentPage--;
        return this;
    }

    public Pagination next(){
        if(!isLast())
            this.currentPage++;
        return this;
    }

    public Pagination last(){
        this.currentPage = this.pages;
        return this;
    }

    public Pagination setItem(ClickableItem item, SlotPos pos){
        int finalPos = pos.toBukkitSlot();
        int arrayPos = ((this.currentPage-1) * this.itemsPerPage) + finalPos;
        this.items[arrayPos] = item;
        return this;
    }

    public Pagination setItem(ClickableItem item, SlotPos pos, int page){
        if(page > this.pages)
            return this;

        int finalPos = pos.toBukkitSlot();
        int arrayPos = ((page-1) * this.itemsPerPage) + finalPos;
        this.items[arrayPos] = item;
        return this;
    }

    public Pagination setPageItems(ClickableItem[] items){
        if(items.length != this.itemsPerPage)
            return this;

        int firsPos = (this.currentPage-1) * this.itemsPerPage;
        int lastPos = (this.currentPage * this.itemsPerPage)-1;
        for(int i = firsPos; i <= lastPos; i++){
            this.items[i] = items[i-firsPos];
        }
        return this;
    }

    public Pagination setPageItems(ClickableItem[] items, int page){
        if(items.length != this.itemsPerPage)
            return this;

        int firsPos = (page-1) * this.itemsPerPage;
        int lastPos = (page * this.itemsPerPage)-1;
        for(int i = firsPos; i <= lastPos; i++){
            this.items[i] = items[i-firsPos];
        }
        return this;
    }

    public void pages(int pages){
        this.pages = pages;
        items = new ClickableItem[pages * itemsPerPage];
        Arrays.fill(items, null);
    }

    public Optional<ClickableItem> get(SlotPos pos){
        return get(pos.getRow(), pos.getColumn());
    }

    public Optional<ClickableItem> get(int row, int column){
        int arrayPos = ((this.currentPage-1) * this.itemsPerPage) + (((row-1)*9) + (column-1));
        if(arrayPos >= this.items.length)
            return Optional.empty();
        return Optional.ofNullable(this.items[arrayPos]);
    }

    public Optional<ClickableItem> get(SlotPos pos, int page){
        return get(pos.getRow(), pos.getColumn(), page);
    }

    public Optional<ClickableItem> get(int row, int column, int page){
        int arrayPos = ((page-1) * this.itemsPerPage) + (((row-1)*9) + (column-1));
        if(arrayPos >= this.items.length)
            return Optional.empty();
        return Optional.ofNullable(this.items[arrayPos]);
    }

    public Pagination set(ClickableItem item, SlotPos pos){
        return set(item, pos.getRow(), pos.getColumn());
    }

    public Pagination set(ClickableItem item, int row, int column){
        int arrayPos = ((this.currentPage-1) * this.itemsPerPage) + (((row-1)*9) + (column-1));
        if(arrayPos >= this.items.length)
            return this;

        this.items[arrayPos] = item;
        return this;
    }

    public Pagination set(ClickableItem item, SlotPos pos, int page){
        return set(item, pos.getRow(), pos.getColumn(), page);
    }

    public Pagination set(ClickableItem item, int row, int column, int page){
        int arrayPos = ((page-1) * this.itemsPerPage) + (((row-1)*9) + (column-1));
        if(arrayPos >= this.items.length)
            return this;

        this.items[arrayPos] = item;
        return this;
    }


    public int itemsPerPage(){
        return this.itemsPerPage;
    }

}
