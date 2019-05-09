package edu.hm.eem_library.model;

import java.util.Collection;
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

    private SortableItem<String, ExamDocument> toSortableItem(){
        return new SortableItem<>(name, this);
    }

    /** Turns any collection of {@link ExamDocument} into a {@link TreeSet} by using the documents
     * name as key. This is used to construct {@link SortableMapLiveData} objects.
     *
     * @param collection input collection
     * @return output set
     */
    static TreeSet<SortableItem<String, ExamDocument>> toSet(final Collection<ExamDocument> collection){
        TreeSet<SortableItem<String, ExamDocument>> treeSet = new TreeSet<>();
        for(ExamDocument doc : collection){
            treeSet.add(doc.toSortableItem());
        }
        return treeSet;
    }
}
