package com.dianping.trek;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import com.dianping.trek.decoder.DecodeResult;
import com.dianping.trek.decoder.LogMsgCoder;
import com.dianping.trek.decoder.LogMsgCoderImpl;

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
        String filePath = args[0].trim();
        LogMsgCoder coder=new LogMsgCoderImpl();
        byte[] inputData = getDataFromFile(filePath);
        
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
        
        
        Socket socket = new Socket("localhost", 8080);
        OutputStream outputStream = socket.getOutputStream();
        for (int i = 0; i < 200; i++) {
            outputStream.write(inputData);
            outputStream.flush();
        }
        socket.close();
        
    }

}
