package edu.hm.eem_library.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

/**
 * A class containing information on a specific document, which is later compared to document
 * information on the students device
 */
public class ExamDocument {
    private final String name;
    @Nullable
    private byte[] hash;
    @Nullable
    private byte[] nonAnnotatedHash;
    private int pages;
    @Nullable
    private String uriString;
    private Date hashCreationDate;

    /**
     * Constructor from uri
     *
     * @param name      name of document
     * @param uriString uri
     */
    ExamDocument(String name, @Nullable final String uriString) {
        this.name = name;
        this.hash = null;
        this.nonAnnotatedHash = null;
        this.pages = 0;
        this.uriString = uriString;
        this.hashCreationDate = new Date(0);
    }

    /**
     * Constructor from pages
     *
     * @param name  name of document
     * @param pages number of pages
     */
    ExamDocument(String name, int pages) {
        this.name = name;
        this.hash = null;
        this.nonAnnotatedHash = null;
        this.pages = pages;
        this.uriString = null;
        this.hashCreationDate = new Date(0);
    }

    /**
     * Full constructor for recreation from YAML
     *
     * @param name             name of document
     * @param hash             classic MD5 of document
     * @param nonAnnotatedHash all COSObjects in 1st hierarchy level of document listed and annot objects removed. Then  MD5ed
     * @param pages            number of pages
     * @param uriString        target uri
     * @param hashCreationDate timestamp for hash creation
     */
    ExamDocument(String name, @Nullable final byte[] hash, @Nullable final byte[] nonAnnotatedHash, int pages, @Nullable final String uriString, Date hashCreationDate) {
        this.name = name;
        this.hash = hash;
        this.nonAnnotatedHash = nonAnnotatedHash;
        this.pages = pages;
        this.uriString = uriString;
        this.hashCreationDate = hashCreationDate;
    }

    /**
     * Removing uri because in transmitted YAML, there does not need to be a URI
     */
    void removeUriString() {
        uriString = null;
    }

    /**
     * Removing normal MD5
     */
    public void removeHash() {
        hash = null;
    }

    /**
     * Removing non annotated MD5
     */
    public void removeNonAnnotatedHash() {
        nonAnnotatedHash = null;
    }

    /**
     * Removing the timestamp because in transmitted YAML, there does not need to be a timestamp
     */
    void removeHashCreationDate() {
        hashCreationDate = new Date(0);
    }

    /**
     * Update meta using the identifier struct
     *
     * @param id update with this id
     */
    void update(Identifiers id) {
        this.pages = id.pages;
        this.hash = id.hash;
        this.nonAnnotatedHash = id.nonAnnotatedHash;
        this.hashCreationDate = id.hashCreationDate;
    }

    // Clone
    @NonNull
    @Override
    public Object clone() {
        byte[] hash = null, nonAnnotatedHash = null;
        String uriString = null;
        Date hashCreationDate = null;
        if (this.hash != null) hash = this.hash;
        if (this.nonAnnotatedHash != null) nonAnnotatedHash = this.nonAnnotatedHash;
        if (this.uriString != null) uriString = this.uriString;
        if (this.hashCreationDate != null) hashCreationDate = this.hashCreationDate;
        return new ExamDocument(name, hash, nonAnnotatedHash, pages, uriString, hashCreationDate);
    }

    /**
     * SnakeYAML needs either a Java bean or a special YAML parse constructor.
     * We work with beans for writing and the constructor for reading as we then
     * can still protect our attributes
     * The bean needs getters
     */
    public String getName() {
        return name;
    }

    /**
     * Getters needed for SnakeYAML
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Getters needed for SnakeYAML
     */
    byte[] getNonAnnotatedHash() {
        return nonAnnotatedHash;
    }

    /**
     * Getters needed for SnakeYAML
     */
    public int getPages() {
        return pages;
    }

    /**
     * Getters needed for SnakeYAML
     */
    @Nullable
    public String getUriString() {
        return uriString;
    }

    /**
     * Getters needed for SnakeYAML
     */
    @Nullable
    Date getHashCreationDate() {
        return hashCreationDate;
    }

    /**
     * Identifiers struct for update purposes
     */
    static class Identifiers {
        int pages = 0;
        byte[] hash = null, nonAnnotatedHash = null;
        Date hashCreationDate = new Date(0);
    }
}
