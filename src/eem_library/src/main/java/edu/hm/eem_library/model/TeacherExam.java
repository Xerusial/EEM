package edu.hm.eem_library.model;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

/** Class, which represents a school exam and includes a list of the allowed books to be used.
 *
 */
public class TeacherExam extends StudentExam{
    private byte[] salt;
    private byte[] passwordHash;

    TeacherExam() {
        this.salt = HASHTOOLBOX.genSalt();
    }

    public TeacherExam(Yaml yaml, File examDir, String name) {
        super(yaml, examDir, name);
    }

    public void setPassword(String pw) {
        this.passwordHash = HASHTOOLBOX.genSha256(pw,salt);
    }

    public boolean checkPW(String pw) {
        return Arrays.equals(passwordHash, HASHTOOLBOX.genSha256(pw,salt));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    protected void stepTags(Node vnode, Map<NodeId, Construct> yamlConstructors, String tag) {
        switch(tag){
            case "salt":
                salt = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                break;
            case "passwordHash":
                passwordHash = (byte[]) yamlConstructors.get(Tag.BINARY).construct(vnode);
                break;
            case "allowedDocuments":
                constructAllowedDocuments((SequenceNode) vnode, yamlConstructors);
                break;
            default:
                break;
        }
        super.stepTags(vnode, yamlConstructors, tag);
    }
}
