/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author abhey
 */

import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.Color;
import javax.swing.*;
import java.awt.MenuItem;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Point;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;
import javax.swing.undo.*;


public class Eureka extends JFrame implements KeyListener,MouseListener,CaretListener{

    /**
     * Creates new form Eureka
     */
    // Read in more details about Key Listener
    
    public static final JDialog dialog = new JDialog();
    
    private static SimpleAttributeSet success = new SimpleAttributeSet();
    private static SimpleAttributeSet failure = new SimpleAttributeSet();
    private static SimpleAttributeSet keyword = new SimpleAttributeSet();
    
    private static final HashSet javaKeywords;
    private static final HashSet cppKeywords;
    private static final HashSet cKeywords;
    private static final HashSet allKeywords;
    
    static{
        
        StyleConstants.setForeground(success, Color.GREEN);
        StyleConstants.setForeground(failure, Color.RED);
        StyleConstants.setForeground(keyword, Color.BLUE);
        StyleConstants.setBold(keyword, true);
        
        javaKeywords = new HashSet();
        cppKeywords = new HashSet();
        cKeywords = new HashSet();
        allKeywords = new HashSet();
        
        BufferedReader br ;
        try {
            
            String curDir = (new File(Eureka.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParent();
            
            br = new BufferedReader(new FileReader(curDir + "/JavaKeyword.txt"));
            String keyword = "";
            
            while((keyword = br.readLine()) != null)
                javaKeywords.add(keyword);
            
            br = new BufferedReader(new FileReader(curDir + "/CppKeyword.txt"));
            while((keyword = br.readLine()) != null)
                cppKeywords.add(keyword);
            
            br = new BufferedReader(new FileReader(curDir + "/CKeywords.txt"));
            while((keyword = br.readLine()) != null)
                cKeywords.add(keyword);
            
            br = new BufferedReader(new FileReader(curDir + "/AllKeyword.txt"));
            while((keyword = br.readLine()) != null)
                allKeywords.add(keyword);
            
            
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        
    }
    
    public void addKeywords(Trie t){
        
        Iterator it = allKeywords.iterator();
        while(it.hasNext()){
            t.addWord((String) it.next());
        }
        
    }
    
        // Take a look at these lines ..........
        /*  final JEditorPane edit = new JEditorPane();
          edit.setEditorKitForContentType("text/java", editorKit);
          edit.setContentType("text/java"); */
    
    public Eureka(){
        initComponents();
        dialog.setAlwaysOnTop(true);
        this.TextPane1.addKeyListener(this);
        this.TextPane1.addMouseListener(this);
        this.TextPane1.addCaretListener(this);
        this.TextPane1.setDocument(new SyntaxDocument(javaKeywords));
        TextLineNumber lineNumber1 = new TextLineNumber(this.TextPane1);
        Eureka.addUndoFuctionality(TextPane1,(SyntaxDocument)TextPane1.getStyledDocument(),new UndoManager());
        Eureka.trie[0] = new Trie();
        addKeywords(Eureka.trie[0]);
        this.ScrollPane1.setRowHeaderView(lineNumber1);
        this.TextPane2.addKeyListener(this);
        this.TextPane2.addMouseListener(this);
        this.TextPane2.addCaretListener(this);
        ProjectStructure.setRootVisible(false);
        ProjectStructure.setShowsRootHandles(true);
        this.getContentPane().setBackground(Color.lightGray);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setFocus();
    }
    
    // A somewhat working version of undo and redo .............
    // More detailed one will be implemented later ..............
    // There are some bugs better to hide them ............
    
    public static void addUndoFuctionality(JTextPane textComp,SyntaxDocument doc,UndoManager undo){
    
       doc.addUndoableEditListener(new UndoableEditListener() {
           public void undoableEditHappened(UndoableEditEvent evt) {
               undo.addEdit(evt.getEdit());
           }
       });
    
   // Create an undo action and add it to the text component
        textComp.getActionMap().put("Undo",
            new AbstractAction("Undo") {
                public void actionPerformed(ActionEvent evt) {
                    try {

                        Element root = doc.getDefaultRootElement();
                        int pos = textComp.getCaretPosition();
                        int row = root.getElementIndex(pos);
                        Element lineElement = root.getElement(row);
                        int col = pos - lineElement.getStartOffset();
                        
                        String str = doc.getText(lineElement.getStartOffset(),col + 1) + " ";
                        
                        int count = 0,temp = 1;
                        for(int i = 0 ; i < str.length() ; i ++){
                            if(str.charAt(i) == ' ' || str.charAt(i) == '\t' && temp == 0){
                                count ++;
                                temp = 1;
                            }
                            if(str.charAt(i) != ' ' && str.charAt(i) != '\t')
                                temp = 0;
                        }
                        
                        count += 2;
                        
                        while(undo.canUndo() && count > 0){
                            undo.undo();
                            count --;
                        }
        
                    } catch (CannotUndoException ex) {
                        System.out.println(ex);
                    } catch (BadLocationException ex) {
                        System.out.println(ex);
                    }
                }
           });

        // Bind the undo action to ctl-Z
        textComp.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        // Create a redo action and add it to the text component
        textComp.getActionMap().put("Redo",
            new AbstractAction("Redo") {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        
                        Element root = doc.getDefaultRootElement();
                        int pos = textComp.getCaretPosition();
                        int row = root.getElementIndex(pos);
                        Element lineElement = root.getElement(row);
                        int col = pos - lineElement.getStartOffset();
                        
                        String str = doc.getText(lineElement.getStartOffset(),col + 1) + " ";
                        
                        int count = 0,temp = 1;
                        for(int i = 0 ; i < str.length() ; i ++){
                            if(str.charAt(i) == ' ' || str.charAt(i) == '\t' && temp == 0){
                                count ++;
                                temp = 1;
                            }
                            if(str.charAt(i) != ' ' && str.charAt(i) != '\t')
                                temp = 0;
                        }
                        
                        count += 2;
                        
                        while(undo.canRedo() && count > 0){
                            undo.redo();
                            count --;
                        }
                    } catch (CannotRedoException ex) {
                        System.out.println(ex);
                    } catch (BadLocationException ex) {
                        System.out.println(ex);
                    }
                }
            });
    
        // Bind the redo action to ctl-Y
        textComp.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

    }
    
    //Sets focus to the TextPane of selected tabbed pane
    public void setFocus(){
        JScrollPane scroll=(JScrollPane)TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        text.grabFocus();
        int index = TabbedPane.getSelectedIndex();
        if(Eureka.FilePath[index] != null){
            
            if(javafilter.accept(new File(Eureka.FilePath[index])))
                languageCombo.setSelectedItem("Java");
            if(cfilter.accept(new File(Eureka.FilePath[index])))
                languageCombo.setSelectedItem("C");
            if(cppfilter.accept(new File(Eureka.FilePath[index])))
                languageCombo.setSelectedItem("C++");
                        
        }
        if(Eureka.FilePath[index] == null){
            
            if(((SyntaxDocument) text.getStyledDocument()).keywords == javaKeywords)
                languageCombo.setSelectedItem("Java");
            if(((SyntaxDocument) text.getStyledDocument()).keywords == cppKeywords)
                languageCombo.setSelectedItem("C++");
            if(((SyntaxDocument) text.getStyledDocument()).keywords == cKeywords)
                languageCombo.setSelectedItem("C");
            
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PopupMenu = new javax.swing.JPopupMenu();
        Item31 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        Item39 = new javax.swing.JMenuItem();
        Item38 = new javax.swing.JMenuItem();
        Separator1 = new javax.swing.JPopupMenu.Separator();
        Item32 = new javax.swing.JMenuItem();
        Item33 = new javax.swing.JMenuItem();
        Item34 = new javax.swing.JMenuItem();
        Separator2 = new javax.swing.JPopupMenu.Separator();
        Item35 = new javax.swing.JMenuItem();
        Separator3 = new javax.swing.JPopupMenu.Separator();
        Item36 = new javax.swing.JMenuItem();
        Item37 = new javax.swing.JMenuItem();
        AutoComplete = new javax.swing.JPopupMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        TabbedPane = new javax.swing.JTabbedPane();
        ScrollPane1 = new javax.swing.JScrollPane();
        TextPane1 = new javax.swing.JTextPane();
        PlusScrollPane = new javax.swing.JScrollPane();
        TextPane2 = new javax.swing.JTextPane();
        Output = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        OutputArea = new javax.swing.JTextPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        InputArea = new javax.swing.JTextPane();
        CompileAndRunButton = new javax.swing.JButton();
        CompileButton = new javax.swing.JButton();
        RedoButton = new javax.swing.JButton();
        UndoButton = new javax.swing.JButton();
        SaveFileButton = new javax.swing.JButton();
        OpenFileButton = new javax.swing.JButton();
        NewFileButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        ProjectStructure = new javax.swing.JTree();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        CaretLocation = new javax.swing.JLabel();
        CaretLocation1 = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        MenuBar = new javax.swing.JMenuBar();
        M1 = new javax.swing.JMenu();
        Item1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        Item2 = new javax.swing.JMenuItem();
        Item3 = new javax.swing.JMenuItem();
        Item4 = new javax.swing.JMenuItem();
        Item5 = new javax.swing.JMenuItem();
        M2 = new javax.swing.JMenu();
        Item6 = new javax.swing.JMenuItem();
        Item7 = new javax.swing.JMenuItem();
        Item8 = new javax.swing.JMenuItem();
        Item9 = new javax.swing.JMenuItem();
        Item10 = new javax.swing.JMenuItem();
        Item11 = new javax.swing.JMenuItem();
        Item12 = new javax.swing.JMenuItem();
        Item13 = new javax.swing.JMenuItem();
        M3 = new javax.swing.JMenu();
        Item15 = new javax.swing.JMenuItem();
        Item14 = new javax.swing.JMenuItem();
        Item30 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        M5 = new javax.swing.JMenu();
        M6 = new javax.swing.JMenu();
        Item19 = new javax.swing.JMenuItem();
        Item20 = new javax.swing.JMenuItem();
        Item21 = new javax.swing.JMenuItem();
        M7 = new javax.swing.JMenu();
        Item22 = new javax.swing.JMenuItem();
        Item23 = new javax.swing.JMenuItem();
        Item24 = new javax.swing.JMenuItem();
        Item25 = new javax.swing.JMenuItem();
        M8 = new javax.swing.JMenu();
        Item26 = new javax.swing.JMenuItem();
        Item27 = new javax.swing.JMenuItem();
        Item28 = new javax.swing.JMenuItem();
        Item29 = new javax.swing.JMenuItem();
        M4 = new javax.swing.JMenu();
        Item16 = new javax.swing.JMenuItem();
        Item17 = new javax.swing.JMenuItem();
        Item18 = new javax.swing.JMenuItem();

        Item31.setText("Show Unsaved Changes");
        Item31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item31ActionPerformed(evt);
            }
        });
        PopupMenu.add(Item31);

        jSeparator2.setToolTipText("");
        PopupMenu.add(jSeparator2);

        Item39.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        Item39.setText("Compile File");
        Item39.setToolTipText("");
        PopupMenu.add(Item39);

        Item38.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, java.awt.event.InputEvent.SHIFT_MASK));
        Item38.setText("Run File");
        Item38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item38ActionPerformed(evt);
            }
        });
        PopupMenu.add(Item38);
        PopupMenu.add(Separator1);

        Item32.setAction(new DefaultEditorKit.CutAction());
        Item32.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        Item32.setText("Cut");
        Item32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item32ActionPerformed(evt);
            }
        });
        PopupMenu.add(Item32);

        Item33.setAction(new DefaultEditorKit.CopyAction());
        Item33.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        Item33.setText("Copy");
        PopupMenu.add(Item33);

        Item34.setAction(new DefaultEditorKit.PasteAction());
        Item34.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        Item34.setText("Paste");
        PopupMenu.add(Item34);
        PopupMenu.add(Separator2);

        Item35.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        Item35.setText("Select All");
        Item35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item35ActionPerformed(evt);
            }
        });
        PopupMenu.add(Item35);
        PopupMenu.add(Separator3);

        Item36.setText("Containing Folder Path");
        PopupMenu.add(Item36);

        Item37.setText("Copy File Path");
        PopupMenu.add(Item37);

        jMenuItem3.setText("Hello");
        AutoComplete.add(jMenuItem3);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Eureka");
        setAlwaysOnTop(true);
        setBackground(java.awt.Color.darkGray);
        setMinimumSize(new java.awt.Dimension(1320, 720));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        TabbedPane.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        TabbedPane.setMaximumSize(new java.awt.Dimension(1020, 500));
        TabbedPane.setMinimumSize(new java.awt.Dimension(1020, 500));
        TabbedPane.setPreferredSize(new java.awt.Dimension(1020, 500));
        TabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabbedPaneMouseClicked(evt);
            }
        });

        TextPane1.setFont(new java.awt.Font("Monospaced", 0, 16)); // NOI18N
        TextPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        TextPane1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                TextPane1CaretUpdate(evt);
            }
        });
        ScrollPane1.setViewportView(TextPane1);

        TabbedPane.addTab("untitled", ScrollPane1);

        TextPane2.setEditable(false);
        TextPane2.setFont(new java.awt.Font("Monospaced", 0, 16)); // NOI18N
        PlusScrollPane.setViewportView(TextPane2);

        TabbedPane.addTab("+", PlusScrollPane);

        Output.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N

        OutputArea.setEditable(false);
        OutputArea.setMaximumSize(null);
        OutputArea.setName(""); // NOI18N
        OutputArea.setPreferredSize(null);
        jScrollPane2.setViewportView(OutputArea);

        Output.addTab("Output", jScrollPane2);

        jTabbedPane2.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N

        jScrollPane1.setViewportView(InputArea);

        jTabbedPane2.addTab("Input", jScrollPane1);

        CompileAndRunButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Run.png"))); // NOI18N
        CompileAndRunButton.setToolTipText("Run File (f9)");
        CompileAndRunButton.setFocusable(false);
        CompileAndRunButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        CompileAndRunButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        CompileAndRunButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CompileAndRunButtonActionPerformed(evt);
            }
        });

        CompileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Compile.png"))); // NOI18N
        CompileButton.setToolTipText("Compile File (Shift+f9)");
        CompileButton.setFocusable(false);
        CompileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        CompileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        CompileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CompileButtonActionPerformed(evt);
            }
        });

        RedoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/redo.png"))); // NOI18N
        RedoButton.setToolTipText("Redo (Ctrl+Y)");
        RedoButton.setFocusable(false);
        RedoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        RedoButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        UndoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/undo.png"))); // NOI18N
        UndoButton.setToolTipText("Undo (Ctrl+Z)");
        UndoButton.setFocusable(false);
        UndoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        UndoButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        UndoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UndoButtonActionPerformed(evt);
            }
        });

        SaveFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SaveFile.png"))); // NOI18N
        SaveFileButton.setToolTipText("Save File (Ctrl+S)");
        SaveFileButton.setFocusable(false);
        SaveFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        SaveFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        SaveFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveFileButtonActionPerformed(evt);
            }
        });

        OpenFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OpenFile.png"))); // NOI18N
        OpenFileButton.setToolTipText("Open File (Ctrl+O)");
        OpenFileButton.setFocusable(false);
        OpenFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        OpenFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        OpenFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenFileButtonActionPerformed(evt);
            }
        });

        NewFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/NewFile.png"))); // NOI18N
        NewFileButton.setToolTipText("New File (Ctrl+N)");
        NewFileButton.setFocusable(false);
        NewFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        NewFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        NewFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewFileButtonActionPerformed(evt);
            }
        });

        ProjectStructure.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        ProjectStructure.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        ProjectStructure.setToolTipText("Only saved files how up here !!!");
        ProjectStructure.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                ProjectStructureValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(ProjectStructure);

        jLabel1.setText("Open Files/Projects");

        CaretLocation.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        CaretLocation.setText(" Line 1 : Column 1");

        CaretLocation1.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        CaretLocation1.setText("INS");
        CaretLocation1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                CaretLocation1MouseClicked(evt);
            }
        });

        languageCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Java", "C", "C++" }));

        jButton1.setText("Change Language");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        MenuBar.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N

        M1.setText("File");

        Item1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        Item1.setText("New File");
        Item1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item1ActionPerformed(evt);
            }
        });
        M1.add(Item1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Open Project");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        M1.add(jMenuItem2);

        Item2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        Item2.setText("Open File");
        Item2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item2ActionPerformed(evt);
            }
        });
        M1.add(Item2);

        Item3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        Item3.setText("Save");
        Item3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item3ActionPerformed(evt);
            }
        });
        M1.add(Item3);

        Item4.setText("Save As");
        Item4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item4ActionPerformed(evt);
            }
        });
        M1.add(Item4);

        Item5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        Item5.setText("Exit");
        Item5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item5ActionPerformed(evt);
            }
        });
        M1.add(Item5);

        MenuBar.add(M1);

        M2.setText("Edit");

        Item6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        Item6.setText("Undo");
        Item6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item6ActionPerformed(evt);
            }
        });
        M2.add(Item6);

        Item7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        Item7.setText("Redo");
        Item7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item7ActionPerformed(evt);
            }
        });
        M2.add(Item7);

        Item8.setAction(new DefaultEditorKit.CutAction());
        Item8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        Item8.setText("Cut");
        M2.add(Item8);

        Item9.setAction(new DefaultEditorKit.CopyAction());
        Item9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        Item9.setText("Copy");
        M2.add(Item9);

        Item10.setAction(new DefaultEditorKit.PasteAction());
        Item10.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        Item10.setText("Paste");
        M2.add(Item10);

        Item11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        Item11.setText("Delete");
        M2.add(Item11);

        Item12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        Item12.setText("Find");
        M2.add(Item12);

        Item13.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        Item13.setText("Replace");
        M2.add(Item13);

        MenuBar.add(M2);

        M3.setText("Tools");

        Item15.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        Item15.setText("Compile File");
        Item15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item15ActionPerformed(evt);
            }
        });
        M3.add(Item15);

        Item14.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, java.awt.event.InputEvent.SHIFT_MASK));
        Item14.setText("Run FIle");
        Item14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item14ActionPerformed(evt);
            }
        });
        M3.add(Item14);

        Item30.setText("Change Time Limit");
        Item30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item30ActionPerformed(evt);
            }
        });
        M3.add(Item30);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Auto Complete");
        M3.add(jMenuItem1);

        MenuBar.add(M3);

        M5.setText("Preferences");

        M6.setText("Font");

        Item19.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, java.awt.event.InputEvent.CTRL_MASK));
        Item19.setText("Larger");
        Item19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item19ActionPerformed(evt);
            }
        });
        M6.add(Item19);

        Item20.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, java.awt.event.InputEvent.CTRL_MASK));
        Item20.setText("Smaller");
        Item20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item20ActionPerformed(evt);
            }
        });
        M6.add(Item20);

        Item21.setText("Reset");
        Item21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item21ActionPerformed(evt);
            }
        });
        M6.add(Item21);

        M5.add(M6);

        M7.setText("Colour Sheme");

        Item22.setText("Mac Classic");
        M7.add(Item22);

        Item23.setText("Solarised Dark");
        M7.add(Item23);

        Item24.setText("Solarised Light");
        M7.add(Item24);

        Item25.setText("Monokai");
        M7.add(Item25);

        M5.add(M7);

        M8.setText("Look And Feel");

        Item26.setText("Metal");
        Item26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item26ActionPerformed(evt);
            }
        });
        M8.add(Item26);

        Item27.setText("Nimbus");
        Item27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item27ActionPerformed(evt);
            }
        });
        M8.add(Item27);

        Item28.setText("GTK+");
        Item28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item28ActionPerformed(evt);
            }
        });
        M8.add(Item28);

        Item29.setText("Motif");
        Item29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item29ActionPerformed(evt);
            }
        });
        M8.add(Item29);

        M5.add(M8);

        MenuBar.add(M5);

        M4.setText("Help");

        Item16.setText("Help Contents");
        Item16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item16ActionPerformed(evt);
            }
        });
        M4.add(Item16);

        Item17.setText("Keyboard Shortcut Card");
        Item17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item17ActionPerformed(evt);
            }
        });
        M4.add(Item17);

        Item18.setText("About");
        Item18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Item18ActionPerformed(evt);
            }
        });
        M4.add(Item18);

        MenuBar.add(M4);

        setJMenuBar(MenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(NewFileButton)
                                .addGap(18, 18, 18)
                                .addComponent(OpenFileButton)
                                .addGap(18, 18, 18)
                                .addComponent(SaveFileButton)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(TabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 828, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(CaretLocation)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addComponent(jButton1)
                                .addGap(28, 28, 28)
                                .addComponent(CaretLocation1)
                                .addGap(45, 45, 45)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                            .addComponent(Output)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(UndoButton)
                        .addGap(18, 18, 18)
                        .addComponent(RedoButton)
                        .addGap(18, 18, 18)
                        .addComponent(CompileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(CompileAndRunButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addComponent(jSeparator1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(UndoButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SaveFileButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(OpenFileButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(NewFileButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(RedoButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CompileAndRunButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CompileButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Output))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(TabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 615, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CaretLocation)
                            .addComponent(CaretLocation1)
                            .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1))
                        .addGap(1, 1, 1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void Item2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item2ActionPerformed
        // TODO add your handling code here:
        Eureka.openFile();
    }//GEN-LAST:event_Item2ActionPerformed

    private void Item5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item5ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_Item5ActionPerformed

    private void NewFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewFileButtonActionPerformed
        // TODO add your handling code here:
        int count=this.TabbedPane.getTabCount();
        if(count==1)
            this.NewFile2();
        else
            this.NewFile1();
    }//GEN-LAST:event_NewFileButtonActionPerformed

    private void Item26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item26ActionPerformed
        this.lookAndFeelChanger("javax.swing.plaf.metal.MetalLookAndFeel");
    }//GEN-LAST:event_Item26ActionPerformed

    private void Item27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item27ActionPerformed
        // TODO add your handling code here:
        this.lookAndFeelChanger("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    }//GEN-LAST:event_Item27ActionPerformed

    private void Item28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item28ActionPerformed
        // TODO add your handling code here:
        this.lookAndFeelChanger("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
    }//GEN-LAST:event_Item28ActionPerformed

    private void Item29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item29ActionPerformed
        // TODO add your handling code here:
        this.lookAndFeelChanger("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
    }//GEN-LAST:event_Item29ActionPerformed

    private void TabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabbedPaneMouseClicked
        // TODO add your handling code here:
        
        // That language change functionality done here ..........
        
        if(evt.getClickCount()==1){
            int index1=this.TabbedPane.getTabCount()-1;
            int index=this.TabbedPane.getSelectedIndex();
            if(index==index1){
                this.NewFile2();
                this.caretHelper();
            }
            else{
                this.setSaveOptions();
                this.caretHelper();
            }
        }
        else if(evt.getClickCount()==2){
            if(JOptionPane.showConfirmDialog(dialog,"Do you want to close this tab any unsaved content will be lost") == JOptionPane.OK_OPTION){
                int index=this.TabbedPane.getSelectedIndex();
                this.TabbedPane.remove(index);
                int index1=this.TabbedPane.getTabCount();
                for(int i=index+1;i<index1;i++){
                    Eureka.TextPane[i-1]=Eureka.TextPane[i];
                    Eureka.ScrollPane[i-1]=Eureka.ScrollPane[i];
                    Eureka.FilePath[i-1]=Eureka.FilePath[i];
                    Eureka.Save[i-1]=Eureka.Save[i];
                }
                if(index1-2>=0)
                   this.TabbedPane.setSelectedIndex(index1-2);
                Eureka.TextPane[index1]=null;
                Eureka.ScrollPane[index1]=null;
                Eureka.FilePath[index1]=null;
                Eureka.Save[index1]=-1;
            }
        }
        this.setSaveOptions();
    }//GEN-LAST:event_TabbedPaneMouseClicked

    // A newly opened pane will always have java as default highlighting ...........
   
    public void NewFile2(){
        int index=this.TabbedPane.indexOfTab("+");
        Eureka.TextPane[index]=new JTextPane(new SyntaxDocument(javaKeywords));
        Eureka.TextPane[index].addKeyListener(this);
        Eureka.TextPane[index].addMouseListener(this);
        Eureka.TextPane[index].addCaretListener(this);
        Eureka.ScrollPane[index]=new JScrollPane();
        Eureka.ScrollPane[index].setViewportView(Eureka.TextPane[index]);
        Eureka.FilePath[index]=null;
        Eureka.addUndoFuctionality(Eureka.TextPane[index],(SyntaxDocument)Eureka.TextPane[index].getStyledDocument(),new UndoManager());
        Eureka.Save[index]=0;
        this.TabbedPane.add(Eureka.ScrollPane[index],"untitled", index);
        Eureka.TextPane[index].setFont(Eureka.TextPane[0].getFont());
        TextLineNumber lineNumber = new TextLineNumber(Eureka.TextPane[index]);
        Eureka.ScrollPane[index].setRowHeaderView(lineNumber);
        Eureka.trie[index] = new Trie();
        addKeywords(Eureka.trie[index]);
        this.TabbedPane.setSelectedIndex(index);
        Eureka.ScrollPane[index+1]=this.PlusScrollPane;
        Eureka.TextPane[index+1]=this.TextPane2;
        Eureka.FilePath[index+1]=null;
        Eureka.Save[index+1]=-1;
        this.setFocus();
        this.setSaveOptions();
    }
    
    // Made suitable modifications here ..............
    
    public void NewFile1(){
        int index=this.TabbedPane.getSelectedIndex();
        int size=this.TabbedPane.getTabCount();
        for(int i=size;i>index+1;i--){
            Eureka.ScrollPane[i]=Eureka.ScrollPane[i-1];
            Eureka.TextPane[i] = Eureka.TextPane[i-1];
            Eureka.FilePath[i] = Eureka.FilePath[i-1];
            Eureka.Save[i] = Eureka.Save[i-1];
            Eureka.trie[i] = Eureka.trie[i - 1];
        }
        Eureka.ScrollPane[index+1]=new JScrollPane();
        Eureka.TextPane[index+1]=new JTextPane(new SyntaxDocument(javaKeywords));
        Eureka.TextPane[index+1].addKeyListener(this);
        Eureka.TextPane[index+1].addMouseListener(this);
        Eureka.TextPane[index+1].addCaretListener(this);
        Eureka.FilePath[index+1]=null;
        Eureka.Save[index+1]=0;
        Eureka.ScrollPane[index+1].setViewportView(Eureka.TextPane[index+1]);
        Eureka.TextPane[index+1].setFont(this.TextPane[size].getFont());
        this.TabbedPane.add(Eureka.ScrollPane[index+1],"untitled",index+1);
        TextLineNumber lineNumber = new TextLineNumber(Eureka.TextPane[index+1]);
        ScrollPane[index+1].setRowHeaderView(lineNumber);
        Eureka.trie[index + 1] = new Trie();
        addKeywords(Eureka.trie[index + 1]);
        this.TabbedPane.setSelectedIndex(index+1);
        Eureka.addUndoFuctionality(Eureka.TextPane[index + 1],(SyntaxDocument)Eureka.TextPane[index + 1].getStyledDocument(),new UndoManager());
        this.setFocus();
        this.setSaveOptions();
        this.caretHelper();
    }
    
    // Most important function to change ..............
    // Changing on own risk ...............
    
    public static void OpenFile(String filename,String filecontent,String filepath,boolean flag){
        int count=frame.TabbedPane.getTabCount();
        for(int i = 0 ; i < count ; i ++){
            if(Eureka.FilePath[i] != null && Eureka.FilePath[i].compareTo(filepath) == 0){
                if(Eureka.Save[i] == 2){
                    if(JOptionPane.showConfirmDialog(dialog,"The state of file on disk and editor has changed do you want to reload it ?") == 0){
                        Eureka.TextPane[i].setText(filecontent);
                        frame.TabbedPane.setSelectedIndex(i);
                        if(cfilter.accept(new File(filepath))){
                            frame.languageCombo.setSelectedItem("C");
                            ((SyntaxDocument) Eureka.TextPane[i].getStyledDocument()).changeHighlighting(cKeywords);
                        }
                        if(cppfilter.accept(new File(filepath))){
                            frame.languageCombo.setSelectedItem("C++");
                            ((SyntaxDocument) Eureka.TextPane[i].getStyledDocument()).changeHighlighting(cppKeywords);
                        }
                        if(javafilter.accept(new File(filepath))){
                            frame.languageCombo.setSelectedItem("Java");
                            ((SyntaxDocument) Eureka.TextPane[i].getStyledDocument()).changeHighlighting(javaKeywords);
                        }
                    }
                    else
                        frame.TabbedPane.setSelectedIndex(i);
                    return;
                }
                else{
                    frame.TabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }
        if(count==1){
            Eureka.TextPane[0]=new JTextPane(new SyntaxDocument(javaKeywords));
            Eureka.TextPane[0].addKeyListener(frame);
            Eureka.TextPane[0].addMouseListener(frame);
            Eureka.ScrollPane[0]=new JScrollPane();
            Eureka.ScrollPane[0].setViewportView(Eureka.TextPane[0]);
            Eureka.FilePath[0]=filepath;
            if(flag)
                frame.setDirectoryStructure(new File(filepath));
            Eureka.Save[0]=1;
            frame.TabbedPane.add(Eureka.ScrollPane[0],filename,0);
            Eureka.TextPane[0].setFont(frame.TextPane2.getFont());
            Eureka.TextPane[0].setText(filecontent);
            TextLineNumber lineNumber = new TextLineNumber(Eureka.TextPane[0]);
            Eureka.ScrollPane[0].setRowHeaderView(lineNumber);
            Eureka.addUndoFuctionality(Eureka.TextPane[0],(SyntaxDocument)Eureka.TextPane[0].getStyledDocument(),new UndoManager());
            Eureka.trie[0] = new Trie();
            frame.addKeywords(Eureka.trie[0]);
            Eureka.TextPane[1]=frame.TextPane2;
            Eureka.ScrollPane[1]=frame.ScrollPane1;
            Eureka.FilePath[1]=null;
            Eureka.Save[1]=-1;
            frame.TabbedPane.setSelectedIndex(0);
            if(cfilter.accept(new File(filepath))){
                frame.languageCombo.setSelectedItem("C");
                ((SyntaxDocument) Eureka.TextPane[0].getStyledDocument()).changeHighlighting(cKeywords);
            }
            if(cppfilter.accept(new File(filepath))){
                frame.languageCombo.setSelectedItem("C++");
                ((SyntaxDocument) Eureka.TextPane[0].getStyledDocument()).changeHighlighting(cppKeywords);
            }
            if(javafilter.accept(new File(filepath))){
                frame.languageCombo.setSelectedItem("Java");
                ((SyntaxDocument) Eureka.TextPane[0].getStyledDocument()).changeHighlighting(javaKeywords);
            }
        }
        else{
            if(Eureka.Save[frame.TabbedPane.getSelectedIndex()] != 1){
                if(JOptionPane.showConfirmDialog(dialog,"Do you want to overwrite the existing unsaved content !") != 0)
                    Eureka.frame.NewFile1();
            }
            int index=frame.TabbedPane.getSelectedIndex();
            Eureka.TextPane[index].setText(filecontent);
            frame.TabbedPane.setTitleAt(index,filename);
            Eureka.FilePath[index]=filepath;
            if(flag)
                frame.setDirectoryStructure(new File(filepath));
            Eureka.Save[index]=1;
            if(cfilter.accept(new File(filepath))){
                frame.languageCombo.setSelectedItem("C");
                ((SyntaxDocument) Eureka.TextPane[index].getStyledDocument()).changeHighlighting(cKeywords);
            }
            if(cppfilter.accept(new File(filepath))){
                frame.languageCombo.setSelectedItem("C++");
                ((SyntaxDocument) Eureka.TextPane[index].getStyledDocument()).changeHighlighting(cppKeywords);
            }
            if(javafilter.accept(new File(filepath))){
                frame.languageCombo.setSelectedItem("Java");
                ((SyntaxDocument) Eureka.TextPane[index].getStyledDocument()).changeHighlighting(javaKeywords);
            }
        }
        Eureka.frame.setSaveOptions();
        Eureka.frame.caretHelper();
    }
    
    private static void openFile(){
        Open open=new Open();
        open.setVisible(true);
        open.setFocusable(true);
    }
    
    public String sendFileName(){
        return this.TabbedPane.getTitleAt(TabbedPane.getSelectedIndex());
    }
    
    private static void saveFile(){
        String filepath=Eureka.FilePath[frame.TabbedPane.getSelectedIndex()];
        java.io.File file;
        if(filepath==null)
            file=null;
        else
            file=new java.io.File(filepath);
        try{
            if(file!=null){
                //file already exists with this name in destination
                java.io.FileWriter writer=new java.io.FileWriter(file);
                writer.write(frame.sendText());
                writer.flush();
                Eureka.Save[frame.TabbedPane.getSelectedIndex()]=1;
            }
            if(file==null){
                Eureka.saveFileAs();
            }
        }
        catch(java.io.IOException e){
            JOptionPane.showMessageDialog(dialog,"Input Output Exception Occurred");
        }
        Eureka.frame.setSaveOptions();
    }
    
    private static void saveFileAs(){
        Save save=new Save();
        save.setVisible(true);
        save.setFocusable(true);
    }
    
    public void SetTitle(String str){
        this.TabbedPane.setTitleAt(this.TabbedPane.getSelectedIndex(),str);
    }
    
    public void setFilePath(String str){
        int index = this.TabbedPane.getSelectedIndex();
        Eureka.FilePath[index]=str;
        Eureka.Save[index]=1;
         System.out.println("Program Control Came Here.");
        if(cfilter.accept(new File(str))){
            frame.languageCombo.setSelectedItem("C");
            ((SyntaxDocument) Eureka.TextPane[index].getStyledDocument()).changeHighlighting(cKeywords);
        }
        if(cppfilter.accept(new File(str))){
            frame.languageCombo.setSelectedItem("C++");
            ((SyntaxDocument) Eureka.TextPane[index].getStyledDocument()).changeHighlighting(cppKeywords);
        }
        if(javafilter.accept(new File(str))){
            frame.languageCombo.setSelectedItem("Java");
            ((SyntaxDocument) Eureka.TextPane[index].getStyledDocument()).changeHighlighting(javaKeywords);
        }
        this.setSaveOptions();
    }
    
    public String sendText(){
        JScrollPane scroll=(JScrollPane)this.TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        return text.getText();
    }
    
    private void setSaveOptions(){
        int index=this.TabbedPane.getSelectedIndex();
        if(Eureka.Save[index]==0){
            this.Item3.setEnabled(true);
            this.Item31.setEnabled(false);
        }
        if(Eureka.Save[index]==1){
            this.Item3.setEnabled(false);
            this.Item31.setEnabled(false);
        }
        if(Eureka.Save[index]==2){
            this.Item3.setEnabled(true);
            this.Item31.setEnabled(true);
        }
    }
    
    private void Item19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item19ActionPerformed
        // TODO add your handling code here:
        this.fontChanger(2.0f);
    }//GEN-LAST:event_Item19ActionPerformed

    private void fontChanger(float value){
        java.awt.Font font=Eureka.TextPane[0].getFont();
        float size=font.getSize()+value;
        font=font.deriveFont(size);
        int index=this.TabbedPane.getTabCount();
        for(int i=0;i<index;i++){
            Eureka.TextPane[i].setFont(font);
        }
        this.OutputArea.setFont(font);
        this.InputArea.setFont(font);
    }

    private void Item20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item20ActionPerformed
        // TODO add your handling code here:
        fontChanger(-2.0f);
    }//GEN-LAST:event_Item20ActionPerformed

    private void Item21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item21ActionPerformed
        // TODO add your handling code here:
        int index=this.TabbedPane.getTabCount();
        java.awt.Font font=new java.awt.Font("Monospaced",java.awt.Font.PLAIN,16);
        for(int i=0;i<index;i++){
            Eureka.TextPane[i].setFont(font);
        }
    }//GEN-LAST:event_Item21ActionPerformed

    /*
    Something is wrong with this part of code 
    */
    private void Item30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item30ActionPerformed
        // TODO add your handling code here:
        String str;
        str = JOptionPane.showInputDialog(dialog,"Current time limit is set to:"+RUNTIME+" Enter new time limit:");
        try{
            int val = Integer.parseInt(str);
            if(val < 0)
                throw new java.lang.NumberFormatException();
            RUNTIME = val;
        }
        catch(java.lang.NumberFormatException e){
            JOptionPane.showMessageDialog(dialog,"Enter a valid value for time limit(Time limit must be an valid integral value)");
        }
    }//GEN-LAST:event_Item30ActionPerformed

    private void OpenFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenFileButtonActionPerformed
        // TODO add your handling code here:
        Eureka.openFile();
    }//GEN-LAST:event_OpenFileButtonActionPerformed

    private void Item1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item1ActionPerformed
        // TODO add your handling code here:
        int count=this.TabbedPane.getTabCount();
        if(count==1)
            this.NewFile2();
        else
            this.NewFile1();
    }//GEN-LAST:event_Item1ActionPerformed

    //This event too is not of any use .... try something else (I am trying)
    //This function is as useless as me ...
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
        System.out.println("Key Pressed");
    }//GEN-LAST:event_formKeyPressed

    private void Item32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item32ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Item32ActionPerformed

    private void Item35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item35ActionPerformed
        // TODO add your handling code here:
        Component comp=this.TabbedPane.getSelectedComponent();
        JScrollPane scroll=(JScrollPane)comp;
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        text.selectAll();
    }//GEN-LAST:event_Item35ActionPerformed

    private void Item3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item3ActionPerformed
        // TODO add your handling code here:
        Eureka.saveFile();
    }//GEN-LAST:event_Item3ActionPerformed

    private void SaveFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveFileButtonActionPerformed
        // TODO add your handling code here:
        Eureka.saveFile();
    }//GEN-LAST:event_SaveFileButtonActionPerformed

    private void Item4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item4ActionPerformed
        // TODO add your handling code here:
        Eureka.saveFileAs();
    }//GEN-LAST:event_Item4ActionPerformed

    //Don't know why input output redirection is not working will figure it out soon.....
    private void Item31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item31ActionPerformed
        // code for showing unsaved changes its not woking bro ??????
        int index=this.TabbedPane.getSelectedIndex();
        JScrollPane scroll=(JScrollPane)TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        java.io.File file1,file2;
        String Os=System.getProperty("os.name");
        if(Os.compareTo("Linux")==0){
            Runtime run=Runtime.getRuntime();
            try{
                if(Eureka.FilePath[index]==null){
                    file1=new java.io.File("File1");
                    file1.createNewFile();
                }
                else
                    file1=new java.io.File(FilePath[index]);
                file2=new java.io.File("File2");
                file2.createNewFile();
                java.io.FileWriter writer=new java.io.FileWriter(file2);
                String str=text.getText();
                writer.write(str);
                writer.flush();
                String command="diff "+file1.getAbsolutePath()+" "+file2.getAbsolutePath();
                Process process=run.exec(command);
                try{
                    process.waitFor();
                    Scanner scan=new Scanner(process.getInputStream());
                    OutputArea.setText("");
                    StyledDocument doc = OutputArea.getStyledDocument();
                    String diff = "";
                    while(scan.hasNext()){
                        diff = diff + " " + scan.nextLine()+ "\n" ;
                    }
                    JOptionPane.showMessageDialog(dialog,diff,"Unsaved Changes",1);
                    if(Eureka.FilePath[index]==null)
                        file1.delete();
                    file2.delete();
                }
                catch(InterruptedException e){
                    JOptionPane.showMessageDialog(dialog,"Process Interrupted");
                }
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(dialog,"Input Output Exception Occurred");
            }
        }
        else
            JOptionPane.showMessageDialog(dialog,"Unsupported Operating System");
    }//GEN-LAST:event_Item31ActionPerformed

    private void CompileAndRunButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CompileAndRunButtonActionPerformed
        // TODO add your handling code here:
        this.runFile();
    }//GEN-LAST:event_CompileAndRunButtonActionPerformed

    private void CompileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CompileButtonActionPerformed
        // TODO add your handling code here:
        this.compile();
    }//GEN-LAST:event_CompileButtonActionPerformed

    private void Item14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item14ActionPerformed
        // TODO add your handling code here:
        this.runFile();
    }//GEN-LAST:event_Item14ActionPerformed

    private void Item15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item15ActionPerformed
        // TODO add your handling code here:
        this.compile();
    }//GEN-LAST:event_Item15ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        OpenProject openProject = new OpenProject();
        openProject.setVisible(true);
        openProject.setFocusable(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void ProjectStructureValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_ProjectStructureValueChanged
        // TODO add your handling code here:
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
        FileNode file = (FileNode) node.getUserObject();
        if(file.getFile().isFile()){
            // do some stuff here .........
            javax.swing.filechooser.FileNameExtensionFilter filter=new javax.swing.filechooser.FileNameExtensionFilter(".c, .cpp, .java","c","cpp","java");
            if(filter.accept(file.getFile())){
                try {
                    FileReader reader = new FileReader(file.getFile());
                    char inputBuffer[]=new char[(int)file.getFile().length()];
                    int red=reader.read(inputBuffer);
                    StringBuffer str=new StringBuffer("");
                    str.append(inputBuffer);
                    String Str=str.toString();
                    Eureka.OpenFile(file.getFile().getName(), Str, file.getFile().getPath(),false);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(dialog,"File not found");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog,"IO Exception Occurred");
                }
                ProjectStructure.clearSelection();
            }
            else{
                JOptionPane.showMessageDialog(dialog,"Editing of files other than C, C++ and Java is not supported yet !!!");
            }
        }
    }//GEN-LAST:event_ProjectStructureValueChanged

    private void Item38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item38ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Item38ActionPerformed

    private void CaretLocation1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CaretLocation1MouseClicked
        // TODO add your handling code here:
        try{
            int lineNum = Integer.parseInt(JOptionPane.showInputDialog(dialog,"Enter the line to which you want to jump."));
            JScrollPane scroll=(JScrollPane)this.TabbedPane.getSelectedComponent();
            JViewport port=(JViewport)scroll.getComponent(0);
            JTextPane text=(JTextPane)port.getComponent(0);
            int lineCount = Eureka.getTextPaneLineCount(text);
            System.out.println(lineNum);
            if(lineNum <= 0 || lineNum > lineCount)
                throw new java.lang.NumberFormatException();
            // implement code here .............
            int totalCharacters = text.getText().length();
            Element root = text.getDocument().getDefaultRootElement();
            int lCount = (totalCharacters == 0) ? 1 : 0;
            int offset = totalCharacters;
            while (offset > 0){
                offset = Utilities.getRowStart(text, offset) - 1;
                Element line = root.getElement(root.getElementIndex(offset + 1));
                if(lCount == (lineCount - lineNum)){
                    text.setCaretPosition(offset + 1);
                    break;
                }
                if(line.getStartOffset() == offset + 1)
                    lCount ++;
            }
        }
        catch(java.lang.NumberFormatException e){
            JOptionPane.showMessageDialog(dialog,"Enter a valid line number.");
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_CaretLocation1MouseClicked

    // This is a testing function ..............
    private void TextPane1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_TextPane1CaretUpdate
        // TODO add your handling code here:
    }//GEN-LAST:event_TextPane1CaretUpdate

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        
        // This button is now funtional ............
        
        JScrollPane scroll=(JScrollPane)TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        String str = (String) languageCombo.getSelectedItem();
        SyntaxDocument syntax = (SyntaxDocument) text.getStyledDocument();
        if(str.compareTo("Java") == 0)
            syntax.changeHighlighting(javaKeywords);
        if(str.compareTo("C") == 0)
            syntax.changeHighlighting(cKeywords);
        if(str.compareTo("C++") == 0)
            syntax.changeHighlighting(cppKeywords);
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void UndoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UndoButtonActionPerformed
        // TODO add your handling code here:
        // Perform undo action ..........
        
        int index=this.TabbedPane.getSelectedIndex();
        JScrollPane scroll=(JScrollPane)this.TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        
        
        
        // implement it here .........
    }//GEN-LAST:event_UndoButtonActionPerformed

    private void Item6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item6ActionPerformed
        // TODO add your handling code here:
        UndoButton.doClick();
    }//GEN-LAST:event_Item6ActionPerformed

    private void Item7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item7ActionPerformed
        // TODO add your handling code here:
        RedoButton.doClick();
    }//GEN-LAST:event_Item7ActionPerformed

    // Add code for keyboard shortcut card here ...........
    
    private void Item17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item17ActionPerformed
        // TODO(3)
    }//GEN-LAST:event_Item17ActionPerformed

    // Add code for about here ...........
    
    private void Item18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item18ActionPerformed
        // TODO(1) 
    }//GEN-LAST:event_Item18ActionPerformed

    // Add code for help content here ...........
    
    private void Item16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Item16ActionPerformed
        // TODO(2)
    }//GEN-LAST:event_Item16ActionPerformed

    private int compile(){
        int index=TabbedPane.getSelectedIndex();
        String filepath=Eureka.FilePath[index];
        if(filepath==null){
            JOptionPane.showMessageDialog(dialog,"File cannot be compiled without saving please first save file and then try to compile");
            return 0;
        }
        else{
            Eureka.saveFile();
            java.io.File file=new java.io.File(filepath);
            String Os=System.getProperty("os.name");
            if(Os.compareTo("Linux")==0){
                Runtime runtime=Runtime.getRuntime();
                try{
                    String command="";
                    if(cfilter.accept(file))
                        command="gcc"+" "+file.getAbsolutePath();
                    if(cppfilter.accept(file))
                        command="g++"+" " +file.getAbsolutePath();
                    if(javafilter.accept(file))
                        command="javac"+" "+file.getAbsolutePath();
                    Process proc=runtime.exec(command,null,file.getParentFile());
                    proc.waitFor();
                    Scanner scan=new Scanner(proc.getErrorStream());
                    StyledDocument doc = OutputArea.getStyledDocument();
                    if(scan.hasNext()){
                        OutputArea.setText("");
                        doc.insertString(doc.getLength(),"Compilation Error \n",failure);
                        while(scan.hasNext())
                            doc.insertString(doc.getLength()," "+scan.nextLine()+"\n",null);
                        return 0;
                    }
                    else{
                        OutputArea.setText("");
                        doc.insertString(doc.getLength(),"Code compiled successfully!!!",success);
                        return 1;
                    }
                }
                catch(IOException e){
                    JOptionPane.showMessageDialog(dialog,"Input Output Exception Occurred");
                    return 0;
                }
                catch(InterruptedException e){
                    JOptionPane.showMessageDialog(dialog,"Process Interrupted");
                    return 0;
                } catch (BadLocationException ex) {
                    System.out.println("Internal Error In Output Area !!!!!!!!");
                    return 0;
                }
            }
            else{
                JOptionPane.showMessageDialog(dialog,"Unsupported Operating System");
                return 0;
            }
        }
    }
    
    private void runFile(){
        if(this.compile()==1){
            int index=TabbedPane.getSelectedIndex();
            java.io.File file=new java.io.File(Eureka.FilePath[index]);
            try{
                String command="";
                if(cfilter.accept(file))
                    command="./a.out";
                if(cppfilter.accept(file))
                    command="./a.out";
                if(javafilter.accept(file)){
                    String name=file.getName().substring(0,file.getName().length()-5);
                    command="java "+name;
                }
                Runtime runtime=Runtime.getRuntime();
                Process proc=runtime.exec(command,null,file.getParentFile());
                java.io.BufferedWriter writer=new java.io.BufferedWriter(new java.io.OutputStreamWriter(proc.getOutputStream()));
                writer.write(InputArea.getText());
                writer.flush();
                writer.write("\n");
                writer.flush();
                java.util.GregorianCalendar date1 = new java.util.GregorianCalendar();
                long time1=date1.getTimeInMillis();
                StyledDocument doc = OutputArea.getStyledDocument();
                if(proc.waitFor(Eureka.RUNTIME,TimeUnit.SECONDS)){
                    java.util.GregorianCalendar date2 = new java.util.GregorianCalendar();
                    long time2=date2.getTimeInMillis();
                    Scanner error=new Scanner(proc.getErrorStream());
                    if(proc.getErrorStream().available()!=0)
                        if(error.hasNext()){
                            OutputArea.setText("");
                            doc.insertString(doc.getLength(),"Error Occurred\n",failure);
                            while(error.hasNext())
                                doc.insertString(doc.getLength()," "+error.nextLine()+"\n",null);
                            return;
                        }
                    Scanner output=new Scanner(proc.getInputStream());
                    OutputArea.setText("");
                    doc.insertString(doc.getLength(),"Run Successful (total time: "+(float)(time2-time1)/1000+" seconds)\n",success);
                    if(proc.getInputStream().available()!=0)
                        while(output.hasNext())
                            doc.insertString(doc.getLength()," "+output.nextLine()+"\n",null);
                    return;
                }
                else{
                    //JOptionPane.showMessageDialog(dialog,"Time limit exceeded");
                    java.util.GregorianCalendar date2 = new java.util.GregorianCalendar();
                    long time2=date2.getTimeInMillis();
                    OutputArea.setText("");
                    doc.insertString(doc.getLength(),"Build Stopped (total time: "+(float)(time2-time1)/1000+" seconds)\n",failure);
                    Scanner output=new Scanner(proc.getInputStream());
                    //output.hasNext() a blocking call do something about it
                    if(proc.getInputStream().available()!=0)
                        while(output.hasNext())
                            doc.insertString(doc.getLength()," "+output.nextLine()+"\n",null);
                    return;
                }
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(dialog,"Input Output Exception Occurred");
                return;
            }
            catch(InterruptedException e){
                JOptionPane.showMessageDialog(dialog,"Process Interrupted");
                return;
            } catch (BadLocationException ex) {
                System.out.println("Internal Error In Output Area !!!!!!!!");
                return;
            }
        }
        else{
            return;
        }
    }
    
    public void setDirectoryStructure(File dir){
        DefaultTreeModel model = (DefaultTreeModel) ProjectStructure.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        DefaultMutableTreeNode curNode = addNodes(null,dir);
        root.add(curNode);
        model.reload(root);
        ProjectStructure.expandPath(new TreePath(curNode.getPath()));
    }
    
    private DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir){
        String curPath = dir.getPath();
        DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(new FileNode(dir));
        if(curTop != null){
            curTop.add(curDir);
        }
        String content[] = dir.list();
        File f;
        ArrayList<File> files = new ArrayList<>();
        for(int i = 0 ; content != null && i < content.length ; i ++){
            String curFile = content[i];
            String newPath;
            if(curPath.compareTo(".") == 0)
                newPath = curFile;
            else
                newPath = curPath + File.separator + curFile;
            if((f = new File(newPath)).isDirectory())
                addNodes(curDir,f);
            else
                files.add(f);
        }
        for(int i = 0 ; i < files.size() ; i ++){
            curDir.add(new DefaultMutableTreeNode(new FileNode(files.get(i))));
        }
        return curDir;
    }
    
    public void setCaretPosition(int offset){
        JScrollPane scroll=(JScrollPane)this.TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        text.setCaretPosition(offset);
    }
    
    public static int getTextPaneLineCount(JTextPane textPane){
        int totalCharacters = textPane.getText().length();
        Element root = textPane.getDocument().getDefaultRootElement();
        int lineCount = (totalCharacters == 0) ? 1 : 0;
        try {
            int offset = totalCharacters; 
            while (offset > 0) {
                offset = Utilities.getRowStart(textPane, offset) - 1;
                Element line = root.getElement(root.getElementIndex(offset + 1));
                if(line.getStartOffset() == offset + 1)
                    lineCount++;
            }
        }
        catch (BadLocationException e){
            e.printStackTrace();
        }
        return lineCount;
    }
    
    /**
     * This function sets the look and feel to the one specified by string Look
     * @param Look String specifying the look and feel class name
     */
    private void lookAndFeelChanger(String Look){
        try {
            boolean k=true;
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if (Look.equals(info.getClassName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    javax.swing.SwingUtilities.updateComponentTreeUI(frame);
                    javax.swing.SwingUtilities.updateComponentTreeUI(frame.PopupMenu);
                    frame.pack();
                    k=false;
                    break;
                }
            }
            if(k)
                JOptionPane.showMessageDialog(dialog,"This LookAndFeel is not supported. Setting default Metal Look And Feel");
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Eureka.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Eureka.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Eureka.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Eureka.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
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
                if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Eureka.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Eureka.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Eureka.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Eureka.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame=new Eureka();
                frame.setVisible(true);
                Eureka.TextPane[0]=frame.TextPane1;
                Eureka.ScrollPane[0]=frame.ScrollPane1;
                Eureka.FilePath[0]=null;
                Eureka.Save[0]=0;
                Eureka.TextPane[1]=frame.TextPane2;
                Eureka.ScrollPane[1]=frame.PlusScrollPane;
                Eureka.FilePath[1]=null;
                Eureka.Save[1]=-1;
                frame.setSaveOptions();
            }
        });
    }
    private static int RUNTIME=2;
    public static Eureka frame;
    private static JTextPane TextPane[]=new JTextPane[256];
    private static JScrollPane ScrollPane[]=new JScrollPane[256];
    private static String FilePath[]=new String[256];
    private static Trie trie[] = new Trie[256];
    private static int Save[]=new int[256];
    private static javax.swing.filechooser.FileFilter cfilter =new javax.swing.filechooser.FileNameExtensionFilter(null,"c");
    private static javax.swing.filechooser.FileFilter cppfilter =new javax.swing.filechooser.FileNameExtensionFilter(null,"cpp");
    private static javax.swing.filechooser.FileFilter javafilter =new javax.swing.filechooser.FileNameExtensionFilter(null,"java");
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu AutoComplete;
    private javax.swing.JLabel CaretLocation;
    private javax.swing.JLabel CaretLocation1;
    private javax.swing.JButton CompileAndRunButton;
    private javax.swing.JButton CompileButton;
    private javax.swing.JTextPane InputArea;
    private javax.swing.JMenuItem Item1;
    private javax.swing.JMenuItem Item10;
    private javax.swing.JMenuItem Item11;
    private javax.swing.JMenuItem Item12;
    private javax.swing.JMenuItem Item13;
    private javax.swing.JMenuItem Item14;
    private javax.swing.JMenuItem Item15;
    private javax.swing.JMenuItem Item16;
    private javax.swing.JMenuItem Item17;
    private javax.swing.JMenuItem Item18;
    private javax.swing.JMenuItem Item19;
    private javax.swing.JMenuItem Item2;
    private javax.swing.JMenuItem Item20;
    private javax.swing.JMenuItem Item21;
    private javax.swing.JMenuItem Item22;
    private javax.swing.JMenuItem Item23;
    private javax.swing.JMenuItem Item24;
    private javax.swing.JMenuItem Item25;
    private javax.swing.JMenuItem Item26;
    private javax.swing.JMenuItem Item27;
    private javax.swing.JMenuItem Item28;
    private javax.swing.JMenuItem Item29;
    private javax.swing.JMenuItem Item3;
    private javax.swing.JMenuItem Item30;
    private javax.swing.JMenuItem Item31;
    private javax.swing.JMenuItem Item32;
    private javax.swing.JMenuItem Item33;
    private javax.swing.JMenuItem Item34;
    private javax.swing.JMenuItem Item35;
    private javax.swing.JMenuItem Item36;
    private javax.swing.JMenuItem Item37;
    private javax.swing.JMenuItem Item38;
    private javax.swing.JMenuItem Item39;
    private javax.swing.JMenuItem Item4;
    private javax.swing.JMenuItem Item5;
    private javax.swing.JMenuItem Item6;
    private javax.swing.JMenuItem Item7;
    private javax.swing.JMenuItem Item8;
    private javax.swing.JMenuItem Item9;
    private javax.swing.JMenu M1;
    private javax.swing.JMenu M2;
    private javax.swing.JMenu M3;
    private javax.swing.JMenu M4;
    private javax.swing.JMenu M5;
    private javax.swing.JMenu M6;
    private javax.swing.JMenu M7;
    private javax.swing.JMenu M8;
    private javax.swing.JMenuBar MenuBar;
    private javax.swing.JButton NewFileButton;
    private javax.swing.JButton OpenFileButton;
    private javax.swing.JTabbedPane Output;
    private javax.swing.JTextPane OutputArea;
    private javax.swing.JScrollPane PlusScrollPane;
    private javax.swing.JPopupMenu PopupMenu;
    private javax.swing.JTree ProjectStructure;
    private javax.swing.JButton RedoButton;
    private javax.swing.JButton SaveFileButton;
    private javax.swing.JScrollPane ScrollPane1;
    private javax.swing.JPopupMenu.Separator Separator1;
    private javax.swing.JPopupMenu.Separator Separator2;
    private javax.swing.JPopupMenu.Separator Separator3;
    private javax.swing.JTabbedPane TabbedPane;
    private javax.swing.JTextPane TextPane1;
    private javax.swing.JTextPane TextPane2;
    private javax.swing.JButton UndoButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JComboBox<String> languageCombo;
    // End of variables declaration//GEN-END:variables
    
    
    public void tabbedPaneAdditionalFunctionality(boolean operation){
        int index=this.TabbedPane.getSelectedIndex();
        JScrollPane scroll=(JScrollPane)this.TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        if(Eureka.Save[index] != 2 && !operation){
            Eureka.Save[index] = 2;
            Eureka.frame.setSaveOptions();
        }
        if(Eureka.FilePath[index] == null && operation){
            String Str = text.getText();
            if(Str.indexOf("\n") != -1)
                Str = Str.substring(0,Str.indexOf("\n"));
            if(!Str.equals(""))
                this.TabbedPane.setTitleAt(index, Str);
            else
                this.TabbedPane.setTitleAt(index,"untitled");
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
       // It is good that i never deleted the dependency over key Listener ...........
       
       /*
       I achieved this by adding a key listener to the JTextPane and checking for CTRL + Space keystrokes. 
       When the appropriate key combo was detected the listener went off and looked up the list of possible 
       matches based on the characters directly to the left of the cursor at the time of the key press and 
       found the best matches and displayed them to the user in a JPopup. If there was an exact match then 
       it simply replaced the partial text with the match. If no matches were found an option was given to 
       the user to add the text that they had already typed, edit it and record it into the list of acceptable data.
       */
       
       // What a jugaad bhai maaza aa gaya dhaaga khol diya .........
       
        JScrollPane scroll=(JScrollPane)TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
       
       if(e.isControlDown() && e.getKeyChar() == ' '){
            Point p =  text.getCaret().getMagicCaretPosition();
            
            if(p != null){
                
                int index = TabbedPane.getSelectedIndex();
                
                int compCount = this.AutoComplete.getComponentCount() - 1;
                while(compCount >= 0)
                    this.AutoComplete.remove(compCount -- );
                
                int pos = text.getCaretPosition();
                Element map = text.getDocument().getDefaultRootElement();
                int row = map.getElementIndex(pos);
                Element lineElement = map.getElement(row);
                int col = pos - lineElement.getStartOffset();
                
                try {
                    String str = text.getDocument().getText(lineElement.getStartOffset(),col);
                    
                    StringBuffer search = new StringBuffer("");
                    
                    for(int i = str.length() - 1 ; i >= 0 ; i --){
                        char ch = str.charAt(i);
                        if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_'){
                            search.append(ch);
                        }
                        else
                            break;
                    }
                    
                    search = search.reverse();
                    int len = search.length();
                    
                    List<String> match = Eureka.trie[index].getWords(search.toString());
                    for(String Str: match){
                        JMenuItem item = new JMenuItem(Str);
                        item.addActionListener(new ActionListener(){
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    JMenuItem item = (JMenuItem) e.getSource();
                                    String str = item.getToolTipText();
                                    ((SyntaxDocument) text.getStyledDocument()).insertString(pos,str,new SimpleAttributeSet());
                                } catch (BadLocationException ex) {
                                    System.out.println(ex);
                                }
                            }
                            
                        });
                        this.AutoComplete.add(item);
                        String ans = "";
                        for(int i = len ; i < Str.length() ; i ++)
                            ans += Str.charAt(i);
                        item.setToolTipText(ans);
                    }
                    
                } catch (BadLocationException ex) {
                    System.out.println(ex);
                }
                
                this.AutoComplete.show(this.TabbedPane.getSelectedComponent(),p.x + 50,p.y + 3);
            }
       }
       
       if(e.getKeyChar() == ' ' || e.getKeyChar() == '\t'){
           
            int index = TabbedPane.getSelectedIndex();
            
            int pos = text.getCaretPosition();
            Element map = text.getDocument().getDefaultRootElement();
            int row = map.getElementIndex(pos);
            Element lineElement = map.getElement(row);
            int col = pos - lineElement.getStartOffset();
            
            try{
                String str = text.getDocument().getText(lineElement.getStartOffset(),col);
                    
                    StringBuffer search = new StringBuffer("");
                    
                    for(int i = str.length() - 1 ; i >= 0 ; i --){
                        char ch = str.charAt(i);
                        if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_'){
                            search.append(ch);
                        }
                        else
                            break;
                    }
                    
                    search = search.reverse();
                    
                    Eureka.trie[index].addWord(search.toString());
                    
            } catch (BadLocationException ex) {
                System.out.println(ex);
            }
                       
       }
       
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //System.out.println("Key Pressed");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //System.out.println("Key Released");
    }
    //Mouse.BUTTON1 refers to Left Button
    //Mouse.BUTTON2 refers to Scroll Whell Button
    //Mouse.BUTTON3 refers to Right Button
    //Figure Out How to work with popup menu's
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3){
            this.PopupMenu.show(this.TabbedPane.getSelectedComponent(),e.getX(),e.getY());
        }
         //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
         //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //To change body of generated methods, choose Tools | Templates.
    }

    public void caretHelper(){
        JScrollPane scroll=(JScrollPane)TabbedPane.getSelectedComponent();
        JViewport port=(JViewport)scroll.getComponent(0);
        JTextPane text=(JTextPane)port.getComponent(0);
        int pos = text.getCaretPosition();
        Element map = text.getDocument().getDefaultRootElement();
        int row = map.getElementIndex(pos);
        Element lineElement = map.getElement(row);
        int col = pos - lineElement.getStartOffset();
        row ++ ; col ++;
        CaretLocation.setText("Line "+row+" : Column "+col);
    }
    
    @Override
    public void caretUpdate(CaretEvent e) {
        //To change body of generated methods, choose Tools | Templates.
        this.caretHelper();
    }
}
