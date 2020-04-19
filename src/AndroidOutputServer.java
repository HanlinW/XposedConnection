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
	public static boolean CheckEmptyFolder(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
			return true;
		}
		return false;
	}
	public static void CheckAllFolders(){
		String path = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/XML/" + currentAPK + '/';
		CheckEmptyFolder(path);
		path = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/screenshots/" + currentAPK + '/';
		CheckEmptyFolder(path);
	}
	public static ViewTree tree;
	public static final String adbPath = "/usr/local/bin/";
	public static int initXML = 0;
	// manuallyCheck = true, output xpath for manually checking, otherwise no extra output
	public static boolean manuallyChecker = false;
	public static void SaveViewTree(Socket sock) {
		String xmlName = initXML + ".xml";
		String xmlPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/XML/" + currentAPK + '/';
		String command = "";
		command = adbPath + "adb pull /sdcard/tmp.xml " + xmlPath + xmlName;
		initXML ++;
		ExecuteCommand(command);
		
		String tmp = "XML: " + "/XposedConnection/result/XML/" + currentAPK + '/' + xmlName + '\n';
		byte[] bytestmp = tmp.getBytes();
		OutputToFile(bytestmp, sock);
		// Save the xml first, in order to find the correct result
		try {
			tree = new ViewTree(xmlPath + xmlName);
		} catch (IOException e){
			e.printStackTrace();
		}
		
		System.out.println("Saved Current View Tree");
		command = adbPath + "adb shell su -c /system/bin/uiautomator dump /sdcard/tmp.xml";
		ExecuteCommand(command);

		if (manuallyChecker) OutputClickableXpath(xmlName, xmlPath);
		
	}
	
	public static String screenShotPath = "";
	public static int initShot = 0;
	public static void SaveScreenShot(Socket sock){
		String 	command = "";
		String screenshotPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/screenshots/" + currentAPK + "/";
		String screenshotName = initShot + ".png";
		
		command = adbPath + "adb pull /sdcard/screenshot.png " + screenshotPath + screenshotName;
		initShot ++;
		ExecuteCommand(command);
		String tmp = "Screenshot: " + "/XposedConnection/result/screenshots/" + currentAPK + "/" + screenshotName + '\n';
		byte[] bytestmp = tmp.getBytes();
		OutputToFile(bytestmp, sock);
		
		
		command = adbPath + "adb shell /system/bin/screencap -p /sdcard/screenshot.png";
		ExecuteCommand(command);	
	}

	public static String nowXpath = "";
	
	// the error between two magnification less than er
	public static double er = 0.1;
	public static boolean compareTwo(double a, double b) {
		if (a < b) {
			if ( a + er > b) return true; else return false;
		} else if (a > b) {
			if ( a - er < b) return true; else return false;
		} else if (a == b) {
			return true;
		}
		return false;
	}
	public static boolean checkNode(ViewNode vn) {
		//System.out.println(vn.xpath + " Current " + vn.resourceID + vn.viewText + "    " +vn.viewTag);
		// vn's coordinate larger
		int Vx = vn.x, Vx2 = vn.x2, Vy = vn.y, Vy2 = vn.y2;
		if (vn.viewTag!=null && vn.viewTag.equals(className)){
			
			if (Vx >= x && Vx2 >= x2 && Vy >= y && Vy2 >= y2) {
				
				double magx = -1, magx2 = -1, magy = -1, magy2 = -1; 
				if (x!=0) {
					magx = ((double)Vx)/x;
				}
				if (x2!=0) {
					magx2 = ((double)Vx2)/x2;
				}
				if (y!=0) {
					magy = ((double)Vy)/y;
				}
				if (y2!=0) {
					magy2 = ((double)Vy2)/y2;
				}
				
				
				//System.out.println(Vx + " " + Vx2 + " " + Vy + " " + Vy2);
				//System.out.println(magx + " " + magx2 + " " + magy + " " + magy2);				
				if ( compareTwo(magx, magx2) && compareTwo(magx, magy) && compareTwo(magx, magy2)) {
					System.out.println(vn.xpath + " GOT IT " + vn.resourceID + vn.viewText);
					System.out.println("mag: " + magx);
					nowXpath = vn.xpath;
					return true;
				}
				
				if ((magx == -1) && compareTwo(magx2, magy) && compareTwo(magy2, magy)) {
					System.out.println(vn.xpath + " GOT IT " + vn.resourceID + vn.viewText);
					nowXpath = vn.xpath;
					return true;
				}
				
				if ((magx2 == -1) && compareTwo(magx, magy) && compareTwo(magx, magy2)) {
					System.out.println(vn.xpath + " GOT IT " + vn.resourceID + vn.viewText);
					nowXpath = vn.xpath;
					return true;
				}
				
				if ((magy == -1) && compareTwo(magx, magx2) && compareTwo(magx, magy2)) {
					System.out.println(vn.xpath + " GOT IT " + vn.resourceID + vn.viewText);
					nowXpath = vn.xpath;
					return true;
				}
				
				if ((magy2 == -1) && compareTwo(magx, magy) && compareTwo(magx, magx2)) {
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
	
	public static String ClickableFile = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/trash/clickableList.txt";
	public static void findAll(ViewNode vn, FileWriter file) throws IOException {
		if (vn.clickable) {
			file.write("[" + vn.x + ", " + vn.x2 + "][" + vn.y + ", " + vn.y2 + "]  : " + vn.xpath + "   id:" + vn.resourceID + '\n');
		}
		
		Iterator iter = null;
		if (vn.children!=null) iter =  vn.children.iterator(); else return ;
		boolean current = false;
		while (iter.hasNext()) {
			findAll((ViewNode)iter.next(), file);
		}
	}
	public static void OutputClickableXpath(String xmlName, String xmlPath) {
		File file = new File(ClickableFile);
		try {
			file.createNewFile();
			FileWriter writeFile = new FileWriter(file);
			ViewTree current_tree = new ViewTree(xmlPath + xmlName);;
			ViewNode current_root = current_tree.root;
			findAll(current_root, writeFile);
			writeFile.close();
		} catch (Exception e) {
			e.printStackTrace();
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
	
	public static String currentAPK = "za.co.lukestonehm.logicaldefence";
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
		CheckAllFolders();
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
				SaveViewTree(sock);
				SaveScreenShot(sock);
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