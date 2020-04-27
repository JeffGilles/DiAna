/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DiAna;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.plugin.Filters3D;
import ij.plugin.GaussianBlur3D;

/**
 *
 * @author jeff
 */
public class ImageUtils {
      
      /**
       *  Perform a 3D median
       * @param stackorig Stack input
       * @param vx Radius x
       * @param vy Radius y
       * @param vz Radius z
       * @return 
       */
      public static ImageStack medianIJ (ImageStack stackorig, float vx, float vy, float vz){
            ImageStack iStack = Filters3D.filter(stackorig, Filters3D.MEDIAN, vx, vy, vz);
            return iStack;
      }
      
      /**
       *  Perform a 3D mean
       * @param stackorig Stack input
       * @param vx Radius x
       * @param vy Radius y
       * @param vz Radius z
       * @return 
       */
      public static ImageStack meanIJ (ImageStack stackorig, float vx, float vy, float vz){
            ImageStack iStack = Filters3D.filter(stackorig, Filters3D.MEAN, vx, vy, vz);
            return iStack;
      }
      
      /**
       * Perform a 3D Gaussian blur
       * @param imaOrig ImagePlus input
       * @param vx Radius x
       * @param vy Radius y
       * @param vz Radius z
       * @return 
       */
      public static ImagePlus gaussianIJ (ImagePlus imaOrig, float vx, float vy, float vz){
            GaussianBlur3D.blur(imaOrig, vx, vy, vz);
            return imaOrig;
      }
      
      /**
       * Return only the channel 1 from a dual channel stack
       * @param ima ImagePlus Input
       * @return 
       */
      public static ImagePlus returnChannel1 (ImagePlus ima){
            Duplicator dup = new Duplicator();
            ImagePlus imp = dup.run(ima, 1, 1, 1, ima.getNSlices(), 1, 1); //return only channel 1
            return imp;
      }
}
