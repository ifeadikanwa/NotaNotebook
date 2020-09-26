package com.ifyezedev.notanotebook;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Checklist_Item {
    private String item_id;
    private String item;
    private boolean checked;
    @ServerTimestamp private Date entryTime;

    public Checklist_Item(){
    }

    public Checklist_Item(String item, boolean checked, Date entryTime) {
        this.item = item;
        this.checked = checked;
        this.entryTime = entryTime;
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

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }
}
