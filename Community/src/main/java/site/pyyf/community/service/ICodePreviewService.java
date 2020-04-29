package site.pyyf.community.service;

import java.util.List;

public interface ICodePreviewService {
    //增加引用标记，将代码用形如 ```java ```包裹起来
    String addQuotationMarks(String language, StringBuilder oriCode);

    StringBuilder addHtmlShowStyle(StringBuilder code, List<String> languages);

    StringBuilder addHtmlCompileModule(StringBuilder code, String language);

    //增加按钮和div
    //类型0  在<pre><code class="language- 前加button
    //类型1  在```language 前加button
    StringBuilder addCompileModule(StringBuilder code, String language, int type);

}
