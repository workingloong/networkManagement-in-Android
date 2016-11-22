package com.example.networkmanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.widget.TextView;

public class FileHelper {
	private Context mContext;
	private String filename;
	
	public FileHelper(){};
	
	public FileHelper(Context mContext,String filename){
		super();
		this.mContext = mContext;
		this.filename = filename;
	}
	
	//定义文件保存的方法，写入到文件中，所以是输出流
	public void save(String name, String content,int mode) throws Exception{
		//Context.MODE_APPEND权限，检查文件是否存在，存在就往文件追加内容，否则创建新文件
		//FileOutputStream output = mContext.openFileOutput(name,Context.MODE_PRIVATE);
		FileOutputStream output = mContext.openFileOutput(name,mode);
		output.write(content.getBytes());
		output.close();
	}
	
	//定义文件读取方法
	public String read(String fileName) throws Exception{
		FileInputStream input = mContext.openFileInput(fileName);
		//定义1M的缓冲区
		byte[] temp = new byte[1024];
		StringBuilder sb = new StringBuilder();
		int len = 0;
		while((len = input.read(temp))>0){
			sb.append(new String(temp,0,len));
		}
		input.close();
		return sb.toString();
	}
	public void dataSave(String fileContent,int mode){
		try{
			save(filename, fileContent,mode);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String readData(){
		String detail = "";
		try{
			detail = read(filename);
		}catch(Exception e){
			e.printStackTrace();
		}
		return detail;
	}

}
