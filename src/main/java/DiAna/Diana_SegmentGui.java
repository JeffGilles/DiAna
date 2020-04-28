/**
 * Copyright (C) 2016 Jean-François Gilles

    License:
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
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
import ij.measure.Calibration;
import ij.plugin.frame.Recorder;
import ij.process.ImageProcessor;
import javax.swing.DefaultComboBoxModel;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import javax.swing.JFrame;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.*;

/**
 *
 * @author jean-francois gilles
 */
public class Diana_SegmentGui extends JFrame {

    DefaultComboBoxModel model;
    String[] title;
    String imgASelect, imgBSelect;
    ImagePlus imA, imA2, imB, imB2;
    ImageStack isA, isB, isA2, isB2;
    ImageProcessor ipA2, ipB2;
    ImageHandler seed3DImageA, seed3DImageB;
    int highthrA = 0, highthrB = 0;
    float radius, rZ, noise, sd=1;
    boolean excludeZ=false, filterboolA=false, filterboolB=false;
    Objects3DPopulation objPopA, objPopB;
    Calibration cali;
    
    private int segtype = -1;
    
    //Preferences
    //A
    int filterBoxAPref = (int) Prefs.get("Diana_filterBoxA.int", 3);
    int minSizeA = (int) Prefs.get("Diana_minSizeA.int", 3);
    int maxSizeA = (int)Prefs.get("Diana_maxSizeA.int", 2000);
    float radXYA = (float)Prefs.get("Diana_radXYA.float", 2);
    float radZA = (float)Prefs.get("Diana_radZA.float", 2);
    float noiseAPref = (float)Prefs.get("Diana_noiseA.float", 50);
    int seedAPref = (int)Prefs.get("Diana_seedThresA.int", 500);
    int gaussRadAPref = (int)Prefs.get("Diana_gaussRadA.int", 10);
    float sdAPref = (float)Prefs.get("Diana_sdA.float", 1.5);
    int iterThrAPref = (int)Prefs.get("Diana_iterThrA.int", 500);
    int iterStepAPref = (int)Prefs.get("Diana_iterStepA.int", 1);
    boolean excludeXYA = Prefs.get("Diana_exludeEdgeXYA1.boolean", false);
    //B
    int filterBoxBPref = Prefs.getInt("Diana_filterBoxB.int", 3);
    int minSizeB = (int) Prefs.get("Diana_minSizeB.int", 3);
    int maxSizeB = (int)Prefs.get("Diana_maxSizeB.int", 2000);
    float radXYB = (float)Prefs.get("Diana_radXYB.int", 2);
    float radZB = (float)Prefs.get("Diana_radZB.int", 2);
    float noiseBPref = (float)Prefs.get("Diana_noiseB.int", 50);
    int seedBPref = (int)Prefs.get("Diana_seedThresB.int", 500);
    int gaussRadBPref = (int)Prefs.get("Diana_gaussRadB.int", 10);
    float sdBPref = (float)Prefs.get("Diana_sdB.int", 1.5);
    int iterThrBPref = (int)Prefs.get("Diana_iterThrB.int", 500);
    int iterStepBPref = (int)Prefs.get("Diana_iterStepB.int", 1);
    boolean excludeXYB = Prefs.get("Diana_exludeEdgeXYB1.boolean", false);
    //macro
    private String handleFilter, imgTitle;
    boolean filterBool=false, segClassicBool=false, spotBool=false, iterBool=false, exXY=false, exZ=false;
    int thr=1, min=0, max=1, seed=1, gauss=1, mth=1, step=1;
    
    /**
     * Creates new form Diana_Gui
     */
    public Diana_SegmentGui() {
        initComponents();        
        if(WindowManager.getImageCount()>0){
            title=Manager.testImageSizes(0, true);
            imgA.setModel(new DefaultComboBoxModel(title) );
            imgA.setSelectedIndex(0);
            imgA.updateUI();
        }
        if(WindowManager.getImageCount()>=2){
            imgB.setModel(new DefaultComboBoxModel(title) );
            imgB.setSelectedIndex(1);
            imgB.updateUI();
        }
    }
        
//    MACRO HANDLE
    public int handleFilter2 (String filter){
        int used;
        if(filter.equals("gaussian")){used=0;}
        else if(filter.equals("median")){used=1;}
        else if (filter.equals("mean")){used=2;}
        else {used=3;}
        return used;
    }
    
    public void macroBatchRunner(ImagePlus img){
        cali=img.getCalibration();
        ImagePlus image=null;
        if(filterBool){
            image = segment.filter(img, handleFilter, radius);
            
            if(segClassicBool){
                Objects3DPopulation pop=new segment().segClassic(image, thr, min, max, exXY, exZ);
                ImagePlus plus = segment.createImageObjects(img.getShortTitle()+"-labelled", img, pop);
                image.close();
                plus.show();
//                image=segment.showImageObjects(img.getShortTitle()+"-labelled", stack, pop, cali);
            }
            else{
                image.setCalibration(cali);
            }
        }
        if(spotBool){
            ImageHandler seed3D = segment.ImagePeaks(img, radius, rZ, noise);
            ImageHandler iHA = ImageHandler.wrap(img.duplicate());
            Objects3DPopulation pop = new segment().segSpot(iHA, seed3D, seed, gauss, sd, min, max, exXY);
            image = segment.createImageObjects(img.getShortTitle()+"-labelled", img, pop);
            image.show();
//            ImageStack stack = segment.createImageObjects(img.getImageStack(), pop);
//            image = segment.showImageObjects(img.getShortTitle()+"-labelled", stack, pop, cali);
        }
        if(iterBool){
            Objects3DPopulation pop = new segment().segIter(img, min, max, step, mth, exXY);
//            ImageStack stack = segment.createImageObjects(img.getImageStack(), pop);
//            image = segment.showImageObjects(img.getShortTitle()+"-labelled", stack, pop, cali);
            image = segment.createImageObjects(img.getShortTitle()+"-labelled", img, pop);
            image.show();
        }
    }
    
    /**
     * Perform the commands in the macro
     * @param arg command
     */
    public void macroInterpreter(String arg){
        
        int start, end;
        filterBool=arg.contains("filter");
        segClassicBool=arg.contains("thr");
        spotBool=arg.contains("spots");
        iterBool=arg.contains("iter");
        
        start=arg.indexOf("img=")+4;
        end=arg.indexOf(" ", start);
        if ((arg.charAt(start)+"").equals("[")){
            start++;
            end=arg.indexOf("]", start);
        }
        imgTitle=arg.substring(start, end);
        ImagePlus im=WindowManager.getImage(imgTitle);
        if (im==null){
            IJ.error("DiAna error, within a macro", "Image not found while running DiAna from a macro\n1-Use \"open(path)\" in your macro to open images\n2-Make sure you have called the right image !");
            return;
        }
        
        if (filterBool || segClassicBool){
            //Filter
            start=arg.indexOf("filter=")+7;
            end=arg.indexOf(" ",start);
            handleFilter=(arg.substring(start, end));
            //Radius
            start=arg.indexOf("rad=")+4;
            end=arg.indexOf(" ",start);
            radius=Float.parseFloat(arg.substring(start, end));
            if(segClassicBool){
                start=arg.indexOf("thr=")+4;
                end=arg.indexOf(" ",start);
                String[] tmp=arg.substring(start, end).split("-");
                thr=Integer.parseInt(tmp[0]);
                min=Integer.parseInt(tmp[1]);
                max=Integer.parseInt(tmp[2]);
                exXY=Boolean.parseBoolean(tmp[3]);
                exZ=Boolean.parseBoolean(tmp[4]);
            }
        }
        if(spotBool){
            //peaks
            start=arg.indexOf("peaks=")+6;
            end=arg.indexOf(" ",start);
            String[] tmp=arg.substring(start, end).split("-");
            radius=Float.parseFloat(tmp[0]);
            rZ=Float.parseFloat(tmp[1]);
            noise=Float.parseFloat(tmp[2]);
            //spots
            start=arg.indexOf("spots=")+6;
            end=arg.indexOf(" ",start);
            tmp=arg.substring(start, end).split("-");
            seed=Integer.parseInt(tmp[0]);
            gauss=Integer.parseInt(tmp[1]);
            sd=Float.parseFloat(tmp[2]);
            min=Integer.parseInt(tmp[3]);
            max=Integer.parseInt(tmp[4]);
            exXY=Boolean.parseBoolean(tmp[5]);
//            IJ.log("arg="+arg);
        }
        if(iterBool){
            start=arg.indexOf("iter=")+5;
            end=arg.indexOf(" ",start);
            String[] tmp=arg.substring(start, end).split("-");
            min=Integer.parseInt(tmp[0]);
            max=Integer.parseInt(tmp[1]);
            mth=Integer.parseInt(tmp[2]);
            step=Integer.parseInt(tmp[3]);
            exXY=Boolean.parseBoolean(tmp[4]);
//            IJ.log("arg="+arg);
        }
        macroBatchRunner(im);
    }
    
    /**
     * Generate the macro command in the recorder
     * @param title command
     */
    public void macroGenerator(final String title){
        Recorder.setCommand("DiAna_Segment");
        Recorder.recordOption("img", title);
        if(filterboolA || filterboolB || segClassicBool ){
            Recorder.recordOption("filter", ""+handleFilter);
            Recorder.recordOption("rad", ""+radius);
            if(segClassicBool){
                Recorder.recordOption("thr", ""+thr+"-"+min+"-"+max+"-"+exXY+"-"+exZ);
                IJ.log("macro commandLine: "+"img="+title+" filter="+handleFilter+" rad="+radius+" thr="+thr+"-"+min+"-"+max+"-"+exXY+"-"+exZ);
            }
        }
        if(spotBool){
            Recorder.recordOption("peaks", ""+radius+"-"+rZ+"-"+noise);
            Recorder.recordOption("spots", ""+seed+"-"+gauss+"-"+sd+"-"+min+"-"+max+"-"+exXY);
            IJ.log("macro commandLine: "+"img="+title+" peaks="+radius+"-"+rZ+"-"+noise+" spots="+seed+"-"+gauss+"-"+sd+"-"+min+"-"+max+"-"+exXY);
        }
        if(iterBool){
            Recorder.recordOption("iter", ""+min+"-"+max+"-"+mth+"-"+step+"-"+exXY);
            IJ.log("macro commandLine: "+"img="+title+" iter="+min+"-"+max+"-"+mth+"-"+step+"-"+exXY);
        }
        
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

        imageToAnaLab = new javax.swing.JLabel();
        imageALab = new javax.swing.JLabel();
        imageBLab = new javax.swing.JLabel();
        imgA = new javax.swing.JComboBox();
        imgB = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        filtersPanelB = new javax.swing.JPanel();
        FilterTabbedPanB = new javax.swing.JTabbedPane();
        filtersClassicB = new javax.swing.JPanel();
        FilterTypeB = new javax.swing.JLabel();
        FilterRadLabB = new javax.swing.JLabel();
        FilterBoxB = new javax.swing.JComboBox();
        FilterButB = new javax.swing.JButton();
        thresholdLabB = new javax.swing.JLabel();
        ThresValB = new javax.swing.JFormattedTextField();
        threSlidB = new javax.swing.JSlider();
        minLabB1 = new javax.swing.JLabel();
        minSizeB1 = new javax.swing.JFormattedTextField();
        maxSizeB1 = new javax.swing.JFormattedTextField();
        maxSizeLabB1 = new javax.swing.JLabel();
        excludeEdgeXYB1 = new javax.swing.JCheckBox("Exclude objects on XY edges", excludeXYB);
        excludeEdgeZB1 = new javax.swing.JCheckBox();
        segButB = new javax.swing.JButton();
        filterRadB = new javax.swing.JFormattedTextField();
        filtersSpotB1 = new javax.swing.JPanel();
        segSpotB = new javax.swing.JButton();
        volminWatershB = new javax.swing.JLabel();
        watVolminB = new javax.swing.JFormattedTextField();
        volmaxWatershB = new javax.swing.JLabel();
        watVolmaxB = new javax.swing.JFormattedTextField();
        excludeEdgeXYB2 = new javax.swing.JCheckBox("Exclude objects on XY edges", excludeXYB);
        questionSpot2 = new javax.swing.JLabel();
        gaussRad1Blab = new javax.swing.JLabel();
        gaussRad1B = new javax.swing.JTextField();
        localsd1B = new javax.swing.JTextField();
        localsd1Blab = new javax.swing.JLabel();
        maxFinderLabB = new javax.swing.JLabel();
        radxyLabB = new javax.swing.JLabel();
        radzB = new javax.swing.JFormattedTextField();
        radZLabB = new javax.swing.JLabel();
        radxyB = new javax.swing.JFormattedTextField();
        noiseLabB = new javax.swing.JLabel();
        noiseB = new javax.swing.JFormattedTextField();
        maxFinderPreviewB = new javax.swing.JButton();
        seedB2 = new javax.swing.JLabel();
        seedThresB = new javax.swing.JFormattedTextField();
        localThrMetB = new javax.swing.JLabel();
        filtersIterB1 = new javax.swing.JPanel();
        volminIterLabB = new javax.swing.JLabel();
        volminIterB = new javax.swing.JFormattedTextField();
        questionItera2 = new javax.swing.JLabel();
        volmaxIterLabB = new javax.swing.JLabel();
        volmaxIterB1 = new javax.swing.JFormattedTextField();
        minThresIterB = new javax.swing.JFormattedTextField();
        minThresIterLabB1 = new javax.swing.JLabel();
        valueMethodLabB = new javax.swing.JLabel();
        valueThresIterB = new javax.swing.JFormattedTextField();
        segIteraB = new javax.swing.JButton();
        excludeEdgeXYB3 = new javax.swing.JCheckBox("Exclude objects on XY edges", excludeXYB);
        filtersPanelA = new javax.swing.JPanel();
        FilterTabbedPanelA = new javax.swing.JTabbedPane();
        filtersClassicA = new javax.swing.JPanel();
        FilterTypeA = new javax.swing.JLabel();
        FilterRadLabA = new javax.swing.JLabel();
        filterBoxA = new javax.swing.JComboBox();
        filterRadA = new javax.swing.JFormattedTextField();
        FilterButA = new javax.swing.JButton();
        thresholdLabA = new javax.swing.JLabel();
        ThresValA = new javax.swing.JFormattedTextField();
        threSlidA = new javax.swing.JSlider();
        minLabA = new javax.swing.JLabel();
        minSizeA1 = new javax.swing.JFormattedTextField();
        maxSizeA1 = new javax.swing.JFormattedTextField();
        maxSizeLabA1 = new javax.swing.JLabel();
        excludeEdgeXYA1 = new javax.swing.JCheckBox("Exclude objects on XY edges", excludeXYA);
        excludeEdgeZA1 = new javax.swing.JCheckBox();
        segButA = new javax.swing.JButton();
        filtersSpotA = new javax.swing.JPanel();
        segSpotA = new javax.swing.JButton();
        volminWatershA = new javax.swing.JLabel();
        watVolminA = new javax.swing.JFormattedTextField();
        volmaxWatershA = new javax.swing.JLabel();
        watVolmaxA = new javax.swing.JFormattedTextField();
        excludeEdgeXYA2 = new javax.swing.JCheckBox("Exclude objects on XY edges", excludeXYA);
        questionSpot1 = new javax.swing.JLabel();
        gaussRad1Alab = new javax.swing.JLabel();
        gaussRad1A = new javax.swing.JTextField();
        localsd1Alab = new javax.swing.JLabel();
        localsd1A = new javax.swing.JTextField();
        radxyLabA = new javax.swing.JLabel();
        radxyA = new javax.swing.JFormattedTextField();
        radZLabA = new javax.swing.JLabel();
        radzA = new javax.swing.JFormattedTextField();
        noiseLabA = new javax.swing.JLabel();
        noiseA = new javax.swing.JFormattedTextField();
        maxFinderPreviewA = new javax.swing.JButton();
        seedThresA = new javax.swing.JFormattedTextField();
        seedA2 = new javax.swing.JLabel();
        maxFinderLabA = new javax.swing.JLabel();
        localThrMetA = new javax.swing.JLabel();
        filtersIterA1 = new javax.swing.JPanel();
        volminIterLabA = new javax.swing.JLabel();
        volminIterA = new javax.swing.JFormattedTextField();
        volmaxIterLabA = new javax.swing.JLabel();
        minThresIterLabA1 = new javax.swing.JLabel();
        volmaxIterA = new javax.swing.JFormattedTextField();
        minThresIterA = new javax.swing.JFormattedTextField();
        valueMethodLabA = new javax.swing.JLabel();
        valueThresIterA = new javax.swing.JFormattedTextField();
        segIteraA = new javax.swing.JButton();
        questionItera1 = new javax.swing.JLabel();
        excludeEdgeXYA3 = new javax.swing.JCheckBox("Exclude objects on XY edges", excludeXYA);
        goAnalyseButton1 = new javax.swing.JButton();
        about = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DiAna Labelisation");
        setName("GUIframe"); // NOI18N

        imageToAnaLab.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        imageToAnaLab.setText("Images to analyse:");

        imageALab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imageALab.setLabelFor(imgA);
        imageALab.setText("Image A :");

        imageBLab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        imageBLab.setLabelFor(imgB);
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

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/allAllico.gif")));
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oneAllico.gif")));
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel2.setPreferredSize(new java.awt.Dimension(65, 65));

        filtersPanelB.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filters image B", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        filtersPanelB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        filtersPanelB.setPreferredSize(new java.awt.Dimension(240, 320));

        FilterTabbedPanB.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        FilterTabbedPanB.setPreferredSize(new java.awt.Dimension(228, 292));

        filtersClassicB.setBackground(new java.awt.Color(225, 225, 225));
        filtersClassicB.setPreferredSize(new java.awt.Dimension(208, 245));

        FilterTypeB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        FilterTypeB.setText("Filter type");

        FilterRadLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        FilterRadLabB.setText("Radius");

        FilterBoxB.setBackground(new java.awt.Color(245, 245, 245));
        FilterBoxB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        FilterBoxB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "gaussian", "median", "mean", "none" }));
        FilterBoxB.setSelectedIndex(filterBoxBPref);
        FilterBoxB.setToolTipText("Indicate which filter you want to use. Select \"None\" if you've already done a segmentation.");
        FilterBoxB.setPreferredSize(new java.awt.Dimension(80, 24));

        FilterButB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        FilterButB.setText("Valide");
        FilterButB.setToolTipText("Press here to perform the filter.");
        FilterButB.setMaximumSize(new java.awt.Dimension(70, 27));
        FilterButB.setMinimumSize(new java.awt.Dimension(70, 27));
        FilterButB.setPreferredSize(new java.awt.Dimension(70, 20));
        FilterButB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterButBActionPerformed(evt);
            }
        });

        thresholdLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        thresholdLabB.setText("Threshold ");

        ThresValB.setToolTipText("Actual value of the threshold");
        ThresValB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        ThresValB.setMaximumSize(new java.awt.Dimension(50, 20));
        ThresValB.setMinimumSize(new java.awt.Dimension(31, 20));
        ThresValB.setPreferredSize(new java.awt.Dimension(32, 20));
        ThresValB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ThresValBActionPerformed(evt);
            }
        });

        threSlidB.setBackground(new java.awt.Color(225, 225, 225));
        threSlidB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        threSlidB.setToolTipText("Slider to select the threshold value");
        threSlidB.setPreferredSize(new java.awt.Dimension(220, 16));
        threSlidB.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                threSlidBStateChanged(evt);
            }
        });

        minLabB1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minLabB1.setText("min. Object Size (pxl):");

        minSizeB1.setText(String.valueOf(minSizeB));
        minSizeB1.setToolTipText("Exclude objects inferior to this value");
        minSizeB1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minSizeB1.setPreferredSize(new java.awt.Dimension(40, 20));

        maxSizeB1.setText(String.valueOf(maxSizeB));
        maxSizeB1.setToolTipText("Exclude objects superior to this value");
        maxSizeB1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        maxSizeB1.setPreferredSize(new java.awt.Dimension(40, 20));

        maxSizeLabB1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        maxSizeLabB1.setText("Max. Object Size (pxl):");

        excludeEdgeXYB1.setBackground(new java.awt.Color(225, 225, 224));
        excludeEdgeXYB1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        excludeEdgeXYB1.setText("Exclude objects on XY edges");
        excludeEdgeXYB1.setToolTipText("If checked, it excludes objects which are touching XY edges");

        excludeEdgeZB1.setBackground(new java.awt.Color(225, 225, 224));
        excludeEdgeZB1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        excludeEdgeZB1.setText("Exclude objects on Z edges");
        excludeEdgeZB1.setToolTipText("If checked, it excludes objects which are touching Z edges (first and last slice)");

        segButB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        segButB.setText("Segment");
        segButB.setToolTipText("Perform the segmentation with the values selected above");
        segButB.setPreferredSize(new java.awt.Dimension(85, 20));
        segButB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segButBActionPerformed(evt);
            }
        });

        filterRadB.setText("1.0");
        filterRadB.setToolTipText("Radius in XY");
        filterRadB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        filterRadB.setMaximumSize(new java.awt.Dimension(50, 20));
        filterRadB.setMinimumSize(new java.awt.Dimension(40, 20));
        filterRadB.setPreferredSize(new java.awt.Dimension(45, 20));

        javax.swing.GroupLayout filtersClassicBLayout = new javax.swing.GroupLayout(filtersClassicB);
        filtersClassicB.setLayout(filtersClassicBLayout);
        filtersClassicBLayout.setHorizontalGroup(
            filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersClassicBLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filtersClassicBLayout.createSequentialGroup()
                        .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(excludeEdgeXYB1)
                            .addComponent(excludeEdgeZB1))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(filtersClassicBLayout.createSequentialGroup()
                        .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(filtersClassicBLayout.createSequentialGroup()
                                .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(filtersClassicBLayout.createSequentialGroup()
                                        .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(maxSizeLabB1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(minLabB1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(minSizeB1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(maxSizeB1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(filtersClassicBLayout.createSequentialGroup()
                                        .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(FilterTypeB)
                                            .addComponent(thresholdLabB)
                                            .addGroup(filtersClassicBLayout.createSequentialGroup()
                                                .addGap(4, 4, 4)
                                                .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(FilterButB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(FilterBoxB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(FilterRadLabB)
                                            .addComponent(ThresValB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(filterRadB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addGap(0, 24, Short.MAX_VALUE))
                            .addComponent(threSlidB, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())))
            .addGroup(filtersClassicBLayout.createSequentialGroup()
                .addGap(74, 74, 74)
                .addComponent(segButB, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        filtersClassicBLayout.setVerticalGroup(
            filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersClassicBLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FilterTypeB)
                    .addComponent(FilterRadLabB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FilterBoxB, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterRadB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FilterButB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ThresValB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(thresholdLabB))
                .addGap(0, 0, 0)
                .addComponent(threSlidB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minLabB1)
                    .addComponent(minSizeB1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(filtersClassicBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxSizeB1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxSizeLabB1))
                .addGap(9, 9, 9)
                .addComponent(excludeEdgeXYB1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(excludeEdgeZB1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(segButB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        FilterTabbedPanB.addTab("Classic", filtersClassicB);

        filtersSpotB1.setBackground(new java.awt.Color(225, 225, 225));
        filtersSpotB1.setPreferredSize(new java.awt.Dimension(208, 245));

        segSpotB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        segSpotB.setText("Segment");
        segSpotB.setToolTipText("Perform the segmentation with the values selected above");
        segSpotB.setPreferredSize(new java.awt.Dimension(45, 20));
        segSpotB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segSpotBActionPerformed(evt);
            }
        });

        volminWatershB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volminWatershB.setText("Volume min (pxl)");

        watVolminB.setText(String.valueOf(minSizeB));
        watVolminB.setToolTipText("Exclude objects inferior to this value");
        watVolminB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        watVolminB.setPreferredSize(new java.awt.Dimension(40, 20));

        volmaxWatershB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volmaxWatershB.setText("Volume max (pxl)");

        watVolmaxB.setText(String.valueOf(maxSizeB));
        watVolmaxB.setToolTipText("Exclude objects superior to this value");
        watVolmaxB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        watVolmaxB.setPreferredSize(new java.awt.Dimension(40, 20));

        excludeEdgeXYB2.setBackground(new java.awt.Color(225, 225, 224));
        excludeEdgeXYB2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        excludeEdgeXYB2.setText("Exclude objects on XY edges");
        excludeEdgeXYB2.setToolTipText("If checked, it excludes objects which are touching XY edges");
        excludeEdgeXYB2.setMaximumSize(new java.awt.Dimension(190, 21));
        excludeEdgeXYB2.setMinimumSize(new java.awt.Dimension(190, 21));
        excludeEdgeXYB2.setPreferredSize(new java.awt.Dimension(190, 17));

        questionSpot2.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        questionSpot2.setForeground(new java.awt.Color(0, 0, 255));
        questionSpot2.setText(" ?");
        questionSpot2.setToolTipText("Link to home page");
        questionSpot2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                questionSpot2MouseReleased(evt);
            }
        });

        gaussRad1Blab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        gaussRad1Blab.setText("Radius Max (pxl)");

        gaussRad1B.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        gaussRad1B.setText(String.valueOf(gaussRadBPref));
        gaussRad1B.setToolTipText("Gaussian fit radius (in pxl)");
        gaussRad1B.setPreferredSize(new java.awt.Dimension(46, 20));

        localsd1B.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        localsd1B.setText(String.valueOf(sdBPref));
        localsd1B.setToolTipText("SD Value");
        localsd1B.setPreferredSize(new java.awt.Dimension(34, 20));

        localsd1Blab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        localsd1Blab.setText("sd value");

        maxFinderLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        maxFinderLabB.setText("Max Finder 3D");

        radxyLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        radxyLabB.setText("RadXY");

        radzB.setText(String.valueOf(radZB));
        radzB.setToolTipText("Raduis in Z");
        radzB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        radzB.setMinimumSize(new java.awt.Dimension(10, 20));
        radzB.setPreferredSize(new java.awt.Dimension(24, 20));

        radZLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        radZLabB.setText("RadZ");

        radxyB.setText(String.valueOf(radXYB));
        radxyB.setToolTipText("Raduis in XY");
        radxyB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        radxyB.setMinimumSize(new java.awt.Dimension(10, 20));
        radxyB.setPreferredSize(new java.awt.Dimension(24, 20));

        noiseLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        noiseLabB.setText("Noise");

        noiseB.setText(String.valueOf(noiseBPref));
        noiseB.setToolTipText("Max Finder noise parameter");
        noiseB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        noiseB.setMinimumSize(new java.awt.Dimension(10, 20));
        noiseB.setPreferredSize(new java.awt.Dimension(24, 20));

        maxFinderPreviewB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        maxFinderPreviewB.setText("Apply");
        maxFinderPreviewB.setToolTipText("Perform the max finder 3D with the indicated values");
        maxFinderPreviewB.setPreferredSize(new java.awt.Dimension(45, 20));
        maxFinderPreviewB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxFinderPreviewBActionPerformed(evt);
            }
        });

        seedB2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        seedB2.setText("Seed threshold");

        seedThresB.setText(String.valueOf(seedBPref));
        seedThresB.setToolTipText("Select the pixels from maxfinder that are above this value");
        seedThresB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        seedThresB.setMinimumSize(new java.awt.Dimension(10, 20));
        seedThresB.setPreferredSize(new java.awt.Dimension(40, 20));

        localThrMetB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        localThrMetB.setText("Local threshold method:");

        javax.swing.GroupLayout filtersSpotB1Layout = new javax.swing.GroupLayout(filtersSpotB1);
        filtersSpotB1.setLayout(filtersSpotB1Layout);
        filtersSpotB1Layout.setHorizontalGroup(
            filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersSpotB1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filtersSpotB1Layout.createSequentialGroup()
                        .addComponent(maxFinderLabB, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(questionSpot2))
                    .addGroup(filtersSpotB1Layout.createSequentialGroup()
                        .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(filtersSpotB1Layout.createSequentialGroup()
                                .addGap(53, 53, 53)
                                .addComponent(segSpotB, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(localThrMetB)
                            .addGroup(filtersSpotB1Layout.createSequentialGroup()
                                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(seedB2, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(gaussRad1Blab, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(localsd1Blab, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(volminWatershB, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(volmaxWatershB, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(watVolminB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(localsd1B, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(gaussRad1B, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addComponent(seedThresB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(watVolmaxB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(filtersSpotB1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(noiseLabB)
                                    .addComponent(radxyLabB))
                                .addGap(8, 8, 8)
                                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(radxyB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(noiseB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12)
                                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(filtersSpotB1Layout.createSequentialGroup()
                                        .addComponent(radZLabB)
                                        .addGap(8, 8, 8)
                                        .addComponent(radzB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(maxFinderPreviewB, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(excludeEdgeXYB2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        filtersSpotB1Layout.setVerticalGroup(
            filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, filtersSpotB1Layout.createSequentialGroup()
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(questionSpot2)
                    .addGroup(filtersSpotB1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(maxFinderLabB)))
                .addGap(2, 2, 2)
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radxyLabB)
                    .addComponent(radxyB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radZLabB)
                    .addComponent(radzB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(noiseLabB)
                    .addComponent(noiseB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxFinderPreviewB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(localThrMetB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(seedB2)
                    .addComponent(seedThresB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gaussRad1B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gaussRad1Blab))
                .addGap(0, 0, 0)
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localsd1B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(localsd1Blab))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(watVolminB, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(volminWatershB))
                .addGap(1, 1, 1)
                .addGroup(filtersSpotB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(watVolmaxB, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(volmaxWatershB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludeEdgeXYB2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(segSpotB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        FilterTabbedPanB.addTab("Spot", filtersSpotB1);

        filtersIterB1.setBackground(new java.awt.Color(225, 225, 225));
        filtersIterB1.setPreferredSize(new java.awt.Dimension(208, 245));

        volminIterLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volminIterLabB.setText("Volume min (pxl):");

        volminIterB.setText(String.valueOf(minSizeB));
        volminIterB.setToolTipText("Exclude objects inferior to this value");
        volminIterB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volminIterB.setMinimumSize(new java.awt.Dimension(10, 20));
        volminIterB.setPreferredSize(new java.awt.Dimension(24, 20));

        questionItera2.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        questionItera2.setForeground(new java.awt.Color(0, 0, 255));
        questionItera2.setText(" ?");
        questionItera2.setToolTipText("Link to home page");
        questionItera2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                questionItera2MouseReleased(evt);
            }
        });

        volmaxIterLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volmaxIterLabB.setText("Volume Max (pxl):");

        volmaxIterB1.setText(String.valueOf(maxSizeB));
        volmaxIterB1.setToolTipText("Exclude objects inferior to this value");
        volmaxIterB1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volmaxIterB1.setMinimumSize(new java.awt.Dimension(10, 20));
        volmaxIterB1.setPreferredSize(new java.awt.Dimension(24, 20));

        minThresIterB.setText(String.valueOf(iterThrBPref));
        minThresIterB.setToolTipText("Minimum threshold value");
        minThresIterB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minThresIterB.setMinimumSize(new java.awt.Dimension(10, 20));
        minThresIterB.setPreferredSize(new java.awt.Dimension(24, 20));

        minThresIterLabB1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minThresIterLabB1.setText("min Threshold:");

        valueMethodLabB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        valueMethodLabB.setText("STEP value:");

        valueThresIterB.setText(String.valueOf(iterStepBPref));
        valueThresIterB.setToolTipText("Iterative step value");
        valueThresIterB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        valueThresIterB.setMinimumSize(new java.awt.Dimension(10, 20));
        valueThresIterB.setPreferredSize(new java.awt.Dimension(24, 20));

        segIteraB.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        segIteraB.setText("Segment");
        segIteraB.setToolTipText("Perform the segmentation with the values selected above");
        segIteraB.setPreferredSize(new java.awt.Dimension(45, 20));
        segIteraB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segIteraBActionPerformed(evt);
            }
        });

        excludeEdgeXYB3.setBackground(new java.awt.Color(225, 225, 224));
        excludeEdgeXYB3.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        excludeEdgeXYB3.setText("Exclude objects on XY edges");
        excludeEdgeXYB3.setToolTipText("If checked, it excludes objects which are touching XY edges");
        excludeEdgeXYB3.setMaximumSize(new java.awt.Dimension(190, 21));
        excludeEdgeXYB3.setMinimumSize(new java.awt.Dimension(190, 21));
        excludeEdgeXYB3.setPreferredSize(new java.awt.Dimension(190, 17));

        javax.swing.GroupLayout filtersIterB1Layout = new javax.swing.GroupLayout(filtersIterB1);
        filtersIterB1.setLayout(filtersIterB1Layout);
        filtersIterB1Layout.setHorizontalGroup(
            filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersIterB1Layout.createSequentialGroup()
                .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, filtersIterB1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(questionItera2))
                    .addGroup(filtersIterB1Layout.createSequentialGroup()
                        .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(filtersIterB1Layout.createSequentialGroup()
                                .addGap(62, 62, 62)
                                .addComponent(segIteraB, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(filtersIterB1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(valueMethodLabB)
                                    .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(minThresIterLabB1)
                                            .addComponent(volmaxIterLabB))
                                        .addComponent(volminIterLabB)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(volminIterB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(minThresIterB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(volmaxIterB1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(valueThresIterB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(filtersIterB1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(excludeEdgeXYB3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        filtersIterB1Layout.setVerticalGroup(
            filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersIterB1Layout.createSequentialGroup()
                .addComponent(questionItera2)
                .addGap(0, 0, 0)
                .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(volminIterLabB)
                    .addComponent(volminIterB, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(volmaxIterLabB)
                    .addComponent(volmaxIterB1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minThresIterLabB1)
                    .addComponent(minThresIterB, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(filtersIterB1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(valueMethodLabB)
                    .addComponent(valueThresIterB, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(excludeEdgeXYB3, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(segIteraB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        FilterTabbedPanB.addTab("Iterative", filtersIterB1);

        javax.swing.GroupLayout filtersPanelBLayout = new javax.swing.GroupLayout(filtersPanelB);
        filtersPanelB.setLayout(filtersPanelBLayout);
        filtersPanelBLayout.setHorizontalGroup(
            filtersPanelBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FilterTabbedPanB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        filtersPanelBLayout.setVerticalGroup(
            filtersPanelBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FilterTabbedPanB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        filtersPanelA.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filters image A", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        filtersPanelA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        filtersPanelA.setPreferredSize(new java.awt.Dimension(240, 320));

        FilterTabbedPanelA.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        FilterTabbedPanelA.setPreferredSize(new java.awt.Dimension(228, 292));

        filtersClassicA.setBackground(new java.awt.Color(225, 225, 225));
        filtersClassicA.setPreferredSize(new java.awt.Dimension(208, 245));

        FilterTypeA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        FilterTypeA.setText("Filter type");

        FilterRadLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        FilterRadLabA.setText("Radius");

        filterBoxA.setBackground(new java.awt.Color(245, 245, 245));
        filterBoxA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        filterBoxA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "gaussian", "median", "mean", "none" }));
        filterBoxA.setSelectedIndex(filterBoxAPref);
        filterBoxA.setToolTipText("Indicate which filter you want to use. Select \"None\" if you've already done a segmentation");
        filterBoxA.setPreferredSize(new java.awt.Dimension(80, 24));

        filterRadA.setText("1.0");
        filterRadA.setToolTipText("Radius in XY");
        filterRadA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        filterRadA.setMinimumSize(new java.awt.Dimension(10, 20));
        filterRadA.setPreferredSize(new java.awt.Dimension(24, 20));

        FilterButA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        FilterButA.setText("Valide");
        FilterButA.setToolTipText("Press here to perform the filter");
        FilterButA.setMaximumSize(new java.awt.Dimension(70, 27));
        FilterButA.setMinimumSize(new java.awt.Dimension(70, 27));
        FilterButA.setPreferredSize(new java.awt.Dimension(70, 20));
        FilterButA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterButAActionPerformed(evt);
            }
        });

        thresholdLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        thresholdLabA.setText("Threshold");

        ThresValA.setToolTipText("Actual value of the threshold");
        ThresValA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        ThresValA.setMaximumSize(new java.awt.Dimension(50, 20));
        ThresValA.setMinimumSize(new java.awt.Dimension(40, 20));
        ThresValA.setPreferredSize(new java.awt.Dimension(45, 20));
        ThresValA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ThresValAActionPerformed(evt);
            }
        });

        threSlidA.setBackground(new java.awt.Color(225, 225, 225));
        threSlidA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        threSlidA.setToolTipText("Slider to select the threshold value");
        threSlidA.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        threSlidA.setMaximumSize(new java.awt.Dimension(32767, 15));
        threSlidA.setMinimumSize(new java.awt.Dimension(32, 15));
        threSlidA.setPreferredSize(new java.awt.Dimension(220, 16));
        threSlidA.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                threSlidAStateChanged(evt);
            }
        });

        minLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minLabA.setText("min. Object Size (pxl):");

        minSizeA1.setText(String.valueOf(minSizeA));
        minSizeA1.setToolTipText("Exclude objects inferior to this value");
        minSizeA1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minSizeA1.setPreferredSize(new java.awt.Dimension(40, 20));

        maxSizeA1.setText(String.valueOf(maxSizeA));
        maxSizeA1.setToolTipText("Exclude objects superior to this value");
        maxSizeA1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        maxSizeA1.setPreferredSize(new java.awt.Dimension(40, 20));

        maxSizeLabA1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        maxSizeLabA1.setText("Max. Object Size (pxl):");

        excludeEdgeXYA1.setBackground(new java.awt.Color(225, 225, 224));
        excludeEdgeXYA1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        excludeEdgeXYA1.setText("Exclude objects on XY edges");
        excludeEdgeXYA1.setToolTipText("If checked, it excludes objects which are touching XY edges");

        excludeEdgeZA1.setBackground(new java.awt.Color(225, 225, 224));
        excludeEdgeZA1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        excludeEdgeZA1.setText("Exclude objects on Z edges");
        excludeEdgeZA1.setToolTipText("If checked, it excludes objects which are touching Z edges (first and last slice)");

        segButA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        segButA.setText("Segment");
        segButA.setToolTipText("Perform the segmentation with the values selected above");
        segButA.setPreferredSize(new java.awt.Dimension(85, 20));
        segButA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segButAActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout filtersClassicALayout = new javax.swing.GroupLayout(filtersClassicA);
        filtersClassicA.setLayout(filtersClassicALayout);
        filtersClassicALayout.setHorizontalGroup(
            filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersClassicALayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filtersClassicALayout.createSequentialGroup()
                        .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(filtersClassicALayout.createSequentialGroup()
                                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(FilterTypeA)
                                    .addGroup(filtersClassicALayout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addComponent(thresholdLabA)))
                                .addGap(38, 38, 38))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, filtersClassicALayout.createSequentialGroup()
                                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(FilterButA, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                                    .addComponent(filterBoxA, 0, 1, Short.MAX_VALUE))
                                .addGap(18, 18, 18)))
                        .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(FilterRadLabA)
                            .addComponent(filterRadA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ThresValA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(68, Short.MAX_VALUE))
                    .addGroup(filtersClassicALayout.createSequentialGroup()
                        .addComponent(threSlidA, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 13, Short.MAX_VALUE))))
            .addGroup(filtersClassicALayout.createSequentialGroup()
                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filtersClassicALayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(excludeEdgeXYA1)
                            .addGroup(filtersClassicALayout.createSequentialGroup()
                                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(minLabA, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(maxSizeLabA1, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(minSizeA1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(maxSizeA1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(excludeEdgeZA1)))
                    .addGroup(filtersClassicALayout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(segButA, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        filtersClassicALayout.setVerticalGroup(
            filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersClassicALayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FilterTypeA)
                    .addComponent(FilterRadLabA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterBoxA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterRadA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FilterButA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(thresholdLabA)
                    .addComponent(ThresValA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(threSlidA, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minLabA)
                    .addComponent(minSizeA1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(filtersClassicALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxSizeLabA1)
                    .addComponent(maxSizeA1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(excludeEdgeXYA1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(excludeEdgeZA1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(segButA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        FilterTabbedPanelA.addTab("Classic", filtersClassicA);

        filtersSpotA.setBackground(new java.awt.Color(225, 225, 225));
        filtersSpotA.setPreferredSize(new java.awt.Dimension(208, 245));

        segSpotA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        segSpotA.setText("Segment");
        segSpotA.setToolTipText("Perform the segmentation with the values selected above");
        segSpotA.setPreferredSize(new java.awt.Dimension(45, 20));
        segSpotA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segSpotAActionPerformed(evt);
            }
        });

        volminWatershA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volminWatershA.setText("Volume min (pxl)");

        watVolminA.setText(String.valueOf(minSizeA));
        watVolminA.setToolTipText("Exclude objects inferior to this value");
        watVolminA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        watVolminA.setPreferredSize(new java.awt.Dimension(24, 20));

        volmaxWatershA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volmaxWatershA.setText("Volume max (pxl)");

        watVolmaxA.setText(String.valueOf(maxSizeA));
        watVolmaxA.setToolTipText("Exclude objects superior to this value");
        watVolmaxA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        watVolmaxA.setPreferredSize(new java.awt.Dimension(40, 20));

        excludeEdgeXYA2.setBackground(new java.awt.Color(225, 225, 224));
        excludeEdgeXYA2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        excludeEdgeXYA2.setText("Exclude objects on XY edges");
        excludeEdgeXYA2.setToolTipText("If checked, it excludes objects which are touching XY edges");
        excludeEdgeXYA2.setMaximumSize(new java.awt.Dimension(190, 21));
        excludeEdgeXYA2.setMinimumSize(new java.awt.Dimension(190, 21));
        excludeEdgeXYA2.setPreferredSize(new java.awt.Dimension(190, 17));

        questionSpot1.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        questionSpot1.setForeground(new java.awt.Color(0, 0, 255));
        questionSpot1.setText(" ?");
        questionSpot1.setToolTipText("Link to home page");
        questionSpot1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                questionSpot1MouseReleased(evt);
            }
        });

        gaussRad1Alab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        gaussRad1Alab.setText("Radius Max (pxl)");

        gaussRad1A.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        gaussRad1A.setText(String.valueOf(gaussRadAPref));
        gaussRad1A.setToolTipText("Gaussian fit radius (in pxl)");
        gaussRad1A.setMinimumSize(new java.awt.Dimension(10, 18));
        gaussRad1A.setPreferredSize(new java.awt.Dimension(46, 20));

        localsd1Alab.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        localsd1Alab.setText("sd value");

        localsd1A.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        localsd1A.setText(String.valueOf(sdAPref));
        localsd1A.setToolTipText("SD value");
        localsd1A.setMinimumSize(new java.awt.Dimension(10, 18));
        localsd1A.setPreferredSize(new java.awt.Dimension(40, 20));

        radxyLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        radxyLabA.setText("RadXY");

        radxyA.setText(String.valueOf(radXYA));
        radxyA.setToolTipText("Radius in XY");
        radxyA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        radxyA.setMinimumSize(new java.awt.Dimension(10, 20));
        radxyA.setPreferredSize(new java.awt.Dimension(24, 20));

        radZLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        radZLabA.setText("RadZ");

        radzA.setText(String.valueOf(radZA));
        radzA.setToolTipText("Raduis in Z");
        radzA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        radzA.setMinimumSize(new java.awt.Dimension(10, 20));
        radzA.setPreferredSize(new java.awt.Dimension(24, 20));

        noiseLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        noiseLabA.setText("Noise");

        noiseA.setText(String.valueOf(noiseAPref));
        noiseA.setToolTipText("Max Finder noise parameter");
        noiseA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        noiseA.setMinimumSize(new java.awt.Dimension(10, 20));
        noiseA.setPreferredSize(new java.awt.Dimension(24, 20));

        maxFinderPreviewA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        maxFinderPreviewA.setText("Apply");
        maxFinderPreviewA.setToolTipText("Perform the max finder 3D with the indicated values");
        maxFinderPreviewA.setPreferredSize(new java.awt.Dimension(45, 20));
        maxFinderPreviewA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxFinderPreviewAActionPerformed(evt);
            }
        });

        seedThresA.setText(String.valueOf(seedAPref));
        seedThresA.setToolTipText("Select the pixels from maxfinder that are above this value");
        seedThresA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        seedThresA.setMinimumSize(new java.awt.Dimension(10, 20));
        seedThresA.setPreferredSize(new java.awt.Dimension(40, 20));

        seedA2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        seedA2.setText("Seed threshold");

        maxFinderLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        maxFinderLabA.setText("Max Finder 3D");

        localThrMetA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        localThrMetA.setText("Local threshold method:");

        javax.swing.GroupLayout filtersSpotALayout = new javax.swing.GroupLayout(filtersSpotA);
        filtersSpotA.setLayout(filtersSpotALayout);
        filtersSpotALayout.setHorizontalGroup(
            filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersSpotALayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filtersSpotALayout.createSequentialGroup()
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(seedA2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(gaussRad1Alab, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(localsd1Alab, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(volminWatershA, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(volmaxWatershA, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(gaussRad1A, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(seedThresA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(localsd1A, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(watVolminA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(watVolmaxA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(filtersSpotALayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(noiseLabA)
                            .addComponent(radxyLabA))
                        .addGap(8, 8, 8)
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radxyA, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(noiseA, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(filtersSpotALayout.createSequentialGroup()
                                .addComponent(radZLabA)
                                .addGap(8, 8, 8)
                                .addComponent(radzA, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(maxFinderPreviewA, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(filtersSpotALayout.createSequentialGroup()
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, filtersSpotALayout.createSequentialGroup()
                                .addComponent(maxFinderLabA, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(questionSpot1))
                            .addGroup(filtersSpotALayout.createSequentialGroup()
                                .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(localThrMetA)
                                    .addGroup(filtersSpotALayout.createSequentialGroup()
                                        .addGap(53, 53, 53)
                                        .addComponent(segSpotA, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(excludeEdgeXYA2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        filtersSpotALayout.setVerticalGroup(
            filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersSpotALayout.createSequentialGroup()
                .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(questionSpot1)
                    .addGroup(filtersSpotALayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(maxFinderLabA)))
                .addGap(2, 2, 2)
                .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filtersSpotALayout.createSequentialGroup()
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radxyLabA)
                            .addComponent(radxyA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(noiseLabA)
                            .addComponent(noiseA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(filtersSpotALayout.createSequentialGroup()
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radzA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radZLabA))
                        .addGap(0, 0, 0)
                        .addComponent(maxFinderPreviewA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(localThrMetA)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(seedA2)
                    .addComponent(seedThresA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filtersSpotALayout.createSequentialGroup()
                        .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gaussRad1Alab)
                            .addComponent(gaussRad1A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(localsd1Alab))
                    .addGroup(filtersSpotALayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(localsd1A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(volminWatershA)
                    .addComponent(watVolminA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(filtersSpotALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(watVolmaxA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(volmaxWatershA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludeEdgeXYA2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(segSpotA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        FilterTabbedPanelA.addTab("Spot", filtersSpotA);

        filtersIterA1.setBackground(new java.awt.Color(225, 225, 225));
        filtersIterA1.setPreferredSize(new java.awt.Dimension(208, 245));

        volminIterLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volminIterLabA.setText("Volume min (pxl):");

        volminIterA.setText(String.valueOf(minSizeA));
        volminIterA.setToolTipText("Exclude objects inferior to this value");
        volminIterA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volminIterA.setMinimumSize(new java.awt.Dimension(10, 20));
        volminIterA.setPreferredSize(new java.awt.Dimension(24, 20));

        volmaxIterLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volmaxIterLabA.setText("Volume Max (pxl):");

        minThresIterLabA1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minThresIterLabA1.setText("min Threshold:");

        volmaxIterA.setText(String.valueOf(maxSizeA));
        volmaxIterA.setToolTipText("Exclude objects inferior to this value");
        volmaxIterA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        volmaxIterA.setMinimumSize(new java.awt.Dimension(10, 20));
        volmaxIterA.setPreferredSize(new java.awt.Dimension(24, 20));

        minThresIterA.setText(String.valueOf(iterThrAPref));
        minThresIterA.setToolTipText("Minimum threshold value");
        minThresIterA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        minThresIterA.setMinimumSize(new java.awt.Dimension(10, 20));
        minThresIterA.setPreferredSize(new java.awt.Dimension(24, 20));

        valueMethodLabA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        valueMethodLabA.setText("STEP value:");

        valueThresIterA.setText(String.valueOf(iterStepAPref));
        valueThresIterA.setToolTipText("Iterative step value");
        valueThresIterA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        valueThresIterA.setMinimumSize(new java.awt.Dimension(10, 20));
        valueThresIterA.setPreferredSize(new java.awt.Dimension(24, 20));

        segIteraA.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        segIteraA.setText("Segment");
        segIteraA.setToolTipText("Perform the segmentation with the values selected above");
        segIteraA.setPreferredSize(new java.awt.Dimension(65, 20));
        segIteraA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segIteraAActionPerformed(evt);
            }
        });

        questionItera1.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        questionItera1.setForeground(new java.awt.Color(0, 0, 255));
        questionItera1.setText(" ?");
        questionItera1.setToolTipText("Link to home page");
        questionItera1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                questionItera1MouseReleased(evt);
            }
        });

        excludeEdgeXYA3.setBackground(new java.awt.Color(225, 225, 224));
        excludeEdgeXYA3.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        excludeEdgeXYA3.setText("Exclude objects on XY edges");
        excludeEdgeXYA3.setToolTipText("If checked, it excludes objects which are touching XY edges");
        excludeEdgeXYA3.setMaximumSize(new java.awt.Dimension(190, 21));
        excludeEdgeXYA3.setMinimumSize(new java.awt.Dimension(190, 21));
        excludeEdgeXYA3.setPreferredSize(new java.awt.Dimension(190, 17));

        javax.swing.GroupLayout filtersIterA1Layout = new javax.swing.GroupLayout(filtersIterA1);
        filtersIterA1.setLayout(filtersIterA1Layout);
        filtersIterA1Layout.setHorizontalGroup(
            filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersIterA1Layout.createSequentialGroup()
                .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, filtersIterA1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(questionItera1))
                    .addGroup(filtersIterA1Layout.createSequentialGroup()
                        .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(filtersIterA1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(valueMethodLabA)
                                    .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(minThresIterLabA1)
                                            .addComponent(volmaxIterLabA))
                                        .addComponent(volminIterLabA)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(volminIterA, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(minThresIterA, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(volmaxIterA, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(valueThresIterA, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(filtersIterA1Layout.createSequentialGroup()
                                .addGap(64, 64, 64)
                                .addComponent(segIteraA, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(12, 12, 12))
            .addGroup(filtersIterA1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(excludeEdgeXYA3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        filtersIterA1Layout.setVerticalGroup(
            filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersIterA1Layout.createSequentialGroup()
                .addComponent(questionItera1)
                .addGap(0, 0, 0)
                .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(volminIterLabA)
                    .addComponent(volminIterA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(volmaxIterLabA)
                    .addComponent(volmaxIterA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minThresIterLabA1)
                    .addComponent(minThresIterA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(filtersIterA1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(valueMethodLabA)
                    .addComponent(valueThresIterA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(excludeEdgeXYA3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(segIteraA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        FilterTabbedPanelA.addTab("Iterative", filtersIterA1);

        javax.swing.GroupLayout filtersPanelALayout = new javax.swing.GroupLayout(filtersPanelA);
        filtersPanelA.setLayout(filtersPanelALayout);
        filtersPanelALayout.setHorizontalGroup(
            filtersPanelALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersPanelALayout.createSequentialGroup()
                .addComponent(FilterTabbedPanelA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        filtersPanelALayout.setVerticalGroup(
            filtersPanelALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filtersPanelALayout.createSequentialGroup()
                .addComponent(FilterTabbedPanelA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        goAnalyseButton1.setText("Go to analyse");
        goAnalyseButton1.setToolTipText("");
        goAnalyseButton1.setMaximumSize(new java.awt.Dimension(150, 25));
        goAnalyseButton1.setPreferredSize(new java.awt.Dimension(140, 25));
        goAnalyseButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goAnalyseButton1ActionPerformed(evt);
            }
        });

        about.setText("About");
        about.setToolTipText("Link to the article");
        about.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                aboutMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(goAnalyseButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105)
                        .addComponent(about))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(filtersPanelA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filtersPanelB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(43, 43, 43)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(imageToAnaLab)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(imageBLab)
                                            .addComponent(imageALab))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(imgA, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(imgB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))))
                .addContainerGap())
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(filtersPanelB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filtersPanelA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(goAnalyseButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(about))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void imgAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgAActionPerformed
        
        imgASelect = (String) imgA.getSelectedItem();
        if(title.length>0){
            WindowManager.getFrame(imgASelect).toFront();
            imA = WindowManager.getImage(imgASelect);
            int slice = WindowManager.getImage(imgASelect).getNSlices();
            WindowManager.getImage(imgASelect).setSlice(slice/2);
            cali= imA.getCalibration();
        }
        if(cali.pixelWidth==1){
            boolean calib=false;
            while(calib==false){
                imA.show();
                IJ.showMessage("Wrong Calibration", "Calibrate your first image ("+imA.getTitle()+")");
                IJ.run("Properties...");
                if(imA.getCalibration().pixelWidth!=1){
                    cali=imA.getCalibration();
                    calib=true;
                }
            }
        }
        
        imgA.updateUI();
        if (WindowManager.getImage(imgASelect).getBitDepth()==8){
            highthrA=255;
        }
        
    }//GEN-LAST:event_imgAActionPerformed

    private void imgBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgBActionPerformed
        Manager.testImageSizes(2, false);
        imgBSelect = (String) imgB.getSelectedItem();
        if(title.length>0){
            WindowManager.getFrame(imgBSelect).toFront();
            imB = WindowManager.getImage(imgBSelect);
            int slice = WindowManager.getImage(imgBSelect).getNSlices();
            WindowManager.getImage(imgBSelect).setSlice(slice/2);
        }
        
        if(imB.getCalibration().pixelWidth==1 ){
            boolean calib=false;
            while(calib==false){
                boolean sameCalib = IJ.showMessageWithCancel("Wrong Calibration", "Calibrate the second image ("+imB.getTitle()+") like the first ?");
                if(sameCalib==true){
                    imB.setCalibration(cali);
                    imB.updateAndRepaintWindow();
                    calib=true;
                }
                else{
                    imB.show();
                    IJ.run("Properties...");
                    if(imB.getCalibration().pixelWidth!=1){
                        calib=true;
                        imB.updateAndRepaintWindow();
                    }
                }
            }
        }
        imgB.updateUI();
        if (WindowManager.getImage(imgBSelect).getBitDepth()==8){
            highthrB=255;
        }
    }//GEN-LAST:event_imgBActionPerformed

    private void segButAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segButAActionPerformed
        thr=threSlidA.getValue();
        minSizeA = Integer.parseInt((String) minSizeA1.getText());
        maxSizeA = Integer.parseInt((String) maxSizeA1.getText());
        excludeXYA = excludeEdgeXYA1.isSelected(); 
        exZ = excludeEdgeZA1.isSelected();
        min=minSizeA;max=maxSizeA;exXY=excludeXYA;//macro
        
        segClassicBool=true;imgTitle=imA.getTitle();
        
        if (Macro.getOptions()==null){
            updatePrefs();
        } else{
            macroInterpreter(Macro.getOptions());
        }
        
        if(filterboolA==true){
            
            objPopA = new segment().segClassic(imA2, thr, minSizeA, maxSizeA, exXY, exZ);
            isA2 = segment.createImageObjects(imA, objPopA);
            
            //show
//            imA2.close();   //for updating the image
//            imA2= segment.showImageObjects("labelled-A", isA2, objPopA, cali);
            imA2=segment.createImageObjects("labelled-A", imA, objPopA);
            imA2.show();
            imA2.updateAndDraw();
            
        }
        else{
            IJ.showMessage("Perform filter before segmentation!");
        }
        
        
    }//GEN-LAST:event_segButAActionPerformed

    private void threSlidAStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_threSlidAStateChanged
        int thrA=threSlidA.getValue();
        if(imA2!= null){
            ipA2.setThreshold(thrA, highthrA,ImageProcessor.RED_LUT);
            imA2.updateAndDraw();
            ThresValA.setText(String.valueOf(thrA));
        }
    }//GEN-LAST:event_threSlidAStateChanged

    private void ThresValAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ThresValAActionPerformed
        int thrA = Integer.parseInt(ThresValA.getText());
        if(thrA>highthrA){
            threSlidA.setValue(highthrA);
        }
        else{
            threSlidA.setValue(thrA);
        }
    }//GEN-LAST:event_ThresValAActionPerformed

    private void FilterButAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterButAActionPerformed
        
        imA = WindowManager.getImage(imgASelect);
        imgTitle=imgASelect;
        handleFilter = (String) filterBoxA.getSelectedItem();
        radius=  Float.parseFloat((String) filterRadA.getText());
        
        //Prefs
        if(handleFilter.equals("gaussian")){filterBoxAPref = 0;}
        if(handleFilter.equals("median")){filterBoxAPref = 1;}
        if(handleFilter.equals("mean")){filterBoxAPref = 2;}
        if(handleFilter.equals("none")){filterBoxAPref = 3;}
        
        
        imA2 = segment.filter(imA, handleFilter, radius);
        
        isA = imA.getImageStack();
        isA2 = imA2.getImageStack();
        ipA2 = imA2.getProcessor();

        int zmax=1;
        //SetmaxValue&maxSlice
        for(int i=1; i<=isA2.getSize();i++){
            if(highthrA<(int)isA2.getProcessor(i).getStats().max){
                highthrA=(int)isA2.getProcessor(i).getStats().max;
                zmax=i;
            }
        }
        imA2.setCalibration(cali);
        imA2.show();
        imA2.setSlice(zmax);//updateSlice

        threSlidA.setMaximum(highthrA);
        threSlidA.setValue(imA2.getProcessor().getAutoThreshold());
        filterboolA=true;
        filterBool=true;segClassicBool=false;
        
        if (Macro.getOptions()==null){
            updatePrefs();
        } else{
//            IJ.log(""+Macro.getOptions());
            macroInterpreter(Macro.getOptions());
        }
        
    }//GEN-LAST:event_FilterButAActionPerformed

    private void goAnalyseButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goAnalyseButton1ActionPerformed
        
        updatePrefs();
        Manager.setImage1(imA);
        Manager.setImage2(imB);
        Manager.setImage1seg(imA2);
        Manager.setImage2seg(imB2);
        Manager.setPopulation1(objPopA);
        Manager.setPopulation2(objPopB);
        DiAna_Analyse diaAn = new DiAna_Analyse();
        diaAn.setVisible(true);
        dispose();
        
    }//GEN-LAST:event_goAnalyseButton1ActionPerformed

    private void segIteraAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segIteraAActionPerformed
        
        isA = imA.getImageStack();
        minSizeA = Integer.parseInt(volminIterA.getText());
        maxSizeA = Integer.parseInt(volmaxIterA.getText());
        iterStepAPref=Integer.parseInt(valueThresIterA.getText());
        iterThrAPref=Integer.parseInt(minThresIterA.getText());
        excludeXYA = excludeEdgeXYA3.isSelected();
        //macro
        imgTitle=imA.getTitle();iterBool=true;min=minSizeA;max=maxSizeA;mth=iterThrAPref;step=iterStepAPref;exXY=excludeXYA;
        
        //process
        objPopA= new segment().segIter(imA, minSizeA, maxSizeA, iterStepAPref, iterThrAPref, excludeXYA);
        //show image
//        isA2 = segment.createImageObjects(imA, objPopA);
//        imA2= segment.showImageObjects("labelled-A", isA2, objPopA, cali);
        imA2=segment.createImageObjects("labelled-A", imA, objPopA);
        imA2.show();
        imA2.updateAndDraw();
        
        
        if (Macro.getOptions()==null){
            updatePrefs();
        } else{
            macroInterpreter(Macro.getOptions());
        }
    }//GEN-LAST:event_segIteraAActionPerformed

    private void questionItera1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_questionItera1MouseReleased
        try{
            URI uri = URI.create("http://imagejdocu.tudor.lu/doku.php?id=plugin:analysis:distance_analysis_diana_2d_3d_:start");
            Desktop.getDesktop().browse(uri);
        }
        catch (IOException ex){
            IJ.error("Open failed");
        }
    }//GEN-LAST:event_questionItera1MouseReleased

    private void imgAMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imgAMouseClicked
        title=Manager.testImageSizes(1, false);
        imgA.setModel(new DefaultComboBoxModel(title) );
        imgA.setSelectedIndex(0);
        imgA.updateUI();
        
    }//GEN-LAST:event_imgAMouseClicked

    private void imgBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imgBMouseClicked
        title=Manager.testImageSizes(1, false);
        imgB.setModel(new DefaultComboBoxModel(title) );
        imgB.updateUI();
    }//GEN-LAST:event_imgBMouseClicked

    private void segIteraBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segIteraBActionPerformed
        //TrackThreshold
        isB = imB.getImageStack();
        minSizeB=Integer.parseInt(volminIterB.getText());
        maxSizeB=Integer.parseInt(volmaxIterB1.getText());
        iterStepBPref=Integer.parseInt(valueThresIterB.getText());
        iterThrBPref=Integer.parseInt(minThresIterB.getText());
        excludeXYB = excludeEdgeXYB3.isSelected();
        //macro
        imgTitle=imB.getTitle();iterBool=true;min=minSizeB;max=maxSizeB;mth=iterThrBPref;step=iterStepBPref;exXY=excludeXYB;

        objPopB= new segment().segIter(imB, minSizeB, maxSizeB, iterStepBPref, iterThrBPref, excludeXYB);
        imB2=segment.createImageObjects("labelled-B", imB, objPopB);
        imB2.show();
        imB2.updateAndDraw();
        
        if (Macro.getOptions()==null){
            updatePrefs();
        } else{
            macroInterpreter(Macro.getOptions());
        }
    }//GEN-LAST:event_segIteraBActionPerformed

    private void questionItera2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_questionItera2MouseReleased
        try{
            URI uri = URI.create("http://imagejdocu.tudor.lu/doku.php?id=plugin:analysis:distance_analysis_diana_2d_3d_:start");
            Desktop.getDesktop().browse(uri);
        }
        catch (IOException ex){
            IJ.error("Open failed");
        }
    }//GEN-LAST:event_questionItera2MouseReleased

    private void maxFinderPreviewBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxFinderPreviewBActionPerformed
        seed3DImageB = segment.ImagePeaks(imB, Float.parseFloat((String)radxyB.getText()), Float.parseFloat((String)radzB.getText()), Float.parseFloat((String)noiseB.getText()));
        seed3DImageB.show();
    }//GEN-LAST:event_maxFinderPreviewBActionPerformed

    private void questionSpot2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_questionSpot2MouseReleased
        try{
            URI uri = URI.create("http://imagejdocu.tudor.lu/doku.php?id=plugin:analysis:distance_analysis_diana_2d_3d_:start");
            Desktop.getDesktop().browse(uri);
        }
        catch (IOException ex){
            IJ.error("Open failed");
        }
    }//GEN-LAST:event_questionSpot2MouseReleased

    private void segSpotBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segSpotBActionPerformed
        imgTitle=imB.getTitle();
        isB = imB.getImageStack();
        radXYB=Float.parseFloat((String)radxyB.getText());
        radZB=Float.parseFloat((String)radzB.getText());
        noiseBPref=Float.parseFloat((String)noiseB.getText());
        seedBPref=Integer.parseInt((String)seedThresB.getText());
        gaussRadBPref=Integer.parseInt((String)gaussRad1B.getText());
        sdBPref=Float.parseFloat((String)localsd1B.getText());
        minSizeB=Integer.parseInt(watVolminB.getText());
        maxSizeB=Integer.parseInt(watVolmaxB.getText());
        excludeXYB = excludeEdgeXYB2.isSelected();
        //macro
        radius=radXYB;rZ=radZB;noise=noiseBPref;seed=seedBPref;gauss=gaussRadBPref;sd=sdBPref;min=minSizeB;max=maxSizeB;exXY=excludeXYB;
        spotBool=true;
        
        //maxFinder3D
        if(seed3DImageB==null){
            seed3DImageB = segment.ImagePeaks(imB, radXYB, radZB, noiseBPref);
//            seed3DImageB.show();
        }
        //process
        ImageHandler iHB = ImageHandler.wrap(imB.duplicate());
        objPopB = new segment().segSpot(iHB, seed3DImageB, seedBPref, gaussRadBPref, sdBPref, minSizeB, maxSizeB, excludeXYB);

        //show
//        isB2 = segment.createImageObjects(imB, objPopB);
//        imB2= segment.showImageObjects("labelled-B", isB2, objPopB, cali);
        
        imB2=segment.createImageObjects("labelled-B", imB, objPopB);
        imB2.show();
        imB2.updateAndDraw();
        
        updatePrefs();
    }//GEN-LAST:event_segSpotBActionPerformed

    private void segButBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segButBActionPerformed

        if(filterboolB==true){
            int thrB=threSlidB.getValue();
            minSizeB = Integer.parseInt((String) minSizeB1.getText());
            maxSizeB =  Integer.parseInt((String) maxSizeB1.getText());
            excludeXYB = excludeEdgeXYB1.isSelected(); 
            excludeZ = excludeEdgeZB1.isSelected();
            imgTitle=imB.getTitle();min=minSizeB;max=maxSizeB;exXY=excludeXYB;//macro

            objPopB = new segment().segClassic(imB2, thrB, minSizeB, maxSizeB, exXY, exZ);
//            isB2 = segment.createImageObjects(imB, objPopB);

            //show
//            imB2.close();   //for updating the image
//            imB2= segment.showImageObjects("labelled-B", isB2, objPopB, cali);
            imB2=segment.createImageObjects("labelled-B", imB, objPopB);
            imB2.show();
            imB2.updateAndDraw();
            
        }
        else{
            IJ.showMessage("Perform filter before segmentation!");
        }
        updatePrefs();
    }//GEN-LAST:event_segButBActionPerformed

    private void threSlidBStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_threSlidBStateChanged
        int thrB=threSlidB.getValue();
        if(imB2!= null){
            ipB2.setThreshold(thrB, highthrB,ImageProcessor.RED_LUT);
            imB2.updateAndDraw();
            ThresValB.setText(String.valueOf(thrB));
        }
    }//GEN-LAST:event_threSlidBStateChanged

    private void ThresValBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ThresValBActionPerformed
        int thrB = Integer.parseInt(ThresValB.getText());
        if(thrB>highthrB){
            threSlidB.setValue(highthrB);
        }
        else{
            threSlidB.setValue(thrB);
        }
    }//GEN-LAST:event_ThresValBActionPerformed

    private void FilterButBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterButBActionPerformed
        
        imB = WindowManager.getImage(imgBSelect);
        imgTitle=imgBSelect;
        handleFilter = (String) FilterBoxB.getSelectedItem();
        radius=  Float.parseFloat((String) filterRadB.getText());

        //Prefs
        if(handleFilter.equals("gaussian")){filterBoxBPref = 0;}
        if(handleFilter.equals("median")){filterBoxBPref = 1;}
        if(handleFilter.equals("mean")){filterBoxBPref = 2;}
        if(handleFilter.equals("none")){filterBoxBPref = 3;}
        
        imB2 = segment.filter(imB, handleFilter, radius);
        
        isB = imB.getImageStack();
        isB2 = imB2.getImageStack();
        ipB2 = imB2.getProcessor();
        int zmax=1;
        for(int i=1; i<=isB2.getSize();i++){//setmaxValue
            if(highthrB<(int)isB2.getProcessor(i).getStatistics().max){
                highthrB=(int)isB2.getProcessor(i).getStatistics().max;
                zmax=i;
            }
        }
        imB2.setCalibration(cali);
        imB2.show();
        imB2.setSlice(zmax);//updateSlice
        
        threSlidB.setMaximum(highthrB);
        threSlidB.setValue(imB2.getProcessor().getAutoThreshold());
        
        filterboolB=true;segClassicBool=false;
        
        if (Macro.getOptions()==null){
            updatePrefs();
        } else{
            macroInterpreter(Macro.getOptions());
        }
    }//GEN-LAST:event_FilterButBActionPerformed

    private void aboutMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMouseReleased
        try{
            URI uri = URI.create("http://www.sciencedirect.com/science/article/pii/S1046202316304649");
            Desktop.getDesktop().browse(uri);
        }
        catch (IOException ex){
            IJ.error("Open failed");
        }
    }//GEN-LAST:event_aboutMouseReleased

    private void maxFinderPreviewAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxFinderPreviewAActionPerformed
        seed3DImageA = segment.ImagePeaks(imA, Float.parseFloat((String)radxyA.getText()), Float.parseFloat((String)radzA.getText()), Float.parseFloat((String)noiseA.getText()));
        seed3DImageA.show();
    }//GEN-LAST:event_maxFinderPreviewAActionPerformed

    private void questionSpot1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_questionSpot1MouseReleased
        try{
            URI uri = URI.create("http://imagejdocu.tudor.lu/doku.php?id=plugin:analysis:distance_analysis_diana_2d_3d_:start");
            Desktop.getDesktop().browse(uri);
        }
        catch (IOException ex){
            IJ.error("Open failed");
        }
    }//GEN-LAST:event_questionSpot1MouseReleased

    private void segSpotAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segSpotAActionPerformed
        imgTitle=imA.getTitle();
        isA=imA.getImageStack();
        radXYA=Float.parseFloat((String)radxyA.getText());
        radZA=Float.parseFloat((String)radzA.getText());
        noiseAPref=Float.parseFloat((String)noiseA.getText());
        seedAPref=Integer.parseInt((String)seedThresA.getText());
        gaussRadAPref=Integer.parseInt((String)gaussRad1A.getText());
        sdAPref=Float.parseFloat((String)localsd1A.getText());
        minSizeA=Integer.parseInt((String)watVolminA.getText());
        maxSizeA=Integer.parseInt((String)watVolmaxA.getText());
        excludeXYA = excludeEdgeXYA2.isSelected();
        //macro
        radius=radXYA;rZ=radZA;noise=noiseAPref;seed=seedAPref;gauss=gaussRadAPref;sd=sdAPref;min=minSizeA;max=maxSizeA;exXY=excludeXYA;
        spotBool=true;
        
        //maxFinder3D
        if(seed3DImageA==null){
            seed3DImageA = segment.ImagePeaks(imA, radXYA, radZA, noiseAPref);
//            seed3DImageA.show();
        }
      //process
      ImageHandler iHA = ImageHandler.wrap(imA.duplicate());
      objPopA = new segment().segSpot(iHA, seed3DImageA, seedAPref, gaussRadAPref, sdAPref, minSizeA, maxSizeA, excludeXYA);
      
      //show
      //isA2 = segment.createImageObjects(imA, objPopA);
      imA2 = segment.createImageObjects("labelled-A", imA, objPopA);
      imA2.show();
      imA2.updateAndDraw();
//      imA2= segment.showImageObjects("labelled-A", isA2, objPopA, cali);

        if (Macro.getOptions()==null){
            updatePrefs();
        } else{
//            IJ.log(""+Macro.getOptions());
            macroInterpreter(Macro.getOptions());
        }
    }//GEN-LAST:event_segSpotAActionPerformed
       
    
    
    private void updatePrefs(){
        //A
        Prefs.set("Diana_filterBoxA.int", filterBoxAPref);
        Prefs.set("Diana_minSizeA1.int", minSizeA);
        Prefs.set("Diana_maxSizeA1.int", maxSizeA);
        Prefs.set("Diana_radXYA.int", radXYA);
        Prefs.set("Diana_radZA.int", radZA);
        Prefs.set("Diana_noiseA.int", noiseAPref);
        Prefs.set("Diana_seedThresA.int", seedAPref);
        Prefs.set("Diana_gaussRadA.int", gaussRadAPref);
        Prefs.set("Diana_sdA.int", sdAPref);
        Prefs.set("Diana_iterThrA.int", iterThrAPref);
        Prefs.set("Diana_iterStepA.int", iterStepAPref);
        Prefs.set("Diana_exludeEdgeXYA1.boolean", excludeXYA);
        //B
        Prefs.set("Diana_filterBoxB.int", filterBoxBPref);
        Prefs.set("Diana_minSizeB1.int", minSizeB);
        Prefs.set("Diana_maxSizeB1.int", maxSizeB);
        Prefs.set("Diana_radXYB.int", radXYB);
        Prefs.set("Diana_radZB.int", radZB);
        Prefs.set("Diana_noiseB.int", noiseBPref);
        Prefs.set("Diana_seedThresB.int", seedBPref);
        Prefs.set("Diana_gaussRadB.int", gaussRadBPref);
        Prefs.set("Diana_sdB.int", sdBPref);
        Prefs.set("Diana_iterThrB.int", iterThrBPref);
        Prefs.set("Diana_iterStepB.int", iterStepBPref);
        Prefs.set("Diana_exludeEdgeXYB1.boolean", excludeXYB);
        if (Recorder.record) {
            macroGenerator(imgTitle);
        }
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    
    public static void main(String args[]) {
        /* Set the Metal look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Metal (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Diana_SegmentGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Diana_SegmentGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Diana_SegmentGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Diana_SegmentGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (Macro.getOptions()==null){
                    new Diana_SegmentGui().setVisible(true);
                }
                
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox FilterBoxB;
    private javax.swing.JButton FilterButA;
    private javax.swing.JButton FilterButB;
    private javax.swing.JLabel FilterRadLabA;
    private javax.swing.JLabel FilterRadLabB;
    private javax.swing.JTabbedPane FilterTabbedPanB;
    private javax.swing.JTabbedPane FilterTabbedPanelA;
    private javax.swing.JLabel FilterTypeA;
    private javax.swing.JLabel FilterTypeB;
    private javax.swing.JFormattedTextField ThresValA;
    private javax.swing.JFormattedTextField ThresValB;
    private javax.swing.JButton about;
    private javax.swing.JCheckBox excludeEdgeXYA1;
    private javax.swing.JCheckBox excludeEdgeXYA2;
    private javax.swing.JCheckBox excludeEdgeXYA3;
    private javax.swing.JCheckBox excludeEdgeXYB1;
    private javax.swing.JCheckBox excludeEdgeXYB2;
    private javax.swing.JCheckBox excludeEdgeXYB3;
    private javax.swing.JCheckBox excludeEdgeZA1;
    private javax.swing.JCheckBox excludeEdgeZB1;
    private javax.swing.JComboBox filterBoxA;
    private javax.swing.JFormattedTextField filterRadA;
    private javax.swing.JFormattedTextField filterRadB;
    private javax.swing.JPanel filtersClassicA;
    private javax.swing.JPanel filtersClassicB;
    private javax.swing.JPanel filtersIterA1;
    private javax.swing.JPanel filtersIterB1;
    private javax.swing.JPanel filtersPanelA;
    private javax.swing.JPanel filtersPanelB;
    private javax.swing.JPanel filtersSpotA;
    private javax.swing.JPanel filtersSpotB1;
    private javax.swing.JTextField gaussRad1A;
    private javax.swing.JLabel gaussRad1Alab;
    private javax.swing.JTextField gaussRad1B;
    private javax.swing.JLabel gaussRad1Blab;
    private javax.swing.JButton goAnalyseButton1;
    private javax.swing.JLabel imageALab;
    private javax.swing.JLabel imageBLab;
    private javax.swing.JLabel imageToAnaLab;
    private javax.swing.JComboBox imgA;
    private javax.swing.JComboBox imgB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel localThrMetA;
    private javax.swing.JLabel localThrMetB;
    private javax.swing.JTextField localsd1A;
    private javax.swing.JLabel localsd1Alab;
    private javax.swing.JTextField localsd1B;
    private javax.swing.JLabel localsd1Blab;
    private javax.swing.JLabel maxFinderLabA;
    private javax.swing.JLabel maxFinderLabB;
    private javax.swing.JButton maxFinderPreviewA;
    private javax.swing.JButton maxFinderPreviewB;
    private javax.swing.JFormattedTextField maxSizeA1;
    private javax.swing.JFormattedTextField maxSizeB1;
    private javax.swing.JLabel maxSizeLabA1;
    private javax.swing.JLabel maxSizeLabB1;
    private javax.swing.JLabel minLabA;
    private javax.swing.JLabel minLabB1;
    private javax.swing.JFormattedTextField minSizeA1;
    private javax.swing.JFormattedTextField minSizeB1;
    private javax.swing.JFormattedTextField minThresIterA;
    private javax.swing.JFormattedTextField minThresIterB;
    private javax.swing.JLabel minThresIterLabA1;
    private javax.swing.JLabel minThresIterLabB1;
    private javax.swing.JFormattedTextField noiseA;
    private javax.swing.JFormattedTextField noiseB;
    private javax.swing.JLabel noiseLabA;
    private javax.swing.JLabel noiseLabB;
    private javax.swing.JLabel questionItera1;
    private javax.swing.JLabel questionItera2;
    private javax.swing.JLabel questionSpot1;
    private javax.swing.JLabel questionSpot2;
    private javax.swing.JLabel radZLabA;
    private javax.swing.JLabel radZLabB;
    private javax.swing.JFormattedTextField radxyA;
    private javax.swing.JFormattedTextField radxyB;
    private javax.swing.JLabel radxyLabA;
    private javax.swing.JLabel radxyLabB;
    private javax.swing.JFormattedTextField radzA;
    private javax.swing.JFormattedTextField radzB;
    private javax.swing.JLabel seedA2;
    private javax.swing.JLabel seedB2;
    private javax.swing.JFormattedTextField seedThresA;
    private javax.swing.JFormattedTextField seedThresB;
    private javax.swing.JButton segButA;
    private javax.swing.JButton segButB;
    private javax.swing.JButton segIteraA;
    private javax.swing.JButton segIteraB;
    private javax.swing.JButton segSpotA;
    private javax.swing.JButton segSpotB;
    private javax.swing.JSlider threSlidA;
    private javax.swing.JSlider threSlidB;
    private javax.swing.JLabel thresholdLabA;
    private javax.swing.JLabel thresholdLabB;
    private javax.swing.JLabel valueMethodLabA;
    private javax.swing.JLabel valueMethodLabB;
    private javax.swing.JFormattedTextField valueThresIterA;
    private javax.swing.JFormattedTextField valueThresIterB;
    private javax.swing.JFormattedTextField volmaxIterA;
    private javax.swing.JFormattedTextField volmaxIterB1;
    private javax.swing.JLabel volmaxIterLabA;
    private javax.swing.JLabel volmaxIterLabB;
    private javax.swing.JLabel volmaxWatershA;
    private javax.swing.JLabel volmaxWatershB;
    private javax.swing.JFormattedTextField volminIterA;
    private javax.swing.JFormattedTextField volminIterB;
    private javax.swing.JLabel volminIterLabA;
    private javax.swing.JLabel volminIterLabB;
    private javax.swing.JLabel volminWatershA;
    private javax.swing.JLabel volminWatershB;
    private javax.swing.JFormattedTextField watVolmaxA;
    private javax.swing.JFormattedTextField watVolmaxB;
    private javax.swing.JFormattedTextField watVolminA;
    private javax.swing.JFormattedTextField watVolminB;
    // End of variables declaration//GEN-END:variables

    
}
