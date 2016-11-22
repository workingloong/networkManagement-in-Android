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
	
	//�����ļ�����ķ�����д�뵽�ļ��У������������
	public void save(String name, String content,int mode) throws Exception{
		//Context.MODE_APPENDȨ�ޣ�����ļ��Ƿ���ڣ����ھ����ļ�׷�����ݣ����򴴽����ļ�
		//FileOutputStream output = mContext.openFileOutput(name,Context.MODE_PRIVATE);
		FileOutputStream output = mContext.openFileOutput(name,mode);
		output.write(content.getBytes());
		output.close();
	}
	
	//�����ļ���ȡ����
	public String read(String fileName) throws Exception{
		FileInputStream input = mContext.openFileInput(fileName);
		//����1M�Ļ�����
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
