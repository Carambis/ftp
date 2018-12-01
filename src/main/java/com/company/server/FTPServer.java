package com.company.server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class FTPServer extends Thread {
    private String repository = "server/";
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public FTPServer(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("FTP Client Connected ...");
        start();
    }

    public void sendFile() throws IOException {
        String filename = dataInputStream.readUTF();
        File file = new File(repository + filename);
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
        File file = new File(repository + filename);
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
                        System.out.println("\tSEND Command Received ...");
                        receiveFile();
                        break;
                    case "CHANGE":
                        System.out.println("\tCHANGE Command Received ...");
                        changeRepository();
                        break;
                    case "GET_LIST":
                        System.out.println("\tGET_LIST Command Received ...");
                        getListFile();
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

    private void getListFile() throws IOException {
        StringBuilder message = new StringBuilder();
        File folder = new File(repository);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null && listOfFiles.length != 0) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    getInformationString(file, message);
                }
            }
            message.deleteCharAt(message.length() - 1);
            dataOutputStream.writeUTF(message.toString());
        } else {
            dataOutputStream.writeUTF("Empty folder");
        }
    }

    private void getInformationString(File file, StringBuilder message) throws IOException {
        message.append(file.getName()).append("@");
        Path path = file.toPath();
        BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        Timestamp date =
                new Timestamp(basicFileAttributes.creationTime().to(TimeUnit.MILLISECONDS));
        message.append(date.toString()).append("@");
        message.append(basicFileAttributes.size()).append("@");
    }

    private void changeRepository() throws IOException {
        dataOutputStream.writeUTF(repository);
        repository = dataInputStream.readUTF();
        if (repository.charAt(repository.length() - 1) != '/') {
            repository += "/";
        }
        System.out.println("New Repository: " + repository);
    }

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("FTP Server Started on Port Number 8080");
        while (true) {
            System.out.println("Waiting for Connection ...");
            new FTPServer(serverSocket.accept());
        }
    }
}