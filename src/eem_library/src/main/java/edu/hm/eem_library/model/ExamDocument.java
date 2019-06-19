package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

/** A class containing information on a specific document, which can be later checked by the client.
 *
 */
public class ExamDocument{
    private String name;
    private byte[] hash;
    private byte[] nonAnnotatedHash;
    private int pages;
    @Nullable private String uri;

    static class Identifiers {
        int pages;
        byte[] hash;
        byte[] nonAnnotatedHash;

        public Identifiers() {
            this.pages = 0;
            this.hash = null;
            this.nonAnnotatedHash = null;
        }
    }

    protected ExamDocument(String name, final String uri) {
        this.name = name;
        this.hash =null;
        this.nonAnnotatedHash = null;
        this.pages = 0;
        this.uri = uri;
    }

    protected ExamDocument(String name, int pages) {
        this.name = name;
        this.hash = null;
        this.nonAnnotatedHash = null;
        this.pages = pages;
        this.uri = null;
    }

    protected ExamDocument(String name, final byte[] hash, final byte[] nonAnnotatedHash, int pages, final String uri){
        this.name = name;
        this.hash = hash;
        this.nonAnnotatedHash = nonAnnotatedHash;
        this.pages = pages;
        this.uri = uri;
    }

    void removeUri(){
        uri = null;
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

    public byte[] getHash() {
        return hash;
    }

    public byte[] getNonAnnotatedHash() { return nonAnnotatedHash; }

    public int getPages() {
        return pages;
    }

    public String getUri() { return uri; }
}
