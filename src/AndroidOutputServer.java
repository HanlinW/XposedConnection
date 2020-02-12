import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class AndroidOutputServer{
	public static String filePath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/xposedLog.txt";
	public static void main(String[] args) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
		}
		//int bytesRead;
		int fileSize = 100;
		long start = System.currentTimeMillis();
		ServerSocket servSock = new ServerSocket(9998);
		System.out.println("Waiting...");
		while (true) {
			Socket sock = servSock.accept();
			
			Date date = new Date();
			System.out.println(date);
			System.out.println("Accepted Connect: " + sock);
			// try receive the strLine
			byte[] byteArray = new byte[fileSize];
			InputStream input = sock.getInputStream();
			FileOutputStream fileOS = new FileOutputStream(filePath, true); // Append
			BufferedOutputStream bufferedOS = new BufferedOutputStream(fileOS);
			input.read(byteArray);
			// remove all null bytes
			byte[] outputArray = new String(byteArray).replaceAll("\0", "").getBytes();
			bufferedOS.write(outputArray);
			bufferedOS.flush();
			bufferedOS.close();
			
			// Send response from server
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.println(99);
			long end = System.currentTimeMillis();
			out.close();
			
			String thisLine = new String(byteArray);
			
			System.out.println("    Saved this in to file:" + thisLine);
			
			sock.close();
		}
	}
}