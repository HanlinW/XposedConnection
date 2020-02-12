import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

class GatorDot2C {
	public GatorDot2C(String MydotPath){
		dotPath = MydotPath; 
	}
	public static String dotPath;
	public LinkedList<String> GatorFile = new LinkedList<String>();
	public HashMap<String, vertex> Boxes = new HashMap<String, vertex>();
	public LinkedList<edge> Edges = new LinkedList<edge>();
	
	static class edge {
		public vertex src;
		public vertex tgt;
		public String className;
		public String widgetID;
		public String event;
		public edge(vertex src, vertex tgt) {
			this.src = src;
			this.tgt = tgt;
			className = "";
			widgetID = "";
		}
	}
	static class vertex {
		public String act;
		public boolean dia;
		public boolean menu;
		public vertex(String a, boolean d, boolean m){
			this.act = a;
			this.dia = d;
			this.menu = m;
		}
	}
	public void Read() {
		File file = new File(dotPath);
		BufferedReader Greader = null;
		try {
			String tempString =null;
			Greader = new BufferedReader(new FileReader(file));
			while ((tempString = Greader.readLine()) !=null) {
				GatorFile.add(tempString.trim());
			}
				
			Greader.close();
		} catch (IOException e){
			e.printStackTrace();
		} 
	}
	
	public void Dot2Class() {
		int start = 3;
		int len = GatorFile.size();
		while (start < len-1 && !GatorFile.get(start).contains("->")){
			String temp = GatorFile.get(start);
			String index = temp.substring(0, temp.indexOf(" "));
			String act = temp.substring(temp.indexOf("\"")+1, temp.indexOf("]"));
			if (act.contains("DIALOG[")) {
				
				String detail = temp.split(" alloc: <")[1];
				
				String DialogAct = "";
				for (char c:detail.toCharArray()){
					if (c!='$' && c!=':'){
						DialogAct += c;
					} else {
						break;
					}
				}
				vertex v = new vertex (DialogAct, true, false);
				
				Boxes.put(index, v);

			} else if (act.contains("OptionsMenu[")) {
				
				String detail = act.split("OptionsMenu\\[")[1];
				String MenuAct = "";
				for (char c:detail.toCharArray()){
					if (c!=']'){
						MenuAct += c;
					} else {
						break;
					}
				}
				vertex v = new vertex (MenuAct, false, true);
				Boxes.put(index, v);
				
			} else if (act.contains("ACT[")) {				
				String detail = act.split("ACT\\[")[1];
				vertex v = new vertex (detail, false, false);
				Boxes.put(index, v);				
			} else if (act.contains("LAUNCHER_NODE[")) {
				String detail = "LAUNCHER_NODE";
				vertex v = new vertex (detail, false, false);
				Boxes.put(index, v);
			}
			
			start ++;
		}
		
		while (start < len-1) {
			String temp = GatorFile.get(start);
			String two = temp.split(" \\[label")[0];
			String index1 = two.split(" -> ")[0];
			String index2 = two.split(" -> ")[1];
			String detail = temp.substring(temp.indexOf("\"")+1, temp.indexOf("];")-1);
			String[] ele = detail.split("\\\\n");
			String tag = ele[2]; // there is an open menu tag
			String event = ele[3];
			if (event.equals("evt: item_click") || event.equals("evt: click") || event.equals("evt: touch")) {
				event = "CLICK";
			} else if (event.equals("evt: item_long_click") || event.equals("evt: long_click")) {
				event = "LongClick";
			} else if (event.equals("evt: implicit_back_event")) {
				event = "BACK";
			} else {
				event = "";
			}
			String widgetT = ele[4];
			String widgetID = "", widget = "";
			if (widgetT.contains("INFL")) {
				widget = widgetT.substring(widgetT.indexOf('[') +1 , widgetT.indexOf(','));
				if (widgetT.contains("WID")) {					
					widgetID = widgetT.substring(widgetT.indexOf('|')+1, widgetT.indexOf(']'));	
				}
			} else if (widgetT.contains("DIALOG")) {
				widget = "android.app.AlertDialog";
			}
			
			vertex src = Boxes.get(index1);
			vertex tgt = Boxes.get(index2);
			
			edge current = new edge(src, tgt);
			current.className = widget;
			current.widgetID = widgetID;
			current.event = event;
			//System.out.println(event);
			Edges.add(current);
			
			start ++;
		}
		
	}

}