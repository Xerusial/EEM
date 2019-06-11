package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

/** A class containing information on a specific document, which can be later checked by the client.
 *
 */
public class ExamDocument{
    String name;
    final byte[] hash;
    final int pages;
    @Nullable String path;

    public ExamDocument(String name, final byte[] hash, final String path) {
        this.name = name;
        this.hash = hash;
        this.pages = 0;
        this.path = path;
    }

    public ExamDocument(String name, int pages) {
        this.name = name;
        this.hash = null;
        this.pages = pages;
        this.path = null;
    }

    ExamDocument(String name, final byte[] hash, int pages, final String path){
        this.name = name;
        this.hash = hash;
        this.pages = pages;
        this.path = path;
    }

    void removePath(){
        path = null;
    }

    void incrName(){
        String[] parts = name.split(" ");
        int preEndIndex = parts.length-1;
        int num;
        try {
            num = Integer.parseInt(parts[preEndIndex]);
            num++;
        } catch (NumberFormatException e){
            num = 1;
            preEndIndex = parts.length;
        }
        name = "";
        StringBuilder builder = new StringBuilder(parts[0] + ' ');
        for(int i = 1; i<preEndIndex; i++){
            builder.append(parts[i]);
            builder.append(' ');
        }
        builder.append(num);
        name = builder.toString();
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
