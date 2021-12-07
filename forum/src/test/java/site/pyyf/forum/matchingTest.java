package site.pyyf.forum;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class matchingTest {

    /**
     * 输入项:有权机关名称校验，包含以下字段则校验通过
     *
     * @param inputAuthorityName 输入的有权机关的名字
     * @return boolean类型
     */
    public boolean isMatching(String inputAuthorityName) {

        List<String> checkingList = Arrays.asList("公安部", "公安局", "人民法院", "人民检察院", "税务局", "海关", "监狱",
                "边防检查站", "部队", "国家安全部", "国家安全局", "走私犯罪侦查局", "缉私局", "证券监督管理委员会",
                "银行保险监督管理会", "审计署", "审计局", "外汇管理局", "人民银行", "公证处", "人力资源与社会保障局");

        int len = inputAuthorityName.length();

        for (int startIndex = 0; startIndex < len; startIndex++) {
            for (int endIndex = startIndex + 1; endIndex < len; endIndex++) {
                String comparedStr = inputAuthorityName.substring(startIndex, endIndex);

                //[\u4E00-\u9FA5]是unicode2的中文区间
                if (checkingList.contains(comparedStr)
                        || Pattern.matches("中国[\u4E00-\u9FA5]{0,3}纪律检查委员会[\u4E00-\u9FA5]{0,3}监察委员会",
                        inputAuthorityName)) {
                    return true;
                }
            }
        }
        return false;
    }


   /*
    @TarestOperation(name = "testAnnotation")
    @ApiOperation(vale = "测试注解",notes = "", httpMethod = "POST")
    @RequestMapping(value="/api/xx/testAnnotation",produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE},method = RequestMethod.POST)
    public void testAnnotation(@RequestBody String str,
                               @RequestParam(name="pIndex",required = false) Integer pIndex,
                               @RequestParam(name="pSize",required = false) Integer pSize,
                               @RequestParam(name="sortField",required = false) Integer sortField,
                               @RequestParam(name="sortOrder",required = false) Integer sortOrder) throws PTPRuntimeException;
   */

    /*
    public static String getUserId(){
        Object value = get("currentUseId");
        return retureObjectValue(value);
    }
     */


    public static void main(String[] args) {
        String inputAuthorityName = "中华人民共和国公安部";
        matchingTest matchingTest = new matchingTest();
        System.out.println(matchingTest.isMatching(inputAuthorityName));
    }
}

