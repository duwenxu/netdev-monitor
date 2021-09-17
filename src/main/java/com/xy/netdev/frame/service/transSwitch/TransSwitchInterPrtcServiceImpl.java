package com.xy.netdev.frame.service.transSwitch;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;

/**
 * 1:1 转换开关
 *
 * @author sunchao
 * @create 2021-05-10 09:30
 */
@Service
@Slf4j
public class TransSwitchInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    //分隔符
    private static final String SEPAR_CHAR ="5f";

    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return063075ff5511ff
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        String dataStr = HexUtil.encodeHexStr(bytes);
        //将参数帧按照分隔符进行分割
        String[] paramStrs = dataStr.split(SEPAR_CHAR);
        //全查询：按容器中的参数顺序解析
        String devType = respData.getDevType();
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for(String str : paramStrs){
            String paraCmk = str.substring(0, 2);  //关键字
            String paraValueStr = str.substring(2);  //参数值
            byte[] paraByte = HexUtil.decodeHex(paraValueStr);
            FrameParaInfo currentPara = BaseInfoContainer.getParaInfoByCmd(devType, paraCmk);
            if (StringUtils.isEmpty(currentPara.getParaNo())){ continue;}
            if(currentPara.getCmplexLevel().equals(PARA_COMPLEX_LEVEL_COMPOSE)){
                //当参数为组合参数时特殊处理生成指定格式的字符串
                paraValueStr = HexStrToBit(paraValueStr).substring(4);
                StringBuffer sb = new StringBuffer(paraValueStr);
                sb.insert(1,"_");
                sb.insert(3,"_");
                sb.insert(5,"_");
                paraValueStr = sb.toString();
                //改变子参数的数据
                String[] paraList = paraValueStr.split("_");
                for(int i=0; i< paraList.length;i++){
                    //填充子参数
                    FrameParaData subFrame = genFramePara(currentPara.getSubParaList().get(i),paraList[i]);
                    subFrame.setDevNo(respData.getDevNo());
                    frameParaDataList.add(subFrame);
                }
            }
            //填充参数本身
            FrameParaData frameParaData = genFramePara(currentPara,paraValueStr);
            frameParaData.setDevNo(respData.getDevNo());
            frameParaData.setParaOrigByte(paraByte);
            frameParaDataList.add(frameParaData);
        }
        respData.setFrameParaList(frameParaDataList);
        //接口查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    /**
     * 生成参数数据帧FrameParaData
     * @param currentPara
     * @param paraValueStr
     * @return
     */
    private  FrameParaData genFramePara(FrameParaInfo currentPara,String paraValueStr){
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentPara.getDevType())
                .paraNo(currentPara.getParaNo())
                .build();
        frameParaData.setParaVal(paraValueStr);
        return frameParaData;
    }

    /**
     * Byte转Bit
     */
    private String HexStrToBit(String hexStr) {
        byte b = HexUtil.decodeHex(hexStr)[0];
        return "" +(byte)((b >> 7) & 0x1) +
                (byte)((b >> 6) & 0x1) +
                (byte)((b >> 5) & 0x1) +
                (byte)((b >> 4) & 0x1) +
                (byte)((b >> 3) & 0x1) +
                (byte)((b >> 2) & 0x1) +
                (byte)((b >> 1) & 0x1) +
                (byte)((b >> 0) & 0x1);
    }
}