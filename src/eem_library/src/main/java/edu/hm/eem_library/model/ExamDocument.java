package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

/** A class containing information on a specific document, which can be later checked by the client.
 *
 */
public class ExamDocument{
    private String name;
    private byte[] hash;
    private int pages;
    @Nullable private String path;

    protected ExamDocument(String name, final byte[] hash, final String path) {
        this.name = name;
        this.hash = hash;
        this.pages = 0;
        this.path = path;
    }

    protected ExamDocument(String name, int pages) {
        this.name = name;
        this.hash = null;
        this.pages = pages;
        this.path = null;
    }

    protected ExamDocument(String name, final byte[] hash, int pages, final String path){
        this.name = name;
        this.hash = hash;
        this.pages = pages;
        this.path = path;
    }

    void removePath(){
        path = null;
    }

    void update(int pages, byte[] hash){
        this.pages = pages;
        this.hash = hash;
    }

    // Getters needed for SnakeYAML
    public String getName() {
        return name;
    }

    public byte[] getHash() {
        return hash;
    }

    public int getPages() {
        return pages;
    }

    public String getPath() { return path; }
}
