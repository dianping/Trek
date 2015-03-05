package com.dianping.trek.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.xerial.snappy.Snappy;

public class CompressUtil {

	
	public static ByteBuffer zlibDecompress(byte[] data) throws IOException{
		ByteArrayInputStream bin=new ByteArrayInputStream(data);
		InflaterInputStream iis=new InflaterInputStream(bin,new Inflater(),data.length);
		try{
			ByteArrayOutputStream bao=new ByteArrayOutputStream(4096);
			byte[] buffer=new byte[4096];
			int readNum=0;
			while( (readNum=iis.read(buffer))!=-1){
				bao.write(buffer, 0, readNum);
			}
			return ByteBuffer.wrap(bao.toByteArray());
		}
		finally{
			if(iis!=null){
				iis.close();
			}
		}
	}
	
	public static ByteBuffer snappyDecompress(byte[] data) throws IOException{
		byte[] uncompressed = Snappy.uncompress(data);
		return ByteBuffer.wrap(uncompressed);
	}
}
