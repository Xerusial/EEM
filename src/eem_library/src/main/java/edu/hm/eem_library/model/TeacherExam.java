package edu.hm.eem_library.model;

import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Class, which represents a school exam and includes a list of the allowed books to be used.
 */
public class TeacherExam extends StudentExam {
    private byte[] salt;
    private byte[] passwordHash;

    /**
     * Generate constant salt for exam
     */
    TeacherExam() {
        this.salt = HASHTOOLBOX.genSalt();
    }

    /**
     * Set password of exam
     *
     * @param pw String of pw
     */
    public void setPassword(String pw) {
        this.passwordHash = HASHTOOLBOX.genSha256(pw, salt);
    }

    /**
     * Check a password string
     *
     * @param pw password
     * @return matched password of this exam
     */
    public boolean checkPW(String pw) {
        return Arrays.equals(passwordHash, HASHTOOLBOX.genSha256(pw, salt));
    }

    /**
     * Scan through the YAML Tag of a given vnode and try to reconstruct our fields
     *
     * @param vnode            node to be searched
     * @param yamlConstructors other standard YAML constructors (int, String...)
     * @param tag              Current Tag
     */
    @Override
    protected void stepTags(Node vnode, Map<Tag, Construct> yamlConstructors, String tag) {
        switch (tag) {
            case "salt":
                salt = (byte[]) Objects.requireNonNull(yamlConstructors.get(Tag.BINARY)).construct(vnode);
                break;
            case "passwordHash":
                passwordHash = (byte[]) Objects.requireNonNull(yamlConstructors.get(Tag.BINARY)).construct(vnode);
                break;
            case "allowedDocuments":
                constructAllowedDocuments((SequenceNode) vnode, yamlConstructors);
                break;
            default:
                break;
        }
        super.stepTags(vnode, yamlConstructors, tag);
    }

    /**
     * Custom YAML constructor to be used with SnakeYAML. Turns an {@link TeacherExam} YAML node into
     * an actual TeacherExam object.
     */
    static class ExamConstructor extends Constructor {
        ExamConstructor() {
            yamlClassConstructors.put(NodeId.mapping, new TeacherExam.ExamConstructor.ExamConstruct());
        }

        class ExamConstruct extends Constructor.ConstructMapping {
            @Override
            public Object construct(Node nnode) {
                TeacherExam exam = new TeacherExam();
                return exam.construct(nnode, yamlConstructors) ? exam : null;
            }
        }
    }
}
