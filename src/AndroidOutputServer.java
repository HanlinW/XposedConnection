import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;

public class AndroidOutputServer{
	public static String filePath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/xposedLog.txt";
	public static boolean xmlSaved = false;
	public static void OutputToFile(byte[] byteArray, Socket sock) {
		try {
			FileOutputStream fileOS = new FileOutputStream(filePath, true); // Append
			
			BufferedOutputStream bufferedOS = new BufferedOutputStream(fileOS);
			
			// remove all null bytes
			byte[] outputArray = new String(byteArray).replaceAll("\0", "").getBytes();
			bufferedOS.write(outputArray);
			bufferedOS.flush();
			bufferedOS.close();
			
	
			String thisLine = new String(byteArray);
			
			System.out.println("    Saved this in to file:" + thisLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ViewTree tree;
	public static String adbPath = "/usr/local/bin/";
	public static void SaveViewTree() {
		String xmlName = "tmp.xml";
		String xmlPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/tmp/";
		String command = "";
		command = adbPath + "adb pull /sdcard/tmp.xml " + xmlPath + xmlName;
		ExecuteCommand(command);
		
		// Save the xml first, in order to find the correct result
		try {
			tree = new ViewTree(xmlPath + xmlName);
		} catch (IOException e){
			e.printStackTrace();
		}
		
		System.out.println("Saved Current View Tree");
		command = adbPath + "adb shell su -c /system/bin/uiautomator dump /sdcard/tmp.xml";
		ExecuteCommand(command);
		

	}

	public static String nowXpath = "";
	public static boolean checkNode(ViewNode vn) {
		//System.out.println(vn.xpath + " Current " + vn.resourceID + vn.viewText + "    " +vn.viewTag);
		// vn's coordinate larger
		int Vx = vn.x, Vx2 = vn.x2, Vy = vn.y, Vy2 = vn.y2;
		if (vn.viewTag!=null && vn.viewTag.equals(className)){
			
			if (Vx >= x && Vx2 >= x2 && Vy >= y && Vy2 >= y2) {
				
				int mag = -1;
				if (x!=0) {
					mag = Vx/x;
				}
				if (x2!=0) {
					mag = Vx2/x2;
				}
				if (y!=0) {
					mag = Vy/y;
				}
				if (y2!=0) {
					mag = Vy2/y2;
				}
				
				//System.out.println(Vx + " " + Vx2 + " " + Vy + " " + Vy2);
				System.out.println("mag: " + mag);				
				if ((mag == -1) && (Vx==Vx2) && (Vy==Vy2) && (Vx==Vy) && (Vx==0)) {
					System.out.println(vn.xpath + " GOT IT " + vn.resourceID + vn.viewText);
					nowXpath = vn.xpath;
					return true;
				}

				if (x * mag == Vx && x2 * mag == Vx2 && y * mag == Vy && y2 * mag == Vy2) {
					System.out.println(vn.xpath + " GOT IT " + vn.resourceID + vn.viewText);
					nowXpath = vn.xpath;
					return true;
				}
			}
			
		}
		
		Iterator iter = null;
		if (vn.children!=null) iter =  vn.children.iterator(); else return false;
		boolean current = false;
		while (iter.hasNext()) {
			current = checkNode((ViewNode)iter.next());
			if (current) {return true;}
		}
		return false;
	}
	public static String className;
	public static int x,x2,y,y2;
	public static void findPosition(String position, Socket sock) {
		
		String[] data = position.split(" ");
		
		className = data[4];
		x = Integer.valueOf(data[0]);
		y = Integer.valueOf(data[1]);
		x2 = Integer.valueOf(data[2]);
		y2 = Integer.valueOf(data[3]);
		
		ViewNode vn = tree.root;
		
		if (checkNode(vn)) {
			String xxPath = "Xpath: " + nowXpath + '\n';
			byte[] bytes = xxPath.getBytes();
			OutputToFile(bytes, sock);
		}
		
	}
	public static void ExecuteCommand(String command) {
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
		
		String[] commandArr = command.split(" ");
        Process process;
		try {
			process = Runtime.getRuntime().exec(commandArr);
			process.waitFor();
			successMsg = new StringBuilder();
			errorMsg = new StringBuilder();
			successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
			errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e){
			e.printStackTrace();
		} finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
		
	}
	public static void main(String[] args) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
		}
		//int bytesRead;
		int fileSize = 100;
		//long start = System.currentTimeMillis();
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
			input.read(byteArray);

			String line = new String(byteArray);
			line = line.trim();
			System.out.println(line);
			if (line.contains("SaveViewTree")) {
				SaveViewTree();
			} else if (line.contains("ClickPosition: ")) {
				String pos = line.split("ClickPosition: ")[1];
				findPosition(pos, sock);
			} else {
				OutputToFile(byteArray, sock);
			}
			
			
			// Send response from server
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.println(99);
			//long end = System.currentTimeMillis();
			out.close();
			sock.close();
		}
	}
}