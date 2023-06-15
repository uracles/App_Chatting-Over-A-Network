package com.uracles.ChatApp;

import java.io.*;
import java.net.*;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;


    @Override
    public void run() {

        try {
            Socket client = new Socket("192.168.0.234", 6500);
            out= new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();
            String inMessage;
            while((inMessage=in.readLine()) != null){
                System.out.println(inMessage);
            }
        }catch (IOException e){
            shutdown();
        }
    }
    public void shutdown(){
        done = true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }

        }catch (IOException e){
            //ignore
        }
    }


    class InputHandler implements Runnable{

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message = null;

                    message = inReader.readLine();

                    if (message.equals("/quit")){
                        inReader.close();
                        shutdown();
                    }else{
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {

        Client client = new Client();
        client.run();

    }

}
