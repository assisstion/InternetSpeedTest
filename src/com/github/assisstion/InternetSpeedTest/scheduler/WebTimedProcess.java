package com.github.assisstion.InternetSpeedTest.scheduler;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import com.github.assisstion.InternetSpeedTest.MainGUI;
import com.github.assisstion.InternetSpeedTest.helper.FileHelper;
import com.github.assisstion.InternetSpeedTest.helper.MathHelper;
import com.github.assisstion.InternetSpeedTest.helper.TimeHelper;
import com.github.assisstion.InternetSpeedTest.web.InfoSender;
import com.github.assisstion.InternetSpeedTest.web.WebProcessor;
import com.github.assisstion.Shared.Pair;

public class WebTimedProcess implements Serializable, Runnable, InfoSender<Pair<Long, Long>>,
LifeLine{

	private static final long serialVersionUID = 1172604926908021885L;

	protected transient WebProcessor processor;

	public transient MainGUI gui;
	public TreeMap<Long, Double> timeMap = new TreeMap<Long, Double>();
	public TreeMap<Long, Double> runMap = new TreeMap<Long, Double>();
	public LinkedHashMap<String, Pair<Long, Long>> siteMap =
			new LinkedHashMap<String, Pair<Long, Long>>();

	protected boolean lastRunZero = false;

	protected int run = 0;
	protected long startTime = 0;

	protected long totalTime = 0;
	protected long totalBytes = 0;

	private File dir = new File("data");
	private File file = new File("data/output2.txt");

	protected transient boolean paused = false;
	protected long pausedTime;
	protected transient long pauseStart;
	protected boolean alive = true;

	public WebTimedProcess(){
		startTime = System.currentTimeMillis();
		setup();
	}

	@Override
	public void run(){
		run++;
		EventQueue.invokeLater(new Runnable(){

			@Override
			public void run(){
				if(gui != null){
					gui.run.setText(String.valueOf(run));
				}
			}

		});

		String in = "";
		if(!lastRunZero){
			try{
				in = FileHelper.read(file);
			}
			catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		long startTime = System.currentTimeMillis();
		System.out.println("Starting timed process at: " +
				TimeHelper.formatSystemTime(startTime));
		processor.timer = this;
		processor.gui = gui;
		processor.process();
		if(!isAlive()){
			return;
		}
		if(gui != null){
			gui.graphWindow.timePanel.runEnd();

		}
		long time = processor.getTotalTime();
		long bytes = processor.getTotalBytes();
		double oldSpeed = (double) bytes / (double) time;
		if(gui != null){
			gui.graphWindow.runPanel.pushLine(System.currentTimeMillis(),
					oldSpeed);
		}
		System.out.println("Total Bytes: " + bytes);
		System.out.println("Total Time (ms): " + time);
		System.out.println("Average Speed (KB/s): " + oldSpeed);
		System.out.println();
		totalTime += time;
		totalBytes += bytes;

		System.out.println("Cumulative Bytes: " + totalBytes);
		System.out.println("Cumulative Time (ms): " + totalTime);

		double speed = (double) totalBytes / (double) totalTime;
		System.out.println("Cumulative Average Speed (KB/s): " + speed);
		System.out.println();
		if(!lastRunZero){
			in += TimeHelper.formatSystemTime(startTime) + "\t" + bytes + "\t" +
					time + "\t" + oldSpeed + "\n";

			try{
				FileHelper.write(file, in);
			}
			catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if(bytes == 0 && time == 0){
			lastRunZero = true;
		}
		else{
			lastRunZero = false;
		}
	}

	@Override
	public void send(Pair<Long, Long> info){
		if(gui == null){
			return;
		}
		long bytes = totalBytes + info.getValueOne();
		long time = totalTime + info.getValueTwo();
		gui.allKB.setText(String.valueOf(bytes / 1000));
		gui.allTime.setText(String.valueOf(time / 1000.0));
		gui.allSpeed.setText(String.valueOf(MathHelper
				.roundThreeDecimals((double) bytes / (double) time)));
	}

	public synchronized boolean paused(){
		return paused;
	}

	public synchronized void setPaused(boolean paused){
		this.paused = paused;
		if(!paused){
			if(pauseStart != 0){
				pausedTime += System.currentTimeMillis() - pauseStart;
				pauseStart = 0;
			}
			notifyAll();
		}
		else{
			if(pauseStart == 0){
				pauseStart = System.currentTimeMillis();
			}
		}
	}

	public void clear(){
		alive = false;
	}

	@Override
	public boolean isAlive(){
		return alive;
	}

	public boolean siteRunning(){
		return processor.siteRunning();
	}

	protected Object readResolve() throws ObjectStreamException{
		setup();
		return this;
	}

	protected void setup(){
		try{
			processor = new WebProcessor();
		}
		catch(RuntimeException e){
			JOptionPane.showOptionDialog(gui, "Invalid Websites File",
					"Error!", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.ERROR_MESSAGE, null, null, null);
			throw e;
		}
		processor.silent = true;
		try{
			dir.mkdir();
			file.createNewFile();
		}
		catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(new RepetitionScheduler(-1, 100, new Runnable(){

			@Override
			public void run(){
				if(gui != null && paused == false){
					gui.timePassed.setText(String.valueOf(TimeHelper
							.formatSeconds((System.currentTimeMillis() -
									startTime - pausedTime) / 100 / 10.0)));
				}
			}

		}, this)).start();
	}
}
