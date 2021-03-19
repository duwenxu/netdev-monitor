package com.xy.netdev.frame.protocol.xml;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.XmlUtil;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.listToBytes;

@Deprecated
public final class ProtocolXmlHandler {

    public static byte[] pack(List<ProtocolXmlEntity.ParamEntity> paramEntities){
       List<byte[]> list = new ArrayList<>(paramEntities.size());
        paramEntities.forEach(paramEntity -> {
//            list.add(NumToBytes(paramEntity.getValue(), paramEntity.getLength()));
        });
        return listToBytes(list);
    }



    public static ProtocolXmlEntity xmlToProtocolXmlEntity(String xmlPath){
        ClassPathResource resource = new ClassPathResource(xmlPath);
        Document document = XmlUtil.readXML(resource.getStream());
        Map<String, Object> map = XmlUtil.xmlToMap(XmlUtil.toStr(document));
        Map<String, Object> device =(Map<String, Object>) map.get("Device");

        Object param = device.get("Param");
        List<ProtocolXmlEntity.ParamBodyEntity> paramBodyEntities = getParamBodyEntities(param);

        String deviceMark = device.get("Mark").toString();
        ProtocolXmlEntity protocolXmlEntity = new ProtocolXmlEntity();
        protocolXmlEntity.setDeviceMark(deviceMark);
        protocolXmlEntity.setBodyEntities(paramBodyEntities);
        return protocolXmlEntity;
    }

    private static List<ProtocolXmlEntity.ParamBodyEntity> getParamBodyEntities(Object param) {
        List<ProtocolXmlEntity.ParamBodyEntity> paramBodyEntities =  new ArrayList<>();
        if (param instanceof List){
            List<Map<String, Object>> list = (List<Map<String, Object>>) param;
            list.forEach(objectMap -> paramBodyEntities.add(setParamBodyEntity(objectMap)));
        }else {
            ProtocolXmlEntity.ParamBodyEntity paramBodyEntity = setParamBodyEntity((Map<String, Object>) param);
            paramBodyEntities.add(paramBodyEntity);
        }
        return paramBodyEntities;
    }

    private static ProtocolXmlEntity.ParamBodyEntity setParamBodyEntity(Map<String, Object> map){
        ProtocolXmlEntity.ParamBodyEntity paramBodyEntity = new ProtocolXmlEntity.ParamBodyEntity();
        paramBodyEntity.setParamMark(map.get("Mark").toString());
        Map<String, String> body = (Map<String, String>) map.get("Body");
        paramBodyEntity.setParamEntities(setParamEntity(body));
        return paramBodyEntity;
    }

    private static List<ProtocolXmlEntity.ParamEntity> setParamEntity(Map<String, String> map){
        List<ProtocolXmlEntity.ParamEntity> paramEntities = new ArrayList<>(map.size());
        map.forEach((key, value) -> {
            //参数赋值
            List<String> list = StrSpliter.split(value, ',', true, true);
            ProtocolXmlEntity.ParamEntity paramEntity = new ProtocolXmlEntity.ParamEntity();
            paramEntity.setName(key);
            paramEntity.setNo(Integer.parseInt(list.get(0)));
            paramEntity.setOffset(Integer.parseInt(list.get(1)));
            paramEntity.setLength(Integer.parseInt(list.get(2)));
            paramEntity.setValue(list.get(3));
            paramEntity.setOrder(Integer.parseInt(list.get(4)));
            paramEntities.add(paramEntity);
        });
        return paramEntities.stream()
                .sorted(Comparator.comparing(ProtocolXmlEntity.ParamEntity::getNo))
                .collect(Collectors.toList());
    }
}
