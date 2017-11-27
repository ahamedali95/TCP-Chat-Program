
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClientGUI extends JFrame implements ActionListener,Runnable
{
    private Socket socket = null;
    private final String serverName = "localhost"; //"localhost"//or your friend's ip address
    private final int serverPortNumber = 9999; //needs to ma

    private DataInputStream strIn = null;
    private DataOutputStream strOut = null;

    private ChatClientThread client = null;
    private boolean done = true;//until connected you are "done"
    private String line = "";

    private JTextArea displayText = new JTextArea();
  //  private JTextArea displayText2 = new JTextArea();
    private JTextField input = new JTextField();
    private JButton btnEncrypt = new JButton();
    private JButton btnConnect = new JButton();
    private JButton btnSend = new JButton();
    private JButton btnQuit = new JButton();
    private JButton btnPrivate = new JButton();
    private JPanel mainJP = new JPanel();//everything goes in here
    private JPanel displayJP = new JPanel();//textarea.. plus whatever
    private JPanel displayJP2 = new JPanel();//textarea.. plus whatever
    private JPanel btnsJP = new JPanel();//put this on the bottom

    private String currentKey = "";

    private String store = "";

    public ChatClientGUI(){
        this.setTitle("Simple TCP Chat");
        mainJP.setLayout(new BorderLayout());
        displayJP.setLayout(new GridLayout(2,1));
        displayJP2.setLayout(new BorderLayout());

        displayText.setBackground(Color.PINK);
        displayText.setFont(new Font("Times New Roman", Font.ITALIC, 20));
        Border br = BorderFactory.createLineBorder(Color.black, 5, true );
        displayText.setBorder(br);

        input.setBackground(Color.cyan);
        input.setFont(new Font("Times New Roman", Font.ITALIC, 20));
        input.setBorder(br);


        displayJP.add(displayText); //added textarea to jpanel
        displayJP.add(input);//added input below textarea to jpanel


       /** displayText2.setText("Instructions: \n For private messaging: \n Enter the keyword 'secret,' \n followed by the client ID \n and the message. \n For example: secret 52010 hello?");
        displayText2.setFont(new Font("Arial",Font.BOLD, 12));
        displayText2.setForeground(Color.red);
        displayText2.setBackground(Color.BLACK);
        ImageIcon c = new ImageIcon("src/ChatClientGUI");
        label.setIcon(c);
        displayJP2.add(label);
        displayJP2.add(displayText2);**/

        btnsJP.setLayout(new GridLayout(1,4));



        ImageIcon v = new ImageIcon("src/lock.jpg");
        btnEncrypt.setIcon(v);
        btnEncrypt.setBackground(Color.WHITE);
        ImageIcon iv = new ImageIcon("src/connect.jpeg");
        btnConnect.setIcon(iv);
        btnConnect.setBackground(Color.WHITE);
        ImageIcon i = new ImageIcon("src/checkmark.png");
        btnSend.setIcon(i);
        btnSend.setBackground(Color.WHITE);
        ImageIcon iii = new ImageIcon("src/secret.png");
        btnPrivate.setIcon(iii);
        btnPrivate.setBackground(Color.WHITE);
        ImageIcon ii = new ImageIcon("src/xmark.png");
        btnQuit.setIcon(ii);
        btnQuit.setBackground(Color.WHITE);

        btnEncrypt.setActionCommand("encryptON");
        btnConnect.setActionCommand("connect");
        btnSend.setActionCommand("send");
        btnPrivate.setActionCommand("private");
        btnQuit.setActionCommand("quit");

        btnEncrypt.setEnabled(false);
        btnSend.setEnabled(false);
        btnPrivate.setEnabled(false);
        btnQuit.setEnabled(false);

        btnEncrypt.addActionListener(this);
        btnPrivate.addActionListener(this);
        btnConnect.addActionListener(this);
        btnSend.addActionListener(this);
        btnQuit.addActionListener(this);

        btnsJP.add(btnEncrypt);
        btnsJP.add(btnPrivate);
        btnsJP.add(btnConnect);
        btnsJP.add(btnSend);
        btnsJP.add(btnQuit);

        mainJP.add(displayJP, BorderLayout.CENTER);//add to center
        mainJP.add(btnsJP, BorderLayout.SOUTH);//add to bottom
      //  mainJP.add(displayText2, BorderLayout.EAST);

        add(mainJP);
        setIconImage((Toolkit.getDefaultToolkit().getImage(getClass().getResource("chat.png"))));

    }

    //*****//
    @Override
    public void run() {
        while(!done){
            try {
                line = strIn.readUTF();
                displayText.setText(line);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();
        if(command.equals("connect"))
        {
            connect(serverName, serverPortNumber);
        }

        if(command.equals("send"))
        {
            send();
        }

        //*****//
        if(command.equals("private"))
        {
            String userInput = input.getText().toString();
            secretMessage(userInput);
        }

        if(command.equals("encryptON"))
        {
            String userInput = input.getText().toString();
            EncryptedSecretMessage(userInput);
        }

        if(command.equals("quit"))
        {
            disconnect();
        }

    }
    public void connect(String serverName, int portNum)
    {
        try
        {
            done=false;
            socket = new Socket(serverName, portNum);
            displayText.setText("Connected with the server");
            btnConnect.setBackground(new java.awt.Color(0,96,169));
            open();
            btnEncrypt.setEnabled(true);
            btnSend.setEnabled(true);
            btnQuit.setEnabled(true);
            btnPrivate.setEnabled(true);

        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            done=true;
            displayText.setText("Unable to connect to connect to the Host");

        }
        catch (IOException e)
        {
            e.printStackTrace();
            done=true;
            displayText.setText("Host not available");
        }



    }


    //*****//
    public void send()
    {
        String input1 = input.getText().toString();
      /**  if(input1.startsWith("encrypt"))
        {
            EncryptedSecretMessage(input1);


        }
        else
        if(input1.startsWith("secret"))
        {
            secretMessage(input1);

        }**/


            normalMessage(input1);



    }


    public void EncryptedSecretMessage(String userInput)
    {
        System.out.println("INPUT: " + userInput);
        String[] inArr = userInput.split(" "); //Split the input by spaces
        int sendToId = Integer.parseInt(inArr[0]); //Parse id from string
        String message = userInput.substring(6,userInput.length()); //parse actual message from the input
        System.out.println("PARSED MESSAGE: " + message);
        generateKey(message); //generate the key needed to encrypt the message
        System.out.println("GENERATED KEY: " + currentKey);
        String encryptedMessage = encrypt(message); //encrypted message ready
        System.out.println("ENCRYPTED MESSAGE: "+encryptedMessage);
        String encryptedMessagePLUS = "encrypt "+ sendToId + " " + currentKey + " " + encryptedMessage; //attach Client ID with the encrpyted message and the generated key and stream out
        System.out.println("MESSAGE THAT IS BEING STREAM OUT: "+ encryptedMessagePLUS);


        try {
            strOut.writeUTF(encryptedMessagePLUS);
            strOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        input.setText("");
        store = userInput;
        displayText.setText("ME: " + store.substring(6,store.length()));
        System.out.println("MESSAGE THAT SHOULD BE DISPLAYED ON THE TEXTAREA OF THE CLIENT GUI: " + store.substring(6,store.length()));


    }

    public void secretMessage(String userInput)
    {
        try {
            strOut.writeUTF("secret " + userInput);
            strOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        input.setText("");
        displayText.setText("ME: " + userInput.substring(6,userInput.length()));
    }

    public void normalMessage(String userInput)
    {
        try {
            strOut.writeUTF(userInput);
            strOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        input.setText("");
    }

    public String encrypt(String plainMsg)
    {
        String encMsg = "";
        for (int i = 0; i < plainMsg.length(); i++)
        {
            int numForPlainChar = getNumFromChar(plainMsg.charAt(i));//get num from plain char
            int numForKeyChar = getNumFromChar(currentKey.charAt(i));//get num from the key's char
            int numForEncChar = numForPlainChar + numForKeyChar;
            char encryptedChar = getCharFromNum(numForEncChar);
            encMsg += encryptedChar;// append the char to out encrypted message
        }
        return encMsg;
    }

    private int getNumFromChar(char c)
    {
        return Character.valueOf(c);
    }

    public void generateKey(String plainMsg)
    {
        String key = "";
        for (int i = 0; i < plainMsg.length(); i++) {
            int randNum = 64 + (int) (Math.random() * 26);
            key += getCharFromNum(randNum);
        }
        currentKey = key;
    }

    private char getCharFromNum(int num)
    {
        return Character.toChars(num)[0];
    }

    //*****//
    public void disconnect()
    {
        done=true;
        displayText.setText("BYE");
        send();
        btnConnect.setEnabled(true);
        btnConnect.setBackground(Color.WHITE);
        btnEncrypt.setEnabled(false);
        btnSend.setEnabled(false);
        btnQuit.setEnabled(false);
        btnPrivate.setEnabled(false);

        if (socket != null)
            socket = null ;
        if (strIn != null)
            strIn = null ;
        if (strOut != null)
            strOut = null ;
    }
    public void open(){
        try {
            strOut = new DataOutputStream(socket.getOutputStream());
            strIn = new DataInputStream(socket.getInputStream());
            new Thread(this).start();//to be able to listen in

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater( new Runnable(){
                                        public void run() {
                                            ChatClientGUI chatclient = new ChatClientGUI();
                                            chatclient.setSize(500,500);
                                            chatclient.pack();

                                            chatclient.setVisible(true);
                                        }


                                    }

        );
    }

}