/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
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

	public static int count = 0;
	private JobWrapper job;

	public static void main(String[] args) throws IOException {
		JobWrapper jobTest = new JobWrapper();
		jobTest.setFileName("Test.java");

		MethodWrapper methodAdd = new MethodWrapper();
		methodAdd.setName("add");
		methodAdd.setArgs(new Object[] { 3, 5 });

		MethodWrapper methodMultiply = new MethodWrapper();
		methodMultiply.setName("multiply");
		methodMultiply.setArgs(new Object[] { 3, 5 });

		jobTest.setMethodCalls(new MethodWrapper[] { methodAdd, methodMultiply });

		new SocketClient(jobTest);

		// HelloWorld.class:
		// Name: de.tu_berlin.kbs.mwk.test.HelloWorld
		// Method 1: public static java.lang.Object helloWorld()

		JobWrapper jobHelloWorld = new JobWrapper();
		jobHelloWorld.setFileName("HelloWorld.class");

		MethodWrapper methodHello = new MethodWrapper();
		methodHello.setName("helloWorld");

		MethodWrapper[] methodsToCallHelloWorl = { methodHello };

		jobHelloWorld.setMethodCalls(methodsToCallHelloWorl);

		new SocketClient(jobHelloWorld);

		// Echo.class:
		// Name: de.tu_berlin.kbs.mwk.test.Echo
		// Method 1: public static java.lang.String echo(java.lang.String s)
		// Method 2: public static java.lang.String echo(java.lang.String s,
		// java.lang.Integer n)
			
		JobWrapper jobEcho = new JobWrapper();
		jobEcho.setFileName("Echo.class");

		MethodWrapper methodEcho = new MethodWrapper();
		methodEcho.setName("echo");
		methodEcho.setArgs(new Object[] { "bla" });

		MethodWrapper methodEcho2 = new MethodWrapper();
		methodEcho2.setName("echo");
		methodEcho2.setArgs(new Object[] { "bla", 3 });

		MethodWrapper[] methodsToCallEcho = { methodEcho, methodEcho2 };

		jobEcho.setMethodCalls(methodsToCallEcho);

		new SocketClient(jobEcho);

		// Annotated:
		// Name: de.tu_berlin.kbs.mwk.test.Annotated
		// Methods are marked by de.tu_berlin.kbs.reflect.InvokeThis
		JobWrapper jobAnnotated = new JobWrapper();
		jobAnnotated.setFileName("Annotated.class");

		new SocketClient(jobAnnotated);
	}

	public SocketClient(JobWrapper jw) {
		this.job = jw;
		new Thread(this).start();
	}

	@Override
	public void run() {
		ObjectInputStream inputStream = null;
		ObjectOutputStream outputStream = null;
		try {
			SocketClient.count++;
			Socket recievSocket = new Socket("localhost", 4321);
			Socket sendSocket = new Socket("localhost", 1234);

			System.out.println("Connect with server (" + sendSocket.getPort()
					+ " / " + recievSocket.getPort() + ")");

			outputStream = new ObjectOutputStream(sendSocket.getOutputStream());
			job.setCode(readCodeFromFile(new File(job.getFileName())));

			outputStream.writeObject(job);
			
			inputStream = new ObjectInputStream(recievSocket.getInputStream());

			try {
				JobWrapper receivedJob = (JobWrapper) inputStream.readObject();

				System.out.println("Received Results - Filename: "
						+ receivedJob.getFileName());
				System.out.println();
				for (final MethodWrapper mw : receivedJob.getMethodCalls()) {
					System.out.println("Method: " + mw.getName());
					System.out.println("Result: " + mw.getResult());
					System.out.println();
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (UnknownHostException ex) {
			Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (IOException ex) {
			Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE,
					null, ex);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void initMethodWrapper() {

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
