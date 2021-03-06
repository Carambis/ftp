package com.company.client;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class FTPClient {

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private BufferedReader bufferedReader;

    public FTPClient(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void sendFile() throws IOException {
        String filename;
        System.out.print("Enter File Name :");
        filename = bufferedReader.readLine();

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not Exists...");
            dataOutputStream.writeUTF("File not found");
            return;
        }

        dataOutputStream.writeUTF(filename);

        String msgFromServer = dataInputStream.readUTF();
        if (msgFromServer.equals("File Already Exists")) {
            String option;
            System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
            option = bufferedReader.readLine();
            if (option.equals("Y")) {
                dataOutputStream.writeUTF("Y");
            } else {
                dataOutputStream.writeUTF("N");
                return;
            }
        }

        System.out.println("Sending File ...");
        FileInputStream fileInputStream = new FileInputStream(file);
        int ch;
        do {
            ch = fileInputStream.read();
            dataOutputStream.writeUTF(String.valueOf(ch));
        }
        while (ch != -1);
        fileInputStream.close();
        System.out.println(dataInputStream.readUTF());

    }

    public void receiveFile() throws IOException {
        String fileName;
        System.out.print("Enter File Name :");
        fileName = bufferedReader.readLine();
        dataOutputStream.writeUTF(fileName);
        String msgFromServer = dataInputStream.readUTF();

        if (msgFromServer.equals("File Not Found")) {
            System.out.println("File not found on Server ...");
        } else if (msgFromServer.equals("READY")) {
            System.out.println("Receiving File ...");
            File file = new File(fileName);
            if (file.exists()) {
                String option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                option = bufferedReader.readLine();
                if (option.equals("N")) {
                    dataOutputStream.flush();
                    return;
                }
            }
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
            System.out.println(dataInputStream.readUTF());
        }
    }

    public void displayMenu() throws Exception {
        while (true) {
            System.out.println("[ MENU ]");
            System.out.println("1. Send File");
            System.out.println("2. Receive File");
            System.out.println("3. Change repository");
            System.out.println("4. Get List File");
            System.out.println("5. Exit");
            System.out.print("\nEnter Choice :");
            int choice;
            choice = Integer.parseInt(bufferedReader.readLine());
            switch (choice) {
                case 1:
                    dataOutputStream.writeUTF("SEND");
                    sendFile();
                    break;
                case 2:
                    dataOutputStream.writeUTF("GET");
                    receiveFile();
                    break;
                case 3:
                    dataOutputStream.writeUTF("CHANGE");
                    changeRepository();
                    break;
                case 4:
                    dataOutputStream.writeUTF("GET_LIST");
                    getListFile();
                    break;
                case 5:
                    dataOutputStream.writeUTF("DISCONNECT");
                    System.exit(1);
                    break;
            }

        }
    }

    private void getListFile() throws IOException {
        String message = dataInputStream.readUTF();
        System.out.println("Files");
        String[] listFile = message.split("@");
        if(listFile.length == 1){
            System.out.println(message + "\n");
            return;
        }
        for (int i = 0; i < listFile.length; i = i + 3) {
            System.out.println("Name: " + listFile[i] + " Time Create: " + listFile[i + 1] + " Size: " + listFile[i + 2] + " Bytes");
        }
        System.out.println();
    }

    private void changeRepository() throws IOException{
        String curRepo = dataInputStream.readUTF();
        System.out.println(curRepo);
        System.out.println("Input new repo");
        String newRepo = bufferedReader.readLine();
        dataOutputStream.writeUTF(newRepo);
    }

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("127.0.0.1", 8080);
        FTPClient ftpClient = new FTPClient(socket);
        ftpClient.displayMenu();
    }
}