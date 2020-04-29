package site.pyyf.commons.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    // hello -> abc123def456
    // hello + 3e4a8 -> abc123def456abc
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static String extractChinese(String str){
        //使用正则表达式
        Pattern pattern = Pattern.compile("[^\u4E00-\u9FA5]");
        //[\u4E00-\u9FA5]是unicode2的中文区间
        Matcher matcher = pattern.matcher(str);
        final String result = matcher.replaceAll("");
        return result;

    }
    public static boolean isChineseNumberAlpha(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        if(Character.isDigit(c))
            return true;
        if(Character.isDigit(c))
            return true;
        return false;
    }

    public static int countStringNumber1(String str,String s) {
        int count = 0;
        while(str.indexOf(s) != -1) {
            str = str.substring(str.indexOf(s) + 1);
            count++;
        }
        return count;
    }
    public static int countStringNumber2(String str,String s) {
        String str1 = str.replaceAll(s, "");
        int len1 = str.length(),len2 = str1.length(),len3 = s.length();
        int count = (len1 - len2) / len3;
        return count;
    }

    //判断字符串中除代码外是否有#
    public static boolean hasjin(String content){
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


    /**
     * @Description 处理掉QQ网名中的特殊表情
     * @Author xw
     * @Date 18:26 2020/2/25
     * @Param [str]
     * @return java.lang.String 返回处理之后的网名
     **/
    public static String removeNonBmpUnicode(String str) {
        if (str == null) {
            return null;
        }
        str = str.replaceAll("[^\\u0000-\\uFFFF]", "");
        if ("".equals(str)) {
            str = "($ _ $)";
        }
        return str;
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 25);
        System.out.println(getJSONString(0, "ok", map));
    }


}
