package com.ifyezedev.notanotebook;

public class Checklist_Item {
    private String item_id;
    private String item;
    private boolean checked;

    public Checklist_Item(){
    }

    public Checklist_Item(String item, boolean checked) {
        this.item = item;
        this.checked = checked;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
