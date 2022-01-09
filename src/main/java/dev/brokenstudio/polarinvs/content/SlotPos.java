package dev.brokenstudio.polarinvs.content;

public class SlotPos {

    private final int row;
    private final int column;

    private SlotPos(int row, int column){
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlotPos slotPos = (SlotPos) o;

        if (row != slotPos.row) return false;
        return column == slotPos.column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }

    public int toBukkitSlot(){
        return (9 * row)-(9 - column);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public static SlotPos of(int row, int column){
        return new SlotPos(row, column);
    }

}
