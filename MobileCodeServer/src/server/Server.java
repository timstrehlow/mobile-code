/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Patrick
 */
public class Server {

    private ServerSocket recieveSocket;
    private ServerSocket sendSocket;

	private static int count = 0;

    public static void main(String[] args) {
        new Server().start();
    }

    private void start() {
        try {
            sendSocket = new ServerSocket(4321);
            recieveSocket = new ServerSocket(1234);
            while (true) {

                Socket clientReceiveSocket = sendSocket.accept();
                Socket clientSendSocket = recieveSocket.accept();

				new Thread(new Connection(clientSendSocket,
						clientReceiveSocket, count++)).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
    }
}
