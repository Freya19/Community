package site.pyyf.forum.service.impl;

import site.pyyf.forum.service.ICodePreviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodePreviewServiceImpl implements ICodePreviewService {
    @Override
    public String addQuotationMarks(String language, StringBuilder oriCode) {

        StringBuilder suffixAndCode = new StringBuilder();
        suffixAndCode.append("```").append(language).append("\n").append(oriCode).append("```");
        return suffixAndCode.toString();

    }

    @Override
    public StringBuilder addHtmlShowStyle(StringBuilder code, List<String> languages) {
        int index;
        int start = 0;
        String codeToString = code.toString();
        while ((index = codeToString.indexOf("<pre><code class=\"language-", start)) != -1) {
            start = addHtmlClass(code,index,languages);
            codeToString = code.toString();

        }
        return code;
    }


    @Override
    public StringBuilder addCompileModule(StringBuilder code, String language, int type) {
        String startMatch = null;
        String endMatch = null;
        if(type == 0){
            startMatch = "<pre><code class=\"language-\"" + language + "\">";
            endMatch = "</code></pre>";
        }
        else {
            startMatch = "```"+language;
            endMatch = "```";
        }
        int count = 0;
        int index;
        int start = 0;
        String codeToString = code.toString();
        while ((index = codeToString.indexOf(startMatch, start)) != -1) {
            //index是<pre><code 或者 ```java的初始处
            String insertContent = "<button id=\"button"+count+"\" onclick=\"compile('" + count + "')\" class=\"btn btn-success\">" +
                    "             <i class=\"fa fa-play\"></i>&nbsp;  运行" +
                    "    </button>" +
                    "<div id=\"code" + count + "\"> \n";
            code.insert(index, insertContent);
            count++;
            //index是<button的初始处
            int end = code.toString().indexOf(endMatch, index+insertContent.length()+startMatch.length());
            code.insert(end+endMatch.length(), "\n </div>");

            codeToString = code.toString();
            start = end;

        }
        return code;
    }

    @Override
    public StringBuilder addHtmlCompileModule(StringBuilder code, String language) {

        int count = 0;
        int index;
        int start = 0;
        String codeToString = code.toString();
        while ((index = codeToString.indexOf("<pre><code class=\"language-" + language + "\">", start)) != -1) {
            code.insert(index, "<button id=\"button"+count+"\" onclick=\"compile('" + count + "')\" class=\"btn btn-success\">\n" +
                    "             <i class=\"fa fa-play\"></i>&nbsp;  运行\n" +
                    "    </button>" +
                    "<div id=\"code" + count + "\">");
            count++;
            int end = code.toString().indexOf("</code></pre>", index) + "</code></pre>".length();
            code.insert(end, "</div>");


            codeToString = code.toString();
            start = end;

        }
        return code;
    }

    private int addHtmlClass(StringBuilder code, int index, List<String> languages) {
        String original = code.toString();
        //判断从index处开始是否是某种语言
        for (int i = 0; i < languages.size(); i++) {
            if (original.substring(index, index+("<pre><code class=\"language-"+languages.get(i)).length()).equals("<pre><code class=\"language-"+languages.get(i))) {
                code.replace(index, index + ("<pre><code class=\"language-"+languages.get(i) + "\">").length(), "<pre class=\"line-numbers\"><code class=\"language-" + languages.get(i) + " match-braces \">");
                return index +("<pre class=\"line-numbers\"><code class=\"language-" + languages.get(i) + " match-braces \">").length();
            }
        }
        //不是设定的这几种语言，表明没有考虑到，则直接跳过
        return index+10;
    }
}
