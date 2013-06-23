package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import wrapper.JobWrapper;

/**
 *
 * @author Patrick
 */
public class Connection implements Runnable {

    private Socket clientReceiveSocket;
    private Socket clientSendSocket;

    /**
     * 
     * @param sender
     * @param reciever 
     */
    public Connection(Socket sender, Socket reciever) {
        this.clientReceiveSocket = reciever;
        this.clientSendSocket = sender;
    }

    /**
     * 
     */
    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        
        ObjectInputStream objectInputStream = null;
        DataOutputStream outData = null;
        
        try {
            System.out.println("Client has connected");
            
            in = new BufferedReader(new InputStreamReader(clientSendSocket.getInputStream()));
            out = new PrintWriter(clientReceiveSocket.getOutputStream(), true);
            
			objectInputStream = new ObjectInputStream(
					clientSendSocket.getInputStream());
            outData = new DataOutputStream(clientReceiveSocket.getOutputStream());
            
            while (true) { 	
            	
				Object inputObject = objectInputStream.readObject();
				
				try {
					final JobWrapper job = (JobWrapper) inputObject;
					
					// TODO

				} catch (ClassCastException e) {
					e.printStackTrace();
				}

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                    in.close();
                    outData.close();
					objectInputStream.close();
                    clientReceiveSocket.close();
                    clientSendSocket.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
