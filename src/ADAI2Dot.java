
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class ADAI2Dot{
	public static String outputFilePath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/smsdroidTEST.dot";
	public String ADAIFilePath;
	public ADAI2Dot (String ADAIFile) {
		this.ADAIFilePath = ADAIFile;
	}
	public LinkedList<String> ADAIFile = new LinkedList<String>();
	public HashMap<String, Integer> Activities = new HashMap<String, Integer>();
	public HashMap<String, Integer> Widgets = new HashMap<String, Integer>();
	public LinkedList<PathTree> allPaths = new LinkedList<PathTree>(); 
	public class PathTree {
		public PathTree(String source, String target){
			src = source;
			tgt = target;
		}
		public PathTree(){
			src = "";
			tgt = "";
		}
		PathTree next = null;
		String src= "";
		String tgt= "";
		String handler= "", widgetID= "", className= "", dialogClass= "", dialogTitle= "", buttonText= "", hash= "", event = "", 
				menuItemID = "";
	}
	
	public PathTree root = new PathTree(); 
	public int acts = 0;
	public void ReadLog(){
		File Afile = new File(ADAIFilePath);
		
		BufferedReader Areader = null;
		try {
			Areader = new BufferedReader(new FileReader(Afile));
			String tempString =null;
			int Aline = 1;
			while ((tempString = Areader.readLine()) !=null) {
				ADAIFile.add(tempString.trim());
				Aline ++;
			}
			
			Areader.close();
		} catch (IOException e){
			e.printStackTrace();
		} 
		
	}
	
	public void WriteDot(){
		File file = new File(outputFilePath);
		try {
			FileWriter Afile = new FileWriter(file);
			Iterator iter = Activities.entrySet().iterator();
			while (iter.hasNext()){
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
				String key = entry.getKey();
				int val = entry.getValue();
				Afile.write("n" + val + " [label=\"ACT[" + key + "]\"];\n");
			}
			
			PathTree nowPath = root;
			while (nowPath.next!=null){
				nowPath = nowPath.next;
				//nextPath = nowPath.next;
				if (nowPath !=null && nowPath.next !=null && nowPath.next.src != "" && nowPath.src !="") {
					String src = nowPath.src;
					String tgt = "";
					if (nowPath.tgt!="") tgt = nowPath.tgt; else {
						tgt = nowPath.next.src;
						nowPath.tgt = tgt;
					}
					String line;
					//System.out.println(src + "->" + tgt);
					int srcIndex = Activities.get(src);
					
					int tgtIndex = Activities.get(tgt);
					line = "n" + srcIndex + " -> n" + tgtIndex 
							+ " [label=\"src: ACT["+ src 
							+ "]\\ntgt: ACT[" + tgt + "]";
					
					String handler= nowPath.handler, widgetID= nowPath.widgetID, className= nowPath.className,
							dialogClass= nowPath.dialogClass, dialogTitle= nowPath.dialogTitle, buttonText= nowPath.buttonText,
							hash= nowPath.hash, event = nowPath.event, menuItemID = nowPath.menuItemID;
					if (className.contains("MenuItem")) {
						className = "class android.view.MenuItem";
						nowPath.className = "class android.view.MenuItem";
					} 
					PathTree current = nowPath;
					allPaths.add(current);
					if (className!=""){
						line = line + "\\nclass: " + className + " ";
					}
					if (widgetID!=""){
						if (menuItemID!="") {
							line = line + "\\nwidgetID: " + menuItemID + " ";
						} else {
							line = line + "\\nwidgetID: " + widgetID + " ";
						}
					}
					if (dialogClass!=""){
						line = line + " dialogClass: " + dialogClass + " ";
					}
					if (dialogTitle!=""){
						line = line + "\\ndialogTitle: " + dialogTitle + " ";
					}
					if (buttonText!=""){
						line = line + "\\nbuttonText: " + buttonText + " ";
					}
					if (handler!="") {
						line = line + "\\nhandler: " + handler + " ";
					}
					if (event!="") {
						line = line + "\\nevent: " + event + " ";
					}
					if (hash!="") {
						line = line + "\\nhash: " + hash + " ";
					}
					line = line + "\"];\n";
					Afile.write(line);
				} else {
					// Last one
					if (nowPath.next == null) {
						
					}
				}
			}
			
			//System.out.println(allPaths.size());
			Afile.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		
		
	}
	public void Run(){
		int Aline = ADAIFile.size();
		int i = 0;
		String srcActName, handler, widgetID, tgtActName, className, 
				dialogClass, dialogTitle, buttonText, hash ,event, menuItemID; 
		srcActName = "";
		handler = "";
		widgetID = "";
		tgtActName = ""; 
		className = "";
		dialogClass = "";
		dialogTitle = "";
		buttonText = "";
		menuItemID = "";
		hash = "";
		event = "CLICK";
		Boolean new_path = true;
		PathTree nowPath = root;
		while (i < Aline) {
			String line = ADAIFile.get(i);
			if (line.split(": ").length ==1) {
				//System.out.println(line);
				i ++;
				continue; 
			}
			String a = line.split(": ")[1];

			if (line.contains("Hash: ")){
				if (a.contains("MENU") && srcActName ==""){
					// skip the empty path
					new_path = true;
				} else if (new_path) {
					new_path = false;
				} else {
					// another path, save all current data
					PathTree putPath = new PathTree(srcActName, tgtActName);
					putPath.handler = handler;
					putPath.widgetID = widgetID;
					putPath.className = className;
					putPath.dialogClass = dialogClass;
					putPath.dialogTitle = dialogTitle;
					putPath.buttonText = buttonText;
					putPath.event = event;
					putPath.menuItemID = menuItemID;
					putPath.hash = hash;
					nowPath.next = putPath;
					nowPath = putPath;
					
					srcActName = "";
					handler = "";
					widgetID = "";
					tgtActName = ""; 
					className = "";
					dialogClass = "";
					dialogTitle = "";
					buttonText = "";
					menuItemID = "";
					event = "CLICK";
					hash = "";
					new_path = true;	
				}

				hash = a;

				if (!Widgets.containsKey(hash)) {
					Widgets.put(hash, 0);
				} else {
					Widgets.replace(hash, Widgets.get(hash) + 1 );
				}
				
			} else if (line.contains("SourceActivity: ")) {
				srcActName = a;
				if (a.charAt(0)=='.') {
					srcActName = srcActName.substring(1);
				}
				if (!Activities.containsKey(srcActName)) {
					Activities.put(srcActName, acts);
					acts ++;
				}
				
			} else if (line.contains("TargetActivity: ")) {
				tgtActName = a;
				if (a.charAt(0)=='.') {
					tgtActName = tgtActName.substring(1);
				}
				
				if (!Activities.containsKey(tgtActName)) {
					Activities.put(tgtActName, acts);
					acts ++;
				}
			} else if (line.contains("WidgetID: ")){
				widgetID = a;
			} else if (line.contains("Handler: ")){
				handler = a;
			} else if (line.contains("DialogClass: ")){
				dialogClass = a;
			} else if (line.contains("Class: ")){
				className = a;
			} else if (line.contains("DialogTitle: ")) {
				dialogTitle = a; 
			} else if (line.contains("ButtonText: ")){
				buttonText = a;
			} else if (line.contains("Event: ")) {
				event = a;
			} else if (line.contains("MenuItemID: ")) {
				menuItemID = a;
			}
			i++;
		}
		
		// Put last path in tree
		PathTree putPath = new PathTree(srcActName, tgtActName);
		putPath.handler = handler;
		putPath.widgetID = widgetID;
		putPath.className = className;
		putPath.dialogClass = dialogClass;
		putPath.dialogTitle = dialogTitle;
		putPath.buttonText = buttonText;
		putPath.event = event;
		putPath.hash = hash;
		nowPath.next = putPath;
		nowPath = putPath;
		nowPath.next = null;
		
	}
}