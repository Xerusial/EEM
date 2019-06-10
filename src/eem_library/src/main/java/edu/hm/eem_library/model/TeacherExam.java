package edu.hm.eem_library.model;

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

/** Class, which represents a school exam and includes a list of the allowed books to be used.
 *
 */
public class TeacherExam extends StudentExam{
    private final byte[] salt;
    private byte[] passwordHash;

    TeacherExam() {
        this.salt = HASHTOOLBOX.genSalt();
    }

    private TeacherExam(byte[] salt, byte[] passwordHash, LinkedList<ExamDocument> allowedDocuments){
        super(allowedDocuments);
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public void setPassword(String pw) {
        this.passwordHash = HASHTOOLBOX.genSha256(pw,salt);
    }

    public boolean checkPW(String pw) {
        return Arrays.equals(passwordHash, HASHTOOLBOX.genSha256(pw,salt));
    }

    /** Custom YAML constructor to be used with SnakeYAML. Turns an {@link TeacherExam} YAML node into
     * an actual TeacherExam object.
     */
    static class ExamConstructor extends StudentExamConstructor {
        ExamConstructor() {
            yamlClassConstructors.put(NodeId.mapping, new ExamConstruct());
        }

        class ExamConstruct extends Constructor.ConstructMapping {
            @Override
            public Object construct(Node nnode) {
                if (nnode.getTag().equals(new Tag(Tag.PREFIX + "edu.hm.eem_library.model.TeacherExam"))) {
                    MappingNode mnode = (MappingNode) nnode;
                    List<NodeTuple> list = mnode.getValue();

                    LinkedList<ExamDocument> allowedDocuments= new LinkedList<>();
                    byte[] salt = null;
                    byte[] passwordHash = null;

                    for(NodeTuple nt : list){
                        Node knode = nt.getKeyNode();
                        Node vnode = nt.getValueNode();
                        String tag = (String) yamlConstructors.get(Tag.STR).construct(knode);
                        switch(tag){
                            case "salt":
                                salt = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                                break;
                            case "passwordHash":
                                passwordHash = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                                break;
                            case "allowedDocuments":
                                allowedDocuments = constructAllowedDocuments((SequenceNode) vnode);
                                break;
                            default:
                                break;
                        }
                    }
                    return new TeacherExam(salt, passwordHash, allowedDocuments);
                }
                return null;
            }
        }
    }
}
