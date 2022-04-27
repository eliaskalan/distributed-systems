package controller;

import model.ProfileName;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static utils.socketMethods.closeEverything;

public class Broker {
    String hash;
    private int port;
    private String ip;
    public ServerSocket socket;
    private static ArrayList<Topic> Topics = new ArrayList<Topic>();

    Broker(String ip, int port) {
        this.port = port;
        this.ip = ip;
//        calculateKeys();
        Topics.add(new Topic("DS"));
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void connect() {
        try {
            socket = new ServerSocket(port, 10);
            System.out.println("Broker is live!");
            while (true) {
                Socket connection = socket.accept();
                ClientHandler clientSock = new ClientHandler(connection);
                new Thread(clientSock).start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public int getPort() {
        return this.port;
    }

    public String getIp() {
        return this.ip;
    }

    public int calculateKeys(String topicname) {
        //Todo we want to return the hash key, compare variables or update an array?
        try {
            String hashtext1, hashtext2;

            //  hashing MD5
            MessageDigest md1 = MessageDigest.getInstance("MD5");
            MessageDigest md2 = MessageDigest.getInstance("MD5");
            //Todo we want also to add ip

            byte[] messageDigest1 = md1.digest((Integer.toString(this.getPort()) + this.getIp()).getBytes());
            byte[] messageDigest2 = md2.digest(topicname.getBytes());

            BigInteger no1 = new BigInteger(1, messageDigest1);
            hashtext1 = no1.toString(16);
            BigInteger no2 = new BigInteger(1, messageDigest2);
            hashtext2 = no2.toString(16);

            while (hashtext1.length() < 32) {
                hashtext1 = "0" + hashtext1;
            }
            while (hashtext2.length() < 32) {
                hashtext2 = "0" + hashtext2;
            }

            String hashText = hashtext1 + hashtext2;

//            this.hash =  hashText.hashCode();

            return (hashText.hashCode()) % 3;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        Broker broker = new Broker("localhost", 12345);
        broker.connect();
//        System.out.println(broker.calculateKeys("DSasgstbxfgbxfA")); // TODO results must be 0, 1, or 2 because of %3. For some values is -1
    }



    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader bufferedReader;
        private BufferedWriter bufferedWriter;
        private String clientUsername;
        public static ArrayList<ClientHandler> registerClient = new ArrayList<>();
        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            try{
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.clientUsername = bufferedReader.readLine();
                acceptConnection(this);
                this.broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
                Topics.get(0).addUser(new ProfileName(clientUsername)); // TODO get(0) must change to a method that return Topics by topicname

            }catch (IOException e){
                removeClient();
                closeEverything(socket, bufferedReader, bufferedWriter);
            }

        }

        private void acceptConnection(ClientHandler client){
            this.registerClient.add(client);
        }

        public void broadcastMessage(String messageToSend) {
            for (ClientHandler client : registerClient) {
                if(!client.clientUsername.equals(clientUsername)){
                    try {
                        client.bufferedWriter.write(messageToSend);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    } catch (NullPointerException | IOException e) {
                        removeClient();
                        closeEverything(clientSocket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }
        public void removeClient() {
            System.out.println(this.clientUsername + " has left the server");
            registerClient.remove(this);
            broadcastMessage("SERVER:" + this.clientUsername + "has left the chat!");
        }

        public void run() {
            String messageFromClient;
            while (clientSocket.isConnected()) {
                try {
                    messageFromClient = bufferedReader.readLine();
                    broadcastMessage(messageFromClient);
                    System.out.println("Server log: " + messageFromClient);

                    String[] arrOfStr = messageFromClient.split(": ");
                    String userid = Topics.get(0).getUserIDbyName(arrOfStr[0]);
                    Topics.get(0).addMessage(arrOfStr[1], userid, arrOfStr[0]);

                } catch (IOException e) {
                    closeEverything(clientSocket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }
    }

    private static int getIndexByTopic(String topic){
        return Topics.indexOf(topic);
    }
}
