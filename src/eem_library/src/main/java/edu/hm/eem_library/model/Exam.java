package edu.hm.eem_library.model;

import android.support.annotation.Nullable;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Class, which represents a school exam and includes a list of the allowed books to be used.
 *
 */
public class Exam{
    private byte[] salt;
    private byte[] passwordHash;
    public boolean allDocumentsAllowed;
    LinkedList<ExamDocument> allowedDocuments;

    Exam(boolean allDocumentsAllowed, @Nullable byte[] salt) {
        this.allDocumentsAllowed = allDocumentsAllowed;
        if(salt==null) this.salt = SHA256TOOLBOX.genSalt();
        else this.salt=salt;
        allowedDocuments = new LinkedList<>();
    }

    private Exam(boolean allDocumentsAllowed, byte[] salt, byte[] passwordHash, LinkedList<ExamDocument> allowedDocuments){
        this(allDocumentsAllowed, salt);
        this.passwordHash = passwordHash;
        this.allowedDocuments = allowedDocuments;
    }

    public void setPassword(String pw) {
        this.passwordHash = SHA256TOOLBOX.genSha256(pw,salt);
    }

    public boolean checkPW(String pw) {
        return Arrays.equals(passwordHash, SHA256TOOLBOX.genSha256(pw,salt));
    }

    /** Custom YAML constructor to be used with SnakeYAML. Turns an {@link Exam} YAML node into
     * an actual Exam object.
     */
    static class ExamConstructor extends Constructor {
        ExamConstructor() {
            yamlClassConstructors.put(NodeId.mapping, new ExamConstruct());
        }

        class ExamConstruct extends Constructor.ConstructMapping {
            @Override
            public Object construct(Node nnode) {
                if (nnode.getTag().equals(new Tag(Tag.PREFIX + "edu.hm.eem_library.model.Exam"))) {
                    MappingNode mnode = (MappingNode) nnode;
                    List<NodeTuple> list = mnode.getValue();

                    LinkedList<ExamDocument> allowedDocuments= new LinkedList<>();
                    boolean allDocumentsAllowed = false;
                    byte[] salt = null;
                    byte[] passwordHash = null;

                    for(NodeTuple nt : list){
                        Node knode = nt.getKeyNode();
                        Node vnode = nt.getValueNode();
                        String tag = (String) yamlConstructors.get(Tag.STR).construct(knode);
                        switch(tag){
                            case "allDocumentsAllowed":
                                allDocumentsAllowed = (boolean) yamlConstructors.get(Tag.BOOL).construct(vnode);
                                break;
                            case "salt":
                                salt = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                                break;
                            case "passwordHash":
                                passwordHash = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                                break;
                            case "allowedDocuments":
                                SequenceNode snode = (SequenceNode) vnode;
                                for(Node child : snode.getValue()) {
                                    Map<String, String> map = (Map<String, String>) yamlConstructors.get(Tag.MAP).construct(child);
                                    allowedDocuments.add(new ExamDocument(map.get("name"), map.get("path")));
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    return new Exam(allDocumentsAllowed, salt, passwordHash, allowedDocuments);
                }
                return null;
            }
        }
    }
}
