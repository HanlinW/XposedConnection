import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

class ADAIMarchGator {

	public static void ElimateMeanlessActivities(GatorDot2C myGator, ADAI2Dot myADAI){
		HashMap<String, GatorDot2C.vertex> boxGator = myGator.Boxes;
		HashMap<String, Integer> boxADAI = myADAI.Activities;
		HashSet<String> bADAI = new HashSet<String>();
		Iterator iter = boxGator.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, GatorDot2C.vertex> entry = (Map.Entry<String, GatorDot2C.vertex>) iter.next();
			String key = entry.getKey();
			GatorDot2C.vertex val = entry.getValue();
			// Check if it show up at static analysis
			System.out.println(val.act);
			if (boxGator.containsKey(key)) {
				//System.out.println(key);
			}
		}
	}
	
	public static void PaladinPruning(GatorDot2C myGator, ADAI2Dot myADAI) {
		// Delete the activities that actually not inside the current APK
		HashMap<String, GatorDot2C.vertex> GatorBoxes = myGator.Boxes;
		HashSet<String> GatorActivity = new HashSet<String>();
		for (Map.Entry<String, GatorDot2C.vertex> entry: GatorBoxes.entrySet()){
			String currentActivity = entry.getValue().act;
			if (!GatorActivity.contains(currentActivity)) {
				GatorActivity.add(currentActivity);
			}
		}
		
		System.out.println("=============");
		
		LinkedList<ADAI2Dot.PathTree> PaladinPaths = myADAI.graphPaths;
		System.out.println(PaladinPaths.size());
		
		Iterator<ADAI2Dot.PathTree> iter = PaladinPaths.iterator();
		while (iter.hasNext()){
			ADAI2Dot.PathTree currentPath = iter.next();
			String srcAct = currentPath.src;
			String tgtAct = currentPath.tgt;
			if (!GatorActivity.contains(srcAct) || !GatorActivity.contains(tgtAct)) {
				iter.remove();
			}
		}
		System.out.println("After Cleaning");
		System.out.println(PaladinPaths.size());
		System.out.println("=============");
		
		// Find edges only between different activities
		int number = 0;
		iter = PaladinPaths.iterator();
		while (iter.hasNext()){
			ADAI2Dot.PathTree currentPath = iter.next();
			String srcAct = currentPath.src;
			String tgtAct = currentPath.tgt;
			if (!srcAct.equals(tgtAct)) {
				number ++;
			}
		}
		System.out.println("current connected edges " + number);
		System.out.println("=============");
		
		HashMap<String, Integer> PaladinActivity = myADAI.Activities;
		Iterator<Map.Entry<String, Integer>> iter2 = PaladinActivity.entrySet().iterator();
		while (iter2.hasNext()){
			Map.Entry<String, Integer> entry = iter2.next();
			if (!GatorActivity.contains(entry.getKey())) {
				iter2.remove();
			}
		}
		
	}
	
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
				
				//System.out.println(currentG.src);
				//System.out.println(currentA.src);
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
		OutputToFile("Gator full edges: " + myGator.Edges.size(), conclusionPath);
		OutputToFile("ADAI full edges: " + myADAI.graphPaths.size(), conclusionPath);
		OutputToFile("Marched:" + Marched, conclusionPath);
		OutputToFile("Gator full nodes: " + myGator.Boxes.size(), conclusionPath);
		OutputToFile("ADAI full nodes: " + myADAI.Activities.size(), conclusionPath);		
	}
	
	public static void CompareADAI(ADAI2Dot a, ADAI2Dot groundtruth) {
		LinkedList<ADAI2Dot.PathTree> TPaths = groundtruth.graphPaths;
		LinkedList<ADAI2Dot.PathTree> aPaths = a.graphPaths;
		
		int Marched = 0;
		Iterator it1 = aPaths.iterator();
		HashSet<ADAI2Dot.PathTree> used = new HashSet<ADAI2Dot.PathTree>();
		while (it1.hasNext()){
			ADAI2Dot.PathTree currentA = (ADAI2Dot.PathTree) it1.next();
			Iterator it2 = TPaths.iterator();
			while (it2.hasNext()){
				ADAI2Dot.PathTree currentT = (ADAI2Dot.PathTree) it2.next();
				if (!used.contains(currentT) && currentT.equals(currentA)) {
					used.add(currentT);
					Marched ++;
					//System.out.println(" src: " + currentT.src + " tgt:  " + currentT.tgt + " event: " + currentT.event);
				}
			}
		}
		
		System.out.println("Marched:" + Marched);
		OutputToFile("Marched:" + Marched, conclusionPath);
		
		Marched = 0;
		it1 = aPaths.iterator();
		used = new HashSet<ADAI2Dot.PathTree>();
		while (it1.hasNext()){
			ADAI2Dot.PathTree currentA = (ADAI2Dot.PathTree) it1.next();
			Iterator it2 = TPaths.iterator();
			while (it2.hasNext()){
				ADAI2Dot.PathTree currentT = (ADAI2Dot.PathTree) it2.next();
				if (!used.contains(currentT) && currentT.equals(currentA) && !currentT.src.equals(currentT.tgt)) {
					used.add(currentT);
					Marched ++;
					//System.out.println(" src: " + currentT.src + " tgt:  " + currentT.tgt + " event: " + currentT.event);
				}
			}
		}
		
		System.out.println("Edges between activities Marched:" + Marched);
	}
	
	public static boolean OutputToFile(String line, String path){
		File file = new File(path);
		try {
			FileWriter Afile = new FileWriter(file,true);
			Afile.write(line + '\n');
			Afile.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		return true;
	}
	public static String APKname = "com.droiddream.lovePositions";
	
	public static String GatorDotPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/GatorDOT/" + APKname + ".apk.wtg.dot";
	 
	public static String PaladinLogPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/PaladinDOT/" + APKname + ".txt";
	public static String PaladinLogOutputPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/PaladinOut/" + APKname + ".dot";
	public static String ADAIFilePath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/groundtruthNEW/" + APKname + ".txt";
	//public static String ADAIFileViewTreePath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/groundtruth/" + APKname + "_ViewTree.txt";
	public static String ADAIDotPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/groundDOT/" + APKname + ".dot";
	
	public static String PaladinFilePath = "/Users/hanlinwang/Desktop/thesis3/myAPK/Paladin-output/graph-" + APKname + ".json";
	public static String PaladinOutputPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/" + APKname + "Paladin.dot";
	
	public static String conclusionPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/conclusion/" + APKname + ".txt";
	
	public static void main(String[] args){		
		// Read Graph.Json from Paladin
		/*Gson g = new Gson();
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
		ElimateMeanlessActivities(myGator, myADAI);		
		*/		
		
		// Paladin+Xposed
		ADAI2Dot paladinADAI = new ADAI2Dot(PaladinLogPath, PaladinLogOutputPath);
		paladinADAI.ReadLog();
		paladinADAI.Run();
		paladinADAI.BuildPath();
		paladinADAI.RemoveDuplicate();
		
		GatorDot2C myGator = new GatorDot2C(GatorDotPath);
		myGator.Read();
		myGator.Dot2Class();
		
		ADAI2Dot gtADAI = new ADAI2Dot(ADAIFilePath, ADAIDotPath);
		gtADAI.ReadLog();
		gtADAI.Run();
		gtADAI.BuildPath();
		gtADAI.RemoveDuplicate();
		
		CompareADAI(paladinADAI, gtADAI);
		
		PaladinPruning(myGator, paladinADAI);
		PaladinPruning(myGator, gtADAI);
		
		// After Pruning, output the Dot file
		gtADAI.WriteDot();
		
		CompareADAI(paladinADAI, gtADAI);
		/*
		// Compare Gator with ground truth
		System.out.println("Compare Gator with ground truth");
		OutputToFile("Compare Gator with ground truth", conclusionPath);
		CompareTwo(myGator, myADAI);
		// Compare Gator with paladin + ProMal
		System.out.println("Compare Gator with paladin + ProMal");
		OutputToFile("Compare Gator with paladin + ProMal", conclusionPath);
		CompareTwo(myGator,paladinADAI);
		// Compare paladin + ProMal with ground truth
		System.out.println("Compare paladin + ProMal with ground truth");
		OutputToFile("Compare paladin + ProMal with ground truth", conclusionPath);
		CompareADAI(paladinADAI, myADAI);
		
		/*
		ADAI2Dot aADAI = new ADAI2Dot("/Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/groundtruth/de.ub0r.android.smsdroid_ViewTree.txt", ADAIDotPath);
		aADAI.ReadLog();
		aADAI.Run();
		aADAI.BuildPath();
		*/
				
		/* Output to Dot/SVG file
		ADAI2Dot myADAI = new ADAI2Dot(ADAIFileViewTreePath, ADAIDotPath);
		myADAI.ReadLog();
		myADAI.Run();
		myADAI.BuildPath();
		myADAI.RemoveDuplicate();
		myADAI.WriteDot();
		*/
		
		//String command = "dot -Tsvg "+ ADAIDotPath + " -o /Users/hanlinwang/Desktop/thesis3/MyProgram/XposedConnection/result/svg/" + APKname + ".svg";
		//System.out.println(command);
		//AndroidOutputServer.ExecuteCommand(command);
		
		
	}
}