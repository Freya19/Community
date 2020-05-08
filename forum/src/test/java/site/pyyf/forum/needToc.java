package site.pyyf.forum;

public class needToc {

    public static void main(String[] args) {
        String content = "```java" +
                "##内容内容" +
                "```" +
                "`" +
                "1##内容内容" +
                "`" +
                "" +
                "##内容内容";
        final long l = System.currentTimeMillis();
        System.out.println(l);
        boolean b = needToc(content);
        final long l1 = System.currentTimeMillis();
        System.out.println(l1);
        System.out.println(b);
    }

    public static boolean needToc(String content){
        int start = 0;
        boolean find = true;
        boolean inlineCode = false;
        boolean multiCode = false;
        while(start+3<content.length()){
            //之前未出现过```并且现在出现了`
            if(!multiCode&&(content.charAt(start)=='`')&&(content.charAt(start+1)!='`'))
            {
                if(!inlineCode){
                    find = false;
                    inlineCode = true;
                }else {
                    find = true;
                    inlineCode = false;
                }
                start++;
                continue;
            }
            //之前未出现过`并且现在出现了```
            if(!inlineCode&&(content.substring(start,start+3).equals("```")))
            {
                if(!multiCode){
                    find = false;
                    multiCode = true;
                }else {
                    find = true;
                    multiCode = false;
                }
                start+=3;
                continue;
            }
            //普通字符
            if(find){
                if(content.charAt(start)=='#')
                    return true;
            }
            start++;
        }
        return false;
    }
}
