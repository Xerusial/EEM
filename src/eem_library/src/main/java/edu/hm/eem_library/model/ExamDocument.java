package edu.hm.eem_library.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;

/** A class containing information on a specific document, which can be later checked by the client.
 *
 */
public class ExamDocument{
    private final String name;
    private final String path;

    public ExamDocument(final String name, final String path) {
        this.name = name;
        this.path = path;
    }

    SortableItem<String, ExamDocument> toSortableItem(){
        return new SortableItem<>(name, this);
    }

    // Needed for SnakeYAML
    public String getName() {
        return name;
    }

    // Needed for SnakeYAML
    public String getPath() {
        return path;
    }
}
