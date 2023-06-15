package com.uracles.ChatApp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService threadPool;
    public Server(){
        connections= new ArrayList<ConnectionHandler>();
        done=false;
    }

    public void run() {

        try {
            server = new ServerSocket(6500);
            threadPool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }


    }
    public void broadcast(String message){
        for (ConnectionHandler ch : connections){
            if(ch!= null){
                ch.sendMessage(message);
            }
        }
    }
    public void shutdown()  {
        done = true;
        if(!server.isClosed()){
            try {
                server.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (ConnectionHandler ch: connections
        ) {
            ch.shutdown();
        }

    }
    class ConnectionHandler implements Runnable{
        private  Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ConnectionHandler(Socket client){
            this.client= client;
        }

        public void run() {
            try {
                out= new PrintWriter(client.getOutputStream(), true);
                in= new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter nickname: ");
                username = in.readLine();
                if (username == null) username ="user 1";
                System.out.println(username + "connected!");
                broadcast(username + " joined the chat.");
                String clientMessage;
                while((clientMessage=in.readLine())!=null){
                    if(clientMessage.startsWith("/nick ")){
                        String[] clientMessageSplit = clientMessage.split(" ", 2);
                        if(clientMessageSplit.length==2){
                            broadcast(username + "renamed themselves to " + clientMessageSplit[1]);
                            System.out.println(username + "renamed themselves to " + clientMessageSplit[1]);
                            username = clientMessageSplit[1];
                            out.println("Successfully changed nickname to " + username);
                        }else {
                            out.println("No nickname provided");
                        }
                    }else if(clientMessage.startsWith("/quit")){
                        broadcast(username +" left the chat.");
                        shutdown();
                    }else {
                        broadcast(username + ": " + clientMessage);
                    }
                }

            }catch (IOException e){
                shutdown();
            }

        }
        public void sendMessage(String message){
            out.println(message);
        }
        public void shutdown(){
            try {
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            }catch (IOException e){
            }
        }
    }

    public static void main(String[] args) {
        Server server1 = new Server();
        server1.run();
    }
}
