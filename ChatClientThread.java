import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClientThread extends Thread
{
    private Socket socket = null;

    private ChatClient client = null;

    DataInputStream strIn = null;

    private boolean done = true;

    public ChatClientThread(ChatClient theClient, Socket theSocket)
    {
        client = theClient;
        socket = theSocket;
        open();
        start();
    }

    public void open()
    {
        try
        {
            strIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }
        catch(IOException e)
        {
            System.out.println("inside ChatClient Thread open.. IOException");

        }
    }

    public void close()
    {
        if(strIn !=null)
        {
            try
            {
                strIn.close();
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }
        }
    }

    public void run()
    {
        done = false;
        while(!done)
        {
            try
            {
                client.handle(strIn.readUTF());
            }
            catch(IOException e)
            {
                client.stop();//will write this
                done = true;
            }
        }
    }

}