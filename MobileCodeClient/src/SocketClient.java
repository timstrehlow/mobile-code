/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import wrapper.JobWrapper;
import wrapper.MethodWrapper;

/**
 *
 * @author Patrick
 */
public class SocketClient implements Runnable {
	private static final String SOURCE_FILE = "HelloWorld.java";
	private static final String BYTE_FILE = "HelloWorld.class";

    public static int count = 0;
    private String name;

    public static void main(String[] args) throws IOException {

        SocketClient client = new SocketClient("1");

    }

    public SocketClient(String name) {
        this.name = name;
        new Thread(this).start();
    }

    @Override
    public void run() {
		ObjectOutputStream outputStream = null;
        try {
            SocketClient.count++;
            Socket recievSocket = new Socket("localhost", 4321);
            Socket sendSocket = new Socket("localhost", 1234);

            System.out.println("Connect with server (" + sendSocket.getPort() + " / " + recievSocket.getPort() + ")");

			outputStream = new ObjectOutputStream(
					sendSocket.getOutputStream());
			JobWrapper job = new JobWrapper();
			job.setCode(readCodeFromFile(new File(SOURCE_FILE)));
			job.setFileName(SOURCE_FILE);
			job.setBinaryClassName("HelloWorld");
			MethodWrapper method = new MethodWrapper();
			method.setName("doSomething");

			MethodWrapper[] methodsToCall = { method };

			job.setMethodCalls(methodsToCall);

			outputStream.writeObject(job);

        } catch (UnknownHostException ex) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    }

	private static char[] readCodeFromFile(File f) {

		byte[] mybytearray = new byte[(int) f.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedInputStream bis = new BufferedInputStream(fis);
		try {
			bis.read(mybytearray, 0, mybytearray.length);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		char[] charArray = new char[(int) f.length()];
		for (int i = 0; i < charArray.length; i++) {
			charArray[i] = (char) mybytearray[i];
		}
		return charArray;
	}
}
