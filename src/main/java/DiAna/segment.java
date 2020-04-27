/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DiAna;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageConverter;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.IterativeThresholding.TrackThreshold;
import mcib3d.image3d.Segment3DImage;
import mcib3d.image3d.Segment3DSpots;
import mcib3d.image3d.processing.MaximaFinder;

/**
 *
 * @author jean-francois gilles
 */
public class segment {
    
    /** Volume minimum **/
    int volumeMin=0;
    
    /** Volume maximum **/
    int volumeMax=0;
    
    /** Object3D population **/
    Objects3DPopulation pop=null;
    
    
    /**
     * Perform the filtering
     * @param ima image to filter
     * @param method method used
     * @param rad radius
     * @return 
     */
    public static ImagePlus filter (ImagePlus ima, String method, float rad){
        
        ImagePlus imp=ima.createImagePlus();

        if (method.equals("gaussian")){
            imp.setImage(ima);    //pb if there is a ROI
            imp=ImageUtils.gaussianIJ(imp, rad, rad, rad);
            imp.setTitle(ima.getShortTitle()+"-filtered");
        }
        else if (method.equals("median")){
            ImageStack isA= ima.getImageStack();
            ImageStack isA2 = ImageUtils.medianIJ(isA, rad, rad, rad);
            imp = new ImagePlus(ima.getShortTitle()+"-filtered", isA2);
        }
        else if (method.equals("mean")){
            ImageStack isA= ima.getImageStack();
            ImageStack isA2 = ImageUtils.meanIJ(isA, rad, rad, rad);
            imp = new ImagePlus(ima.getShortTitle()+"-filtered", isA2);
        }
        else if (method.equals("none")){
              imp.setImage(ima);    //pb if there is a ROI
              imp.setTitle(ima.getShortTitle()+"-filtered");
        }
        return imp;
    }
    
    /**
     *  Get the Objects3D Population from the image to segment
     * @param ima image to segment 
     * @param thrValue Threshold value
     * @param volmin minimum volume value (pxl)
     * @param volmax maximum volume value (pxl)
     * @param excluXY exclude objects on edgesXY
     * @param excluZ exclude objects on edgesZ
     * @return 
     */
    public Objects3DPopulation segClassic(ImagePlus ima, int thrValue, int volmin, int volmax, boolean excluXY, boolean excluZ){
        
        Segment3DImage seg= new Segment3DImage(ima, thrValue, 65535);
        seg.setMinSizeObject(volmin);
        seg.setMaxSizeObject(volmax);
        seg.segment();
        Objects3DPopulation objpop = new Objects3DPopulation(seg.getLabelledObjectsImage3D());
        pop=objpop;
        if(excluXY==true){excludeXY(ima.getWidth(), ima.getHeight());}
        if(excluZ==true){excludeZ(ima.getNSlices());}
        return pop;
    }
    
    /**
     * Perform a SpotSegmentation3D and return the Objects3DPopulation
     * @param iha ImageHandler input
     * @param seeds ImageHandler of the seeds (peaks)
     * @param seedThr Seed threshold
     * @param gaussRad Gaussian radius
     * @param sdVal SD value
     * @param volmin Volume minimum of the objects
     * @param volmax Volume maximum of the objects
     * @param excluXY Exclude objects on XY edges
     * @return 
     */
    public Objects3DPopulation segSpot(ImageHandler iha, ImageHandler seeds, int seedThr, int gaussRad, float sdVal, int volmin, int volmax, boolean excluXY){
        Segment3DSpots seg = new Segment3DSpots(iha, seeds);
        seg.setLocalThreshold(0);//0=auto
        seg.setSeedsThreshold(seedThr); //500
        seg.setMethodLocal(Segment3DSpots.LOCAL_GAUSS);
        seg.setGaussPc(sdVal);//1.5
        seg.setGaussMaxr(gaussRad);//10
        seg.setWatershed(true);
        seg.setVolumeMin(volmin);
        seg.setVolumeMax(volmax);
        seg.setMethodSeg(Segment3DSpots.SEG_BLOCK);
        seg.segmentAll();
        Objects3DPopulation objpop = new Objects3DPopulation(seg.getObjects());
        pop=objpop;
        if(excluXY==true){excludeXY(iha.getImagePlus().getWidth(), iha.getImagePlus().getHeight());}
        return pop;
    }
    
    /**
     * Perform an iterative segmentation and return the Objects3DPopulation
     * @param ima Image input
     * @param volmin volume minimum of the objects
     * @param volmax Volume maximum of the objects
     * @param step Increment of the iterative process
     * @param thresVal Threshold value
     * @param excluXY Exclude objects which touching borders
     * @return 
     */
    public Objects3DPopulation segIter(ImagePlus ima, int volmin, int volmax, int step, int thresVal, boolean excluXY){
        TrackThreshold trackThreshold = new TrackThreshold(volmin, volmax, step, 100, thresVal);
        trackThreshold.setCriteriaMethod(3);//3=MSER
        trackThreshold.setMethodThreshold(1);//1=STEP
        ImagePlus imp=trackThreshold.segment(ima, true);
        imp=ImageUtils.returnChannel1(imp);
        ImageInt imaInt=ImageInt.wrap(imp.getImageStack());
        Objects3DPopulation objpop= new Objects3DPopulation(imaInt);
        pop=objpop;
        if(excluXY==true){excludeXY(ima.getWidth(), ima.getHeight());}
        imp.close();
        return pop;
    }
    
    /**
     * Perform a MaxFinder3D and return an ImageHandler
     * @param ima ImagePlus input
     * @param radXY Radius XY
     * @param radZ Radius Z
     * @param noise Noise
     * @return 
     */
    public static ImageHandler ImagePeaks(ImagePlus ima, float radXY, float radZ, float noise){
        ImageHandler ihand = ImageHandler.wrap(ima.duplicate());
        MaximaFinder maxFind = new MaximaFinder(ihand, radXY, radZ, noise);
        ImageHandler seed3D=maxFind.getImagePeaks();
        return seed3D;
    }
    
    
    /**
     * Exclude objects out of the bounds
     * @param width image width=X boundary
     * @param height image height=Y boundary
     */
    public void excludeXY(int width, int height){
        for(int i=(pop.getNbObjects()-1); i>=0; i--){
            Object3D ob = pop.getObject(i);
            if((ob.getXmin()==0) || (ob.getXmax()==width-1) || (ob.getYmin()==0) || (ob.getYmax()==height-1)){
                pop.removeObject(i);
            }
        }
        incrementPop(pop);
    }
    
    /**
     * Exclude objects out of the bounds
     * @param slices image slices=Z boundary
     */
    public void excludeZ(int slices){
        for(int i=(pop.getNbObjects()-1); i>=0; i--){
            Object3D ob = pop.getObject(i);
            if(ob.getZmin()==1 || ob.getZmax()==slices){
                pop.removeObject(i);
            }
        }
        incrementPop(pop);
    }
    
    /**
     * Create an ImageStack from another and Objects3DPopulation
     * @param ima imagePlus where dimensions are taken
     * @param pop Objects3DPopulation to draw
     * @return 
     */
    public static ImageStack createImageObjects (ImagePlus ima, Objects3DPopulation pop){
        
        if(ima.getBitDepth()==8) {
              IJ.run(ima, "16-bit", "");
        }
        ImageStack stack = ima.createEmptyStack();
          //Fill with value 0
          for(int z=0; z<stack.getSize(); z++){
                for(int y=0; y<stack.getHeight(); y++){
                      for(int x=0; x<stack.getWidth(); x++){
                            stack.setVoxel(x, y, z, 0);
                      }
                }
          }
          pop.draw(stack);
          return stack;
    }
    
    public static ImagePlus createImageObjects (String title, ImagePlus ima, Objects3DPopulation pop){
        ImagePlus plus = ima.createImagePlus();
        plus.setImage(ima);
        //if(plus.getBitDepth()==8 && pop.getNbObjects()>254) { //generate pb when number of objects are closed in both channels
        ImageConverter conv = new ImageConverter(plus);
        conv.convertToGray16();
        //}
        ImageHandler hand=ImageHandler.wrap(plus);
        hand.erase();
        pop.draw(hand);
        plus.setTitle(title);
        IJ.log(""+pop.getNbObjects()+" objects found after segmentation and volume selection");
        plus.getProcessor().setMinAndMax(0, pop.getNbObjects());
        IJ.run(plus, "3-3-2 RGB", "");
//        plus.show();
//        plus.updateAndDraw();
        return plus;
    }
    
    /**
     *  Show the labelled image with LUT and Calibration
     * @param title Name of the new image
     * @param isa ImageStack with objects
     * @param pop Objects3DPopulation to initialize setMinMax and indicate the number of objects
     * @param cali Return the calibration
     * @return 
     */
    public static ImagePlus showImageObjects(String title, ImageStack isa, Objects3DPopulation pop, Calibration cali){
        ImagePlus ima = new ImagePlus(title, isa);
        IJ.log(""+pop.getNbObjects()+" objects found after segmentation and volume selection");
        ima.getProcessor().setMinAndMax(0, pop.getNbObjects());
        IJ.run(ima, "3-3-2 RGB", "");
        ima.setCalibration(cali);
        ima.show();
        return ima;
    }
    
    public static Objects3DPopulation incrementPop(Objects3DPopulation pop){
        int val = pop.getObject(pop.getNbObjects()-1).getValue();
        if(val!=pop.getNbObjects()){
            for(int i=0; i<pop.getNbObjects();i++){
                pop.getObject(i).setValue(i+1);
            }
        }
        return pop;
    }
    
}
