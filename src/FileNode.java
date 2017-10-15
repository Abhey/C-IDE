/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author abhey
 */

import java.io.*;

public class FileNode {
    
    private File file;
    
    public FileNode(File file){
        this.file = file;
    }
    
    public File getFile(){
        return this.file;
    }
    
    @Override
    public String toString(){
        return file.getName();
    }
    
}
