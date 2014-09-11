package com.github.assisstion.InternetSpeedTest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.assisstion.Shared.Pair;

public class BarGraphPanel extends JPanel{

	private static final long serialVersionUID = -509125879716516235L;

	private static final int TOP_Y = 30;
	private static final int BOTTOM_Y = 390;
	private static final int LEFT_X = 50;
	private static final int RIGHT_X = 920;

	private static final int MARKER_LENGTH = 10;
	private static final int DIST_FROM_MARKER = 2;

	private long max = 0;
	private static final int CAP = 200;


	public LinkedHashMap<String, Pair<Long, Long>> speeds = new LinkedHashMap<String, Pair<Long, Long>>();

	/**
	 * Create the panel.
	 */
	public BarGraphPanel(){
		setLayout(null);

		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setBounds(10, 10, 61, 16);
		add(lblNewLabel);


	}
	@Override
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawRect(LEFT_X, TOP_Y, getGraphWidth(), getGraphHeight());
		for(int i = 0; i < elements() - 1;i++){
			int x = LEFT_X + siteP() * (i+1);
			g.drawLine(x, BOTTOM_Y, x, BOTTOM_Y+MARKER_LENGTH);

		}
		int i = 0;
		for(Map.Entry<String, Pair<Long, Long>> entry : speeds.entrySet()){
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.BLACK);
			AffineTransform form = new AffineTransform();
			form.rotate(-Math.PI / 2, getWidth()/2, getHeight()/2);
			g2d.transform(form);
			g2d.drawString(entry.getKey(), BOTTOM_Y+20-150, LEFT_X+siteP()*i+DIST_FROM_MARKER - 205);
			try{
				g2d.transform(form.createInverse());
			}
			catch(NoninvertibleTransformException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			g2d.setColor(Color.BLUE);
			if(entry.getValue().getValueOne() != 0 && entry.getValue().getValueTwo() != 0){
				long speedNow = entry.getValue().getValueOne() / entry.getValue().getValueTwo();

				g.fillRect(LEFT_X+siteP()*i+DIST_FROM_MARKER, BOTTOM_Y - (int)(getGraphHeight()/((double)getMax()/speedNow)), siteP()-2*DIST_FROM_MARKER, (int)(getGraphHeight()/((double)getMax()/speedNow)));
			}
			i++;
		}
		//markerAmount = (int)getMax() / 50 * 5;
		/*
		if(getMax() >0 && max+ CAP <=25){
			markerAmount = 5;
		}
		else if(getMax() >25 && max+ CAP <=100){
			markerAmount = 5;
		}
		else if(getMax() >100 && max+ CAP <=250){
			markerAmount = 25;
		}
		else if(getMax() >250 && max+ CAP <=500){
			markerAmount = 50;
		}
		else if(getMax() >500 && max+ CAP <=1000){
			markerAmount = 100;
		}
		else if(getMax() >1000 && max+ CAP <=2000){
			markerAmount = 200;
		}
		else if(getMax() >2000 && max+ CAP <=5000){
			markerAmount = 500;
		}
		else if(getMax() >5000 && max+ CAP <=10000){
			markerAmount = 1000;
		}
		else if(getMax() >10000){
			markerAmount = 2000;
		}
		 */
		g.setColor(Color.BLACK);

		int counter = 0;
		/*
		for(int j = 0; j<getMax()+1; j+=markerAmount){
			g.drawLine(LEFT_X, (int) (TOP_Y + j*((getGraphHeight())/((getMax())/markerAmount))), LEFT_X + MARKER_LENGTH, (int)(TOP_Y + j*((getGraphHeight())/((getMax())/markerAmount))));
			g.drawString(""+ (getMax()-j), LEFT_X-25, (int) (TOP_Y + counter++*((getGraphHeight())/((double)(getMax())/markerAmount))));
		}
		 */

		for(int j = (int)getMax() % markerAmount(); j <= getMax(); j+=markerAmount()){
			g.drawLine(LEFT_X, (int) (TOP_Y + j*(getGraphHeight()/(getMax()/markerAmount()))), LEFT_X + MARKER_LENGTH, (int)(TOP_Y + j*(getGraphHeight()/(getMax()/markerAmount()))));
			g.drawString(String.valueOf(getMax()-j), LEFT_X - 32, (int) (TOP_Y + counter++*(getGraphHeight()/((double)getMax()/markerAmount()))) + (int)(getMax() % markerAmount()/(double)getMax()*getGraphHeight()));
		}
		g.drawString(String.valueOf(getMax()), LEFT_X + 2, (int) (TOP_Y + getGraphHeight()/((double)getMax()/markerAmount())) + (int)(getMax() % markerAmount()/(double)getMax()*getGraphHeight()) - (int)(markerAmount()/(double)getMax()*getGraphHeight()));
	}


	public void pushBar(String site, long bytes, long loadingTime, int sites){

		//if(xElements < sites){
		//	xElements++;
		//}
		if(speeds.get(site) != null){
			Pair<Long, Long> pair = speeds.get(site);
			speeds.put(site, new Pair<Long, Long>(
					pair.getValueOne() + bytes, pair.getValueTwo() + loadingTime));
		}
		else{
			speeds.put(site, new Pair<Long, Long>(
					bytes, loadingTime));
		}
		//siteP = getGraphWidth() / elements();
		max = 0;
		for(Map.Entry<String, Pair<Long, Long>> entry : speeds.entrySet()){
			Pair<Long, Long> pair = entry.getValue();
			if(pair.getValueTwo() == 0){
				continue;
			}
			long temp = pair.getValueOne() / pair.getValueTwo();
			if(temp>max){
				max = temp;
			}
		}
		repaint();

	}

	public static int getGraphWidth(){
		return RIGHT_X - LEFT_X;
	}

	public static int getGraphHeight(){
		return BOTTOM_Y - TOP_Y;
	}

	public long getMax(){
		return max + CAP;
	}

	public int elements(){
		return speeds.size();
	}

	public int siteP(){
		return getGraphWidth() / elements();
	}

	public int markerAmount(){
		return (int)getMax() / 50 * 5;
	}
}
