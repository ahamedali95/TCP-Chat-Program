import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatServer implements Runnable {

    private int clientCount = 0;
    private ChatServerThread clients[] = new ChatServerThread[50];
    private ServerSocket server = null;
    Thread thread = null;

    //same as version3
    public ChatServer(int port){
        try{
            server = new ServerSocket(port);//step1
            System.out.println("Started the server...waiting for a client");
            start(); //the chatserver's start method that goes ahead and creates a new thread
        }
        catch(IOException e){
            System.err.println("ERROR "+e.getMessage());

        }
    }

    public void start(){
        if(thread == null){
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {//same as version 3
        while(thread !=null){
            try{
                System.out.println("Waiting for a client...");
                //now we add a new Thread and accept a client
                addThread(server.accept());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void addThread(Socket socket){
        if(clientCount < clients.length){
            clients[clientCount] = new ChatServerThread(this, socket);
            try {
                clients[clientCount].open();//open the stream for the ChatServerThread client
                clients[clientCount].start();//start to run the ChatServerThread client
                clientCount++;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //--------------------------------------------------------//
    //Here once the message is starting with encrypt then we should not again go in the next if
    //else statement.. earlier because it was just if the code was going into the next if else statement

    public synchronized void handle(int ID, String input)
    {
        //Check if input is in the form "encrypt id key message"
        if(input.startsWith("encrypt"))
        {
            String[] inArr = input.split(" "); //Split the input by space
            int sendToId = Integer.parseInt(inArr[1]); //Parse id from string
            int clientID = findClient(sendToId); //call findClient method to get the index for id

            String currentKey = inArr[2]; //parse the key
            int currentKeyLength = currentKey.length(); //get the key length
            String encryptedMessage = input.substring(14+ currentKeyLength+1, input.length()); //parse the encrypted message("encrypt 51999 KEYLENGTH message")

            String decrypted = decrypt(currentKey, encryptedMessage); //call decryptedMessage to decrypt the encrypted message
            System.out.println("DECRYPTED MESSAGE: " + decrypted);
            clients[clientID].send("User: "+ ID + ": "+ decrypted);

        }
        //*****//
        //Check if input is in the form "secret id message"
        else if(input.startsWith("secret"))
        {
            String[] inputArr = input.split(" "); //Split the input by space
            int sendToId = Integer.parseInt(inputArr[1]); //Parse id from string
            int clientIndex = findClient(sendToId); //Call findclient method to get the index for id
            clients[clientIndex].send("User: " + ID + ": " + input.substring(13, input.length())); //Send to index found in the above method //clients[clientIndex].send("User: " + ID + ": "+ inputArr[3]);
        }

        //Else treat it as normal message which is to be sent to everyone
        else{
            for(int i=0; i<clientCount; i++){
                //add line of code to print the user's message
                //on the server side for spying
                clients[i].send("User: "+ ID + ": "+input);
            }
            if(input.equalsIgnoreCase("bye")){
                remove(ID);//person said bye so remove them
            }
        }
    }

    public String decrypt(String currentKey, String encMsg)
    {
        String decMsg = "";
        for (int i = 0; i < encMsg.length(); i++)
        {
            int numForEncChar = getNumFromChar(encMsg.charAt(i));
            int numForKeyChar = getNumFromChar(currentKey.charAt(i));
            int numForPlainChar = numForEncChar - numForKeyChar;
            char plainChar = getCharFromNum(numForPlainChar);
            decMsg += plainChar;
        }
        return decMsg;
    }

    private int getNumFromChar(char c)
    {
        return Character.valueOf(c);
    }

    private char getCharFromNum(int num)
    {
        return Character.toChars(num)[0];
    }

    public synchronized void remove(int ID){
        int position = findClient(ID);
        if(position >=0){
            ChatServerThread toRemove = clients[position];
            if(position <clientCount-1){
                for(int i= position+1; i <clientCount; i++){
                    clients[i-1] = clients[i];
                }
                clientCount--;
            }
            try {
                toRemove.close();//close the person's that said bye connection
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    private int findClient(int ID){
        for(int i=0; i<clientCount; i++){
            if(clients[i].getID() == ID){
                return i;
            }
        }
        return -1;//not in the array
    }

    public static void main(String [] args){
        ChatServer l = new ChatServer(9999);
    }



}