import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatServerThread extends Thread{


    Socket socket = null;
    private ChatServer server = null;
    private  int ID = -1;
    private DataInputStream strIn = null;
    private DataOutputStream strOut = null;//new in version4
    boolean done = true;


    public ChatServerThread(ChatServer chatServer, Socket theSocket){//same as version 3
        server = chatServer;
        socket = theSocket;
        ID = socket.getPort();
        System.out.println("Chat Server Info - THREAD INFO - "+
                server + "SOCKET: "+socket+" ID: "+ ID) ;
    }

    public int getID(){
        return ID;
    }
    public void run(){
        while(ID !=-1){
            try {
                server.handle(ID, strIn.readUTF());
            } catch (IOException e) {
                server.remove(ID);
                ID = -1;
            }
        }
    }
    public void close() throws IOException{//same as version 1,version2
        if(strIn!=null){
            strIn.close();
        }
        if(strOut!=null){//new in version 4
            strOut.close();
        }
        if(socket!=null){
            socket.close();
        }
    }
    public void open() throws IOException{//same as version 1,version2
        strIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        strOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

    }

    public void send(String message){
        try {
            strOut.writeUTF(message);
            strOut.flush();
        } catch (IOException e) {
            server.remove(ID); //remove the client... via the ChatServer
            ID = -1;
        }
    }

}