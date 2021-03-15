package com.xy.netdev.common.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.Attribute;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * @author dongxingli
 * Created on 2019/4/3 19:00
 */
@Slf4j
public class JsonXmlUtils {

    private static final String ENCODING = "UTF-8";

    /**
     * JSON对象转的xml字符串-格式化 带空格 换行符 输出到文件可读性好
     *
     * @param json JSON对象
     * @return 漂亮的xml字符串
     */
    public static String jsonToFormatXml(JSONObject json) {
        StringWriter formatXml = new StringWriter();
        try {
            Document document = jsonToDocument(json);
            /* 格式化xml */
            OutputFormat format = OutputFormat.createPrettyPrint();
            // 设置缩进为4个空格
            format.setIndent(" ");
            format.setIndentSize(4);
            XMLWriter writer = new XMLWriter(formatXml, format);
            writer.write(document);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return formatXml.toString();
    }

    /**
     * JSON对象转xml字符串-不带空格换行符
     *
     * @param json JSON对象
     * @return xml字符串
     * @throws SAXException
     */
    public static String JsonToXml(JSONObject json) {
        return jsonToDocument(json).asXML();
    }

    /**
     * JSON对象转Document对象
     *
     * @param json JSON对象
     * @return Document对象
     */
    public static Document jsonToDocument(JSONObject json) {
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding(ENCODING);
        Set<String> rootKey = json.keySet();
        Element root = DocumentHelper.createElement(rootKey.iterator().next());
        addJsonObjectElement(json.getJSONObject(rootKey.iterator().next()), root);
        document.add(root);
        return document;
    }

    /**
     * 添加JSONArray Element
     *
     * @param jsonArray 传入的json数组
     * @param nodeName  节点名称
     * @param arrRoot   上层节点
     * @return Element对象
     */
    private static void addJsonArrayElement(JSONArray jsonArray, String nodeName, Element arrRoot) {
        for (Object aJsonArray : jsonArray) {
            JSONObject jsonObject = (JSONObject) aJsonArray;
            Element node = DocumentHelper.createElement(nodeName);
            // 继续遍历
            for (String key : jsonObject.keySet()) {
                Element element = DocumentHelper.createElement(key);
                Object child = JSON.toJSON(jsonObject.get(key));
                if (child instanceof JSONArray) {
                    // 递归
                    addJsonArrayElement(jsonObject.getJSONArray(key), key, node);
                } else if (child instanceof JSONObject) {
                    addJsonObjectElement(jsonObject.getJSONObject(key), element);
                    node.add(element);
                }else if(key.contains("-")){
                    node.addAttribute(key.replace("-",""), (String) child);
                }else if("".equals(key)){
                    //当为“”时直接给node设值
                    node.addText((String) child);
                }else {
                    element.setText(jsonObject.getString(key));
                    node.add(element);
                }
            }
            arrRoot.add(node);
        }
    }


    /**
     * 添加JSONObject Element
     *
     * @param json JSON对象
     * @param root 上层节点
     * @return Element对象
     */
    private static void addJsonObjectElement(JSONObject json, Element root) {

        for (String key : json.keySet()) {
            Element node = DocumentHelper.createElement(key);
            Object child = json.get(key);
            if (child instanceof JSONObject) {
                addJsonObjectElement(json.getJSONObject(key), node);
                root.add(node);
            } else if (child instanceof JSONArray) {
                addJsonArrayElement(json.getJSONArray(key), key, root);
            }else if(key.contains("-")){
                //当key中包含-号则为上一节点增加属性值
                root.addAttribute(key.replace("-",""), (String) child);
            }else {
                node.setText(json.getString(key));
                root.add(node);
            }
        }

    }

    /**
     * XML字符串转JSON对象
     *
     * @param xml xml字符串
     * @return JSON对象
     * @throws DocumentException
     */
    public static JSONObject xmlToJson(String xml) throws DocumentException {
        JSONObject json = new JSONObject();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(xml));
        Element root = document.getRootElement();
        json.put(root.getName(), elementToJson(root));

        return json;
    }

    /**
     * Element对象转JSON对象
     *
     * @param element Element对象
     * @return JSON对象
     */
//    public static JSONObject elementToJson(Element element) {
//        JSONObject json = new JSONObject();
//        for (Object child : element.elements()) {
//            Element e = (Element) child;
//            if (e.elements().isEmpty()) {
//                json.put(e.getName(), e.getText());
//            } else {
//                json.put(e.getName(), elementToJson(e));
//            }
//        }
//
//        return json;
//    }

    /**
     * org.dom4j.Element 转  com.alibaba.fastjson.JSONObject
     * @param node
     * @return
     */
    public static JSONObject elementToJson(Element node) {
        JSONObject result = new JSONObject();
        // 当前节点的名称、文本内容和属性
        List<Attribute> listAttr = node.attributes();// 当前节点的所有属性的list
        for (Attribute attr : listAttr) {// 遍历当前节点的所有属性
            result.put(attr.getName(), attr.getValue());
        }
        // 递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            for (Element e : listElement) {// 遍历所有一级子节点
                if (e.attributes().isEmpty() && e.elements().isEmpty()) // 判断一级节点是否有属性和子节点
                    result.put(e.getName(), e.getTextTrim());// 沒有则将当前节点作为上级节点的属性对待
                else {
//                    if (!result.containsKey(e.getName())) // 判断父节点是否存在该一级节点名称的属性
//                        result.put(e.getName(), new JSONArray());// 没有则创建
//                    ((JSONArray) result.get(e.getName())).add(elementToJson(e));// 将该一级节点放入该节点名称的属性对应的值中
                    // 判断父节点是否存在该一级节点名称的属性
                    if (!result.containsKey(e.getName())){
                        result.put(e.getName(), elementToJson(e));// 没有则创建
                    }else {
                        Object obj = result.get(e.getName());
                        if(obj instanceof JSONArray){
                            ((JSONArray) result.get(e.getName())).add(elementToJson(e));
                        }else{
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.add(result.get(e.getName()));
                            jsonArray.add(elementToJson(e));
                            result.put(e.getName(), jsonArray);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 文件内容转换成字符串
     *
     * @param filePath 文件路径
     * @return 内容字符串
     * @throws IOException
     */
    public static String fileToString(URL filePath) throws IOException {
        return IOUtils.toString(filePath, ENCODING);
    }

    /**
     * 文件内容转换成字符串
     *
     * @param filePath 文件路径
     * @return 内容字符串
     * @throws IOException
     */
    public static String fileToString(String filePath) throws IOException {
        return IOUtils.toString(Paths.get(filePath).toUri(), ENCODING);
    }

    /**
     * 字符串输出到文件
     *
     * @param str      字符串内容
     * @param filePath 文件路径
     * @throws IOException
     */
    public static void stringToFile(String str, String filePath) throws IOException {
        FileUtils.writeStringToFile(Paths.get(filePath).toFile(), str, ENCODING);
    }

    /**
     * 字符串输出到文件
     *
     * @param str      字符串内容
     * @param filePath 文件路径
     * @throws IOException
     */
    public static void stringToFile(String str, URL filePath) throws IOException {
        FileUtils.writeStringToFile(new File(filePath.getPath()), str, ENCODING);
    }
}
