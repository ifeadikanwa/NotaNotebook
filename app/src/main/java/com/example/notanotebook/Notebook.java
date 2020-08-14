package com.example.notanotebook;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Notebook {
    /**
     * @param contents : Hashmap containing notes
     */

    private String name;
    private int contents;
    private String notebookId;
    @ServerTimestamp private Date latestUpdateTime;
    private boolean archive;

    public Notebook(){
    }

    public Notebook(String notebookId, String name, int contents, Date latestUpdateTime, boolean archive) {
        this.name = name;
        this.contents = contents;
        this.latestUpdateTime = latestUpdateTime;
        this.notebookId = notebookId;
        this.archive = archive;
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
