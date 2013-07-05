package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.tu_berlin.kbs.reflect.InvokeThis;

import wrapper.JobWrapper;
import wrapper.MethodWrapper;

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

			in = new BufferedReader(new InputStreamReader(
					clientSendSocket.getInputStream()));
			out = new PrintWriter(clientReceiveSocket.getOutputStream(), true);

			objectInputStream = new ObjectInputStream(
					clientSendSocket.getInputStream());
			outData = new DataOutputStream(
					clientReceiveSocket.getOutputStream());

			Object inputObject = objectInputStream.readObject();

			try {
				final JobWrapper job = (JobWrapper) inputObject;

				File codeFile = saveJobCodeFile(job);

				if (job.getFileName().endsWith(".java")) {
					compileJavaFile(codeFile);
				}

				loadClassAndExecMethods(job.getBinaryClassName(), "temp",
						job.getMethodCalls());
				// TODO

			} catch (ClassCastException e) {
				e.printStackTrace();
			}

		} catch (Exception ex) {
			Logger.getLogger(Server.class.getName())
					.log(Level.SEVERE, null, ex);
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
				Logger.getLogger(Server.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		}
	}

	private static File saveJobCodeFile(final JobWrapper job) {
		final String tempDirName = "temp";

		final File tmpDir = new File(tempDirName);
		if (!tmpDir.exists()) {
			tmpDir.mkdir();
		}
		final String filePath = tempDirName + "/" + job.getFileName();
		File file = new File(filePath);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] byteArray = new byte[job.getCode().length];
		for (int i = 0; i < byteArray.length; i++) {
			byteArray[i] = (byte) job.getCode()[i];
		}
		try {
			fos.write(byteArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	private static void compileJavaFile(final File file) {
		File[] files1 = { file };

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				null, null, null);

		Iterable<? extends JavaFileObject> compilationUnits1 = fileManager
				.getJavaFileObjectsFromFiles(Arrays.asList(files1));
		compiler.getTask(null, fileManager, null, null, null, compilationUnits1)
				.call();

		try {
			fileManager.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void loadClassAndExecMethods(String binaryClassName,
			String classDir, final MethodWrapper[] methods) {
		classDir = "temp";
		// Create a File object on the root of the directory containing the
		// class file
		File file = new File(classDir);

		try {
			// Convert File to a URL
			URL url = file.toURI().toURL();
			URL[] urls = new URL[] { url };

			// Create a new class loader with the directory
			ClassLoader cl = new URLClassLoader(urls);

			// Load in the class; MyClass.class should be located in
			// the directory file:/c:/myclasses/com/mycompany
			Class cls = cl.loadClass(binaryClassName);

			for (final Method method : cls.getMethods()) {
				for (final MethodWrapper mw : methods) {
					Object instance = cls.newInstance();
					Object object = null;
					Method invokingMethod = getStaticAuthorizedMethod(method);
					invokingMethod = (mw.getName().equals(method.getName())) ? method
							: invokingMethod;
					if (invokingMethod != null) {
						try {
							if (Modifier
									.isStatic(invokingMethod.getModifiers())) {
								object = invokingMethod.invoke(null,
										mw.getArgs());
							} else {
								object = invokingMethod.invoke(instance,
										mw.getArgs());
							}
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}

						System.out.println("Result: " + object);
						mw.setResult(object);
					}
				}
			}
		} catch (MalformedURLException e) {
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param method
	 * @return
	 */
	private static Method getStaticAuthorizedMethod(Method method) {
		Annotation[] annotations = method.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof InvokeThis) {
				return method;
			}
		}
		return null;
	}

}
