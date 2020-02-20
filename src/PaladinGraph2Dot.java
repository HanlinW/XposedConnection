import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

class PaladinGraph2Dot{
	class Activities {
		Activity[] activities;
		String package_name;
		String[] webFragments;
	}
	
	class Activity{
		String activity_name;
		Fragment[] fragments;
	}
	
	class Fragment {
		String[] Clickable_list;
		Boolean VISIT;
		String activity;
		Path[] allPaths;
		String[] clicked_edges;
		String color;
		String[] edit_fields;
		Path[] interAppPaths;
		Path[] interpaths;
		Path[] intrapaths;
		Boolean menuClicked;
		int[] path_index;
		String[] path_list;
		String signature;
		String structure_hash;
		String[] targets;
		String[] text_path_index;
		String[] text_path_list;
		String[] text_xpath_list;
		Boolean traverse;
		Boolean traverse_over;
		String[] xpath_index;
	}
	
	class Path {
		int action;
		String content;
		String path;
		String target;
		String target_activity;
		String source;
		String target_hash;
	}
	
	public HashMap<String, Integer> allActs = new HashMap<String, Integer>();
	public LinkedList<Path> allPaths = new LinkedList<Path>();
	
	public void BuildDot(Activities activities) {
		int acts = 0;
		for (Activity act:activities.activities){
			String src = act.activity_name;
			if (!allActs.containsKey(src)) {
				allActs.put(src, acts);
				acts ++;
			}
			for (Fragment frag:act.fragments){
				for (Path pa:frag.allPaths){
					Path current = pa;
					current.source = src;
					allPaths.add(current);
					if (!allActs.containsKey(pa.target_activity)) {
						allActs.put(pa.target_activity, acts);
						acts ++;
					}
				}
			}
		}
		
	}
	
	static String outputFilePath;
	public String PaladinGraphPath;
	
	public PaladinGraph2Dot(String path, String output){
		this.PaladinGraphPath = path;
		this.outputFilePath = output;
	}
	
	public void WriteDot(){
		File file = new File(outputFilePath);
		try {
			FileWriter Afile = new FileWriter(file);
			Iterator iter = allActs.entrySet().iterator();
			while (iter.hasNext()){
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
				String key = entry.getKey();
				int val = entry.getValue();
				Afile.write("n" + val + " [label=\"ACT[" + key + "]\"];\n");
			}
			
			iter = allPaths.iterator();
			while (iter.hasNext()){
				Path current = (Path) iter.next();
				String src = current.source;
				String tgt = current.target_activity;
				String path = current.path;
				String line = "";
				int srcIndex = allActs.get(src);
				int tgtIndex = allActs.get(tgt);
				line = "n" + srcIndex + " -> n" + tgtIndex 
						+ " [label=\"src: ACT["+ src 
						+ "]\\ntgt: ACT[" + tgt + "]"
						+ "\\npath: " + path
						+ "\"];\n";
				
				Afile.write(line);
			}
			
			Afile.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		
	}

}