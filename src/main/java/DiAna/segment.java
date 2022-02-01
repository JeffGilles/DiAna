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
import mcib3d.geom2.Objects3DIntPopulation;
import mcib3d.geom2.Objects3DIntPopulationComputation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.IterativeThresholding.TrackThreshold;
import mcib3d.image3d.segment.Segment3DSpots;
import mcib3d.image3d.processing.MaximaFinder;
import mcib3d.image3d.segment.LocalThresholder;
import mcib3d.image3d.segment.LocalThresholderGaussFit;
import mcib3d.image3d.segment.SpotSegmenter;
import mcib3d.image3d.segment.SpotSegmenterBlock;

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
    Objects3DIntPopulation pop=null;
    
    
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
    public Objects3DIntPopulation segClassic(ImagePlus ima, int thrValue, int volmin, int volmax, boolean excluXY, boolean excluZ){

        ImageLabeller labeller = new ImageLabeller();
        labeller.setMinSize(volmin);
        labeller.setMaxsize(volmax);
        
        ImageHandler imh = ImageHandler.wrap(ima);
        ImageInt bin = imh.thresholdAboveInclusive(thrValue);
        bin.setScale(imh);
        
        ImageHandler res;
        labeller.getLabels(bin);
        res = labeller.getLabels(bin);
        res.setScale(imh);
//        Objects3DPopulation objpop = new Objects3DPopulation(res);
        Objects3DIntPopulation objpop = new Objects3DIntPopulation(res);
        Objects3DIntPopulation pop1;
        if(excluXY){
            pop1 = new Objects3DIntPopulationComputation(objpop).getExcludeBorders(res, excluZ);
        }
        else{
            pop1 = objpop;
        }
        pop=objpop;
        
//        if(excluXY){excludeXY(res.getImagePlus().getWidth(), res.getImagePlus().getHeight());}
//        if(excluZ){excludeZ(res.getImagePlus().getNSlices()-1);}
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
    public Objects3DIntPopulation segSpot(ImageHandler iha, ImageHandler seeds, int seedThr, int gaussRad, float sdVal, int volmin, int volmax, boolean excluXY){
        Segment3DSpots seg = new Segment3DSpots(iha, seeds);
        seg.setSeedsThreshold(seedThr);
        seg.setUseWatershed(true);
        seg.setVolumeMin(volmin);
        seg.setVolumeMax(volmax);
        // create thresholder
        LocalThresholder localThresholder;
        localThresholder = new LocalThresholderGaussFit(gaussRad,sdVal);

        // create spot segmenter
        SpotSegmenter spotSegmenter;
        spotSegmenter = new SpotSegmenterBlock();
        
        // segment
        seg.setLocalThresholder(localThresholder);
        seg.setSpotSegmenter(spotSegmenter);
        seg.segmentAll();
        
        ImageHandler labeled = seg.getLabeledImage();
        
        Objects3DIntPopulation objpop = new Objects3DIntPopulation(labeled);
        
        if(excluXY){
            pop = new Objects3DIntPopulationComputation(objpop).getExcludeBorders(labeled, false);
        }
        else{
            pop = objpop;
        }
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
    public Objects3DIntPopulation segIter(ImagePlus ima, int volmin, int volmax, int step, int thresVal, boolean excluXY){
        
        TrackThreshold trackThreshold = new TrackThreshold(volmin, volmax, step, 100, thresVal);
        trackThreshold.setCriteriaMethod(3);//3=MSER
        trackThreshold.setMethodThreshold(1);//1=STEP
        ImagePlus imp=trackThreshold.segment(ima, true);
        imp=ImageUtils.returnChannel1(imp);
        
        ImageHandler ih = ImageInt.wrap(imp.getImageStack());
        Objects3DIntPopulation objpop= new Objects3DIntPopulation(ih);
        
        pop=objpop;
        
        if(excluXY){
            pop = new Objects3DIntPopulationComputation(objpop).getExcludeBorders(ih, false);
        }
        else{
            pop = objpop;
        }
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
    
    
//    /**
//     * Exclude objects out of the bounds
//     * @param width image width=X boundary
//     * @param height image height=Y boundary
//     */
//    
//    public void excludeXY(int width, int height){
//        for(int i=(pop.getNbObjects()-1); i>=0; i--){
//            Object3D ob = pop.getObject(i);
//            if((ob.getXmin()==0) || (ob.getXmax()==width-1) || (ob.getYmin()==0) || (ob.getYmax()==height-1)){
//                pop.removeObject(i);
//            }
//        }
//        incrementPop(pop);
//    }
    
    /**
     * Exclude objects out of the bounds
     * @param slices image slices=Z boundary
     */
//    public void excludeZ(int slices){
//        for(int i=(pop.getNbObjects()-1); i>=0; i--){
//            Object3D ob = pop.getObject(i);
//            if(ob.getZmin()==1 || ob.getZmax()==slices){
//                pop.removeObject(i);
//            }
//        }
//        incrementPop(pop);
//    }
    
    
//    /**
//     * Create an ImageStack from another and Objects3DPopulation
//     * @param ima imagePlus where dimensions are taken
//     * @param pop Objects3DPopulation to draw
//     * @return 
//     */
//    public static ImageStack createImageObjects (ImagePlus ima, Objects3DIntPopulation pop){
//        
//        if(ima.getBitDepth()==8) {
//              IJ.run(ima, "16-bit", "");
//        }
//        ImageStack stack = ima.createEmptyStack();
//          //Fill with value 0
//          for(int z=0; z<stack.getSize(); z++){
//                for(int y=0; y<stack.getHeight(); y++){
//                      for(int x=0; x<stack.getWidth(); x++){
//                            stack.setVoxel(x, y, z, 0);
//                      }
//                }
//          }
//          pop.draw(stack);
//          return stack;
//    }
    
    public static ImagePlus createImageObjects (String title, ImagePlus ima, Objects3DIntPopulation pop){
        ImagePlus plus = ima.createImagePlus();
        plus.setImage(ima);
        //if(plus.getBitDepth()==8 && pop.getNbObjects()>254) { //generate pb when number of objects are closed in both channels
        ImageConverter conv = new ImageConverter(plus);
        conv.convertToGray16();
        //}
        ImageHandler hand=ImageHandler.wrap(plus);
        hand.erase();
        pop.drawInImage(hand);
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
    public static ImagePlus showImageObjects(String title, ImageStack isa, Objects3DIntPopulation pop, Calibration cali){
        ImagePlus ima = new ImagePlus(title, isa);
        IJ.log(""+pop.getNbObjects()+" objects found after segmentation and volume selection");
        ima.getProcessor().setMinAndMax(0, pop.getNbObjects());
        IJ.run(ima, "3-3-2 RGB", "");
        ima.setCalibration(cali);
        ima.show();
        return ima;
    }
    
    //Check if used
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
