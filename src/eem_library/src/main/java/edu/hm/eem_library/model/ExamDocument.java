package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

import java.util.Date;

/** A class containing information on a specific document, which can be later checked by the client.
 *
 */
public class ExamDocument {
    private String name;
    @Nullable private byte[] hash;
    @Nullable private byte[] nonAnnotatedHash;
    private int pages;
    @Nullable private String uriString;
    private Date hashCreationDate;

    static class Identifiers {
        int pages = 0;
        byte[] hash = null, nonAnnotatedHash = null;
        Date hashCreationDate = new Date(0);
    }

    protected ExamDocument(String name, @Nullable final String uriString) {
        this.name = name;
        this.hash =null;
        this.nonAnnotatedHash = null;
        this.pages = 0;
        this.uriString = uriString;
        this.hashCreationDate = new Date(0);
    }

    protected ExamDocument(String name, int pages) {
        this.name = name;
        this.hash = null;
        this.nonAnnotatedHash = null;
        this.pages = pages;
        this.uriString = null;
        this.hashCreationDate = new Date(0);
    }

    protected ExamDocument(String name, @Nullable final byte[] hash, @Nullable final byte[] nonAnnotatedHash, int pages, @Nullable final String uriString, Date hashCreationDate){
        this.name = name;
        this.hash = hash;
        this.nonAnnotatedHash = nonAnnotatedHash;
        this.pages = pages;
        this.uriString = uriString;
        this.hashCreationDate = hashCreationDate;
    }

    void removeUriString(){
        uriString = null;
    }

    public void removeHash() { hash = null; }

    public void removeNonAnnotatedHash() { nonAnnotatedHash = null; }

    void removeHashCreationDate() { hashCreationDate = new Date(0);  }

    void update(Identifiers id){
        this.pages = id.pages;
        this.hash = id.hash;
        this.nonAnnotatedHash = id.nonAnnotatedHash;
        this.hashCreationDate = id.hashCreationDate;
    }

    @Override
    public Object clone() {
        byte[] hash = null, nonAnnotatedHash = null;
        String uriString = null;
        Date hashCreationDate = null;
        if(this.hash!=null) hash = this.hash;
        if(this.nonAnnotatedHash!=null) nonAnnotatedHash = this.nonAnnotatedHash;
        if(this.uriString!=null) uriString = this.uriString;
        if(this.hashCreationDate!=null) hashCreationDate = this.hashCreationDate;
        return new ExamDocument(name, hash, nonAnnotatedHash, pages, uriString, hashCreationDate);
    }

    // Getters needed for SnakeYAML
    public String getName() {
        return name;
    }

    public byte[] getHash() {
        return hash;
    }

    byte[] getNonAnnotatedHash() { return nonAnnotatedHash; }

    public int getPages() {
        return pages;
    }

    @Nullable
    public String getUriString() { return uriString; }

    @Nullable
    Date getHashCreationDate() {
        return hashCreationDate;
    }
}
