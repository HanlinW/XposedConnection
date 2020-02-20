import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


class ADAIMarchGator {

	public static void CompareTwo(GatorDot2C myGator, ADAI2Dot myADAI) {
		HashMap<String, GatorDot2C.vertex> GActivities = myGator.Boxes;
		LinkedList<GatorDot2C.edge> GPaths = myGator.Edges;
		
		HashMap<String, Integer> AActivities = myADAI.Activities;
		LinkedList<ADAI2Dot.PathTree> APaths = myADAI.graphPaths; 
		
		int Marched = 0;
		
		Iterator it1 = GPaths.iterator();
		while (it1.hasNext()){
			GatorDot2C.edge currentG = (GatorDot2C.edge) it1.next();
			
			Iterator it2 = APaths.iterator();
			//System.out.println("GGGG!!"+currentG.src.act + " to  " + currentG.tgt.act + "||widget:" + currentG.className + "..." + currentG.widgetID + currentG.event);
			while (it2.hasNext()) {
				ADAI2Dot.PathTree currentA = (ADAI2Dot.PathTree) it2.next();
				
				String AclassName = "", AwidgetID = "", dialog = ""; 
				if (!currentA.className.equals("")) {
					AclassName = currentA.className.substring(6);
					if (!currentA.dialogClass.equals("")) {
						dialog = currentA.dialogClass.substring(6);
					}
				}
				if (!currentA.widgetID.equals("")){
					if (currentA.menuItemID.equals("")){
						AwidgetID = currentA.widgetID.substring(currentA.widgetID.indexOf('/')+1);
					} else {
						AwidgetID = currentA.menuItemID.substring(currentA.menuItemID.indexOf('/')+1);
					}
				}
				
				if (currentG.src.act.equals(currentA.src) && currentG.tgt.act.equals(currentA.tgt)) {
					//System.out.println(AclassName + "|||11||| " +  AwidgetID);
					//System.out.println(currentG.className + "|||22|||" + currentG.widgetID);
					if (currentG.widgetID.equals(AwidgetID) && currentG.className.equals(AclassName))
						if (currentG.event.equals(currentA.event))
						{
							//System.out.println(currentA.src + " to  " + currentA.tgt );
							//System.out.println(AclassName + "  + " +  AwidgetID);
							//System.out.println(currentG.event + "    " + currentA.event);
							Marched ++;
							break;
						} 
				}
			}
		}
		
		Iterator it2 = APaths.iterator();
		while (it2.hasNext()) {
			ADAI2Dot.PathTree currentA = (ADAI2Dot.PathTree) it2.next();
			
			String AclassName = "", AwidgetID = ""; 
			if (!currentA.className.equals("")) {
				AclassName = currentA.className.substring(6);
			}
			if (!currentA.widgetID.equals("")){
				AwidgetID = currentA.widgetID.substring(currentA.widgetID.indexOf('/')+1);
			}
			//System.out.println("AAAA!!"+currentA.src + " to  " + currentA.tgt + "||widget:" + AclassName + "..." + AwidgetID + currentA.event);
		}
		
		System.out.println("Gator full path: " + myGator.Edges.size());
		//System.out.println(myADAI.allPaths.size());
		System.out.println("ADAI full path: " + myADAI.graphPaths.size());
		System.out.println("Marched: " +Marched);
	}
	
	public static String GatorDotPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/smsdroidGator.dot";
	
	public static String ADAIFilePath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/groundtruth/de.ub0r.android.smsdroid.txt";
	public static String ADAIDotPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/groundDOT/de.ub0r.android.smsdroid.dot";
	
	public static String PaladinFilePath = "/Users/hanlinwang/Desktop/thesis3/myAPK/Paladin-output/graph-de.ub0r.android.smsdroid.json";
	public static String PaladinOutputPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/smsdroidPaladin.dot";
	
	public static void main(String[] args){
		
		// Read Graph.Json from Paladin
		Gson g = new Gson();
		try {
			PaladinGraph2Dot myPala = new PaladinGraph2Dot(PaladinFilePath, PaladinOutputPath);
			
			PaladinGraph2Dot.Activities act = g.fromJson(new FileReader(myPala.PaladinGraphPath), PaladinGraph2Dot.Activities.class);
			myPala.BuildDot(act);
			System.out.println("Paldin Graph:" +myPala.allPaths.size());
			//myPala.WriteDot();
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Read dot file from Gator 
		GatorDot2C myGator = new GatorDot2C(GatorDotPath);
		myGator.Read();
		myGator.Dot2Class();
		// Read ADAI file
		ADAI2Dot myADAI = new ADAI2Dot(ADAIFilePath, ADAIDotPath);
		myADAI.ReadLog();
		myADAI.Run();
		myADAI.BuildPath();
		myADAI.RemoveDuplicate();
		CompareTwo(myGator, myADAI);
	}
}