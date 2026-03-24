package bg.display.images.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import bg.util.GpsPositionFactory;
import bg.metadata.MetaData;
import bg.metadata.MetaDatasCsv;
import bg.util.GpsPosition2;


public class DisplayImagesPanel extends JPanel{

    private final List<File> listFilesJPEG = new ArrayList<File>();
    final MetaDatasCsv metadatas;
    private int index = -1;

    private Image currentImage;
    private Timer timer;

  
    private final Canvas canvas;
    private final JTextField infoField;
    private boolean pause=false;
    private boolean forward=true;

    private static final Comparator<File> COMPARATOR_FILE = new Comparator<File>() {
        @Override
        public int compare(File a, File b) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    };

    /** Construit la GUI + charge les JPG du répertoire + démarre le diaporama. */
    public DisplayImagesPanel(File dir) throws Exception {
    	File metadataCsvFile= new File(dir,"metadata.csv");
    	File dirImages= new File(dir,"images");
    	System.out.println("metadata.csv exists :"+metadataCsvFile.exists());
		this.metadatas= new MetaDatasCsv(metadataCsvFile,dirImages);
		System.out.println("MetaDatasCsv "+metadataCsvFile);
        initListFile(dirImages);
        System.out.println("listFilesJPEG size :"+this.listFilesJPEG.size());
 
        infoField = new JTextField();
        infoField.setEditable(false);

        canvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                DisplayImagesPanel.this.paintImage(g);
            }

            @Override
            public void update(Graphics g) {
                paint(g);
            }
        };
        
        JButton buttonPause = new JButton(" || ");
        buttonPause.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pause=true;
			}
		});
        JButton buttonGoPrevious = new JButton(" < ");
        buttonGoPrevious.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pause=false;
				forward=false;
			}
		}) ;
        JButton buttonGoForward = new JButton(" > ");
        buttonGoForward.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pause=false;
				forward=true;
			}
		});
        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.add(infoField,BorderLayout.NORTH);
        panelNorth.add(buttonPause, BorderLayout.CENTER);
        panelNorth.add(buttonGoForward, BorderLayout.EAST);
        panelNorth.add(buttonGoPrevious, BorderLayout.WEST);

        this.setLayout(new BorderLayout());
        this.add(panelNorth, BorderLayout.NORTH);
        this.add(canvas, BorderLayout.CENTER);

        this.canvas.setSize(900, 700);
        
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

        timer = new Timer(50, e -> showNext());
        timer.start();
    }

    private void showNext() {
    	  if (pause) {
          	return;
          }
    	int ii;
    	if (forward==true) {
    		ii=1;
    	}else {
    		ii=-1;
    	}

    	
        index = (index + ii) % listFilesJPEG.size();
    	if (index <0) {
    		index =0;
    	}
      
        File f = listFilesJPEG.get(index);

        currentImage = new ImageIcon(f.getAbsolutePath()).getImage();
        Double gpsZ =0d;
        double gpsLAtitude=0d;
        double gpsLongitude=0d;
        GpsPosition2 position =null;
        try {
        	position =GpsPositionFactory.extractPosition(f);
        	gpsZ = position.getAltitudeMeters();
        	gpsLAtitude =position.getLatitude();
        	gpsLongitude = position.getLongitude();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String gpsZStr =  String.format("%7.2f", gpsZ);
        String gpsLatStr = String.format("%9.4f", gpsLAtitude);
        String gpsLonStr = String.format("%9.4f", gpsLongitude);

        MetaData metaData = this.metadatas.getMetaData(f.getName());
        Double pitch ;
        Double roll;
        Double yaw ;
        double x;
        double y ;
        double z ;
        if(metaData==null) {
        	 pitch =   0.0d;
             roll =   0.0d;
             yaw = 0.0d;
             if (position== null) {
            	 x=0;
            	 y=0;
            	 z=0;
            	 System.err.println("Big Probleme : no position "+f.getName());
             }else {
             x =position.getX_process();
             y =position.getY();
             z =position.getAltitudeMeters();
             }
        	
        }else {
        	 pitch =   metaData.pitch;
             roll =   metaData.roll;
             yaw = metaData.yaw;
             x =metaData.x;
             y =metaData.y;
             z =metaData.z;
        }
        
        String pitchStr = String.format("%7.2f", pitch);
        String rollStr  = String.format("%7.2f", roll);
        String yawStr  = String.format("%7.2f", yaw);
        String xStr =  String.format("%7.2f", x);
        String yStr =  String.format("%7.2f", y);
        String zStr =  String.format("%7.2f", z);
       

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