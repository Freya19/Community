package site.pyyf.commons.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

public class MultipartSerializable {
    public static String file2json(MultipartFile file) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            String filename = file.getOriginalFilename();
            byte[] bytes = file.getBytes();
            map.put("fileName", filename);
            map.put("fileByte", Base64.encodeBase64String(bytes));

            return JSONObject.toJSONString(map);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Object json2file(String json, String getObj) {
        try {
            Map map = (Map) JSONObject.parseArray(json, Map.class);
            if (map != null && map.size() > 0) {
                if (getObj.equals("fileByte"))
                    return Base64.decodeBase64(map.get("fileByte") == null ? "" : map.get("fileByte").toString());
                if (getObj.equals("fileName"))
                    return map.get("fileName").toString();

            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }
}
