package json.graphic;


import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import json.geojson.objects.Bounding;

public class Display extends Canvas implements Runnable, KeyListener {

	JFrame _frame;
	
	int _width;
	int _height;
	BufferedImage offscreen; 
	public Graphics2D bufferGraphics;
	Bounding _bound;
	double sx,sy;
	
	DisplayListener _listener;
	
	boolean _visible = true;
	
	public Display(int iWidth, int iHeight)
	{

		_width = iWidth;
		_height = iHeight;
		
		offscreen = new BufferedImage(_width,_height, BufferedImage.TYPE_4BYTE_ABGR);
		bufferGraphics = (Graphics2D) offscreen.getGraphics();
		
		Graphics2D g2 = (Graphics2D) bufferGraphics;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		                    RenderingHints.VALUE_ANTIALIAS_ON);
		
		clear();

	}

	public void setDisplayListener(DisplayListener iListener){
		_listener = iListener;
	}
	
	public void setBound(Bounding iBound){
		_bound = iBound;
		sx = (double) (_width/(iBound.maxx-iBound.minx));
		sy = (double) (_height/(iBound.maxy-iBound.miny));
		
	}
	
	public void clear(){
		bufferGraphics.setComposite(AlphaComposite.Clear);
		bufferGraphics.setColor(new Color(0f, 0f, 0f, 0.0f ));
		bufferGraphics.fillRect(0, 0, _width, _height);
		bufferGraphics.setComposite(AlphaComposite.Src);
	}
	
	public void drawLine(double x1, double y1, double x2, double y2, Color iCol) {
		bufferGraphics.setColor(iCol);
		bufferGraphics.drawLine((int)((x1-_bound.minx)*sx), 
								_height-(int)((y1-_bound.miny)*sy), 
								(int)((x2-_bound.minx)*sx), 
								_height-(int)((y2-_bound.miny)*sy));
	}
	
	public void drawPoint(double x1, double y1, int iSize, Color iCol) {
		bufferGraphics.setColor(iCol);
		bufferGraphics.fillRect((int)((x1-_bound.minx)*sx)-iSize, // factorize
								_height-(int)((y1-_bound.miny)*sy)-iSize, 
								2*iSize, 
								2*iSize);
	}
	
	public void init(){
		
		_frame = new JFrame();
		//frame.setUndecorated(true);
		_frame.setSize(_width, _height);
		_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		_frame.getContentPane().add(this);                    
		_frame.setVisible(_visible);
		
		_frame.addKeyListener(this);
	}
	
	public void setVisible(){
		_visible = true;
		if (_frame!=null)  _frame.setVisible(_visible);
	}
	
	public void hide(){
		_visible = false;
		if (_frame!=null)  _frame.setVisible(_visible);
	}

	public void start(){
		init();
		new Thread(this).start();
	}
	
	public void paint(Graphics graphics)
	{
		if (_visible)  graphics.drawImage(offscreen,0,0,this); 
	}
	
	public void update(Graphics g)
	{
		paint(g);
	} 

	public void render(){
		paint(this.getGraphics());
	}

	
	public void fillPolygons(double[] x, double[] y, int length, Color color){
		
		int[] px = new int[length];
		int[] py = new int[length];
		
		for (int i=0; i<length; i++){
			
			px[i] = (int)((x[i]-_bound.minx)*sx);
			py[i] = _height-(int)((y[i]-_bound.miny)*sy);
			
		}
		
		bufferGraphics.setColor(color);
		bufferGraphics.fillPolygon(px, py, length);
		
	}
	
	
	public void saveImage(String iFilename){
		 try {
		       ImageIO.write(offscreen, "png", new File(iFilename)); 
		    } catch (java.io.IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
	}
	
	public byte[] getImageData(){
		 
			ByteArrayOutputStream aStream = new ByteArrayOutputStream();
		
			try {
		       ImageIO.write(offscreen, "png", aStream); 
		    } catch (java.io.IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
			
			return aStream.toByteArray();
	}
	
	
	@Override
	public void run() {

		while (true) {

			paint(this.getGraphics());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		//System.out.println("KeyCode "+e.getKeyCode());
		//System.out.println("KeyChar "+e.getKeyChar());
		//System.out.println("KeyExtended "+e.getExtendedKeyCode());
		
		if (_listener!=null) {
			
			switch (e.getKeyCode()) {
			case 37: _listener.left(); break;
			case 39: _listener.right(); break;
			case 38: _listener.up(); break;
			case 40: _listener.down(); break;
			}
			
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
