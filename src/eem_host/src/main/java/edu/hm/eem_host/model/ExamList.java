package edu.hm.eem_host.model;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.LinkedList;

public class ExamList extends SelectableItemList<Nameable> {
    private File examdir;

    ExamList(LinkedList<Nameable>list, File examdir){
        super(list);
        initDir(examdir);
    }

    ExamList(File examdir){
        initDir(examdir);
    }

    private void initDir(File examdir){
        if(!examdir.isDirectory()) {
            throw new InvalidParameterException("Param needs to be a dir!");
        }
        this.examdir = examdir;
        for(File f: examdir.listFiles()){
            if(f.isFile()){
                add(new SelectableItem<>(new Nameable(f.getName())));
            }
        }
    }

    @Override
    public void removeSelected() {
        for(SelectableItem item : this){
            if(item.selected){
                File f = new File(examdir.getPath()+File.separator+item.dataItem.getName());
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }
    }
}
