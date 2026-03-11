package bg.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.Timer;

import bg.MetaData;
import bg.MetaDatasCsv;
import bg.util.GpsPositionFactory;
import bg.util.GpsPosition2;


public class DataGui {

    private final List<File> listFilesJPEG = new ArrayList<File>();
    MetaDatasCsv metadatas;
    private int index = -1;

    private Image currentImage;
    private Timer timer;

    private final JFrame frame;
    private final Canvas canvas;
    private final JTextField infoField;

    private static final Comparator<File> COMPARATOR_FILE = new Comparator<File>() {
        @Override
        public int compare(File a, File b) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    };

    /** Construit la GUI + charge les JPG du répertoire + démarre le diaporama. */
    public DataGui(File dir) throws Exception {
    	File metadataCsvFile= new File(dir,"metadata.csv");
    	File dirImages= new File(dir,"images");
		this.metadatas= new MetaDatasCsv(metadataCsvFile,dirImages);
		
        initListFile(dir);

        frame = new JFrame("bg");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        infoField = new JTextField();
        infoField.setEditable(false);

        canvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                DataGui.this.paintImage(g);
            }

            @Override
            public void update(Graphics g) {
                paint(g);
            }
        };

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(infoField, BorderLayout.NORTH);
        frame.getContentPane().add(canvas, BorderLayout.CENTER);

        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        startSlideshow();
    }

    private void initListFile(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            String n = f.getName().toLowerCase();
            if (n.endsWith(".jpg") || n.endsWith(".jpeg")) {
                listFilesJPEG.add(f);
            }
        }
        Collections.sort(listFilesJPEG, COMPARATOR_FILE);
    }

    private void startSlideshow() {
        if (listFilesJPEG.isEmpty()) {
            infoField.setText("0 / 0");
            return;
        }

        showNext(); // première image tout de suite

        timer = new Timer(250, e -> showNext());
        timer.start();
    }

    private void showNext() {
        index = (index + 1) % listFilesJPEG.size();
        File f = listFilesJPEG.get(index);

        currentImage = new ImageIcon(f.getAbsolutePath()).getImage();
        MetaData metaData = this.metadatas.getImageDroneView(f.getName());
        Double pitch =   metaData.pitch;
        Double roll =   metaData.roll;
        Double yaw = metaData.yaw;
        double x =metaData.x;
        double y =metaData.y;
        double z =metaData.z;
         String pitchStr = String.format("%7.2f", pitch);
        String rollStr  = String.format("%7.2f", roll);
        String yawStr  = String.format("%7.2f", yaw);
        String xStr =  String.format("%7.2f", x);
        String yStr =  String.format("%7.2f", y);
        String zStr =  String.format("%7.2f", z);
        Double gpsZ =0d;
        double gpsLAtitude=0d;
        double gpsLongitude=0d;
        try {
        	GpsPosition2 position =GpsPositionFactory.extractPosition(f);
        	gpsZ = position.getAltitudeMeters();
        	gpsLAtitude =position.getLatitude();
        	gpsLongitude = position.getLongitude();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String gpsZStr =  String.format("%7.2f", gpsZ);
        String gpsLatStr = String.format("%9.4f", gpsLAtitude);
        String gpsLonStr = String.format("%9.4f", gpsLongitude);

        infoField.setText(
                (index + 1) + " / " + listFilesJPEG.size()               
                + "   | pitch= " + pitchStr
                + " | roll= " + rollStr
                + " | yaw= "+yawStr
                + " | x= "+xStr
                + " | y= "+yStr
                + " | z= "+zStr
                + " | lat= "+gpsLatStr
                + " | long= "+gpsLonStr
                + " | z= "+gpsZStr
                               
               // + " | "+metaData.line
        );
     
        canvas.repaint();
    }

    private void paintImage(Graphics g) {
        if (currentImage == null) return;

        int w = canvas.getWidth();
        int h = canvas.getHeight();

        int imgW = currentImage.getWidth(null);
        int imgH = currentImage.getHeight(null);
        if (imgW <= 0 || imgH <= 0) return;

        double s = Math.min((double) w / imgW, (double) h / imgH);
        int dw = (int) (imgW * s);
        int dh = (int) (imgH * s);
        int x = (w - dw) / 2;
        int y = (h - dh) / 2;

        g.drawImage(currentImage, x, y, dw, dh, null);
    }
}