package edu.hm.eem_library.model;

/** A class containing information on a specific document, which can be later checked by the client.
 *
 */
public class ExamDocument{
    private String name;
    private final byte[] hash;
    private final int pages;

    public ExamDocument(String name, final byte[] hash) {
        this.name = name;
        this.hash = hash;
        this.pages = 0;
    }

    public ExamDocument(String name, int pages) {
        this.name = name;
        this.hash = null;
        this.pages = pages;
    }

    ExamDocument(String name, final byte[] hash, int pages){
        this.name = name;
        this.hash = hash;
        this.pages = pages;
    }

    SortableItem<String, ExamDocument> toSortableItem(){
        return new SortableItem<>(name, this);
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

    public void incrName(){
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
}
