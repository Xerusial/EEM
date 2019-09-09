package edu.hm.eem_library.model;

import android.content.Context;

import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

public class StudentExam {
    private LinkedList<ExamDocument> allowedDocuments;

    StudentExam() {
        this.allowedDocuments = new LinkedList<>();
    }

    public LinkedList<ExamDocument> getAllowedDocuments() {
        return allowedDocuments;
    }

    //needed for SnakeYAML
    public void setAllowedDocuments(LinkedList<ExamDocument> allowedDocuments) {
        this.allowedDocuments = allowedDocuments;
    }

    /**
     * updates allowedDocuments from a {@link SelectableSortableItemLiveData} values instance
     *
     * @param docs new values for allowedDocuments
     */
    void documentsFromLivedata(Collection<ThumbnailedExamDocument> docs) {
        allowedDocuments = new LinkedList<>();
        for (ThumbnailedExamDocument item : docs) {
            allowedDocuments.add(item.item);
        }
    }

    /**
     * Turns allowedDocuments into a {@link TreeSet} by using the documents
     * name as key. This is used to construct {@link SortableItemLiveData} objects.
     *
     * @return output set
     */
    TreeSet<ThumbnailedExamDocument> toLiveDataSet(Context context) {
        TreeSet<ThumbnailedExamDocument> treeSet = new TreeSet<>();
        if (allowedDocuments != null) {
            ThumbnailedExamDocument.loadInstances(context, treeSet, allowedDocuments);
        }
        return treeSet;
    }

    final void constructAllowedDocuments(SequenceNode snode, Map<Tag, Construct> yamlConstructors) {
        allowedDocuments = new LinkedList<>();
        for (Node child : snode.getValue()) {
            MappingNode mnode = (MappingNode) child;
            List<NodeTuple> list = mnode.getValue();
            String name = null, uriString = null;
            byte[] hash = null, nonAnnotatedHash = null;
            int pages = 0;
            Date hashCreationDate = null;
            for (NodeTuple nt : list) {
                Node knode = nt.getKeyNode();
                Node vnode = nt.getValueNode();
                String subtag = (String) Objects.requireNonNull(yamlConstructors.get(Tag.STR)).construct(knode);
                switch (subtag) {
                    case "name":
                        name = (String) Objects.requireNonNull(yamlConstructors.get(Tag.STR)).construct(vnode);
                        break;
                    case "hash":
                        if (Objects.requireNonNull(yamlConstructors.get(Tag.STR)).construct(vnode).equals("null"))
                            hash = null;
                        else
                            hash = (byte[]) Objects.requireNonNull(yamlConstructors.get(Tag.BINARY)).construct(vnode);
                        break;
                    case "nonAnnotatedHash":
                        if (Objects.requireNonNull(yamlConstructors.get(Tag.STR)).construct(vnode).equals("null"))
                            nonAnnotatedHash = null;
                        else
                            nonAnnotatedHash = (byte[]) Objects.requireNonNull(yamlConstructors.get(Tag.BINARY)).construct(vnode);
                        break;
                    case "pages":
                        pages = (int) Objects.requireNonNull(yamlConstructors.get(Tag.INT)).construct(vnode);
                        break;
                    case "uriString":
                        uriString = (String) Objects.requireNonNull(yamlConstructors.get(Tag.STR)).construct(vnode);
                        if (uriString.equals("null")) uriString = null;
                        break;
                    case "hashCreationDate":
                        hashCreationDate = (Date) Objects.requireNonNull(yamlConstructors.get(Tag.TIMESTAMP)).construct(vnode);
                        break;
                }
            }
            allowedDocuments.add(new ExamDocument(name, hash, nonAnnotatedHash, pages, uriString, hashCreationDate));
        }
    }

    protected void stepTags(Node vnode, Map<Tag, Construct> yamlConstructors, String tag) {
        if ("allowedDocuments".equals(tag)) {
            constructAllowedDocuments((SequenceNode) vnode, yamlConstructors);
        }
    }

    final boolean construct(Node nnode, Map<Tag, Construct> yamlConstructors) {
        if (nnode.getTag().equals(new Tag(Tag.PREFIX + getClass().getName()))) {
            MappingNode mnode = (MappingNode) nnode;
            List<NodeTuple> list = mnode.getValue();

            for (NodeTuple nt : list) {
                Node knode = nt.getKeyNode();
                Node vnode = nt.getValueNode();
                String tag = (String) Objects.requireNonNull(yamlConstructors.get(Tag.STR)).construct(knode);
                stepTags(vnode, yamlConstructors, tag);
            }
            return true;
        }
        return false;
    }

    /**
     * Custom YAML constructor to be used with SnakeYAML. Turns an {@link StudentExam} YAML node into
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
