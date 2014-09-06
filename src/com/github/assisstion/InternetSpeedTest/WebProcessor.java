package com.github.assisstion.InternetSpeedTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.assisstion.Shared.Pair;

public class WebProcessor implements InfoSender<Pair<Long, Long>>{


	protected Map<String, String> data;

	//Time in ms
	protected long totalTime = 0;
	protected long totalBytes = 0;
	protected int success = 0;
	protected int counter = 0;
	protected boolean https = false;
	protected int failedAttempts = 0;
	public boolean silent = false;
	public ArrayList<Long> bytes = new ArrayList<Long>();
	public ArrayList<Long> time = new ArrayList<Long>();
	public MainGUI gui;

	public static void main(String[] args){
		WebProcessor wp = new WebProcessor(getWebsites());
		wp.process();
	}

	public static Map<String, String> getWebsites(){
		Map<String, String> out = new LinkedHashMap<String, String>();
		try{
			//Insert your own file here
			String in = FileHelper.read(new File("websites.txt"));
			String[] inArray = in.split("\n");
			for(String part : inArray){
				String[] partArray = part.split("\t");
				out.put(partArray[0], partArray[1]);
			}
		}
		catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}

	public WebProcessor(Map<String, String> map){
		data = map;
	}

	public WebProcessor(){
		this(getWebsites());
	}


	public void process(){
		bytes.clear();
		time.clear();
		totalTime = 0;
		totalBytes = 0;
		counter = 0;
		https = false;
		failedAttempts = 0;
		if(!silent){
			System.out.println("Start processing...");
		}
		BufferedWriter writer = null;
		try{
			try{
				new File("data").mkdir();
				writer = new BufferedWriter(new FileWriter(new File("data/output.txt")));
			}
			catch(IOException e1){
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for(Map.Entry<String, String> entry : data.entrySet()){
				boolean completelyFailed = false;
				String newValue = null;
				counter++;
				if(!processSite(entry.getKey(), entry.getValue())){
					newValue = "https://" + entry.getValue().substring(7);
					https = true;
					failedAttempts++;
					completelyFailed = !processSite(entry.getKey(), newValue);
					success += completelyFailed ? 0 : 1;
					https = false;
				}
				else{
					success++;
				}
				if(!completelyFailed){
					String string = "";
					string += entry.getKey() + "\t"+ (newValue == null ? entry.getValue() : newValue) + "\t" + bytes.get(bytes.size() - 1) + "\t" + time.get(bytes.size() - 1) + "\n";

					try{
						writer.write(string);
					}
					catch(IOException e){
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				//Waits for 1 minute before stopping
				if(totalTime > 60000){
					break;
				}
			}
		}
		finally{
			if(writer != null){
				try{
					writer.close();
				}
				catch(IOException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(!silent){
			System.out.println();
			System.out.println("Done processing!");
			System.out.println("Amount of total websites: "+ success + " out of " + counter);
			System.out.println("Amount of websites switching to https://: "+failedAttempts);
			System.out.println("Total Bytes: " + totalBytes);
			System.out.println("Total Time (ms): " + totalTime);
			System.out.println("Average Speed (KB/s): " + (double) totalBytes / (double) totalTime);
			/*for(int i = 0; i<bytes.size();i++){
				System.out.println(i);
				System.out.println(bytes.get(i));
				System.out.println(time.get(i));
				System.out.println();
			}*/
		}



	}

	public long getTotalTime(){
		return totalTime;
	}

	public long getTotalBytes(){
		return totalBytes;
	}

	public boolean processSite(String name, String website){
		try{
			if(!https){
				if(!silent){
					System.out.println();
				}
			}
			if(!silent){
				System.out.println(counter + (https ? "B" : "A") +
						". Trying website: " + name);
			}
			URL url = new URL(website);
			HashSet<InfoSender<Pair<Long, Long>>> set = new HashSet<InfoSender<Pair<Long, Long>>>();
			set.add(this);
			Pair<Long, Long> total =  WebConnector.webpageByteCount(url, silent, gui, set);
			totalBytes += total.getValueOne();
			totalTime += total.getValueTwo();


			if(total.getValueOne() == 0){
				return false;
			}
			if(!silent){
				System.out.println("Bytes: " + total.getValueOne());
				System.out.println("Time (ms): " + total.getValueTwo());
				System.out.println("Speed (KB/s): " + (double) total.getValueOne() / (double) total.getValueTwo());

			}
			bytes.add(total.getValueOne());
			time.add(total.getValueTwo());
			return true;
		}
		catch(IOException e){
			if(!silent){
				System.out.println("Site unavailable! Trying next one.");
				System.out.println();
			}
			return false;
		}
	}

	@Override
	public void send(Pair<Long, Long> info){
		long bytes = totalBytes + info.getValueOne();
		long time = totalTime + info.getValueTwo();
		gui.kb.setText(String.valueOf(bytes / 1000));
		gui.time.setText(String.valueOf(time / 1000.0));
		gui.speed.setText(String.valueOf((double) bytes / (double) time));
	}
}
