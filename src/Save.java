/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author abhey
 */
import java.io.File;
import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.*;
import javax.swing.Box.Filler;
public class Save extends javax.swing.JFrame {

    /**
     * Creates new form Save
     */
    public Save(){
        initComponents();
        SaveDialog.setDialogTitle("Save File");
        javax.swing.filechooser.FileFilter filter=new javax.swing.filechooser.FileNameExtensionFilter(".c, .cpp, .java","c","cpp","java");
        SaveDialog.setFileFilter(filter);
        java.io.File file = new java.io.File(Eureka.frame.sendFileName());
        SaveDialog.setSelectedFile(file);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SaveDialog = new javax.swing.JFileChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocation(new java.awt.Point(410, 155));
        setType(java.awt.Window.Type.POPUP);

        SaveDialog.setAcceptAllFileFilterUsed(false);
        SaveDialog.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        SaveDialog.setDialogTitle("Save File");
        SaveDialog.setName(""); // NOI18N
        SaveDialog.setPreferredSize(new java.awt.Dimension(502, 410));
        SaveDialog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveDialogActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SaveDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SaveDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Think and then work don't act like stupids ........
    private void SaveDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveDialogActionPerformed
        // TODO add your handling code here:
        if(evt.getActionCommand().compareTo("CancelSelection")==0){
            this.dispose();
            Eureka.frame.toFront();
            return;
        }
        File file=SaveDialog.getSelectedFile();
        javax.swing.filechooser.FileFilter filter=SaveDialog.getFileFilter();
        if(file!=null){
            try{
                if(filter.accept(file)){
                    if(file.createNewFile()){
                        java.io.FileWriter writer=new java.io.FileWriter(file);
                        writer.write(Eureka.frame.sendText());
                        writer.flush();
                        Eureka.frame.SetTitle(file.getName());
                        Eureka.frame.setFilePath(file.getAbsolutePath());
                        Eureka.frame.setDirectoryStructure(file);
                        this.dispose();
                        Eureka.frame.toFront();
                        return;
                    }
                    else{
                        int result=JOptionPane.showConfirmDialog(null,"File aready exists do you want to overwrite it?");
                        if(result==0){
                            java.io.FileWriter writer =new java.io.FileWriter(file);
                            writer.write(Eureka.frame.sendText());
                            writer.flush();
                            Eureka.frame.SetTitle(file.getName());
                            Eureka.frame.setFilePath(file.getAbsolutePath());
                            Eureka.frame.setDirectoryStructure(file);
                            this.dispose();
                            Eureka.frame.toFront();
                            return;
                        }
                    }
                }
                else{
                    javax.swing.JOptionPane.showMessageDialog(null,"Invalid file format, please make sure your file is of .c, .cpp and .java format only.");
                    this.toFront();
                }
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(null,"Input Output Exception Occurred");
                this.dispose();
                Eureka.frame.toFront();
                return;
            }
        }
        else{
            this.dispose();
            Eureka.frame.toFront();
            return;
        }
    }//GEN-LAST:event_SaveDialogActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Save.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Save.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Save.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Save.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Save().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser SaveDialog;
    // End of variables declaration//GEN-END:variables
}
