package edu.hm.eem_library.model;

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
import java.util.TreeSet;

class StudentExam {
    private LinkedList<ExamDocument> allowedDocuments;

    StudentExam() {
        this.allowedDocuments = new LinkedList<>();
    }

    StudentExam(LinkedList<ExamDocument> allowedDocuments) {
        this.allowedDocuments = allowedDocuments;
    }

    /** updates allowedDocuments from a {@link SelectableSortableMapLiveData} values instance
     *
     * @param docs new values for allowedDocuments
     */
    void documentsFromLivedata(Collection<SortableItem<String, ExamDocument>> docs){
        allowedDocuments = new LinkedList<>();
        for(SortableItem<String, ExamDocument> item : docs){
            allowedDocuments.add(item.item);
        }
    }

    /** Turns allowedDocuments into a {@link TreeSet} by using the documents
     * name as key. This is used to construct {@link SortableMapLiveData} objects.
     *
     * @return output set
     */
    TreeSet<SortableItem<String, ExamDocument>> toLiveDataSet(){
        TreeSet<SortableItem<String, ExamDocument>> treeSet = new TreeSet<>();
        for(ExamDocument doc : allowedDocuments){
            treeSet.add(doc.toSortableItem());
        }
        return treeSet;
    }

    /** Custom YAML constructor to be used with SnakeYAML. Turns an {@link TeacherExam} YAML node into
     * an actual TeacherExam object.
     */
    static class StudentExamConstructor extends Constructor {
        StudentExamConstructor() {
            yamlClassConstructors.put(NodeId.mapping, new StudentExam.StudentExamConstructor.ExamConstruct());
        }

        LinkedList<ExamDocument> constructAllowedDocuments(SequenceNode snode){
            LinkedList<ExamDocument> allowedDocuments= new LinkedList<>();
            for(Node child : snode.getValue()) {
                                    /*Map<String, String> map = (Map<String, String>) yamlConstructors.get(Tag.MAP).construct(child);
                                    allowedDocuments.add(new ExamDocument(map.get("name"), map.get("hash"), map.get("pages")));*/
                MappingNode mnode = (MappingNode) child;
                List<NodeTuple> list = mnode.getValue();
                String name = null;
                byte[] hash = null;
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
                            hash = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                            break;
                        case "pages":
                            pages = (int) yamlConstructors.get(Tag.INT).construct(vnode);
                            break;
                    }
                }
                allowedDocuments.add(new ExamDocument(name, hash, pages));
            }
            return allowedDocuments;
        }

        class ExamConstruct extends Constructor.ConstructMapping {
            @Override
            public Object construct(Node nnode) {
                if (nnode.getTag().equals(new Tag(Tag.PREFIX + "edu.hm.eem_library.model.TeacherExam"))) {
                    MappingNode mnode = (MappingNode) nnode;
                    List<NodeTuple> list = mnode.getValue();

                    LinkedList<ExamDocument> allowedDocuments = null;

                    for(NodeTuple nt : list){
                        Node knode = nt.getKeyNode();
                        Node vnode = nt.getValueNode();
                        String tag = (String) yamlConstructors.get(Tag.STR).construct(knode);
                        if ("allowedDocuments".equals(tag)) {
                            allowedDocuments = constructAllowedDocuments((SequenceNode) vnode);
                        }
                    }
                    return new StudentExam(allowedDocuments);
                }
                return null;
            }
        }
    }
}
