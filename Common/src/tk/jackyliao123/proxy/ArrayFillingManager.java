package tk.jackyliao123.proxy;

import java.util.ArrayDeque;

public class ArrayFillingManager {
    public final Object data;
    private ArrayDeque<ArrayFiller> fillers;

    public ArrayFillingManager(Object extraData) {
        this.data = extraData;
        this.fillers = new ArrayDeque<ArrayFiller>();
    }

    public void fillArray(byte[] bytes) {
        fillers.addLast(new ArrayFiller(bytes));
    }

    public ArrayFiller popFiller() {
        return fillers.removeFirst();
    }
}
