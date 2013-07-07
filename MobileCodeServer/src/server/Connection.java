package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import wrapper.JobWrapper;
import wrapper.MethodWrapper;
import de.tu_berlin.kbs.reflect.InvokeThis;

/**
 * 
 * @author Patrick
 */
public class Connection implements Runnable {

	private final Socket clientReceiveSocket;
	private final Socket clientSendSocket;
	private final int id;
	/**
	 * 
	 * @param sender
	 * @param reciever
	 */
	public Connection(Socket sender, Socket reciever, int id) {
		this.clientReceiveSocket = reciever;
		this.clientSendSocket = sender;
		this.id = id;
	}

	/**
     * 
     */
	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;

		ObjectInputStream objectInputStream = null;
		ObjectOutputStream objectOutputStream = null;

		try {
			System.out.println("Client has connected");

			in = new BufferedReader(new InputStreamReader(
					clientSendSocket.getInputStream()));
			out = new PrintWriter(clientReceiveSocket.getOutputStream(), true);

			objectInputStream = new ObjectInputStream(
					clientSendSocket.getInputStream());
			objectOutputStream = new ObjectOutputStream(
					clientReceiveSocket.getOutputStream());

			Object inputObject = objectInputStream.readObject();

			try {
				final JobWrapper job = (JobWrapper) inputObject;

				final String classDir = "temp/" + id;
				new File("temp").mkdir();
				File codeFile = saveJobCodeFile(job, classDir);

				char[] code = job.getCode();

				if (job.getFileName().endsWith(".java")) {
					compileJavaFile(codeFile);
					code = readCodeFromClassFile(classDir);
				}
				loadClassAndExecMethods(code, classDir, job);

				objectOutputStream.writeObject(job);
				deleteDir(new File(classDir));

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
					objectOutputStream.close();
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

	private char[] readCodeFromClassFile(String classDir) {
		File classDirF = new File(classDir);

		for (final File f : classDirF.listFiles()) {
			if (!f.getAbsolutePath().endsWith(".class")) {
				continue;
			}
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
			} finally {
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			char[] charArray = new char[(int) f.length()];
			for (int i = 0; i < charArray.length; i++) {
				charArray[i] = (char) mybytearray[i];
			}

			return charArray;
			}
		return null;
		}

	private static File saveJobCodeFile(final JobWrapper job,
			final String classDir) {


		final File tmpDir = new File(classDir);
		if (!tmpDir.exists()) {
			tmpDir.mkdir();
		}
		final String filePath = classDir + "/" + job.getFileName();
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

	private void loadClassAndExecMethods(char[] code, String classDir,
			final JobWrapper job) {
		System.out.println("Server: executing job " + job.getFileName());
		MethodWrapper[] methods = job.getMethodCalls();
		// Create a File object on the root of the directory containing the
		// class file
		if (methods == null) {
			methods = new MethodWrapper[0];
		}
		ArrayList<MethodWrapper> methodsToAdd = new ArrayList<MethodWrapper>();
		
		File file = new File(classDir);

		try {
			// Convert File to a URL
			URL url = file.toURI().toURL();
			URL[] urls = new URL[] { url };

			byte[] byteCode = new byte[code.length];
			for (int i = 0; i < code.length; i++) {
				byteCode[i] = (byte) code[i];
			}

			CustomClassLoader cl = new CustomClassLoader();

			Class cls = cl.loadClass(byteCode);

			for (final MethodWrapper mw : methods) {
				for (final Method method : cls.getMethods()) {
					if (method.getName().equals(mw.getName())
							&& ((mw.getArgs() == null && method
									.getParameterTypes().length == 0) || method
									.getParameterTypes().length == mw.getArgs().length)) {
						Object object = null;
						try {
							System.out.println("Server: invoking method "
									+ method.getName());
							object = method.invoke(mw.getInstance(),
									mw.getArgs());
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}

						mw.setResult(object);
						break;
					}
				}
			}

			for (final Method method : cls.getMethods()) {
				Object object = null;
				Method invokingMethod = null;
				if (isMethodAnnotated(method)) {
					invokingMethod = method;
				}
				if (invokingMethod != null) {
						try {
						if (Modifier.isStatic(invokingMethod.getModifiers())) {
							System.out.println("Server: invoking method "
									+ invokingMethod.getName());
							MethodWrapper mw = new MethodWrapper();
							mw.setName(invokingMethod.getName());
							object = invokingMethod.invoke(null, null);
							mw.setResult(object);
							methodsToAdd.add(mw);

						}
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
					}

				}
			}

		} catch (MalformedURLException e) {
		} catch (ClassNotFoundException e) {
		}

		MethodWrapper[] allMethods = new MethodWrapper[methodsToAdd.size()
				+ methods.length];
		int index = 0;
		for (final MethodWrapper mw : methods) {
			allMethods[index++] = mw;
		}
		for (final MethodWrapper mw : methodsToAdd) {
			allMethods[index++] = mw;
		}

		job.setMethodCalls(allMethods);
	}

	/**
	 * 
	 * @param method
	 * @return
	 */
	private static boolean isMethodAnnotated(Method method) {

		Annotation[] annotations = method.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof InvokeThis) {
				return true;
			}
		}
		return false;
	}

	
	public static class CustomClassLoader extends ClassLoader{

		public Class<?> loadClass(byte[] bytes)
				throws ClassNotFoundException {

			Class<?> c = defineClass(bytes, 0, bytes.length);

			// name, bytes, 0, bytes.length);
			resolveClass(c);
			// name = c.getName();
			// System.out.println("name " + name);
			return c;
		}

	}
	
	private void deleteDir(final File dir) {
		for (final File f : dir.listFiles()) {
			if (f.isDirectory()) {
				deleteDir(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}

}
