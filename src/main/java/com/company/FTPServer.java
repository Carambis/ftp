package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer extends Thread {
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    public FTPServer(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("FTP Client Connected ...");
        start();
    }

    public void sendFile() throws IOException {
        String filename = dataInputStream.readUTF();
        File file = new File(filename);
        if (!file.exists()) {
            dataOutputStream.writeUTF("File Not Found");
        } else {
            dataOutputStream.writeUTF("READY");
            FileInputStream fileInputStream = new FileInputStream(file);
            int ch;
            do {
                ch = fileInputStream.read();
                dataOutputStream.writeUTF(String.valueOf(ch));
            }
            while (ch != -1);
            fileInputStream.close();
            dataOutputStream.writeUTF("File Receive Successfully");
        }
    }

    public void receiveFile() throws IOException {
        String filename = dataInputStream.readUTF();
        if (filename.equals("File not found")) {
            return;
        }
        File file = new File(filename);
        String option;

        if (file.exists()) {
            dataOutputStream.writeUTF("File Already Exists");
            option = dataInputStream.readUTF();
        } else {
            dataOutputStream.writeUTF("SendFile");
            option = "Y";
        }

        if (option.equals("Y")) {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int ch;
            String temp;
            do {
                temp = dataInputStream.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) {
                    fileOutputStream.write(ch);
                }
            } while (ch != -1);
            fileOutputStream.close();
            dataOutputStream.writeUTF("File Send Successfully");
        }
    }


    public void run() {
        try {
            while (true) {
                System.out.println("Waiting for Command ...");
                String Command = dataInputStream.readUTF();
                switch (Command) {
                    case "GET":
                        System.out.println("\tGET Command Received ...");
                        sendFile();
                        break;
                    case "SEND":
                        System.out.println("\tSEND Command Receiced ...");
                        receiveFile();
                        break;
                    case "DISCONNECT":
                        System.out.println("\tDisconnect Command Received ...");
                        System.exit(1);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String args[]) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("FTP Server Started on Port Number 8080");
        while (true) {
            System.out.println("Waiting for Connection ...");
            new FTPServer(serverSocket.accept());
        }
    }
}