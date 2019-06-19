package edu.hm.eem_library.model;

import android.app.Application;
import android.util.Log;

import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class StudentExam {
    private LinkedList<ExamDocument> allowedDocuments;

    public LinkedList<ExamDocument> getAllowedDocuments() {
        return allowedDocuments;
    }

    //needed for SnakeYAML
    public void setAllowedDocuments(LinkedList<ExamDocument> allowedDocuments) {
        this.allowedDocuments = allowedDocuments;
    }

    StudentExam() {
        this.allowedDocuments = new LinkedList<>();
    }

    /** updates allowedDocuments from a {@link SelectableSortableMapLiveData} values instance
     *
     * @param docs new values for allowedDocuments
     */
    void documentsFromLivedata(Collection<ThumbnailedExamDocument> docs){
        allowedDocuments = new LinkedList<>();
        for(ThumbnailedExamDocument item : docs){
            allowedDocuments.add(item.item);
        }
    }

    /** Turns allowedDocuments into a {@link TreeSet} by using the documents
     * name as key. This is used to construct {@link SortableMapLiveData} objects.
     *
     * @return output set
     */
    TreeSet<ThumbnailedExamDocument> toLiveDataSet(Application apl){
        TreeSet<ThumbnailedExamDocument> treeSet = new TreeSet<>();
        if(allowedDocuments!=null) {
            for (ExamDocument doc : allowedDocuments) {
                treeSet.add(ThumbnailedExamDocument.getInstance(apl.getApplicationContext(), doc));
            }
        }
        return treeSet;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    final void constructAllowedDocuments(SequenceNode snode, Map<Tag, Construct> yamlConstructors){
        allowedDocuments= new LinkedList<>();
        for(Node child : snode.getValue()) {
                                    /*Map<String, String> map = (Map<String, String>) yamlConstructors.get(Tag.MAP).construct(child);
                                    allowedDocuments.add(new ExamDocument(map.get("name"), map.get("hash"), map.get("pages")));*/
            MappingNode mnode = (MappingNode) child;
            List<NodeTuple> list = mnode.getValue();
            String name = null, uri = null;
            byte[] hash = null, nonAnnotatedHash = null;
            int pages = 0;
            for(NodeTuple nt : list) {
                Node knode = nt.getKeyNode();
                Node vnode = nt.getValueNode();
                String subtag = (String) yamlConstructors.get(Tag.STR).construct(knode);
                switch (subtag){
                    case "name":
                        name = (String) yamlConstructors.get(Tag.STR).construct(vnode);
                        break;
                    case "hash":
                        if(((String)yamlConstructors.get(Tag.STR).construct(vnode)).equals("null"))
                            hash = null;
                        else
                            hash = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                        break;
                    case "nonAnnotatedHash":
                        if(((String)yamlConstructors.get(Tag.STR).construct(vnode)).equals("null"))
                            nonAnnotatedHash = null;
                        else
                            nonAnnotatedHash = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                        break;
                    case "pages":
                        pages = (int) yamlConstructors.get(Tag.INT).construct(vnode);
                        break;
                    case "uri":
                        uri = (String) yamlConstructors.get(Tag.STR).construct(vnode);
                        if(uri.equals("null")) uri = null;
                        break;
                }
            }
            allowedDocuments.add(new ExamDocument(name, hash, nonAnnotatedHash, pages, uri));
        }
    }

    protected void stepTags(Node vnode, Map<Tag, Construct> yamlConstructors, String tag){
        if ("allowedDocuments".equals(tag)) {
            constructAllowedDocuments((SequenceNode) vnode, yamlConstructors);
        }
    }

    final boolean construct(Node nnode, Map<Tag, Construct> yamlConstructors){
        if (nnode.getTag().equals(new Tag(Tag.PREFIX + getClass().getName()))) {
            MappingNode mnode = (MappingNode) nnode;
            List<NodeTuple> list = mnode.getValue();

            for(NodeTuple nt : list){
                Node knode = nt.getKeyNode();
                Node vnode = nt.getValueNode();
                String tag = (String) yamlConstructors.get(Tag.STR).construct(knode);
                stepTags(vnode, yamlConstructors, tag);
            }
            return true;
        }
        return false;
    }

    /** Custom YAML constructor to be used with SnakeYAML. Turns an {@link StudentExam} YAML node into
     * an actual TeacherExam object.
     */
    static class ExamConstructor extends Constructor {
        ExamConstructor() {
            yamlClassConstructors.put(NodeId.mapping, new StudentExam.ExamConstructor.ExamConstruct());
        }

        class ExamConstruct extends Constructor.ConstructMapping {
            @Override
            public Object construct(Node nnode) {

                StudentExam exam = new StudentExam();
                return exam.construct(nnode, yamlConstructors) ? exam : null;
            }
        }
    }
}
