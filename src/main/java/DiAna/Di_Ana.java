/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DiAna;

import ij.Macro;
import ij.WindowManager;
import ij.plugin.PlugIn;


/**
 *
 * @author Jean-François Gilles
 */
public class Di_Ana implements PlugIn {
    

    @Override
    public void run(String arg) {
       Diana_SegmentGui dia = new Diana_SegmentGui();
       
       if (Macro.getOptions()==null){
           dia.setVisible(true);
        }else{
           dia.imA=WindowManager.getImage(dia.imgASelect);
            dia.imB=WindowManager.getImage(dia.imgBSelect);
           dia.macroInterpreter(Macro.getOptions());
        }
    }
    
}
