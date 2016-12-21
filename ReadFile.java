package GBDT;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class ReadFile {
	public File file;
	public ReadFile(){
		
	}
	public ReadFile(File file){
		this.file = file;
	}
	public ArrayList<ArrayList<Double>> readData(){
		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
		try{
			Scanner sc = new Scanner(file);
			while(sc.hasNext()){
				String line = sc.nextLine();
				data.add(splitLine(line));
			}
			sc.close();
		}catch(Exception e){
			
		}
		return data;
	}
	
	public double getMinutes(String str){
		double minute = 0.0;
		String time = str.substring(11,19);
		String[] HMS = time.split(":"); // time is spitted to hour/minute/second
		minute = Double.parseDouble(HMS[0])*60.0 + Double.parseDouble(HMS[1]);
		return minute;
	}
	
	public ArrayList<Double> splitLine(String line){
		String[] lineNums = line.split(",");
		ArrayList<Double> sample = new ArrayList<Double>();
		for(int i = 0;i<lineNums.length;i++){
			String numStr = lineNums[i];
			if(i == 0){
				sample.add(getMinutes(numStr));
			}else if(i == 2 || i== 3){
				continue;
			}
			else {
				sample.add(Double.parseDouble(numStr));
			}
		}
		return sample;
		
	}
}
