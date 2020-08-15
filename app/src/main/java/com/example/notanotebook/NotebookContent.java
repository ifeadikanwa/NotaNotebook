package com.example.notanotebook;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class NotebookContent {
    private String notebookId;
    private String notebookContentId;
    private String title;
    private int color;
    private int priority;
    @ServerTimestamp private Date createdTime;
    @ServerTimestamp private Date latestUpdateTime;
    private boolean isNote;
    private String noteContent;
    private List<String> checklistContent;

    public NotebookContent() {
    }

    public NotebookContent(String notebookId, String notebookContentId, String title, int color, int priority, Date createdTime, Date latestUpdateTime, boolean isNote) {
        this.notebookId = notebookId;
        this.notebookContentId = notebookContentId;
        this.title = title;
        this.color = color;
        this.priority = priority;
        this.createdTime = createdTime;
        this.latestUpdateTime = latestUpdateTime;
        this.isNote = isNote;
    }

    public String getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(String notebookId) {
        this.notebookId = notebookId;
    }

    public String getNotebookContentId() {
        return notebookContentId;
    }

    public void setNotebookContentId(String notebookContentId) {
        this.notebookContentId = notebookContentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getColor() {
        return color;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLatestUpdateTime() {
        return latestUpdateTime;
    }

    public void setLatestUpdateTime(Date latestUpdateTime) {
        this.latestUpdateTime = latestUpdateTime;
    }

    public boolean isNote() {
        return isNote;
    }

    public void setNote(boolean note) {
        isNote = note;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public List<String> getChecklistContent() {
        return checklistContent;
    }

    public void setChecklistContent(List<String> checklistContent) {
        this.checklistContent = checklistContent;
    }
}
