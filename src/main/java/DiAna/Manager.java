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
import ij.WindowManager;
import ij.plugin.ChannelSplitter;
import mcib3d.geom.Objects3DPopulation;

/**
 *
 * @author jeff
 */
public class Manager {
    
    static ImagePlus plus1;
    static ImagePlus plus2;
    static ImagePlus plus1seg;
    static ImagePlus plus2seg;
    static Objects3DPopulation pop1;
    static Objects3DPopulation pop2;

    static void setImage1(ImagePlus plus){
        plus1=plus; 
    }

    static void setImage2(ImagePlus plus){
        plus2=plus; 
    }
    
    static void setImage1seg(ImagePlus plus){
        plus1seg=plus; 
    }
    
    static void setImage2seg(ImagePlus plus){
        plus2seg=plus; 
    }
    
    static void setPopulation1(Objects3DPopulation pop){
        pop1=pop;
    }
    
    static void setPopulation2(Objects3DPopulation pop){
        pop2=pop;
    }
    
    /**
     * Test sizes of opened images & list their title
     * @param minOpenedImages 
     * @param splitimage
     * @return 
     */
    public static String[] testImageSizes(int minOpenedImages, boolean splitimage){
        if(splitimage==true){
            SplitOpenedImages();
        }
        int nb=WindowManager.getImageCount();
        int [] IDList= WindowManager.getIDList();
        int[] ID1 = new int[nb];
        String[] titles1;
        int a=0;
        for (int i=0;i<nb;i++){
            ImagePlus currImg=WindowManager.getImage(IDList[i]);
            if (currImg.getBitDepth()!=24 && currImg.getBitDepth()!=32){
                ID1[a]=IDList[i];
                a++;
            }
        }
        nb=a;
        a=1;
        titles1=new String[nb];
        if(nb>1){
            //Test Sizes
            for(int i=0;i<nb-1;i++){
                ImagePlus currImg=WindowManager.getImage(ID1[i]);
                for(int j=1; j<nb; j++){
                    if(i<j){
                        ImagePlus testedImg=WindowManager.getImage(ID1[j]);
                        if((currImg.getWidth()==testedImg.getWidth()) && (currImg.getHeight()==testedImg.getHeight()) && (currImg.getNSlices()==testedImg.getNSlices()) && (currImg.getNDimensions()==testedImg.getNDimensions())){
                            if(i==0){titles1[a-1]=currImg.getTitle();}
                            if(titles1[a]==null){
                                if(!currImg.getTitle().equals(titles1[a-1])){titles1[a]=currImg.getTitle();a++;}//a different set of images has been fund & has been added
                                titles1[a]=testedImg.getTitle();
                                a++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        //adjust the length
        String[] titles2=new String[a];
        for(int i=0; i<a;i++){
            titles2[i]=titles1[i];
            if(nb==1)titles2[i]=WindowManager.getImage(ID1[i]).getTitle();
        }
        return titles2;
    }
    
    /**
     * Find the number in the list corresponding to the image
     * @param plus ImagePlus
     * @param list List of image titles
     * @return 
     */
    public static int setImageList (ImagePlus plus, String[] list){
        int number = 0;
        for(int i=0; i<list.length; i++){
            if(plus.getID()==WindowManager.getImage(list[i]).getID()){
                number=i;
            }
        }
        return number;
    }
    
    /**
     * Test with a random position if Image is labelled with neighbours values
     * @param plus image to test
     * @return 
     */
    public static boolean testLabelled(ImagePlus plus){
        boolean test=true;
        int a=0;
        for(int i=0; i<10; i++){
            int randZ = 1 + (int)(Math.random() * ((plus.getNSlices() - 1) + 1));
            int randX = 1 + (int)(Math.random() * ((plus.getWidth() - 2) + 1));      //exclude x=0 and x=width
            int randY = 1 + (int)(Math.random() * ((plus.getHeight() - 2) + 1));     //exclude y=0 and y=height
            plus.setSliceWithoutUpdate(randZ);
            int valueCenter = plus.getProcessor().getPixel(randX, randY);
            int value1 = plus.getProcessor().getPixel(randX-1, randY);
            int value2 = plus.getProcessor().getPixel(randX, randY-1);
            int value3 = plus.getProcessor().getPixel(randX+1, randY);
            int value4 = plus.getProcessor().getPixel(randX, randY+1);
            if(valueCenter!=value1 || valueCenter!=value2 || valueCenter!=value3 || valueCenter!=value4){
                a++;
            }
        }
        if(a>=5){
            test=false;
        }
        return test;
    }
    
    /**
     * Split Channels
     * @param plus 
     * @return 
     */
    public static ImagePlus[] splitChannels(ImagePlus plus){
        ImagePlus[] splited = ChannelSplitter.split(plus);
        return splited;
    }
    
    /**
     * split opened images
     */
    public static void SplitOpenedImages(){
        int nb=WindowManager.getImageCount();
        int [] IDList= WindowManager.getIDList();
        //Test exclude RGB & 32bits images
        for (int i=0;i<nb;i++){
            ImagePlus currImg=WindowManager.getImage(IDList[i]);
            if(currImg.getBitDepth()==24 || currImg.getNChannels()>1 ){
                boolean splitOK = IJ.showMessageWithCancel("MultiChannel image", "Split channel on "+currImg.getTitle()+" ?" );
                if(splitOK==true){
                     ImagePlus[] splited = splitChannels(currImg);
                    int a=splited.length;
                    for(int j=0; j<a;j++){
                        splited[j].show();
                    }
                    currImg.close();
                }
            }
        }
    }
}
