package com.github.assisstion.InternetSpeedTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.github.assisstion.InternetSpeedTest.web.WebConnector;

public class WebProcessor{
	
	
	protected Map<String, String> data;
	
	//Time in ms
	protected long totalTime = 0;
	protected long totalBytes = 0;

	public static void main(String[] args){
		WebProcessor wp = new WebProcessor(getWebsites());
		wp.process();
	}
	
	public static Map<String, String> getWebsites(){
		Map<String, String> out = new HashMap<String, String>();
		try{
			//Insert your own file here
			String in = FileHelper.read(new File("/Users/mfeng17/Desktop/test.txt"));
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
	
	public void process(){
		totalTime = 0;
		totalBytes = 0;
		for(Map.Entry<String, String> entry : data.entrySet()){
			processSite(entry.getValue());
		}
		System.out.println("Total Bytes: " + totalBytes);
		System.out.println("Total Time (ms): " + totalTime);
		long wholePart = totalBytes / (totalTime * 1000);
		long fractionPart = totalBytes / totalTime % 1000;
		System.out.println("Bytes / Second" + wholePart + "." + fractionPart);
	}
	
	public void processSite(String website){
		try{
			URL url = new URL(website);
			long time = System.currentTimeMillis();
			String site = WebConnector.getWebpage(url);
			long difference = System.currentTimeMillis() - time;
			totalBytes += site.length();
			totalTime += difference;
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
