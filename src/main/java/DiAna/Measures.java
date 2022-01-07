/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DiAna;

import ij.IJ;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.spatial.analysis.SpatialStatistics;
import mcib3d.spatial.descriptors.SpatialDescriptor;
import mcib3d.spatial.sampler.SpatialShuffle;

/**
 *
 * @author jeff
 */
public class Measures {
      
    private ImageHandler iHA= null;
    private ImageHandler iHB= null;
    private ImageHandler iHC= null;
    private Objects3DPopulation popA=null;
    private Objects3DPopulation popB=null;
    private Objects3DPopulation touchA=null;
    private Objects3DPopulation touchB=null;
    
    /**
     * initialize parameters
     * @param ihA labelled image of popA
     * @param ihB labelled image of popB
     * @param popA
     * @param popB 
     */
    public Measures(ImageHandler ihA, ImageHandler ihB, Objects3DPopulation popA, Objects3DPopulation popB){
        this.iHA = ihA;
        this.iHB = ihB;
        this.popA = popA;
        this.popB = popB;
        this.iHC = ImageHandler.newBlankImageHandler("coloc", ihA);
        touchA = touchingPop(popB, popA, iHA);
        touchB = touchingPop(popA, popB, iHB);
    }
    
    public static Comparator<Object3D>objComparator = new Comparator<Object3D>() {
        @Override
        public int compare(Object3D o1, Object3D o2) {
           int v1 = o1.getValue();
           int v2 = o2.getValue();
           return v1-v2;
        }
    };
    
    
    /**
   * Select only the objects 2 which are touching when population 1 and 2 are in contact.
   * @param objPop1 
   * @param objPop2 
   * @param iHand2 ImageHandler corresponding to the objPop2
   * @return 
   */
    public static Objects3DPopulation touchingPop (Objects3DPopulation objPop1, Objects3DPopulation objPop2, ImageHandler iHand2){
        ArrayList<Object3D> touch = new ArrayList<> ();
        for(int i=0; i<objPop1.getNbObjects();i++){
            Object3D ob1 = objPop1.getObject(i);
            //ArrayList<Voxel3D> vox = ob1.listVoxels(iHand2);//ih  //old version
            LinkedList<Voxel3D> vox = ob1.listVoxels(iHand2);//ih // THOMAS new mcib3d v3.94
            for (Voxel3D voxel3D : vox) {
                int val = (int) voxel3D.getValue();
                Object3D object3D = objPop2.getObjectByValue(val);
                if ((val > 0) && (!touch.contains(object3D))) {
                    touch.add(object3D);
                }
            }
            
//            for(int j=0; j<vox.size(); j++){
//                int val=(int)vox.get(j).getValue();
//                if(val!=0 && touch.contains(objPop2.getObject(val-1))==false){
//                    touch.add(objPop2.getObject(val-1));
//                }
//            }
        }
        touch.sort(objComparator);
        Objects3DPopulation pop = new Objects3DPopulation(touch);
        return pop;
    }
    
    /**
     * Perform the coloc
     * @param colocA Show purcentage of object-colocalization relatively with Volume of the object A
     * @param colocB Show purcentage of object-colocalization relatively with Volume of the object B
     * @param colocAB Show purcentage of object-colocalization relatively with Volume of the objects A and B
     * @param colocCC Show distance Center-Center between object A and B
     * @param colocCE Show distance Center-Edge between object A and B
     * @param colocEC Show distance Center-Center between object A and B
     * @param surf Show surface in contact between the two objects
     * @param distSurf_max Limite of the distance in contact
     */
    public void computeColoc(boolean colocA, boolean colocB, boolean colocAB, boolean colocCC, boolean colocCE, boolean colocEC, boolean surf, double distSurf_max){
//        touchA=touchingPop(popB, popA, iHA);
//        touchB=touchingPop(popA, popB, iHB);
//        IJ.log("measurTouchA="+touchA.getNbObjects()+"measurTouchB="+touchB.getNbObjects());
        ArrayList<Object3D> bTouch = new ArrayList<> ();
        ArrayList<Object3D> aTouch = new ArrayList<> ();
        ResultsTable rtColoc = new ResultsTable();
        int a = 1;
        
        //Calendar cal = Calendar.getInstance();
        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        for(int i=0; i<touchA.getNbObjects();i++){          //scan of the objects touchPopA
            Object3D obA = touchA.getObject(i);
//            ArrayList<Voxel3D> vox = obA.listVoxels(iHB);
            LinkedList<Voxel3D> vox = obA.listVoxels(iHB);  // THOMAS new mcib3d v3.94
            //for(int j=0; j<vox.size(); j++){  //before 3.94              
            for(Voxel3D voxel : vox){ //scan voxels values in object A
                //int val=(int)vox.get(j).getValue();       //before 3.94 
                int val = (int) voxel.getValue();
                //if(val>0 && bTouch.contains(popB.getObject(val-1))==false){   //before 3.94
                if(val>0 && bTouch.contains(popB.getObjectByValue(val))==false){
                    Object3D obB = popB.getObjectByValue(val);
                    bTouch.add(obB);
                    aTouch.add(obA);
                    Object3DVoxels obVox = new Object3DVoxels();
                    obVox.addVoxelsIntersection(obA, obB);
                    obVox.draw(iHC, a);
                    rtColoc.incrementCounter();
                    rtColoc.addLabel("objA"+((int) obA.getPixMeanValue(iHA))+"_objB"+(val));
//                    obA.inside(obB.getCenterAsPoint());//TODO
                    if(colocA==true){rtColoc.addValue("ColocFromAvolume", (100*((double)obVox.getVolumePixels())/(double)obA.getVolumePixels()));}
                    if(colocB==true){rtColoc.addValue("ColocFromBvolume", (100*((double)obVox.getVolumePixels())/(double)obB.getVolumePixels()));}
                    if(colocAB==true){rtColoc.addValue("ColocFromABvolume", (100*((double)obVox.getVolumePixels())/((double)obA.getVolumePixels()+(double)obB.getVolumePixels())));}
                    if(colocCC==true){rtColoc.addValue("Dist CenterA-CenterB", obA.distCenterUnit(obB));}
//                    if(colocEE==true){rtColoc.addValue("Dist EdgeA-EdgeB", obA.distBorderUnit(obB));}
                    if(colocCE==true){rtColoc.addValue("Dist min CenterA-EdgeB", obA.distCenterBorderUnit(obB));}
                    if(colocEC==true){rtColoc.addValue("Dist min EdgeA-CenterB", obB.distCenterBorderUnit(obA));}
                    if(surf==true){
                        int[] surface=obA.surfaceContact(obB, distSurf_max);
                        rtColoc.addValue("Surface contact", (surface[0]+surface[1]));
                    }
                    a++;
                }
                if(val>0 && aTouch.contains(obA)==false){   // if ObjB is already detected with another objectA
                    Object3D obB = popB.getObjectByValue(val);
                    bTouch.add(obB);
                    aTouch.add(obA);
                    Object3DVoxels obVox = new Object3DVoxels();
                    obVox.addVoxelsIntersection(obA, obB);
                    obVox.draw(iHC, a);
                    rtColoc.incrementCounter();
                    rtColoc.addLabel("objA"+((int) obA.getPixMeanValue(iHA))+"_objB"+val);
                    if(colocA==true){rtColoc.addValue("ColocFromAvolume", (100*((double)obVox.getVolumePixels())/(double)obA.getVolumePixels()));}
                    if(colocB==true){rtColoc.addValue("ColocFromBvolume", (100*((double)obVox.getVolumePixels())/(double)obB.getVolumePixels()));}
                    if(colocAB==true){rtColoc.addValue("ColocFromABvolume", (100*((double)obVox.getVolumePixels())/((double)obA.getVolumePixels()+(double)obB.getVolumePixels())));}
                    if(colocCC==true){rtColoc.addValue("Dist CenterA-CenterB", obA.distCenterUnit(obB));}
//                    if(colocEE==true){rtColoc.addValue("Dist EdgeA-EdgeB", obA.distBorderUnit(obB));}
                    if(colocCE==true){rtColoc.addValue("Dist min CenterA-EdgeB", obA.distCenterBorderUnit(obB));}
                    if(colocEC==true){rtColoc.addValue("Dist min EdgeA-CenterB", obB.distCenterBorderUnit(obA));}
                    if(surf==true){
                        int[] surface=obA.surfaceContact(obB, distSurf_max);
                        rtColoc.addValue("Surface contact", (surface[0]+surface[1]));
                    }
                    a++;
                }
            }
        }
        
        IJ.log("number of objects in image A = "+popA.getNbObjects());
        IJ.log("number of objects in image B = "+popB.getNbObjects());
        IJ.log("number of touching objects in image A = "+touchA.getNbObjects());
        IJ.log("number of touching objects in image B = "+touchB.getNbObjects());
            
        
        rtColoc.show("ColocResults");                  
    }
    
    /**
     * Return the ROIs in a list
     * @param pop Objects3DPopulation on which we return ROIs
     * @param prefix Prefix of the name of each Object
     * @return 
     */
    public static Roi[] arrayRoi (Objects3DPopulation pop, String prefix){
        Roi[] array = new Roi[pop.getNbObjects()];
        for(int i=0; i<pop.getNbObjects(); i++){
              Roi roiA = new Roi(pop.getObject(i).getXmin(), pop.getObject(i).getYmin(), pop.getObject(i).getXmax() - pop.getObject(i).getXmin(), pop.getObject(i).getYmax() - pop.getObject(i).getYmin());
              roiA.setName(prefix + pop.getObject(i).getValue());
              roiA.setPosition((int)(pop.getObject(i).getZmax()+pop.getObject(i).getZmin())/2); //z
              array[i]=roiA;
        }
        return array;
    }
    
    /**
     * Compute the distances between objects
     * @param kClosest Number of objects in B closed to the object in A to analyse
     * @param distCC Show distance Center-Center
     * @param distEE Show distance Edge-Edge
     * @param distCE Show distance Center-Edge
     * @param distEC Show distance Edge-Center
     * @param surf Show surface in contact between the two objects
     * @param distSurf_max Limite of the distance in contact
     */
    public void  ComputeAdjacency(int kClosest, boolean distCC, boolean distEE, boolean distCE, boolean distEC, boolean surf, double distSurf_max){
        
        ResultsTable rtAdjacA = new ResultsTable();
        if(kClosest>popB.getNbObjects()){
            kClosest = popB.getNbObjects();
        }
        for(int i=0; i<popA.getNbObjects();i++){
            Object3D obA = popA.getObject(i);
            Object3D obB = popB.closestCenter(obA, 0.010);
            
            if(kClosest==1){
                rtAdjacA.incrementCounter();
                rtAdjacA.addLabel("ObjA"+((int) obA.getPixMeanValue(iHA))+"_ObjB"+ ((int) obB.getPixMeanValue(iHB)));
                if(distCC==true){rtAdjacA.addValue("Dist CenterA-CenterB", obA.distCenterUnit(obB));}
                if(distEE==true){rtAdjacA.addValue("Dist min EdgeA-EdgeB", obA.distBorderUnit(obB));}
                if(distCE==true){rtAdjacA.addValue("Dist min CenterA-EdgeB", obA.distCenterBorderUnit(obB));}
                if(distEC==true){rtAdjacA.addValue("Dist min EdgeA-CenterB", obB.distCenterBorderUnit(obA));}
                if(surf==true){
                    int[] surface=obA.surfaceContact(obB, distSurf_max);
                    rtAdjacA.addValue("Surface contact", (surface[0]+surface[1]));
                }
            }
            if(kClosest>1){
                for(int j=1; j<=kClosest; j++){
                    rtAdjacA.incrementCounter();
                    Object3D obBk=popB.kClosestCenter(obA, j, false);
                    rtAdjacA.addLabel("ObjA"+((int)obA.getPixMeanValue(iHA))+"_ObjB"+((int)obBk.getPixMeanValue(iHB)));
                    if(distCC==true){rtAdjacA.addValue("Dist CenterA-CenterB", obA.distCenterUnit(obBk));}
                    if(distEE==true){rtAdjacA.addValue("Dist min EdgeA-EdgeB", obA.distBorderUnit(obBk));}
                    if(distCE==true){rtAdjacA.addValue("Dist min CenterA-EdgeB", obA.distCenterBorderUnit(obBk));}
                    if(distEC==true){rtAdjacA.addValue("Dist min EdgeA-CenterB", obBk.distCenterBorderUnit(obA));}
                    if(surf==true){
                        int[] surface=obA.surfaceContact(obBk, distSurf_max);
                        rtAdjacA.addValue("Surface contact", (surface[0]+surface[1]));
                    }
                }
            }
        }
        rtAdjacA.show("AdjacencyResults");
        
    }
    
    /**
     * Return the resultsTable of the measures of each object in the Objects3DPopulation related to the raw image
     * @param rawimage ImageHandler of the raw image
     * @param pop Objects3DPopulation to measure
     * @param prefix Prefix of the name of the objects shown in the table
     * @param volume Show the volume (unit & pixel)
     * @param mean Show the mean value
     * @param surface Show the surface area
     * @param minMax Show the min & max values
     * @param stdDev Show the standard deviation grey value
     * @param centro Show the center localization (X, Y, Z)
     * @param mass Show the center of mass localization (X, Y, Z)
     * @param feret Show the feret value
     * @return 
     */
    public static ResultsTable measureResult(ImageHandler rawimage, Objects3DPopulation pop, String prefix, boolean volume, boolean  mean, boolean  surface, boolean minMax, boolean  stdDev, boolean centro, boolean mass, boolean feret, boolean  intDen){
        ResultsTable rt = new ResultsTable();
        for(int i=0; i<pop.getNbObjects(); i++){
            rt.incrementCounter();
            rt.addLabel(prefix+(i+1));
            if(volume==true){
                rt.addValue("Volume (unit)", pop.getObject(i).getVolumeUnit());
                rt.addValue("Volume (pixel)", pop.getObject(i).getVolumePixels());
            }
            if(mean==true){ rt.addValue("Mean", pop.getObject(i).getPixMeanValue(rawimage));}
            if(surface==true){
                rt.addValue("SurfaceArea", pop.getObject(i).getAreaUnit());
                rt.addValue("surfaceArea (pxl)", pop.getObject(i).getAreaPixels());
            }
            if(minMax==true){
                rt.addValue("Min", pop.getObject(i).getPixMinValue(rawimage));
                rt.addValue("Max", pop.getObject(i).getPixMaxValue(rawimage));}
            if(stdDev==true){ rt.addValue("Std Deviation", pop.getObject(i).getPixStdDevValue(rawimage));}
            if(centro==true){
                rt.addValue("X", pop.getObject(i).getCenterX());
                rt.addValue("Y", pop.getObject(i).getCenterY());
                rt.addValue("Z", pop.getObject(i).getCenterZ());}
            if(mass==true){
                rt.addValue("Xmass", pop.getObject(i).getMassCenterX(rawimage));
                rt.addValue("Ymass", pop.getObject(i).getMassCenterY(rawimage));
                rt.addValue("Zmass", pop.getObject(i).getMassCenterZ(rawimage));}
            if(feret==true){rt.addValue("Feret", pop.getObject(i).getFeret());}
            if(intDen==true){rt.addValue("integrated Density", pop.getObject(i).getIntegratedDensity(rawimage));}
        }
        return rt;
    }
    
    
    /**
     * Perform the shuffle
     * @param mask if there is no mask=fullImage
     * @param popA
     * @param popB 
     */
    public static void computeShuffle (Object3D mask, Objects3DPopulation popA, Objects3DPopulation popB){
        
        popA.setMask(mask);
        SpatialShuffle shuffle = new SpatialShuffle(popA);
        //coloc
        SpatialDescriptor colocalisation = new Coloc_Center_Function(popB);
        int numRandomSamples = 100;
        SpatialStatistics stat = new SpatialStatistics(colocalisation, shuffle, numRandomSamples, popA);
        stat.setEnvelope(0.05); // 0.1 = envelope error 5 %-95%, 0.05=2.5-97.5%
        stat.getPlot().show();
        if((stat.getSdi()<=0.025) || (stat.getSdi()>=0.975)){
            IJ.log("The colocalisation is considered as statistically signifiant  p= " + stat.getSdi());
            IJ.log("The experimental data (blue curve) fall outside the confidence intervals of the random data (green curves)");
        }
        else{
            IJ.log("The colocalisation is not considered as statistically signifiant p= " + stat.getSdi());
            IJ.log("The experimental data (blue curve) fall inside the confidence intervals of the random data (green curves)");
        }

    }
    
    
    public Objects3DPopulation getTouchingPopA(){
        return touchA;
    }
    
    public Objects3DPopulation getTouchingPopB(){
        return touchB;
    }
    
    public ImageHandler getImageColoc(){
        return iHC;
    }
}
