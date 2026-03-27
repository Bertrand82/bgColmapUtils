package bg.util;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import bg.util.map.UtilMap;

import java.io.File;

public class UtilMapTest {
    public static void main(String[] args) throws Exception {
        

        // Exemple bbox (à remplacer par tes valeurs)
        double latMin = 48.84;
        double longMin = 2.28;
        double latMax = 48.89;
        double longMax = 2.38;
        
        longMax =-80.12799541666666;
        longMin =-80.13276155555555;
        latMax = 25.783360861111113;
        latMin = 25.776194722222222;
        int zoom = 16;
        BufferedImage img = UtilMap.fetchBbox(latMin, longMin, latMax, longMax, zoom);
        File file  =new File("target","map_"+zoom+"_"+img.getWidth() + "x" + img.getHeight()+".png");
        ImageIO.write(img, "png",file );
        System.out.println("Saved "+file.getName()+": " + img.getWidth() + "x" + img.getHeight());
    }
}