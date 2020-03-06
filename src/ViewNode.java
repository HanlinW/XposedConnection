import java.io.Serializable;
import java.util.List;

// Modified from Paladin's code
public class ViewNode implements Comparable{
    public String viewTag;
    public int total_view;
    public String viewText;
    public String xpath;

    public boolean isList;

    public int nodeHash;
    public boolean clickable;

    public int nodeRelateHash;

    //在树种的层级
    public int depth;

    //view的子节点
    public List<ViewNode> children;

    //view的父节点
    public ViewNode parent;


    //viewNodeID表示node在树中的编号
    public int viewNodeID;
    public int viewNodeIDRelative;

    public String resourceID;
    public String contentDesc;

    public int width;
    public int height;
    public int x;
    public int y;
    public int x2;
    public int y2;
    @Override
    public int compareTo(Object another) {
        int res = nodeRelateHash - ((ViewNode) another).nodeRelateHash;
        if (res != 0)
            return res;
        res = y - ((ViewNode) another).y;
        if (res != 0)
            return res;
        return x - ((ViewNode) another).x;
    }
    
    //返回的是class+深度+位置
    public String calString(){
        return getAbbr(this.viewTag) + "-" + depth + "-" + viewText + "-" + this.x + "-" + this.y;
    }


    public String calStringWithoutPosition(){
        return getAbbr(this.viewTag) + "-" + this.depth;
    }
    
    public static String getAbbr(String name){
        String[] words = name.split("\\.");
        if(words.length == 0)
            return name;
        String result = "";
        for(int i = 0; i < words.length; i++){
            result += (""+words[i].charAt(0));
        }
        return result;
    }
    
    public ViewNode findRootNode(){
        ViewNode root = parent;
        while(root != null){
            if(root.parent != null)
                root = root.parent;
            else
                break;
        }
        return root;
    }
}