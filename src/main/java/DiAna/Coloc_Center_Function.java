package DiAna;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Point3D;
import mcib3d.spatial.descriptors.SpatialDescriptor;
import mcib3d.utils.ArrayUtil;

/**
 * @author thomasb
 */
public class Coloc_Center_Function implements SpatialDescriptor {
    private Objects3DPopulation objectsColocalisation;

    public Coloc_Center_Function(Objects3DPopulation objectsColoc) {
        objectsColocalisation = objectsColoc;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public ArrayUtil compute(Objects3DPopulation pop) {
        pop.createKDTreeCenters();
        Point3D[] point3Ds = new Point3D[pop.getNbObjects()];
        for (int i = 0; i < pop.getNbObjects(); i++) {
            point3Ds[i] = pop.getObject(i).getCenterAsPoint();
        }
        return objectsColocalisation.computeDistances(point3Ds);
    }

    @Override
    public String getName() {
        return "Coloc";
    }

}
