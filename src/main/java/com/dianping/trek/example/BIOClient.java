package com.dianping.trek.example;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import com.dianping.trek.decoder.DecodeResult;
import com.dianping.trek.decoder.LogMsgCoder;
import com.dianping.trek.decoder.LogMsgCoderImpl;
import com.dianping.trek.server.TrekContext;
import com.dianping.trek.server.TrekServer;

public class BIOClient {

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
        String host = args[0].trim();
        int port = Integer.parseInt(args[1].trim());
        String filePath = args[2].trim();
        int frequency = Integer.parseInt(args[3].trim());
        LogMsgCoder coder=new LogMsgCoderImpl();
        byte[] inputData = getDataFromFile(filePath);
        Properties prop = new Properties();
        prop.load(TrekServer.class.getClassLoader().getResourceAsStream("config.properties"));
        String encryKey = prop.getProperty("trek.encryKey");
        if (encryKey == null) {
            System.exit(1);
        }
        TrekContext.getInstance().setEncryKey(encryKey);
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
        Socket socket = new Socket(host, port);
        OutputStream outputStream = socket.getOutputStream();
        while(true) {
            for (int j = 0; j < frequency; j++) {
                outputStream.write(inputData);
            }
            outputStream.flush();
            Thread.sleep(1000);
        }
//        socket.close();
    }
}
