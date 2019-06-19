package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

/** A class containing information on a specific document, which can be later checked by the client.
 *
 */
public class ExamDocument {
    private String name;
    private byte[] hash;
    private byte[] nonAnnotatedHash;
    private int pages;
    @Nullable private String uriString;

    static class Identifiers {
        int pages;
        byte[] hash;
        byte[] nonAnnotatedHash;

        Identifiers() {
            this.pages = 0;
            this.hash = null;
            this.nonAnnotatedHash = null;
        }
    }

    protected ExamDocument(String name, @Nullable final String uriString) {
        this.name = name;
        this.hash =null;
        this.nonAnnotatedHash = null;
        this.pages = 0;
        this.uriString = uriString;
    }

    protected ExamDocument(String name, int pages) {
        this.name = name;
        this.hash = null;
        this.nonAnnotatedHash = null;
        this.pages = pages;
        this.uriString = null;
    }

    protected ExamDocument(String name, final byte[] hash, final byte[] nonAnnotatedHash, int pages, @Nullable final String uriString){
        this.name = name;
        this.hash = hash;
        this.nonAnnotatedHash = nonAnnotatedHash;
        this.pages = pages;
        this.uriString = uriString;
    }

    void removeUriString(){
        uriString = null;
    }

    public void removeHash() { hash = null; }

    public void removeNonAnnotatedHash() { nonAnnotatedHash = null; }

    void update(Identifiers id){
        this.pages = id.pages;
        this.hash = id.hash;
        this.nonAnnotatedHash = id.nonAnnotatedHash;
    }

    // Getters needed for SnakeYAML
    public String getName() {
        return name;
    }

    byte[] getHash() {
        return hash;
    }

    byte[] getNonAnnotatedHash() { return nonAnnotatedHash; }

    public int getPages() {
        return pages;
    }

    @Nullable
    public String getUriString() { return uriString; }
}
