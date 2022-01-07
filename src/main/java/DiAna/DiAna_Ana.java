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
import ij.WindowManager;
import ij.plugin.PlugIn;
import java.util.ArrayList;
import java.util.LinkedList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

/**
 *
 * @author jeff
 */
public class DiAna_Ana implements PlugIn {
    
    /**
     * Select only the objects 2 which are touching when population 1 and 2 are in contact.
     * @param objPop1 
     * @param objPop2 
     * @param im2 im2 is the imagePlus of the population 2
     * @param delete delete object on the image
     * @return 
     */
    public static Objects3DPopulation touchingPop (Objects3DPopulation objPop1, Objects3DPopulation objPop2, ImagePlus im2, boolean delete){
        ImageStack iStack2 = im2.getImageStack();
        Objects3DPopulation pop = new Objects3DPopulation(ImageInt.wrap(im2));
        ArrayList<Object3D> bTouch = new ArrayList<> ();
        ImageHandler iHand = ImageHandler.wrap(iStack2);
        for(int i=0; i<objPop1.getNbObjects();i++){
            Object3D obA = objPop1.getObject(i);
            LinkedList<Voxel3D> vox = obA.listVoxels(iHand);// THOMAS new mcib3d v3.94
            for (Voxel3D voxel3D : vox) {
                int val = (int) voxel3D.getValue();
                Object3D object3D = pop.getObjectByValue(val);
                if ((val > 0) && (!bTouch.contains(object3D))) {
                    bTouch.add(object3D);
                }
            }
            
//            ArrayList<Voxel3D> vox = obA.listVoxels(iHand);   //OLD
//            for(int j=0; j<vox.size(); j++){
//                if(vox.get(j).getValue()!=0 && bTouch.contains(pop.getObject((int) vox.get(j).getValue()-1))==false){
//                    bTouch.add(pop.getObject((int) vox.get(j).getValue()-1));
//                }
//            }
        }
        for(int i=(pop.getNbObjects()-1); i>=0; i--){    //delete objects not touching
            if(!bTouch.contains(pop.getObject(i))){
                if(delete==true){
                pop.getObject(i).draw(iStack2, 0);  //change this with iHand and test
                }
                pop.removeObject(i);
            }
        }
        return pop;
    }
    
    
    /**
     * Draw the population to the ImageHandler
     * @param objpop
     * @param ihand
     */
    public static void drawPop (Objects3DPopulation objpop, ImageHandler ihand){
        for(int i=1; i<=objpop.getNbObjects(); i++){       //re-asign values with numbers.
            objpop.getObject(i-1).draw(ihand, i);
        }
    }
    
    
    /**
     * Select the objects 2 which are not touching objects in pop 1
     * @param objPop1
     * @param objPop2
     * @param im2
     * @return 
     */
    public static Objects3DPopulation nottouchingPop (Objects3DPopulation objPop1, Objects3DPopulation objPop2, ImagePlus im2){
        ImageStack iStack2 = im2.getImageStack();
        Objects3DPopulation pop = new Objects3DPopulation(ImageInt.wrap(im2));
        ArrayList<Object3D> notTouch = new ArrayList<> ();
        ImageHandler iHand = ImageHandler.wrap(iStack2);
        for(int i=0; i<objPop1.getNbObjects();i++){
            Object3D obA = objPop1.getObject(i);
            
            LinkedList<Voxel3D> vox = obA.listVoxels(iHand); // THOMAS new mcib3d v3.94
            for (Voxel3D voxel3D : vox) {
                int val = (int) voxel3D.getValue();
                Object3D object3D = pop.getObjectByValue(val);
                if ((val > 0) && (!notTouch.contains(object3D))) {
                    notTouch.add(object3D);
                }
            }
//            ArrayList<Voxel3D> vox = obA.listVoxels(iHand);
//            for(int j=0; j<vox.size(); j++){
//                if(vox.get(j).getValue()!=0 && bTouch.contains(pop.getObject((int) vox.get(j).getValue()-1))==false){
//                    bTouch.add(pop.getObject((int) vox.get(j).getValue()-1));
//                }
//            }
        }
        for(int i=(pop.getNbObjects()-1); i>=0; i--){    //delete touching objects
            if(notTouch.contains(pop.getObject(i))){
                pop.removeObject(i);
            }
        }
        return pop;
    }    
    
    /**
     * Create the mask on full image
     * @param stack 
     * @return 
     */
    public static Object3D imageMask (ImageStack stack){    //Not Used now
        Objects3DPopulation maskPop = new Objects3DPopulation();
        ImagePlus imP = new ImagePlus("stack", stack);
        imP.getProcessor().setValue(1);
        for(int z=1; z<=imP.getNSlices(); z++){
            imP.getProcessor().fill();
        }
        maskPop.addImage(ImageInt.wrap(imP), 1);
         Object3D mask=maskPop.getObject(0);    //one object
        return mask;
    }
    
    /**
     * Return the mask from ImageStack stack
     * @param stack  image with 0 and 1 values
     * @param fullImage true=mask on fullImage
     * @return 
     */
    @Deprecated
    public static Object3D getmask(ImageStack stack, boolean fullImage){
        Objects3DPopulation shuffleMask = new Objects3DPopulation();
        //Create the mask
        //ImagePlus imP = new ImagePlus("stack", stack);
        
        if(fullImage==true){
            shuffleMask.addImage(ImageHandler.wrap(stack), 0);
            ImageHandler ih = ImageHandler.wrap(stack);
            ih.fill(1, 0, 65535);
            shuffleMask.addImage(ih, 0);
//            imP.getProcessor().setValue(1);
//            for(int z=1; z<=stack.getSize(); z++){
//                imP.setSlice(z);
//                imP.getProcessor().fill();
//            }
        }
        else{
            shuffleMask.addImage(ImageHandler.wrap(stack), 1);
            //imP.setStack(stack);
            
        }
        //imP.updateAndDraw();
        //shuffleMask.addImage(imP);
//        shuffleMask.addImage(ImageInt.wrap(imP), 1);//PB
        Object3D mask=shuffleMask.getObject(0);    //one object
        
        //imP.close();
        return mask;
    }
    
    /**
     * Return the mask from ImageHandler hand
     * @param hand  image
     * @param fullImage true=mask on fullImage
     * @return 
     */
    public static Object3D getmask(ImageHandler hand, boolean fullImage){
        
        Objects3DPopulation shuffleMask = new Objects3DPopulation();
        if(fullImage==true){
            ImageHandler ih = hand.createSameDimensions();
            
            ih.fill(1, 0, 65535);
            shuffleMask.addImage(ih, 0);
        }
        else{
            shuffleMask.addImage(hand, 1);
        }
        Object3D mask = shuffleMask.getObject(0);    //one object
        return mask;
    }
    
    /**
     * Return shuffle of pop1 in the mask
     * @param mask 
     * @param pop1
     * @return 
     */
    public static Objects3DPopulation popShuffle (Object3D mask, Objects3DPopulation pop1){
        Objects3DPopulation shufflePop = pop1;
        shufflePop.setMask(mask);
        ArrayList<Object3D> shuObj=shufflePop.shuffle();
        shufflePop.addObjects(shuObj);
        return shufflePop;
    }
    
    /**
     * Return ArrayList of distances Center-Center of the two populations
     * @param pop1
     * @param pop2
     * @return 
     */
    public static ArrayList getDistancesCC (Objects3DPopulation pop1, Objects3DPopulation pop2){    //not used
        ArrayList list = new ArrayList();
        for(int i=0; i<pop1.getNbObjects(); i++){
            Object3D obj1=pop1.getObject(i);
            Object3D obj2 = pop2.closestCenter(obj1, false);
            list.add(obj1.distCenterUnit(obj2));
        }
        return list;
    }
    
    /**
     * mean value from arrayList
     * @param arrayL
     * @return 
     */
    public static Double meanValue (ArrayList<Double> arrayL){
        Double mean;
        Double sum=0.0;
        for(int i=0; i<arrayL.size();i++){
            sum = sum+arrayL.get(i);
        }
        mean=sum/arrayL.size();
        return mean;
    }
        
    
     @Override
    public void run(String arg) {
       DiAna_Analyse diaAn = new DiAna_Analyse();
       
       if (Macro.getOptions()==null){
           diaAn.setVisible(true);
        }else{
           
           diaAn.imA=WindowManager.getImage(diaAn.imgASelect);
           diaAn.imB=WindowManager.getImage(diaAn.imgBSelect);
           diaAn.imA2=WindowManager.getImage(diaAn.imgA2Select);
           diaAn.imB2=WindowManager.getImage(diaAn.imgB2Select);
           diaAn.titleMask=null;
           diaAn.macroInterpreter(Macro.getOptions());
        }
    }
    
}
