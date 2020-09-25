package com.ifyezedev.notanotebook;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class Notebook {

    private String userID;
    private String name;
    private int contents;
    private String notebookId;
    @ServerTimestamp private Date latestUpdateTime;
    private boolean archive;
    private int color;

    public Notebook(){
    }

    public Notebook(String userID, String notebookId, String name, int contents, int color, Date latestUpdateTime, boolean archive) {
        this.userID = userID;
        this.name = name;
        this.contents = contents;
        this.color = color;
        this.latestUpdateTime = latestUpdateTime;
        this.notebookId = notebookId;
        this.archive = archive;
    }

    public String getUserID() {
        return userID;
    }

    public String getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(String notebookId) {
        this.notebookId = notebookId;
    }

    public String getName() {
        return name;
    }

    public int getContents() {
        return contents;
    }

    public int getColor() {
        return color;
    }

    public Date getLatestUpdateTime() {
        return latestUpdateTime;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }
}
