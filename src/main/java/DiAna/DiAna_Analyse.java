/**
 * Copyright (C) 2016 Jean-François Gilles

    License:
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without e
    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
ven the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */

package DiAna;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.Recorder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;


/**
 *
 * @author jeff
 */
public class DiAna_Analyse extends JFrame implements MouseListener {
    
    boolean init=false;
    int nbImg, oldNbImg;
    String[] title;
    String imgASelect, imgBSelect, imgA2Select, imgB2Select, imgMaskSelect, titleMask;
    ImagePlus imA, imA2, imB, imB2, colocPlus, imMask;
    //ImagePlus imA2, imB2, colocPlus;
    ImageStack isA, isB, isA2, isB2;
    ImageProcessor ipA, ipB, ipA2, ipB2;
    ImageHandler iHandA, iHandB, iHA, iHB;
    Objects3DPopulation objPopA, objPopB, currPopA, currPopB, touchPopA, touchPopB;
    Object3D mask;
    Calibration cali;
    
    //Preferences
    boolean allTouch1bool = Prefs.get("Diana_allTouch1.boolean", false);
    boolean colocFromAbool = Prefs.get("Diana_colocFromA1.boolean", true);
    boolean colocFromBbool = Prefs.get("Diana_colocFromB1.boolean", true);
    boolean colocFromABbool = Prefs.get("Diana_colocFromAB1.boolean", true);
    boolean colocCCbool = Prefs.get("Diana_colocCC1.boolean", true);
    boolean colocECbool = Prefs.get("Diana_colocEC1.boolean", true);
    boolean colocCEbool = Prefs.get("Diana_colocCE1.boolean", true);
    boolean surfcontactbool = Prefs.get("Diana_contactSurface.boolean", false);
    boolean contactMap1bool = Prefs.get("Diana_contactMap1.boolean", true);
    boolean proxyCCbool = Prefs.get("Diana_proxyCC1.boolean", true);
    boolean proxyEEbool = Prefs.get("Diana_proxyEE1.boolean", true);
    boolean proxyECbool = Prefs.get("Diana_proxyEC1.boolean", true);
    boolean proxyCEbool = Prefs.get("Diana_proxyCE1.boolean", true);
    boolean surfcontactboolAdj = Prefs.get("Diana_contactSurface.boolean", false);
    boolean volumebool = Prefs.get("Diana_volume1.boolean", true);
    boolean meanbool = Prefs.get("Diana_mean1.boolean", true);
    boolean surfacebool = Prefs.get("Diana_surface1.boolean", false);
    boolean stdDevbool = Prefs.get("Diana_stdDev1.boolean", true);
    boolean minMaxbool = Prefs.get("Diana_minMax1.boolean", true);
    boolean centrobool = Prefs.get("Diana_centro1bool.boolean", true);
    boolean massbool = Prefs.get("Diana_mass1.boolean", true);
    boolean feretbool = Prefs.get("Diana_feret1.boolean", true);
    boolean intDenbool = Prefs.get("Diana_intDen1.boolean", true);
    
    //macro
    private String imgTitle1, imgTitle2, labTitle1, labTitle2, maskTitle;
    boolean colocBool=false, adjaBool=false, shuffleBool=false, maskbool=false, measureBool=false;
    int numClo;
    double distC, distA;
    
    //rois
    //private int[] IDList= WindowManager.getIDList();
    public DefaultListModel modeAcurr = new DefaultListModel();
    public DefaultListModel modeBcurr = new DefaultListModel();
    public DefaultListModel modeAtouch = new DefaultListModel();
    public DefaultListModel modeBtouch = new DefaultListModel();
    private Roi[] arrayRoisA, arrayRoisB, arrayB, arrayA, arrayRois;
    
    
    
    /**
     * Creates new form DiAna_Analyse
     */
    public DiAna_Analyse() {
        initComponents();
        init=false;
        
        //Init combobox
        title=Manager.testImageSizes(4, true);
        imgA.setModel(new DefaultComboBoxModel(title));
        imgB.setModel(new DefaultComboBoxModel(title));
        imgA2.setModel(new DefaultComboBoxModel(title));
        imgB2.setModel(new DefaultComboBoxModel(title));
        if(Manager.plus1!=null){
            imgA.setSelectedIndex(Manager.setImageList(Manager.plus1, title));
            imA=Manager.plus1;
        }
        else{imgA.setSelectedIndex(0);}
        imgA.updateUI();
        if(Manager.plus2!=null){
            imgB.setSelectedIndex(Manager.setImageList(Manager.plus2, title));
            imB=Manager.plus2;
        }
        else{imgB.setSelectedIndex(1);}
        imgB.updateUI();

        if(Manager.plus1seg!=null){
            imgA2.setSelectedIndex(Manager.setImageList(Manager.plus1seg, title));
            imA2=Manager.plus1seg;
        }
        else{
            if(WindowManager.getWindowCount()<4){imgA2.setSelectedIndex(0);}
            else{imgA2.setSelectedIndex(2);}
        }
        imgA2.updateUI();
        if(Manager.plus2seg!=null){
            imgB2.setSelectedIndex(Manager.setImageList(Manager.plus2seg, title));  
            imB2=Manager.plus2seg;
        }
        else{
            if(WindowManager.getWindowCount()<4){imgB2.setSelectedIndex(1);}
            else{imgB2.setSelectedIndex(3);}
        }
        imgB2.updateUI();
        
    }
    
    /**
     *  Execute the macro operations
     * @param img1 raw image 1
     * @param img2 raw image 2
     * @param lab1 labelled image 1
     * @param lab2  labelled image 2
     */
    public void macroBatchRunner(ImagePlus img1, ImagePlus img2, ImagePlus lab1, ImagePlus lab2, String maskTitle){
        //IJ.log("img1:"+img1.getTitle()+" img2:"+img2.getTitle()+" lab1:"+lab1.getTitle()+" lab2:"+lab2.getTitle());
        iHandA=ImageHandler.wrap(img1);iHandB=ImageHandler.wrap(img2);//raw
        iHA=ImageHandler.wrap(lab1); iHB=ImageHandler.wrap(lab2);//label
        ImageInt imInt1=ImageInt.wrap(lab1);
        Objects3DPopulation popA= new Objects3DPopulation(imInt1);//PB curr vs objpop
        ImageInt imInt2=ImageInt.wrap(lab2);
        Objects3DPopulation popB= new Objects3DPopulation(imInt2);//PB curr vs objpop
        Measures meas= new Measures(iHA, iHB, popA, popB);
        if(colocBool){
            meas.computeColoc(true, true, true, true, true, true, surfcontactbool, distC);
            ImageHandler ColocHandler = meas.getImageColoc();
            ColocHandler.setMinAndMax((float)0, (float)ColocHandler.getMax());

            //Calibration cal = img1.getCalibration();//calibration
            ColocHandler.setCalibration(cali);
            ColocHandler.set332RGBLut();
            ColocHandler.show();
        }
        if(adjaBool){
            meas.ComputeAdjacency(numClo, true, true, true, true, surfcontactboolAdj, distA);
        }
        if(measureBool){
            ResultsTable resultsA = Measures.measureResult(iHandA, popA, "ObjA-", true, true, true, true,true, true, true, true, true);
            ResultsTable resultsB = Measures.measureResult(iHandB, popB, "ObjB-", true, true, true, true,true, true, true, true, true);
            resultsA.show("ObjectsMeasuresResults-A");
            resultsB.show("ObjectsMeasuresResults-B");
        }
        if(shuffleBool){
            if(maskbool){
                ImagePlus imask = WindowManager.getImage(maskTitle);
                ImageStack maskStack=imask.getStack();
                mask = DiAna_Ana.getmask(maskStack, false);
                //ImageHandler ihMask=ImageHandler.wrap(imMask);
            }
            else {
                mask = DiAna_Ana.getmask(iHA.getImageStack(), true);
            }
            Measures.computeShuffle(mask, popA, popB);
        }
    }
    
    
    /**
     * Perform the commands in the macro
     * @param arg command
     */
    public void macroInterpreter(String arg){
        int start, end;
        colocBool=arg.contains("coloc");
        surfcontactbool=arg.contains("distc");
        adjaBool=arg.contains("adja");
        surfcontactboolAdj=arg.contains("dista");
        shuffleBool=arg.contains("shuffle");
        maskbool=arg.contains("mask=");
        measureBool=arg.contains("measure");

        start=arg.indexOf("img1=")+5;
        end=arg.indexOf(" ", start);
        if ((arg.charAt(start)+"").equals("[")){
            start++;
            end=arg.indexOf("]", start);
        }
        imgTitle1=arg.substring(start, end);
        ImagePlus im1=WindowManager.getImage(imgTitle1);

        start=arg.indexOf("img2=")+5;
        end=arg.indexOf(" ", start);
        if ((arg.charAt(start)+"").equals("[")){
            start++;
            end=arg.indexOf("]", start);
        }
        imgTitle2=arg.substring(start, end);
        ImagePlus im2=WindowManager.getImage(imgTitle2);

        start=arg.indexOf("lab1=")+5;
        end=arg.indexOf(" ", start);
        if ((arg.charAt(start)+"").equals("[")){
            start++;
            end=arg.indexOf("]", start);
        }
        labTitle1=arg.substring(start, end);
        ImagePlus im3=WindowManager.getImage(labTitle1);

        start=arg.indexOf("lab2=")+5;
        end=arg.indexOf(" ", start);
        if ((arg.charAt(start)+"").equals("[")){
            start++;
            end=arg.indexOf("]", start);
        }
        labTitle2=arg.substring(start, end);
        ImagePlus im4=WindowManager.getImage(labTitle2);

        if (im1==null || im2==null || im3==null || im4==null){
            IJ.error("DiAna error, within a macro", "Image not found while running DiAna from a macro\n1-Use \"open(path)\" in your macro to open images\n2-Make sure you have called the right images !");
            return;
        }

        if(colocBool){
              //coloc
              if(surfcontactbool){
                  start=arg.indexOf("distc=")+6;
                  end=arg.indexOf(" ",start);
                  distC=Double.parseDouble(arg.substring(start, end));
              }
        }
        if(adjaBool){
              //adjacency
              start=arg.indexOf("kclosest=")+9;
              end=arg.indexOf(" ",start);
              numClo=Integer.parseInt(arg.substring(start, end));
            if(surfcontactboolAdj){
                start=arg.indexOf("dista=")+6;
                end=arg.indexOf(" ",start);
                distA=Double.parseDouble(arg.substring(start, end));
            }
        }

        //if(measureBool){
        //}
        if(shuffleBool){
            //shuffle by default
            if(maskbool){
                start=arg.indexOf("mask=")+5;
                end=arg.length()-1;
                if ((arg.charAt(start)+"").equals("[")){
                    start++;
                    end=arg.indexOf("]", start);
                }
                maskTitle=arg.substring(start, end);
            }
        }
        macroBatchRunner(im1, im2, im3, im4, maskTitle);
    }
    
    
/**
 * Generate the macro command in the recorder
 * @param title1 image1
 * @param title2 image2
 * @param title3 labelled 1
 * @param title4 labelled 2
 */
    public void macroGenerator(final String title1, final String title2, final String title3, final String title4, final String titlemask){
        
        Recorder.setCommand("DiAna_Analyse");
        Recorder.recordOption("img1", title1);
        Recorder.recordOption("img2", title2);
        Recorder.recordOption("lab1", title3);
        Recorder.recordOption("lab2", title4);
        //String command="macro commandLine: "+"img1="+title1+" img2="+title2+" lab1="+title3+" lab2="+title4;
        if(colocBool==true){
              Recorder.recordOption("coloc");
            //command=command+" coloc";
            if(surfcontactbool){
                Recorder.recordOption("distc", ""+distC);
                //command=command+" distc="+distC;
            }
        }
        if(adjaBool==true){
          Recorder.recordOption("adja");
          Recorder.recordOption("kclosest", ""+numClo);
          //command=command+" adja "+"kclosest="+numClo;
            //IJ.log("macro commandLine: "+"img1="+title1+" img2="+title2+" lab1="+title3+" lab2="+title4+" adja "+"kclosest"+numClo+" dista="+distA);
            if(surfcontactboolAdj){
                Recorder.recordOption("dista", ""+distA);
                //command=command+" dista="+distA;
            }
        }
        if(measureBool==true){
            Recorder.recordOption("measure");
            //command=command+" measure";
            //IJ.log("macro commandLine: "+"img1="+title1+" img2="+title2+" lab1="+title3+" lab2="+title4+" measure");
        }
        if(shuffleBool==true){
            Recorder.recordOption("shuffle");
            //command=command+" shuffle";
            if(maskbool){
                Recorder.recordOption("mask", titlemask);
                //command=command+" mask="+titlemask;
            }
            //IJ.log("macro commandLine: "+"img1="+title1+" img2="+title2+" lab1="+title3+" lab2="+title4+" shuffle");
        }
//        IJ.log(command);
        Recorder.saveCommand();
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        RoiFrame = new javax.swing.JFrame();
        labelImA = new javax.swing.JLabel();
        labelImB = new javax.swing.JLabel();
        scrollPaneA = new javax.swing.JScrollPane();
        jListA = new javax.swing.JList();
        scrollPaneB = new javax.swing.JScrollPane();
        jListB = new javax.swing.JList();
        nbObA = new javax.swing.JLabel();
        nbObB = new javax.swing.JLabel();
        changePop = new javax.swing.JComboBox();
        popLabel = new javax.swing.JLabel();
        totalLabA = new javax.swing.JLabel();
        mergeObjectsA = new javax.swing.JButton();
        deleteObjectsA = new javax.swing.JButton();
        mergeObjectsB = new javax.swing.JButton();
        deleteObjectsB = new javax.swing.JButton();
        totalLabA1 = new javax.swing.JLabel();
        savePopA = new javax.swing.JButton();
        savePopB = new javax.swing.JButton();
        microGroup1 = new javax.swing.ButtonGroup();
        microGroup2 = new javax.swing.ButtonGroup();
        boundingGroup1 = new javax.swing.ButtonGroup();
        imageToAnaLab = new javax.swing.JLabel();
        imageALab = new javax.swing.JLabel();
        imageBLab = new javax.swing.JLabel();
        imgA = new javax.swing.JComboBox();
        imgB = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        allAllPanel = new javax.swing.JPanel();
        imageALab1 = new javax.swing.JLabel();
        imgA2 = new javax.swing.JComboBox();
        imageBLab1 = new javax.swing.JLabel();
        imgB2 = new javax.swing.JComboBox();
        about = new javax.swing.JButton();
        interacLab1 = new javax.swing.JLabel();
        allTouch1 = new javax.swing.JCheckBox("All objects touching", allTouch1bool);
        initial = new javax.swing.JButton();
        measAnaLab1 = new javax.swing.JLabel();
        MeasuresTabbedPanel = new javax.swing.JTabbedPane();
        colocPanel = new javax.swing.JPanel();
        tablColocLabA = new javax.swing.JLabel();
        colocFromA = new javax.swing.JCheckBox("% from objects in A", colocFromAbool);
        colocFromB = new javax.swing.JCheckBox("% from objects in B", colocFromBbool);
        colocFromAB = new javax.swing.JCheckBox("% from objects A+B", colocFromABbool);
        disColocLabA = new javax.swing.JLabel();
        colocCC = new javax.swing.JCheckBox("Center-Center", colocCCbool);
        colocCE = new javax.swing.JCheckBox("Center-Edge", colocCEbool);
        colocEC = new javax.swing.JCheckBox("Edge-Center", colocECbool);
        surfcoloc = new javax.swing.JCheckBox("Surface in contact", surfcontactbool);
        surfMax = new javax.swing.JFormattedTextField();
        distMaxSurface = new javax.swing.JLabel();
        viewColocLabA = new javax.swing.JLabel();
        contactMap = new javax.swing.JCheckBox("Contact", contactMap1bool);
        analyseColoc = new javax.swing.JButton();
        proxyPanel1 = new javax.swing.JPanel();
        closestLabel1 = new javax.swing.JLabel();
        closestField1 = new javax.swing.JFormattedTextField();
        closestLabel2 = new javax.swing.JLabel();
        disProxyLabA1 = new javax.swing.JLabel();
        proxyCC = new javax.swing.JCheckBox("Center-Center", proxyCCbool);
        proxyEE = new javax.swing.JCheckBox("Edge-Edge", proxyEEbool);
        proxyCE = new javax.swing.JCheckBox("Center-Edge", proxyCEbool);
        proxyEC = new javax.swing.JCheckBox("Edge-Center", proxyECbool);
        surfproxy1 = new javax.swing.JCheckBox("Surface in contact", surfcontactbool);
        surfMax2 = new javax.swing.JFormattedTextField();
        distMaxSurface1 = new javax.swing.JLabel();
        analyseAdjacency = new javax.swing.JButton();
        shufflePanel = new javax.swing.JPanel();
        labelShuffle = new javax.swing.JLabel();
        shuffleLab1 = new javax.swing.JLabel();
        shufflebounds1 = new javax.swing.JRadioButton();
        shufflebounds2 = new javax.swing.JRadioButton();
        imageBoundRefLab = new javax.swing.JLabel();
        imgBoundRef1 = new javax.swing.JComboBox();
        analyseShuffle = new javax.swing.JButton();
        measurePanel1 = new javax.swing.JPanel();
        volume = new javax.swing.JCheckBox("Volume", volumebool);
        mean = new javax.swing.JCheckBox("Mean", meanbool);
        surface = new javax.swing.JCheckBox("Surface area", surfacebool);
        stdDev = new javax.swing.JCheckBox("Standard deviation", stdDevbool);
        minMax = new javax.swing.JCheckBox("Min & Max", minMaxbool);
        centro = new javax.swing.JCheckBox("Centroid", centrobool);
        mass = new javax.swing.JCheckBox("Center of mass", massbool);
        feret = new javax.swing.JCheckBox("Feret's diameter", feretbool);
        analyseMeasures = new javax.swing.JButton();
        intDen = new javax.swing.JCheckBox("Feret's diameter", feretbool);

        RoiFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        RoiFrame.setTitle("ROI Selections & Localisation");
        RoiFrame.setMinimumSize(new java.awt.Dimension(350, 380));
        RoiFrame.setName("RoiFrame1"); // NOI18N

        labelImA.setText("Rois Image A");

        labelImB.setText("Rois Image B");

        jListA.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "I6", "I7", "I8" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListA.setToolTipText("Ctrl+click for multiple selections");
        jListA.setMaximumSize(new java.awt.Dimension(44, 100));
        jListA.setMinimumSize(new java.awt.Dimension(44, 100));
        jListA.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jListAMouseReleased(evt);
            }
        });
        scrollPaneA.setViewportView(jListA);

        jListB.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListB.setToolTipText("Ctrl+click for multiple selections");
        jListB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jListBMouseReleased(evt);
            }
        });
        scrollPaneB.setViewportView(jListB);

        nbObA.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        nbObA.setText("2000");

        nbObB.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        nbObB.setText("2000");

        changePop.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        changePop.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Current", "Touching" }));
        changePop.setPreferredSize(new java.awt.Dimension(93, 21));
        changePop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePopActionPerformed(evt);
            }
        });

        popLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        popLabel.setText("population");

        totalLabA.setText("Total :");

        mergeObjectsA.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        mergeObjectsA.setText("Merge");
        mergeObjectsA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeObjectsAActionPerformed(evt);
            }
        });

        deleteObjectsA.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        deleteObjectsA.setText("Delete");
        deleteObjectsA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteObjectsAActionPerformed(evt);
            }
        });

        mergeObjectsB.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        mergeObjectsB.setText("Merge");
        mergeObjectsB.setPreferredSize(new java.awt.Dimension(65, 25));
        mergeObjectsB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeObjectsBActionPerformed(evt);
            }
        });

        deleteObjectsB.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        deleteObjectsB.setText("Delete");
        deleteObjectsB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteObjectsBActionPerformed(evt);
            }
        });

        totalLabA1.setText("Total :");

        savePopA.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        savePopA.setText("Save");
        savePopA.setToolTipText("select a file where ROIs will be saved");
        savePopA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePopAActionPerformed(evt);
            }
        });

        savePopB.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        savePopB.setText("Save");
        savePopB.setToolTipText("Selected a file where the ROIs will be saved");
        savePopB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePopBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RoiFrameLayout = new javax.swing.GroupLayout(RoiFrame.getContentPane());
        RoiFrame.getContentPane().setLayout(RoiFrameLayout);
        RoiFrameLayout.setHorizontalGroup(
            RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RoiFrameLayout.createSequentialGroup()
                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(RoiFrameLayout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(changePop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(popLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(RoiFrameLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RoiFrameLayout.createSequentialGroup()
                                .addComponent(mergeObjectsB, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteObjectsB, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RoiFrameLayout.createSequentialGroup()
                                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(scrollPaneB, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(RoiFrameLayout.createSequentialGroup()
                                        .addComponent(totalLabA1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(nbObB)))
                                .addGap(20, 20, 20))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RoiFrameLayout.createSequentialGroup()
                                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(savePopA, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(RoiFrameLayout.createSequentialGroup()
                                        .addComponent(mergeObjectsA, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(deleteObjectsA, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(RoiFrameLayout.createSequentialGroup()
                                        .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(scrollPaneA, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(RoiFrameLayout.createSequentialGroup()
                                                    .addComponent(totalLabA)
                                                    .addGap(24, 24, 24)
                                                    .addComponent(nbObA))
                                                .addComponent(labelImA)))
                                        .addGap(17, 17, 17)))
                                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(RoiFrameLayout.createSequentialGroup()
                                        .addGap(49, 49, 49)
                                        .addComponent(labelImB))
                                    .addGroup(RoiFrameLayout.createSequentialGroup()
                                        .addGap(24, 24, 24)
                                        .addComponent(savePopB, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(31, 31, 31)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        RoiFrameLayout.setVerticalGroup(
            RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RoiFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelImA)
                    .addComponent(labelImB))
                .addGap(12, 12, 12)
                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(changePop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(popLabel))
                .addGap(18, 18, 18)
                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RoiFrameLayout.createSequentialGroup()
                        .addComponent(scrollPaneA, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                        .addGap(6, 6, 6))
                    .addGroup(RoiFrameLayout.createSequentialGroup()
                        .addComponent(scrollPaneB, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalLabA)
                    .addComponent(nbObA)
                    .addComponent(nbObB)
                    .addComponent(totalLabA1))
                .addGap(9, 9, 9)
                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mergeObjectsA)
                    .addComponent(deleteObjectsA)
                    .addComponent(mergeObjectsB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteObjectsB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RoiFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(savePopA)
                    .addComponent(savePopB))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DiAna (Distance Analysis)");

        imageToAnaLab.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        imageToAnaLab.setText("Images to analyse:");

        imageALab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imageALab.setText("Image A :");

        imageBLab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imageBLab.setText("Image B :");

        imgA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imgA.setToolTipText("Select the first image");
        imgA.setPreferredSize(new java.awt.Dimension(200, 18));
        //imgA.setModel(new javax.swing.DefaultComboBoxModel(title));
        imgA.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imgAMouseClicked(evt);
            }
        });
        imgA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgAActionPerformed(evt);
            }
        });

        imgB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imgB.setToolTipText("Select the second image");
        imgB.setPreferredSize(new java.awt.Dimension(200, 18));
        imgB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imgBMouseClicked(evt);
            }
        });
        imgB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgBActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/allAllico.gif")));
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/oneAllico.gif"))); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel2.setPreferredSize(new java.awt.Dimension(65, 65));

        allAllPanel.setBackground(new java.awt.Color(230, 230, 230));
        allAllPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        allAllPanel.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        allAllPanel.setPreferredSize(new java.awt.Dimension(490, 585));

        imageALab1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imageALab1.setText("Labelled image A :");

        imgA2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imgA2.setToolTipText("Select the labelled image corresponding to image A");
        imgA2.setPreferredSize(new java.awt.Dimension(200, 18));
        //imgA.setModel(new javax.swing.DefaultComboBoxModel(title));
        imgA2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imgA2MouseClicked(evt);
            }
        });
        imgA2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgA2ActionPerformed(evt);
            }
        });

        imageBLab1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imageBLab1.setText("Labelled image B :");

        imgB2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imgB2.setToolTipText("Select the labelled image corresponding to image B");
        imgB2.setPreferredSize(new java.awt.Dimension(200, 18));
        imgB2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imgB2MouseClicked(evt);
            }
        });
        imgB2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgB2ActionPerformed(evt);
            }
        });

        about.setText("About");
        about.setToolTipText("Link to the article");
        about.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                aboutMouseReleased(evt);
            }
        });

        interacLab1.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        interacLab1.setText("Interaction Filter");

        allTouch1.setBackground(new java.awt.Color(230, 230, 230));
        allTouch1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        allTouch1.setText("Select only all objects touching");
        allTouch1.setToolTipText("Select only the objects which are touching each other between the two images");

        initial.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        initial.setText("Initialize");
        initial.setToolTipText("<html>Initialize the images for detecting objects <br/>Re-initialize the object population if you don't want \"only all objects touching\"</html>\n");
        initial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initialActionPerformed(evt);
            }
        });

        measAnaLab1.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        measAnaLab1.setText("Measures & Analyse");

        MeasuresTabbedPanel.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        MeasuresTabbedPanel.setPreferredSize(new java.awt.Dimension(458, 191));

        colocPanel.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        colocPanel.setPreferredSize(new java.awt.Dimension(458, 162));

        tablColocLabA.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        tablColocLabA.setText("Table");

        colocFromA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        colocFromA.setText("% from objects in A");
        colocFromA.setToolTipText("Measure the percentage of the colocalisation part with the object in A");

        colocFromB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        colocFromB.setText("% from objects in B");
        colocFromB.setToolTipText("Measure the percentage of the colocalisation part with the object in B");

        colocFromAB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        colocFromAB.setText("% from objects A+B");
        colocFromAB.setToolTipText("Measure the percentage of the colocalisation part with the objects in A and B.");

        disColocLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        disColocLabA.setText("Distance from objects in A :");

        colocCC.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        colocCC.setText("Center-Center");
        colocCC.setToolTipText("Measure the distance between the center of the object in A and the object in B");

        colocCE.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        colocCE.setText("Center-Edge");
        colocCE.setToolTipText("Measure the minimum distance between the center of the object in A and the edge of the object in B");

        colocEC.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        colocEC.setText("Edge-Center");
        colocEC.setToolTipText("Measure the minimum distance between the edge of the object in A and the center of the object in B");

        surfcoloc.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        surfcoloc.setText("Surface in contact");
        surfcoloc.setToolTipText("<html>Measure the number of pixels at the edges of the object which are in contact <br/>It can be very slow to compute</html>");

        surfMax.setText("50");
        surfMax.setToolTipText("Distance for the closest objects in B for each object in image A");
        surfMax.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        surfMax.setMinimumSize(new java.awt.Dimension(10, 20));
        surfMax.setPreferredSize(new java.awt.Dimension(24, 20));

        distMaxSurface.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        distMaxSurface.setText("distance max (nm) *slow computation*");
        distMaxSurface.setToolTipText("<html>Measure the number of pixels at the edges of the object which are in contact <br/>It can be very slow to compute</html>");

        viewColocLabA.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        viewColocLabA.setText("View");

        contactMap.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        contactMap.setText("ROI Manager");
        contactMap.setToolTipText("Show the image with only the contact between the objects");

        analyseColoc.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        analyseColoc.setText("Analyse");
        analyseColoc.setToolTipText("Perform the colocalisation measures");
        analyseColoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyseColocActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout colocPanelLayout = new javax.swing.GroupLayout(colocPanel);
        colocPanel.setLayout(colocPanelLayout);
        colocPanelLayout.setHorizontalGroup(
            colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colocPanelLayout.createSequentialGroup()
                        .addComponent(tablColocLabA)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colocPanelLayout.createSequentialGroup()
                        .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(colocPanelLayout.createSequentialGroup()
                                .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(colocPanelLayout.createSequentialGroup()
                                            .addGap(13, 13, 13)
                                            .addComponent(disColocLabA))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, colocPanelLayout.createSequentialGroup()
                                            .addComponent(viewColocLabA)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(contactMap)))
                                    .addGroup(colocPanelLayout.createSequentialGroup()
                                        .addComponent(surfcoloc)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(surfMax, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(12, 12, 12)
                                .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(distMaxSurface)
                                    .addGroup(colocPanelLayout.createSequentialGroup()
                                        .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(colocCC)
                                            .addComponent(colocCE))
                                        .addGap(16, 16, 16)
                                        .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(colocFromAB)
                                            .addComponent(colocEC)))))
                            .addGroup(colocPanelLayout.createSequentialGroup()
                                .addComponent(colocFromA)
                                .addGap(18, 18, 18)
                                .addComponent(colocFromB)))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(colocPanelLayout.createSequentialGroup()
                .addGap(186, 186, 186)
                .addComponent(analyseColoc, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        colocPanelLayout.setVerticalGroup(
            colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, colocPanelLayout.createSequentialGroup()
                .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colocPanelLayout.createSequentialGroup()
                        .addComponent(tablColocLabA)
                        .addGap(0, 0, 0)
                        .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(colocFromA)
                            .addComponent(colocFromB))
                        .addComponent(disColocLabA))
                    .addGroup(colocPanelLayout.createSequentialGroup()
                        .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(colocPanelLayout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(colocCC)
                                .addGap(0, 0, 0)
                                .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(colocCE)
                                    .addComponent(colocEC))
                                .addGap(4, 4, 4))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, colocPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(colocFromAB)
                                .addGap(48, 48, 48)))
                        .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(surfcoloc)
                            .addComponent(surfMax, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(distMaxSurface))
                        .addGap(0, 0, 0)
                        .addGroup(colocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(viewColocLabA)
                            .addComponent(contactMap))))
                .addGap(0, 0, 0)
                .addComponent(analyseColoc)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        MeasuresTabbedPanel.addTab("Colocalisation", colocPanel);

        proxyPanel1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        proxyPanel1.setPreferredSize(new java.awt.Dimension(452, 162));

        closestLabel1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        closestLabel1.setText("Analyse the");
        closestLabel1.setToolTipText("Measure distance for the N closest objects in B for each object in image A");

        closestField1.setText("1");
        closestField1.setToolTipText("Measure distances for the N closest objects in B for each object in image A");
        closestField1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        closestField1.setMinimumSize(new java.awt.Dimension(10, 20));
        closestField1.setPreferredSize(new java.awt.Dimension(24, 20));

        closestLabel2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        closestLabel2.setText("Closest object(s)");
        closestLabel2.setToolTipText("Measure distance for the N closest objects in B for each object in image A (center-center)");

        disProxyLabA1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        disProxyLabA1.setText("Distance from objects in A :");

        proxyCC.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        proxyCC.setText("Center-Center");
        proxyCC.setToolTipText("Measure the distance between the center of the object in A and the center of the object in B");

        proxyEE.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        proxyEE.setText("Edge-Edge");
        proxyEE.setToolTipText("Measure the shortest distance between the edge of the object in A and the edge of the object in B");

        proxyCE.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        proxyCE.setText("Center-Edge");
        proxyCE.setToolTipText("Measure the shortest distance between the center of the object in A and the edge of the object in B");

        proxyEC.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        proxyEC.setText("Edge-Center");
        proxyEC.setToolTipText("Measure the shortest distance between the edge of the object in A and the center of the object in B");

        surfproxy1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        surfproxy1.setText("Surface in contact:");
        surfproxy1.setToolTipText("<html>Measure the number of pixels at the edges of the object which are in contact <br/>It can be very slow to compute</html>");

        surfMax2.setText("50");
        surfMax2.setToolTipText("Distance for the closest objects in B for each object in image A");
        surfMax2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        surfMax2.setMinimumSize(new java.awt.Dimension(10, 20));
        surfMax2.setPreferredSize(new java.awt.Dimension(24, 20));

        distMaxSurface1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        distMaxSurface1.setText("distance max (nm) *slow computation*");
        distMaxSurface1.setToolTipText("<html>Measure the number of pixels at the edges of the object which are in contact <br/>It can be very slow to compute</html>");

        analyseAdjacency.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        analyseAdjacency.setText("Analyse");
        analyseAdjacency.setToolTipText("Perform the distance measures");
        analyseAdjacency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyseAdjacencyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout proxyPanel1Layout = new javax.swing.GroupLayout(proxyPanel1);
        proxyPanel1.setLayout(proxyPanel1Layout);
        proxyPanel1Layout.setHorizontalGroup(
            proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proxyPanel1Layout.createSequentialGroup()
                .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proxyPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(disProxyLabA1))
                    .addGroup(proxyPanel1Layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(proxyPanel1Layout.createSequentialGroup()
                                .addComponent(surfproxy1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(surfMax2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(distMaxSurface1))
                            .addGroup(proxyPanel1Layout.createSequentialGroup()
                                .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(proxyCC)
                                    .addComponent(proxyCE))
                                .addGap(50, 50, 50)
                                .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(proxyEC)
                                    .addComponent(proxyEE)))))
                    .addGroup(proxyPanel1Layout.createSequentialGroup()
                        .addGap(186, 186, 186)
                        .addComponent(analyseAdjacency, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(proxyPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(closestLabel1)
                        .addGap(4, 4, 4)
                        .addComponent(closestField1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(closestLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        proxyPanel1Layout.setVerticalGroup(
            proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proxyPanel1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closestLabel2)
                    .addComponent(closestField1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closestLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(disProxyLabA1)
                .addGap(0, 0, 0)
                .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyEE)
                    .addComponent(proxyCC))
                .addGap(0, 0, 0)
                .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyEC)
                    .addComponent(proxyCE))
                .addGap(2, 2, 2)
                .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proxyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(surfproxy1)
                        .addComponent(distMaxSurface1))
                    .addGroup(proxyPanel1Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(surfMax2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(analyseAdjacency)
                .addContainerGap())
        );

        MeasuresTabbedPanel.addTab("Distance", proxyPanel1);

        labelShuffle.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        labelShuffle.setText("100 times");

        shuffleLab1.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        shuffleLab1.setText("Bounding box (mask) :");

        boundingGroup1.add(shufflebounds1);
        shufflebounds1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        shufflebounds1.setSelected(true);
        shufflebounds1.setText("Total image");
        shufflebounds1.setToolTipText("Bounding box of the shuffle");

        boundingGroup1.add(shufflebounds2);
        shufflebounds2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        shufflebounds2.setText("Object from another image");
        shufflebounds2.setToolTipText("Bounding box of the shuffle");
        shufflebounds2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shufflebounds2ActionPerformed(evt);
            }
        });

        imageBoundRefLab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imageBoundRefLab.setText("Segmented Image ref :");

        imgBoundRef1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imgBoundRef1.setToolTipText("Select the reference (mask)");
        imgBoundRef1.setPreferredSize(new java.awt.Dimension(200, 18));
        imgBoundRef1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgBoundRef1ActionPerformed(evt);
            }
        });

        analyseShuffle.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        analyseShuffle.setText("Analyse");
        analyseShuffle.setToolTipText("\n<html>Perform the shuffle<br/>Note: Shuffle is made on the original objects populations</html>");
        analyseShuffle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyseShuffleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shufflePanelLayout = new javax.swing.GroupLayout(shufflePanel);
        shufflePanel.setLayout(shufflePanelLayout);
        shufflePanelLayout.setHorizontalGroup(
            shufflePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shufflePanelLayout.createSequentialGroup()
                .addGroup(shufflePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shufflePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(shufflePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(shuffleLab1)
                            .addComponent(shufflebounds1))
                        .addGap(109, 109, 109)
                        .addComponent(shufflebounds2))
                    .addGroup(shufflePanelLayout.createSequentialGroup()
                        .addGap(112, 112, 112)
                        .addComponent(imageBoundRefLab)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imgBoundRef1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(shufflePanelLayout.createSequentialGroup()
                        .addGap(186, 186, 186)
                        .addComponent(analyseShuffle, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(shufflePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelShuffle)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        shufflePanelLayout.setVerticalGroup(
            shufflePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shufflePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelShuffle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(shuffleLab1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shufflePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shufflebounds1)
                    .addComponent(shufflebounds2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shufflePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(imgBoundRef1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(imageBoundRefLab))
                .addGap(18, 18, 18)
                .addComponent(analyseShuffle)
                .addContainerGap())
        );

        MeasuresTabbedPanel.addTab("Shuffle", shufflePanel);

        measurePanel1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        measurePanel1.setPreferredSize(new java.awt.Dimension(458, 123));

        volume.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volume.setText("Volume");
        volume.setToolTipText("Measure the Volume of the object.");

        mean.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        mean.setText("Mean");
        mean.setToolTipText("Measure the mean gray values in the object.");

        surface.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        surface.setText("Surface area");
        surface.setToolTipText("Indicate the coordinates of the center of the object");

        stdDev.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        stdDev.setText("Standard deviation");
        stdDev.setToolTipText("Measure the standard deviation of the gray values of the object.");

        minMax.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minMax.setText("Min & Max");
        minMax.setToolTipText("Indicate the minimum and the maximum gray value in the object.");

        centro.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        centro.setText("Centroid");
        centro.setToolTipText("Indicate the coordinates of the center of the object");

        mass.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        mass.setText("Center of mass");
        mass.setToolTipText("Indicate the coordinates of the center of mass of the object (using the gray values)");

        feret.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        feret.setText("Feret's diameter");
        feret.setToolTipText("Measure the Feret's diameter of the object");

        analyseMeasures.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        analyseMeasures.setText("Analyse");
        analyseMeasures.setToolTipText("Perform the selected measures on the two images");
        analyseMeasures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyseMeasuresActionPerformed(evt);
            }
        });

        intDen.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        intDen.setText("Integrated Density");
        intDen.setToolTipText("Measure the Feret's diameter of the object");

        javax.swing.GroupLayout measurePanel1Layout = new javax.swing.GroupLayout(measurePanel1);
        measurePanel1.setLayout(measurePanel1Layout);
        measurePanel1Layout.setHorizontalGroup(
            measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(measurePanel1Layout.createSequentialGroup()
                .addGroup(measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(measurePanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(volume)
                            .addComponent(mass)
                            .addComponent(stdDev))
                        .addGap(35, 35, 35)
                        .addGroup(measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(measurePanel1Layout.createSequentialGroup()
                                .addComponent(feret)
                                .addGap(16, 16, 16)
                                .addComponent(intDen))
                            .addGroup(measurePanel1Layout.createSequentialGroup()
                                .addGroup(measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mean)
                                    .addComponent(minMax))
                                .addGap(50, 50, 50)
                                .addGroup(measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(centro)
                                    .addComponent(surface)))))
                    .addGroup(measurePanel1Layout.createSequentialGroup()
                        .addGap(186, 186, 186)
                        .addComponent(analyseMeasures, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        measurePanel1Layout.setVerticalGroup(
            measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(measurePanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(volume)
                    .addComponent(mean)
                    .addComponent(surface))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minMax)
                    .addComponent(centro)
                    .addComponent(stdDev))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(measurePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(feret)
                    .addComponent(mass)
                    .addComponent(intDen))
                .addGap(36, 36, 36)
                .addComponent(analyseMeasures)
                .addContainerGap())
        );

        MeasuresTabbedPanel.addTab("Measures", measurePanel1);

        javax.swing.GroupLayout allAllPanelLayout = new javax.swing.GroupLayout(allAllPanel);
        allAllPanel.setLayout(allAllPanelLayout);
        allAllPanelLayout.setHorizontalGroup(
            allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(allAllPanelLayout.createSequentialGroup()
                .addGroup(allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(allAllPanelLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(allAllPanelLayout.createSequentialGroup()
                                .addGroup(allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(imageBLab1)
                                    .addComponent(imageALab1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(imgA2, 0, 250, Short.MAX_VALUE)
                                    .addComponent(imgB2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(about))
                            .addComponent(allTouch1)))
                    .addGroup(allAllPanelLayout.createSequentialGroup()
                        .addGap(180, 180, 180)
                        .addGroup(allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(interacLab1)
                            .addComponent(measAnaLab1)))
                    .addGroup(allAllPanelLayout.createSequentialGroup()
                        .addGap(193, 193, 193)
                        .addComponent(initial, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(allAllPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(MeasuresTabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        allAllPanelLayout.setVerticalGroup(
            allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, allAllPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(allAllPanelLayout.createSequentialGroup()
                        .addGroup(allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(imageALab1)
                            .addComponent(imgA2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(2, 2, 2)
                        .addGroup(allAllPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(imgB2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(imageBLab1)))
                    .addComponent(about))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(interacLab1)
                .addGap(2, 2, 2)
                .addComponent(allTouch1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(initial)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(measAnaLab1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(MeasuresTabbedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 202, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(allAllPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 506, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(imageALab)
                                    .addComponent(imageBLab))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(imgA, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(imgB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(imageToAnaLab))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(imageToAnaLab)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(imageALab)
                            .addComponent(imgA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(imgB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(imageBLab)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(allAllPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );

        imgB.getAccessibleContext().setAccessibleDescription("Select the mask image, shuffle will be performed in the bounding box");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void imgAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgAActionPerformed
        Manager.testImageSizes(1, false);
        imgASelect=(String)  imgA.getSelectedItem();
        if(title.length>0){
            WindowManager.getFrame(imgASelect).toFront();
            imA = WindowManager.getImage(imgASelect);
            int slice = WindowManager.getImage(imgASelect).getNSlices();
            WindowManager.getImage(imgASelect).setSlice(slice/2);
            cali= imA.getCalibration();
        }
        
        imA=WindowManager.getImage(imgASelect);
        imgA.updateUI();

    }//GEN-LAST:event_imgAActionPerformed

    private void imgBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgBActionPerformed
        Manager.testImageSizes(1, false);
        imgBSelect=(String)  imgB.getSelectedItem();
        imB=WindowManager.getImage(imgBSelect);
        
    }//GEN-LAST:event_imgBActionPerformed

    private void initialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialActionPerformed
        
        isA=imA.getImageStack();isA2=imA2.getImageStack();objPopA=Manager.pop1;
        isB=imB.getImageStack();isB2=imB2.getImageStack();objPopB=Manager.pop2;
        iHA = ImageHandler.wrap(isA);iHandA = ImageHandler.wrap(isA2);
        iHB = ImageHandler.wrap(isB);iHandB = ImageHandler.wrap(isB2);
        if(allTouch1.isSelected()){
            touchPopA = DiAna_Ana.touchingPop(objPopB, objPopA, imA2, true);
            DiAna_Ana.drawPop(touchPopA, iHandA);
            imA2.updateAndDraw();
            imA2.show();
            isA2 = imA2.getImageStack();
            touchPopB = DiAna_Ana.touchingPop(objPopA, objPopB, imB2, true);
            DiAna_Ana.drawPop(touchPopB, iHandB);
            imB2.updateAndDraw();
            imB2.show();
            isB2 = imB2.getImageStack();
            initial.setText("Initialized");
        }
        else{
            DiAna_Ana.drawPop(objPopA, iHandA);
            imA2.updateAndDraw();
            imA2.show();
            DiAna_Ana.drawPop(objPopB, iHandB);
            imB2.updateAndDraw();
            imB2.show();
            initial.setText("Initialized");
            if(init==true){ //reset the "all object touching"
                DiAna_Ana.drawPop(objPopA, iHandA);
                imA2.updateAndDraw();
                imA2.show();
                DiAna_Ana.drawPop(objPopB, iHandB);
                imB2.updateAndDraw();
                imB2.show();
                initial.setText("Re-initialized");
             }
        }
        
        currPopA = new Objects3DPopulation (ImageInt.wrap(imA2));
        currPopB = new Objects3DPopulation (ImageInt.wrap(imB2));
        
        init=true;

    }//GEN-LAST:event_initialActionPerformed

    private void jListAMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListAMouseReleased
        int zmin = imA2.getNSlices() + 1;
        int zmax = -1;
        // draw mask of rois
        int[] indexes = jListA.getSelectedIndices();
        arrayRois = new Roi[imA2.getNSlices()];
        
        // get zmin and zmax
        Object3D obj = null;
        
        for (int i = 0; i < indexes.length; i++) {
            if(changePop.getSelectedItem()=="Current"){
                obj = currPopA.getObject(indexes[i]);
                if (obj.getZmin() < zmin) {
                    zmin = obj.getZmin();
                }
                if (obj.getZmax() > zmax) {
                    zmax = obj.getZmax();
                }
            }
            if(changePop.getSelectedItem()=="Touching"){
                obj = touchPopA.getObject(indexes[i]);
                if (obj.getZmin() < zmin) {
                    zmin = obj.getZmin();
                }
                if (obj.getZmax() > zmax) {
                    zmax = obj.getZmax();
                }
            }
        }
        for (int zz = zmin; zz <= zmax; zz++) {
            IJ.showStatus("Computing Roi " + zz);
            ByteProcessor mask = new ByteProcessor(imA2.getWidth(), imA2.getHeight());
            for (int i = 0; i < indexes.length; i++) {
                if(changePop.getSelectedItem()=="Current"){
                    obj = currPopA.getObject(indexes[i]);
                    obj.draw(mask, zz, 255);
                }
                if(changePop.getSelectedItem()=="Touching"){
                    obj = touchPopA.getObject(indexes[i]);
                    obj.draw(mask, zz, 255);
                }
            }
            mask.setThreshold(1, 255, ImageProcessor.NO_LUT_UPDATE);
            ImagePlus maskPlus = new ImagePlus("mask " + zz, mask);
            ThresholdToSelection tts = new ThresholdToSelection();
            tts.setup("", maskPlus);
            tts.run(mask);
            
            arrayRois[zz] = maskPlus.getRoi();
        }
        int middle = (int) (0.5 * zmin + 0.5 * zmax);
        
        imA2.setSlice(middle + 1);
        imA2.setRoi(arrayRois[middle]);
        imA2.updateAndDraw();

        if(colocPlus!=null){
            colocPlus.setSlice(middle + 1);
            colocPlus.setRoi(arrayRois[middle]);
            colocPlus.updateAndDraw();
            colocPlus.getWindow().toFront();
            colocPlus.getWindow().toFront();
        }
        imA2.getWindow().toFront();
        
    }//GEN-LAST:event_jListAMouseReleased

    private void jListBMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListBMouseReleased
        int zmin = imB2.getNSlices() + 1;
        int zmax = -1;
        // draw mask of rois
        int[] indexes = jListB.getSelectedIndices();
        arrayRois = new Roi[imB2.getNSlices()];

        Object3D obj;
        for (int i = 0; i < indexes.length; i++) {
            if(changePop.getSelectedItem()=="Current"){
                obj = currPopB.getObject(indexes[i]);
                if (obj.getZmin() < zmin) {
                    zmin = obj.getZmin();
                }
                if (obj.getZmax() > zmax) {
                    zmax = obj.getZmax();
                }
            }
            if(changePop.getSelectedItem()=="Touching"){
                obj = touchPopB.getObject(indexes[i]);
                if (obj.getZmin() < zmin) {
                    zmin = obj.getZmin();
                }
                if (obj.getZmax() > zmax) {
                    zmax = obj.getZmax();
                }
            }
        }
        for (int zz = zmin; zz <= zmax; zz++) {
//            IJ.showStatus("Computing Roi " + zz);
            ByteProcessor mask = new ByteProcessor(imB2.getWidth(), imB2.getHeight());
            for (int i = 0; i < indexes.length; i++) {
                if(changePop.getSelectedItem()=="Current"){
                    obj = currPopB.getObject(indexes[i]);
                    obj.draw(mask, zz, 255);
                }
                if(changePop.getSelectedItem()=="Touching"){
                    obj = touchPopB.getObject(indexes[i]);
                    obj.draw(mask, zz, 255);
                }
            }
            mask.setThreshold(1, 255, ImageProcessor.NO_LUT_UPDATE);
            ImagePlus maskPlus = new ImagePlus("mask " + zz, mask);

            ThresholdToSelection tts = new ThresholdToSelection();
            tts.setup("", maskPlus);
            tts.run(mask);

            arrayRois[zz] = maskPlus.getRoi();
        }
        int middle = (int) (0.5 * zmin + 0.5 * zmax);
        imB2.setSlice(middle + 1);
        imB2.setRoi(arrayRois[middle]);

        imB2.updateAndDraw();
        if(colocPlus!=null){
            colocPlus.setSlice(middle + 1);
            colocPlus.setRoi(arrayRois[middle]);
            colocPlus.updateAndDraw();
            colocPlus.getWindow().toFront();
            colocPlus.getWindow().toFront();
        }
        imB2.getWindow().toFront();

    }//GEN-LAST:event_jListBMouseReleased

    private void changePopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePopActionPerformed
        if(changePop.getSelectedItem()=="Current"){
            jListA.removeAll();
            jListB.removeAll();
            jListA.setModel(modeAcurr);
            jListB.setModel(modeBcurr);
            nbObA.setText(""+modeAcurr.getSize());
            nbObB.setText(""+modeBcurr.getSize());
        }
        if(changePop.getSelectedItem()=="Touching"){
            jListA.removeAll();
            jListB.removeAll();
            jListA.setModel(modeAtouch);
            jListB.setModel(modeBtouch);
            nbObA.setText(""+modeAtouch.getSize());
            nbObB.setText(""+modeBtouch.getSize());
        }
    }//GEN-LAST:event_changePopActionPerformed

    private void imgA2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgA2ActionPerformed
        imgA2Select = (String) imgA2.getSelectedItem();
        if(Manager.testLabelled(WindowManager.getImage(imgA2Select))==true){
            WindowManager.getFrame(imgA2Select).toFront();
            imA2 = WindowManager.getImage(imgA2Select);
            int slice = imA2.getNSlices();
            imA2.setSlice(slice/2);
            imgA2.updateUI();
            ImageInt imInt = ImageInt.wrap(imA2);
            objPopA = new Objects3DPopulation(imInt);
            Manager.setPopulation1(objPopA);
        }
        else{
            if(Macro.getOptions()==null){
                IJ.showMessage("Be carefull", "Your selected image in Labelled A is not valid");
            }

        }
        
    }//GEN-LAST:event_imgA2ActionPerformed

    private void imgB2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgB2ActionPerformed
        imgB2Select = (String) imgB2.getSelectedItem();
        if(Manager.testLabelled(WindowManager.getImage(imgA2Select))==true){
            WindowManager.getFrame(imgB2Select).toFront();
            imB2=WindowManager.getImage(imgB2Select);
            int slice = imB2.getNSlices();
            imB2.setSlice(slice/2);
            imgB2.updateUI();
            ImageInt imInt=ImageInt.wrap(imB2);
            objPopB = new Objects3DPopulation(imInt);
            Manager.setPopulation2(objPopB);
        }
        else{
            if(Macro.getOptions()==null){
                IJ.showMessage("Be carefull", "Your selected image in Labelled B is not valid");
            }
        }
    }//GEN-LAST:event_imgB2ActionPerformed

    private void mergeObjectsAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeObjectsAActionPerformed

        int[] indexes = jListA.getSelectedIndices();
        Object3DVoxels objVox;
        if(changePop.getSelectedItem()=="Current"){
            Object3DVoxels obj0 = (Object3DVoxels) currPopA.getObject(indexes[0]);
            for (int i = 0; i < indexes.length; i++) {
                objVox = (Object3DVoxels) currPopA.getObject(indexes[i]);
                obj0.addVoxels(objVox.getVoxels());
                
                //update of Touch
                if(modeAtouch.contains(modeAcurr.get(indexes[i]))==true && modeAtouch.contains(modeAcurr.get(indexes[0]))==false){
                    modeAtouch.addElement(modeAcurr.get(indexes[0]));
                    touchPopA.addObject(currPopA.getObject(indexes[0]));
                    touchPopA.updateNamesAndValues();
                }
                if(modeAtouch.contains(modeAcurr.get(indexes[0]))==true){
                    Object3DVoxels objT = (Object3DVoxels) touchPopA.getObjectByValue(indexes[0]+1);
                    objT.addVoxels(objVox.getVoxels());
                }
            }
            for (int i = indexes.length-1; i >0 ; i--) {
                if(modeAtouch.contains(modeAcurr.get(indexes[i]))==true){
                    int el = modeAtouch.indexOf(modeAcurr.get(indexes[i]));
                    modeAtouch.removeElementAt(el);
                    touchPopA.removeObject(el);
                }
                modeAcurr.remove(indexes[i]);
                currPopA.getObject(indexes[i]).draw(iHandA, indexes[0]+1);
                currPopA.removeObject(indexes[i]);
            }
            //reOrderTouch, needed for the selection
            DefaultListModel modeAtouch2 = new DefaultListModel();
            while(modeAtouch.isEmpty()==false){
                Object p = modeAtouch.get(0);
                if(modeAtouch.size()>1){
                    for(int i=1;i<modeAtouch.size();i++){
                        if(Integer.parseInt(p.toString().substring(5))>Integer.parseInt(modeAtouch.get(i).toString().substring(5))){
                            p = modeAtouch.get(i);
                        }
                    }
                    modeAtouch2.addElement(p);
                    modeAtouch.removeElement(p);
                }
                else{
                    modeAtouch2.addElement(p);
                    modeAtouch.removeElement(p);
                }
            }
            modeAtouch=modeAtouch2;
        }
        
        if(changePop.getSelectedItem()=="Touching"){
            Object3DVoxels objT = (Object3DVoxels) touchPopA.getObject(indexes[0]);
            Object3DVoxels obj0 = (Object3DVoxels) currPopA.getObject(touchPopA.getObject(indexes[0]).getValue()-1);
            for (int i = 0; i < indexes.length; i++) {
                objVox = (Object3DVoxels) touchPopA.getObject(indexes[i]);
                objT.addVoxels(objVox.getVoxels());
                obj0.addVoxels(objVox.getVoxels());
            }
            for (int i = indexes.length-1; i >0 ; i--) {
                int el = touchPopA.getObject(indexes[i]).getValue()-1;
                modeAtouch.remove(indexes[i]);
                modeAcurr.removeElementAt(el);
                touchPopA.getObject(indexes[i]).draw(iHandA, touchPopA.getObject(indexes[0]).getValue());
                touchPopA.removeObject(indexes[i]);
                currPopA.removeObject(el);
            }
        }
        imA2.updateAndRepaintWindow();
        
    }//GEN-LAST:event_mergeObjectsAActionPerformed

    private void deleteObjectsAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteObjectsAActionPerformed
        int[] indexes = jListA.getSelectedIndices();
        for (int i = indexes.length-1; i >=0 ; i--) {
            if(changePop.getSelectedItem()=="Current"){
                if(modeAtouch.contains(modeAcurr.get(indexes[i]))==true){
                    modeAtouch.removeElement(modeAcurr.get(indexes[i]));
                }
                modeAcurr.remove(indexes[i]);
                currPopA.getObject(indexes[i]).draw(iHandA, 0);
                currPopA.removeObject(indexes[i]);
            }
            if(changePop.getSelectedItem()=="Touching"){
                int element = modeAcurr.indexOf(modeAtouch.get(indexes[i]));
                modeAcurr.removeElement(modeAtouch.get(indexes[i]));
                modeAtouch.remove(indexes[i]);
                currPopA.getObject(element).draw(iHandA, 0);
                currPopA.removeObject(element);
                touchPopA.removeObject(indexes[i]);
            }
        }
        imA2.updateAndRepaintWindow();
    }//GEN-LAST:event_deleteObjectsAActionPerformed

    private void mergeObjectsBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeObjectsBActionPerformed
        int[] indexes = jListB.getSelectedIndices();
        Object3DVoxels objVox;
        if(changePop.getSelectedItem()=="Current"){
            Object3DVoxels obj0 = (Object3DVoxels) currPopB.getObject(indexes[0]);
            for (int i = 0; i < indexes.length; i++) {
                objVox = (Object3DVoxels) currPopB.getObject(indexes[i]);
                obj0.addVoxels(objVox.getVoxels());
                
                //update of Touch
                if(modeBtouch.contains(modeBcurr.get(indexes[i]))==true && modeBtouch.contains(modeBcurr.get(indexes[0]))==false){
                    modeBtouch.addElement(modeBcurr.get(indexes[0]));
                    touchPopB.addObject(objPopB.getObject(indexes[0]));
                    touchPopB.updateNamesAndValues();
                }
                if(modeBtouch.contains(modeBcurr.get(indexes[0]))==true){
                    Object3DVoxels objT = (Object3DVoxels) touchPopB.getObjectByValue(indexes[0]+1);
                    objT.addVoxels(objVox.getVoxels());
                }
            }
            for (int i = indexes.length-1; i >0 ; i--) {
                if(modeBtouch.contains(modeBcurr.get(indexes[i]))==true){
                    int el= modeBtouch.indexOf(modeBcurr.get(indexes[i]));
                    modeBtouch.removeElementAt(el);
                    touchPopB.removeObject(el);
                }
                modeBcurr.remove(indexes[i]);
//                objPopB.getObject(indexes[i]).draw(isB2, indexes[0]+1);
                currPopB.getObject(indexes[i]).draw(iHandB, indexes[0]+1);
                currPopB.removeObject(indexes[i]);
            }
            //reOrderTouch, needed for the selection
            DefaultListModel modeBtouch2 = new DefaultListModel();
            while(modeBtouch.isEmpty()==false){
                Object p=modeBtouch.get(0);
                if(modeBtouch.size()>1){
                    for(int i=1; i<modeBtouch.size(); i++){
                        if(Integer.parseInt(p.toString().substring(5))>Integer.parseInt(modeBtouch.get(i).toString().substring(5))){
                            p=modeBtouch.get(i);
                        }
                    }
                    modeBtouch2.addElement(p);
                    modeBtouch.removeElement(p);
                }
                else{
                    modeBtouch2.addElement(p);
                    modeBtouch.removeElement(p);
                }
            }
            modeBtouch=modeBtouch2;
        }
        if(changePop.getSelectedItem()=="Touching"){
            Object3DVoxels objT = (Object3DVoxels) touchPopB.getObject(indexes[0]);
            Object3DVoxels obj0 = (Object3DVoxels) currPopB.getObject(touchPopB.getObject(indexes[0]).getValue()-1);
            for (int i = 0; i < indexes.length; i++) {
                objVox = (Object3DVoxels) touchPopB.getObject(indexes[i]);
                objT.addVoxels(objVox.getVoxels());
                obj0.addVoxels(objVox.getVoxels());
            }
            for (int i = indexes.length-1; i >0 ; i--) {
                int el = touchPopB.getObject(indexes[i]).getValue()-1;
                modeBtouch.remove(indexes[i]);
                modeBcurr.removeElementAt(el);
                touchPopB.getObject(indexes[i]).draw(iHandB, touchPopB.getObject(indexes[0]).getValue());
                touchPopB.removeObject(indexes[i]);
                currPopB.removeObject(el);
            }
        }
        imB2.updateAndRepaintWindow();
        
    }//GEN-LAST:event_mergeObjectsBActionPerformed

    private void deleteObjectsBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteObjectsBActionPerformed
        int[] indexes = jListB.getSelectedIndices();
        for (int i = indexes.length-1; i >=0 ; i--) {
            if(changePop.getSelectedItem()=="Current"){
                if(modeBtouch.contains(modeBcurr.get(indexes[i]))==true){
                    modeBtouch.removeElement(modeBcurr.get(indexes[i]));
                }
                modeBcurr.remove(indexes[i]);
//                objPopB.getObject(indexes[i]).draw(isB2, 0);
                currPopB.getObject(indexes[i]).draw(iHandB, 0);
                currPopB.removeObject(indexes[i]);
            }
            if(changePop.getSelectedItem()=="Touching"){
                int element = modeBcurr.indexOf(modeBtouch.get(indexes[i]));
                modeBcurr.removeElement(modeBtouch.get(indexes[i]));
                modeBtouch.remove(indexes[i]);
                currPopB.getObject(element).draw(iHandB, 0);
                currPopB.removeObject(element);
                touchPopB.removeObject(indexes[i]);
            }
        }
        imB2.updateAndRepaintWindow();
    }//GEN-LAST:event_deleteObjectsBActionPerformed

    private void imgAMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imgAMouseClicked
        title = Manager.testImageSizes(1, false);
        imgA.setModel(new DefaultComboBoxModel(title) );
        imgA.setSelectedIndex(0);
        imgA.updateUI();
        imgB.setModel(new DefaultComboBoxModel(title) );
        imgB.setSelectedIndex(1);
        imgB.updateUI();
    }//GEN-LAST:event_imgAMouseClicked

    private void imgBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imgBMouseClicked
        title = Manager.testImageSizes(1, false);
        imgB.setModel(new DefaultComboBoxModel(title) );
        imgB.updateUI();
    }//GEN-LAST:event_imgBMouseClicked

    private void imgA2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imgA2MouseClicked
        title = Manager.testImageSizes(1, false);
        imgA2.setModel(new DefaultComboBoxModel(title) );
        imgA2.updateUI();
    }//GEN-LAST:event_imgA2MouseClicked

    private void imgB2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imgB2MouseClicked
        title = Manager.testImageSizes(1, false);
        imgB2.setModel(new DefaultComboBoxModel(title) );
        imgB2.updateUI();
    }//GEN-LAST:event_imgB2MouseClicked

    private void savePopAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePopAActionPerformed
        if (!saveObjectsA()) {
            IJ.showMessage("Could not write RoiSet3D ");
        }
    }//GEN-LAST:event_savePopAActionPerformed

    private void savePopBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePopBActionPerformed
        if (!saveObjectsB()) {
            IJ.showMessage("Could not write RoiSet3D ");
        }
    }//GEN-LAST:event_savePopBActionPerformed

    private void aboutMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMouseReleased
        try{
            URI uri = URI.create("http://www.sciencedirect.com/science/article/pii/S1046202316304649");
            Desktop.getDesktop().browse(uri);
        }
        catch (IOException ex){
            IJ.error("Open failed");
        }
    }//GEN-LAST:event_aboutMouseReleased

      private void analyseMeasuresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyseMeasuresActionPerformed
        if(init==true){

            if(volume.isSelected())volumebool=true;
            if(mean.isSelected())meanbool=true;
            if(surface.isSelected())surfacebool=true;
            if(minMax.isSelected())minMaxbool=true;
            if(stdDev.isSelected())stdDevbool=true;
            if(centro.isSelected())centrobool=true;
            if(mass.isSelected())massbool=true;
            if(feret.isSelected())feretbool=true;

            ResultsTable resultsA = Measures.measureResult(iHA, currPopA, "ObjA-", volumebool, meanbool, surfacebool, minMaxbool, stdDevbool, centrobool, massbool, feretbool, intDenbool);
            ResultsTable resultsB = Measures.measureResult(iHB, currPopB, "ObjB-", volumebool, meanbool, surfacebool, minMaxbool, stdDevbool, centrobool, massbool, feretbool, intDenbool);
            resultsA.show("ObjectsMeasuresResults-A");
            resultsB.show("ObjectsMeasuresResults-B");
            
            imgTitle1=imA.getTitle();imgTitle2=imB.getTitle();
            labTitle1=imA2.getTitle();labTitle2=imB2.getTitle();
            measureBool = true;
            
            if (Macro.getOptions()==null){
                updatePrefs();
            } else{
                macroInterpreter(Macro.getOptions());
            }
        }
        else{IJ.showMessage("Initialize before analyse");}
      }//GEN-LAST:event_analyseMeasuresActionPerformed

      private void analyseShuffleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyseShuffleActionPerformed
        if(init==true) {
            //Object3D mask2 = null;
            if (shufflebounds1.isSelected()) {
                mask = DiAna_Ana.getmask(isA, true);
            }
            if (shufflebounds2.isSelected()) {
                ImageStack maskStack = imMask.getStack();
                mask = DiAna_Ana.getmask(maskStack, false);
                ImageHandler ihMask = ImageHandler.wrap(imMask);
                //macro
                maskbool = true;
                maskTitle = imMask.getTitle();
            }

            Measures.computeShuffle(mask, objPopA, objPopB);

            //Macro
            imgTitle1 = imA.getTitle();
            imgTitle2 = imB.getTitle();
            labTitle1 = imA2.getTitle();
            labTitle2 = imB2.getTitle();
            shuffleBool = true;

            if (Macro.getOptions() == null) {
                updatePrefs();
            } else {
                macroInterpreter(Macro.getOptions());
            }
        }
        else{IJ.showMessage("Initialize before analyse");}

      }//GEN-LAST:event_analyseShuffleActionPerformed

      private void imgBoundRef1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgBoundRef1ActionPerformed
        title = Manager.testImageSizes(1, false);
        imgMaskSelect = (String) imgBoundRef1.getSelectedItem();
        //        imMask = WindowManager.getImage(imgMaskSelect);
        if(Manager.testLabelled(WindowManager.getImage(imgMaskSelect))==true){
              WindowManager.getFrame(imgMaskSelect).toFront();
              imMask = WindowManager.getImage(imgMaskSelect);
        }
        else{IJ.showMessage("Be carefull", "Your selected image for the shuffle is not valid");}
      }//GEN-LAST:event_imgBoundRef1ActionPerformed

      private void shufflebounds2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shufflebounds2ActionPerformed
        //shufflebounds2.isSelected();
        title = Manager.testImageSizes(1, false);
        imgBoundRef1.setModel(new DefaultComboBoxModel(title));
        int a=0;boolean goodMask=false;
        while (goodMask==false && a<title.length){
              goodMask=Manager.testLabelled(WindowManager.getImage(title[a]));
              if(goodMask==true){imgBoundRef1.setSelectedIndex(a);}
              a++;
        }
        imgBoundRef1.updateUI();
      }//GEN-LAST:event_shufflebounds2ActionPerformed

      private void analyseAdjacencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyseAdjacencyActionPerformed
        if(init==true){
            numClo = Integer.parseInt(closestField1.getText());
            proxyCCbool = proxyCC.isSelected();
            proxyEEbool = proxyEE.isSelected();
            proxyCEbool = proxyCE.isSelected();
            proxyECbool = proxyEC.isSelected();
            surfcontactboolAdj = surfproxy1.isSelected();
            Measures meas = new Measures(iHandA, iHandB, currPopA, currPopB);
            distA = Double.parseDouble(surfMax2.getText());
            meas.ComputeAdjacency(numClo, proxyCCbool, proxyEEbool, proxyCEbool, proxyECbool, surfcontactboolAdj, distA);
                        
//            distC=Double.parseDouble(surfMax2.getText());
            imgTitle1=imA.getTitle();imgTitle2=imB.getTitle();
            labTitle1=imA2.getTitle();labTitle2=imB2.getTitle();
            adjaBool = true;
            
            if (Macro.getOptions()==null){
                  updatePrefs();
            } else{
                macroInterpreter(Macro.getOptions());
            }
        }
        else{IJ.showMessage("Initialize before analyse");}
      }//GEN-LAST:event_analyseAdjacencyActionPerformed

      private void analyseColocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyseColocActionPerformed
        if(init==true){
            jListA.removeAll();
            jListB.removeAll();
            
            IJ.log("number of objects in image A = "+currPopA.getNbObjects());
            IJ.log("number of objects in image B = "+currPopB.getNbObjects());
            
            colocFromAbool = colocFromA.isSelected();
            colocFromBbool = colocFromB.isSelected();
            colocFromABbool = colocFromAB.isSelected();
            colocCCbool = colocCC.isSelected();
//            if(colocEE.isSelected())
            colocCEbool = colocCE.isSelected();
            colocECbool = colocEC.isSelected();
            surfcontactbool = surfcoloc.isSelected();
            Measures meas = new Measures(iHandA, iHandB, currPopA, currPopB);
            
            distC = Double.parseDouble(surfMax.getText());
            meas.computeColoc(colocFromAbool, colocFromBbool, colocFromABbool, colocCCbool, colocCEbool, colocECbool, surfcontactbool, distC);
            touchPopA = meas.getTouchingPopA();
            touchPopB = meas.getTouchingPopB();
            
            if(contactMap.isSelected()){
                //save Rois
                arrayRoisA=Measures.arrayRoi(currPopA, "objA-");
                for (Roi arrayRoisA1 : arrayRoisA) {
                    modeAcurr.addElement(arrayRoisA1.getName());
                }
                arrayRoisB=Measures.arrayRoi(currPopB, "objB-");
                for (Roi arrayRoisB1 : arrayRoisB) {
                    modeBcurr.addElement(arrayRoisB1.getName());
                }
                arrayA=Measures.arrayRoi(touchPopA, "objA-");
                for (Roi arrayA1 : arrayA) {
                    modeAtouch.addElement(arrayA1.getName());
                }
                arrayB=Measures.arrayRoi(touchPopB, "objB-");
                for (Roi arrayB1 : arrayB) {
                    modeBtouch.addElement(arrayB1.getName());
                }
                
                jListA.setModel(modeAcurr);
                jListB.setModel(modeBcurr);
                jListA.updateUI();
                jListB.updateUI();
                nbObA.setText(""+modeAcurr.getSize());
                nbObB.setText(""+modeBcurr.getSize());

                RoiFrame.setVisible(true);
                ImageHandler ColocHandler = meas.getImageColoc();
                ColocHandler.setMinAndMax((float)0, (float)ColocHandler.getMax());
                ColocHandler.setCalibration(cali);
                ColocHandler.set332RGBLut();
                ColocHandler.show();
            }
            
            //Macro
//            distC=Double.parseDouble(surfMax.getText());
            imgTitle1=imA.getTitle();imgTitle2=imB.getTitle();
            labTitle1=imA2.getTitle();labTitle2=imB2.getTitle();
            colocBool=true;
            
            if (Macro.getOptions()==null){
                  updatePrefs();
            } else{
                macroInterpreter(Macro.getOptions());
            }
        }
        else{IJ.showMessage("Initialize before analyse");}
      }//GEN-LAST:event_analyseColocActionPerformed

    public void clickOnObjectAon(java.awt.event.MouseEvent evt){
        if(RoiFrame.isActive()==true){
            imA2.getCanvas().addMouseListener(this);
            imB2.getCanvas().addMouseListener(this);
        }
    }
    
    public void clickOnObjectAoff(java.awt.event.MouseEvent evt){
        if(RoiFrame.isActive()==true){
            imA2.getCanvas().removeMouseListener(this);
            imB2.getCanvas().removeMouseListener(this);
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void mouseReleased(MouseEvent e) {
        int id = WindowManager.getCurrentImage().getID();
        int x = e.getX();
        int y = e.getY();
        //int v=0;
        int[] val = WindowManager.getCurrentImage().getPixel(x, y);
        int v = val[0];
//        if(id==imA2.getID() || id==imB2.getID()){
//            int[] val=WindowManager.getCurrentImage().getPixel(x, y);
//            v=val[0];
//        }
        if(id==imA2.getID()){
            if( (v!=0) && (changePop.getSelectedItem()=="Current")){
                jListA.setSelectedIndex(v);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    /**
     *
     * @return
     */
    public Object3D getMask() {
        return mask;
    }

    /**
     *
     * @param mask
     */
    public void setMask(Object3D mask) {
        this.mask = mask;
        mask.init();
    }
    
    private boolean saveObjectsA() {
        SaveDialog sav = new SaveDialog("Save RoiSet3D", "popA-"+changePop.getSelectedItem(), ".zip");
        return saveObjectsA(sav.getDirectory()+sav.getFileName());
    }

    private boolean saveObjectsA(String path) {
        Objects3DPopulation pop = objPopA;
        if(changePop.getSelectedItem()=="Touching"){
            pop = touchPopA;
        }
        return pop.saveObjects(path);
    }
    
    private boolean saveObjectsB() {
        SaveDialog sav = new SaveDialog("Save RoiSet3D", "popB-"+changePop.getSelectedItem(), ".zip");
        return saveObjectsB(sav.getDirectory()+sav.getFileName());
    }

    private boolean saveObjectsB(String path) {
        Objects3DPopulation pop = objPopB;
        if(changePop.getSelectedItem()=="Touching"){
            pop = touchPopB;
        }
        return pop.saveObjects(path);
    }
    
    /* Gets the allIndexes attribute of the RoiManager object
     *
     * @return The allIndexes value
     */
    private int[] getAllIndexesA() {
        int count = modeAcurr.getSize();
        int[] indexes = new int[count];
        for (int i = 0; i < count; i++) {
            indexes[i] = i;
        }
        return indexes;
    }
    
    /* Gets the allIndexes attribute of the RoiManager object
     *
     * @return The allIndexes value
     */
    private int[] getAllIndexesB() {
        int count = modeBcurr.getSize();
        int[] indexes = new int[count];
        for (int i = 0; i < count; i++) {
            indexes[i] = i;
        }
        return indexes;
    }    
    
    private void updatePrefs(){
        //Others
        allTouch1bool=allTouch1.isSelected();
        Prefs.set("Diana_allTouch1.boolean", allTouch1bool);
        colocFromAbool=colocFromA.isSelected();
        Prefs.set("Diana_colocFromA1.boolean", colocFromAbool);
        colocFromBbool=colocFromB.isSelected();
        Prefs.set("Diana_colocFromB1.boolean", colocFromBbool);
        colocFromABbool=colocFromAB.isSelected();
        Prefs.set("Diana_colocFromAB1.boolean", colocFromABbool);
        colocCCbool=colocCC.isSelected();
        Prefs.set("Diana_colocCC1.boolean", colocCCbool);
        colocECbool=colocEC.isSelected();
        Prefs.set("Diana_colocEC1.boolean", colocECbool);
        colocCEbool=colocCE.isSelected();
        Prefs.set("Diana_colocCE1.boolean", colocCEbool);
        surfcontactbool=surfcoloc.isSelected();
        Prefs.set("Diana_contSurf1.boolean", surfcontactbool);
        contactMap1bool=contactMap.isSelected();
        Prefs.set("Diana_contactMap1.boolean", contactMap1bool);
        proxyCCbool=proxyCC.isSelected();
        Prefs.set("Diana_proxyCC1.boolean", proxyCCbool);
        proxyEEbool=proxyEE.isSelected();
        Prefs.set("Diana_proxyEE1.boolean", proxyEEbool);
        proxyCEbool=proxyCE.isSelected();
        Prefs.set("Diana_proxyCE1.boolean", proxyCEbool);
        proxyECbool=proxyEC.isSelected();
        Prefs.set("Diana_proxyEC1.boolean", proxyECbool);
        surfcontactboolAdj=surfproxy1.isSelected();
        Prefs.set("Diana_contSurfAdj.boolean", surfcontactboolAdj);
        volumebool=volume.isSelected();
        Prefs.set("Diana_volume1.boolean", volumebool);
        meanbool=mean.isSelected();
        Prefs.set("Diana_mean1.boolean", meanbool);
        surfacebool=surface.isSelected();
        Prefs.set("Diana_surface1.boolean", surfacebool);
        stdDevbool=stdDev.isSelected();
        Prefs.set("diana_stdDev1.boolean", stdDevbool);
        minMaxbool=minMax.isSelected();
        Prefs.set("Diana_minMax1.boolean", minMaxbool);
        centrobool=centro.isSelected();
        Prefs.set("Diana_centro1.boolean", centrobool);
        massbool=mass.isSelected();
        Prefs.set("Diana_mass1.boolean", massbool);
        feretbool=feret.isSelected();
        Prefs.set("Diana_feret1.boolean", feretbool);
        intDenbool=intDen.isSelected();
        Prefs.set("Diana_intDen1.boolean", intDenbool);
        if (Recorder.record) {
            macroGenerator(imgTitle1, imgTitle2, labTitle1, labTitle2, maskTitle);
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
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
            java.util.logging.Logger.getLogger(DiAna_Analyse.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DiAna_Analyse.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DiAna_Analyse.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DiAna_Analyse.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (Macro.getOptions()==null){
                    new DiAna_Analyse().setVisible(true);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane MeasuresTabbedPanel;
    private javax.swing.JFrame RoiFrame;
    private javax.swing.JButton about;
    private javax.swing.JPanel allAllPanel;
    private javax.swing.JCheckBox allTouch1;
    private javax.swing.JButton analyseAdjacency;
    private javax.swing.JButton analyseColoc;
    private javax.swing.JButton analyseMeasures;
    private javax.swing.JButton analyseShuffle;
    private javax.swing.ButtonGroup boundingGroup1;
    private javax.swing.JCheckBox centro;
    private javax.swing.JComboBox changePop;
    private javax.swing.JFormattedTextField closestField1;
    private javax.swing.JLabel closestLabel1;
    private javax.swing.JLabel closestLabel2;
    private javax.swing.JCheckBox colocCC;
    private javax.swing.JCheckBox colocCE;
    private javax.swing.JCheckBox colocEC;
    private javax.swing.JCheckBox colocFromA;
    private javax.swing.JCheckBox colocFromAB;
    private javax.swing.JCheckBox colocFromB;
    private javax.swing.JPanel colocPanel;
    private javax.swing.JCheckBox contactMap;
    private javax.swing.JButton deleteObjectsA;
    private javax.swing.JButton deleteObjectsB;
    private javax.swing.JLabel disColocLabA;
    private javax.swing.JLabel disProxyLabA1;
    private javax.swing.JLabel distMaxSurface;
    private javax.swing.JLabel distMaxSurface1;
    private javax.swing.JCheckBox feret;
    private javax.swing.JLabel imageALab;
    private javax.swing.JLabel imageALab1;
    private javax.swing.JLabel imageBLab;
    private javax.swing.JLabel imageBLab1;
    private javax.swing.JLabel imageBoundRefLab;
    private javax.swing.JLabel imageToAnaLab;
    private javax.swing.JComboBox imgA;
    private javax.swing.JComboBox imgA2;
    private javax.swing.JComboBox imgB;
    private javax.swing.JComboBox imgB2;
    private javax.swing.JComboBox imgBoundRef1;
    private javax.swing.JButton initial;
    private javax.swing.JCheckBox intDen;
    private javax.swing.JLabel interacLab1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    public javax.swing.JList jListA;
    public javax.swing.JList jListB;
    private javax.swing.JLabel labelImA;
    private javax.swing.JLabel labelImB;
    private javax.swing.JLabel labelShuffle;
    private javax.swing.JCheckBox mass;
    private javax.swing.JCheckBox mean;
    private javax.swing.JLabel measAnaLab1;
    private javax.swing.JPanel measurePanel1;
    private javax.swing.JButton mergeObjectsA;
    private javax.swing.JButton mergeObjectsB;
    private javax.swing.ButtonGroup microGroup1;
    private javax.swing.ButtonGroup microGroup2;
    private javax.swing.JCheckBox minMax;
    private javax.swing.JLabel nbObA;
    private javax.swing.JLabel nbObB;
    private javax.swing.JLabel popLabel;
    private javax.swing.JCheckBox proxyCC;
    private javax.swing.JCheckBox proxyCE;
    private javax.swing.JCheckBox proxyEC;
    private javax.swing.JCheckBox proxyEE;
    private javax.swing.JPanel proxyPanel1;
    private javax.swing.JButton savePopA;
    private javax.swing.JButton savePopB;
    public javax.swing.JScrollPane scrollPaneA;
    private javax.swing.JScrollPane scrollPaneB;
    private javax.swing.JLabel shuffleLab1;
    private javax.swing.JPanel shufflePanel;
    private javax.swing.JRadioButton shufflebounds1;
    private javax.swing.JRadioButton shufflebounds2;
    private javax.swing.JCheckBox stdDev;
    private javax.swing.JFormattedTextField surfMax;
    private javax.swing.JFormattedTextField surfMax2;
    private javax.swing.JCheckBox surface;
    private javax.swing.JCheckBox surfcoloc;
    private javax.swing.JCheckBox surfproxy1;
    private javax.swing.JLabel tablColocLabA;
    private javax.swing.JLabel totalLabA;
    private javax.swing.JLabel totalLabA1;
    private javax.swing.JLabel viewColocLabA;
    private javax.swing.JCheckBox volume;
    // End of variables declaration//GEN-END:variables

//    @Override
//    public String handleExtension(String string, Object[] os) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public ExtensionDescriptor[] getExtensionFunctions() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

}
