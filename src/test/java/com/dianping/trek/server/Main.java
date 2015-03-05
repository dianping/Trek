package com.dianping.trek.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.dianping.trek.server.decoder.DecodeResult;
import com.dianping.trek.server.decoder.LogMsgCoder;
import com.dianping.trek.server.decoder.LogMsgCoderImpl;

public class Main {

    private static byte[] getDataFromFile(String filePath) throws IOException{
        FileInputStream fin=null;
        try{
            fin=new FileInputStream(new File(filePath));
            int readBytes=0;
            byte[] buffer=new byte[1024];
            ByteArrayOutputStream stream=new ByteArrayOutputStream(1024);
            while((readBytes=fin.read(buffer))>0){
                stream.write(buffer, 0, readBytes);
            }
            return stream.toByteArray();
        }
        finally{
            if(fin!=null){
                fin.close();
            }
        }
    }
    public static void main(String[] args) throws Exception {
        String filePath=args[0].trim();
        LogMsgCoder coder=new LogMsgCoderImpl();
        byte[] inputData=getDataFromFile(filePath);
        
        if(coder.isValidMsg(inputData)){
            DecodeResult result=coder.decode(inputData);
            System.out.println("\n<----------------------------->\n");
            System.out.println(result);
            System.out.println("\n<----------------------------->\n");
        }
        else{
            System.out.println("\n<----------------------------->\n");
            System.out.println("invalid data!");
            System.out.println("\n<----------------------------->\n");
        }
        
    }

}
