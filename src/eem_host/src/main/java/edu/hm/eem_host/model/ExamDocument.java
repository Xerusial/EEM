package edu.hm.eem_host.model;

public class ExamDocument extends Nameable{
    private final String path;

    public ExamDocument(String name, String path) {
        super(name);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
