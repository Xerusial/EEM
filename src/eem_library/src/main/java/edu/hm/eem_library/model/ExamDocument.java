package edu.hm.eem_library.model;

import java.util.Collection;
import java.util.TreeSet;

public class ExamDocument{
    private final String name;
    private final String path;

    public ExamDocument(String name, String path) {
        this.name = name;
        this.path = path;
    }

    private SortableItem<String, ExamDocument> toSortableItem(){
        return new SortableItem<>(name, this);
    }

    static TreeSet<SortableItem<String, ExamDocument>> toSet(Collection<ExamDocument> collection){
        TreeSet<SortableItem<String, ExamDocument>> treeSet = new TreeSet<>();
        for(ExamDocument doc : collection){
            treeSet.add(doc.toSortableItem());
        }
        return treeSet;
    }
}
