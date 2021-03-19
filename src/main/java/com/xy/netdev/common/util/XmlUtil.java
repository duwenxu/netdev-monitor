package com.xy.netdev.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.xy.common.exception.BaseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * XML工具类
 *
 * @author 唐玺良
 * @date 2020-04-02
 */
public class XmlUtil {

    /**
     * JavaBean转换成xml
     *
     * @param obj
     * @return
     */
    public static String convertToXml(Object obj) {
        //此处增加Feature.OrderedField是为了不影响字段的顺序
        return JsonXmlUtils.jsonToFormatXml(JSONObject.parseObject(JSON.toJSONString(obj), Feature.OrderedField));
    }
    /**
     * xml转换成JavaBean
     * @param xml
     * @param c
     * @return
     */
    public static <T> T converyToJavaBean(String xml, Class<T> c){
        T t = null;
        try {
            JSONObject jsonObject = JsonXmlUtils.xmlToJson(xml);
            t = JSON.parseObject(jsonObject.toJSONString(),c);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
        return t;
    }

    /**
     * xml转换成Json
     * @param xml
     * @return
     */
    public static String  xmlToJsonStr(String xml){
        try {
            return JsonXmlUtils.xmlToJson(xml).toJSONString();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    // 对象转xml
    public static String convertJaxbToXml(Object object) {
        String xml  = "";
        try {
            JAXBContext jc = JAXBContext.newInstance(object.getClass());

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");// 编码格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// 是否格式化生成的xml串
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);// 是否省略xml头声明信息
            StringWriter writer = new StringWriter();
            marshaller.marshal(object,writer);
            xml = writer.toString();
        } catch (JAXBException e) {
            throw new BaseException("对象转换为xml失败，错误原因：" +e.getMessage());
        }
        return xml;
    }
}

