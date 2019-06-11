package edu.hm.eem_library.model;

import android.app.Application;
import android.graphics.Bitmap;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

class StudentExam {
    LinkedList<ExamDocument> allowedDocuments;

    StudentExam() {
        this.allowedDocuments = new LinkedList<>();
    }

    LinkedList<ExamDocument> getAllowedDocuments() {
        return allowedDocuments;
    }

    StudentExam(Yaml yaml, File examDir, String name) {
        File in = new File(examDir.getPath() + File.separator + name);
        StudentExam retval;
        try {
            FileInputStream fis = new FileInputStream(in);
            retval = yaml.load(fis);
            fis.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    void writeExamToFile(Yaml yaml, File examDir, String name) {
        File out = new File(examDir.getPath() + File.separator + name);
        try {
            FileWriter fw = new FileWriter(out);
            yaml.dump(this, fw);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void createSendableVersion(Yaml yaml, File mainDir, File examDir, String name){
        TeacherExam exam = new TeacherExam(yaml, examDir, name);
        for(ExamDocument doc : exam.allowedDocuments){
            doc.removePath();
        }
        exam.writeExamToFile(yaml, mainDir, "sendable_exam");
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
                String path = doc.getPath();
                Bitmap thumbnail = null;
                if (path != null) {
                    File file = new File(doc.getPath());
                    thumbnail = THUMBNAILTOOLBOX.getThumbnailBitmap(file, apl);
                }
                treeSet.add(new ThumbnailedExamDocument(doc.name, doc, thumbnail));
            }
        }
        return treeSet;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    final void constructAllowedDocuments(SequenceNode snode, Map<NodeId, Construct> yamlConstructors){
        allowedDocuments= new LinkedList<>();
        for(Node child : snode.getValue()) {
                                    /*Map<String, String> map = (Map<String, String>) yamlConstructors.get(Tag.MAP).construct(child);
                                    allowedDocuments.add(new ExamDocument(map.get("name"), map.get("hash"), map.get("pages")));*/
            MappingNode mnode = (MappingNode) child;
            List<NodeTuple> list = mnode.getValue();
            String name = null;
            byte[] hash = null;
            int pages = 0;
            String path = null;
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
                    case "path":
                        path = (String) yamlConstructors.get(Tag.STR).construct(vnode);
                }
            }
            allowedDocuments.add(new ExamDocument(name, hash, pages, path));
        }
    }

    protected void stepTags(Node vnode, Map<NodeId, Construct> yamlConstructors, String tag){
        if ("allowedDocuments".equals(tag)) {
            constructAllowedDocuments((SequenceNode) vnode, yamlConstructors);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void construct(Node nnode, Map<NodeId, Construct> yamlConstructors){
        if (nnode.getTag().equals(new Tag(Tag.PREFIX + getClass().toString()))) {
            MappingNode mnode = (MappingNode) nnode;
            List<NodeTuple> list = mnode.getValue();

            for(NodeTuple nt : list){
                Node knode = nt.getKeyNode();
                Node vnode = nt.getValueNode();
                String tag = (String) yamlConstructors.get(Tag.STR).construct(knode);
                stepTags(vnode, yamlConstructors, tag);
            }
        }
    }

    /** Custom YAML constructor to be used with SnakeYAML. Turns an {@link TeacherExam} YAML node into
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
                exam.construct(nnode, yamlClassConstructors);
                return exam;
            }
        }
    }
}
