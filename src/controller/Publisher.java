package controller;


import model.MultimediaFile;
import model.ProfileName;
import model.Value;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import static utils.socketMethods.closeEverything;

public class Publisher{

    private BufferedWriter bufferedWriter;
    private Socket socket;
    ProfileName profileName;
    public final static String FILE_TO_SEND = MultimediaFile.FOLDER_SAVE + "new.jpeg";
    Publisher(Socket socket, ProfileName profileName) throws IOException {
        this.socket = socket;
        this.profileName = profileName;
        try{
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ioException) {
            System.out.println("There was a problem in the connection of the client");
            closeEverything(socket, bufferedWriter);
        }
    }


//    public void push(String a, Value b) {
//        //ToDo string a is hash code?
//        try{
//            out.writeChars(a);
//            out.flush();
//        }catch (IOException ioException){
//
//        }
//    }

    public void sendMessage(){
                try {
                    Scanner scanner = new Scanner(System.in);
                    while (socket.isConnected()) {
                        String messageToSend = scanner.nextLine();
                        bufferedWriter.write(this.profileName.getProfileName() + ": " + messageToSend);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedWriter);
                }

    }

    public void sendOneTimeMessage(String messageToSend) throws IOException {
        bufferedWriter.write(this.profileName.getProfileName() + ": " + messageToSend);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }



    public void sendImage() throws IOException {
        Scanner scanner = new Scanner(System.in);
        int a = Integer.parseInt(scanner.nextLine());
        if (a == 1) {
            a=2;
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            OutputStream os = null;


            System.out.println("Waiting...");

            System.out.println("Accepted connection : " + socket);

                File myFile = new File(FILE_TO_SEND);
                byte[] mybytearray = new byte[(int) myFile.length()];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray, 0, mybytearray.length);
                os = socket.getOutputStream();
                System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();
                System.out.println("Done.");
        }
    }
}