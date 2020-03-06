import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class ViewTree {
	public ViewNode root;
	//表示树的结构的hash值这里只考虑了节点的class+深度，而且对于子节点性质相同的list，子节点不考虑
    public int treeStructureHash;
	public boolean hasWebview = false;
	static String[] filtsBys = new String[]{"AbsListView", "GridView", "RecyclerView"};
    public int totalViewCount;
    public int relativeCount;
    public String activityName;
    
    public ViewTree(String xml) throws IOException{
    	File f = new File(xml);
    	FileInputStream fis = new FileInputStream(f);
        Document doc = Jsoup.parse(fis, null, "", Parser.xmlParser());
        
        if (doc.children().size() == 0) {
            root = null;
            System.out.println("root is null");
            return;
        }

        Element startNode = doc.child(0);
        if (startNode == null) return;
        root = construct(startNode, 0, null);
        totalViewCount = root.total_view;
        treeStructureHash = root.nodeRelateHash;
    }

    ViewNode construct(Element rootView, int depth, ViewNode par){
        ViewNode now = new ViewNode();
        String relate_hash_string;
        if (!rootView.tagName().equals("hierarchy")) {
            now.clickable = Boolean.parseBoolean(rootView.attr("clickable")) ||
                    Boolean.parseBoolean(rootView.attr("long-clickable")) ||
                    Boolean.parseBoolean(rootView.attr("enabled"));
            // not include native background view
            if (rootView.attr("resource-id").contains("BarBackground")) {
                return null;
            }

            now.resourceID = rootView.attr("resource-id");
            now.depth =depth;
            List<Integer> coordinates = parse_coordinates(rootView.attr("bounds"));
            now.x = coordinates.get(0);
            now.x2 = coordinates.get(1);
            now.y = coordinates.get(2);
            now.y2 = coordinates.get(3);
            now.width = coordinates.get(1) - coordinates.get(0);
            now.height = coordinates.get(3) - coordinates.get(2);
            //pxy[0] = vn.x + (vn.x2 - vn.x) / 2;
            //pxy[1] = vn.y + (vn.y2 - vn.y) / 2;
            now.viewTag = rootView.attr("class");
            now.contentDesc = rootView.attr("content-desc");
            now.parent = par;
            if (par != null)
                now.xpath = par.xpath + "/" + getLast(rootView.attr("class"));
            else
                now.xpath = getLast(rootView.attr("class"));

            if (rootView.attr("class").contains("TextView")) {
                now.viewText = rootView.attr("text");
            } else
                now.viewText = "";


            relate_hash_string = now.calStringWithoutPosition();
            now.total_view = 1;

            // not include webview content
            if (rootView.attr("class").contains("webkit") ||
                    rootView.attr("class").contains("WebView")){
                hasWebview = true;
                now.nodeRelateHash = relate_hash_string.hashCode();
                return now;
            }
        }else{
            now.xpath = "";
            relate_hash_string = "";
        }
        Elements children = rootView.children();
        List<ViewNode> child_list = new ArrayList<>();
        for(Element child: children){
            if (child.attr("package").equals("com.android.systemui"))
                continue;
            ViewNode child_node = construct(child, depth+1, now);
            if (child_node == null) continue;
            child_list.add(child_node);
            now.total_view += child_node.total_view;
        }

        if (child_list.size() > 0){
            Collections.sort(child_list);
            boolean isListNode = isList(rootView);
            List<Integer> cnt = new ArrayList<>();
            int ccnt = 0;
            for(ViewNode childNode: child_list) {
                int id = childNode.nodeRelateHash;
                if (cnt.contains(id))
                    ++ccnt;
                else{
                    cnt.add(id);
                    relate_hash_string += id;
                }
            }
            if (!isListNode && ccnt > child_list.size() * 2 / 3)
                isListNode = true;
            now.isList = isListNode;
            now.children = child_list;
        }
        now.nodeRelateHash =relate_hash_string.hashCode();
        return now;
    }
    
    public List<Integer> parse_coordinates(String bounds){
        int x1 = Integer.parseInt(bounds.substring(1, bounds.indexOf(",")));
        int x2 = Integer.parseInt(bounds.substring(bounds.lastIndexOf("[")+1, bounds.lastIndexOf(",")));
        int y1 = Integer.parseInt(bounds.substring(bounds.indexOf(",")+1, bounds.indexOf("]")));
        int y2 = Integer.parseInt(bounds.substring(bounds.lastIndexOf(",")+1, bounds.lastIndexOf("]")));
        List<Integer> coordinates = new ArrayList<>();
        coordinates.add(x1);
        coordinates.add(x2);
        coordinates.add(y1);
        coordinates.add(y2);
        return coordinates;
    }
    public static String getLast(String name){
        if (name == null){
            return "";
        }

        if (!name.contains("."))
            return name;
        String[] words = name.split("\\.");
        return  words[words.length-1];
    }
    public boolean isList(Element view){
        String className = view.attr("class");
        boolean beFiltered = false;
        for(int i=0; i < filtsBys.length; i++){
            beFiltered = (beFiltered || className.contains(filtsBys[i]));
            if (beFiltered)
                return true;

        }
        return beFiltered;
    }
}