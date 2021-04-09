package com.xy.netdev;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.annotation.AutoLog;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.network.util.UdpClientUtil;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import io.swagger.models.auth.In;
import lombok.val;
import lombok.var;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author duwenxu
 * @create 2021-03-17 9:28
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NetdevApplicationTest {

    //54所ip地址 172.92.36.23:6933


//    设备主备切换状态上报：Recv:>010/ONLINE_1\r\n]
//    设备告警状态上报+告警上报：Recv:>010/SAS_FT\r\n]

    private static final String TEST_ADDRESS = "172.21.2.66";

    private static final Integer TEST_PORT = 9900;

    @Autowired
    private IBaseInfoService baseInfoService;

    @Autowired
    private ISysParamService sysParamService;

    public static byte[] pack(int cmd, byte[] bytes){
       return ArrayUtil.addAll(
                //信息类别
                ByteUtils.objToBytes(cmd, 2)
                //数据字段长度
                , ByteUtils.objToBytes(bytes.length, 2)
                //预留
                , ByteUtils.objToBytes(0, 4)
                //数据字段
                , bytes);
    }


    /**
     * 参数查询命令
     */
    @Test
    public void queryParam(){
        //获取设备信息
        Collection<BaseInfo> devInfos = BaseInfoContainer.getDevInfos();
        List<byte[]> list = new ArrayList<>();
        String stationNo = sysParamService.getParaRemark1(SysConfigConstant.PUBLIC_PARA_STATION_NO);
        int stationNum = devInfos.size();
        list.add(ByteUtils.objToBytes(0, 4));
        //查询标识
        list.add(ByteUtils.objToBytes(0x0003, 1));
        //站号
        list.add(ByteUtils.objToBytes(stationNo, 1));
        //设备数量
        list.add(ByteUtils.objToBytes(stationNum, 1));
        devInfos.stream()
                .filter(baseInfo -> baseInfo.getDevNo().equals("19"))
                .findFirst().ifPresent(baseInfo ->{
            String devNo = baseInfo.getDevNo();
            List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getParasByDevType(baseInfo.getDevType());
            int size = frameParaInfos.size();
            if (size>0){
                //设备型号
                list.add(ByteUtils.objToBytes(baseInfo.getDevType(), 2));
                //设备编号
                list.add(ByteUtils.objToBytes(devNo, 1));
                //设备参数数量
                list.add(ByteUtils.objToBytes(size, 1));
                frameParaInfos.forEach(frameParaInfo -> {
                    //参数编号
                    list.add(ByteUtils.objToBytes(frameParaInfo.getParaNo(), 1));
                });
            }
        });
        byte[] bytes = ByteUtils.listToBytes(list);
        byte[] pack = pack(0x0003, bytes);
        System.out.println(HexUtil.encodeHexStr(pack(0x0003, pack)));
        UdpClientUtil.send(TEST_ADDRESS, TEST_PORT, pack);
    }

    /**
     * 参数设置命令
     */
    @Test
    public void setParam(){
        //获取设备信息
        Collection<BaseInfo> devInfos = BaseInfoContainer.getDevInfos();
        List<byte[]> list = new ArrayList<>();
        String stationNo = sysParamService.getParaRemark1(SysConfigConstant.PUBLIC_PARA_STATION_NO);
        int stationNum = devInfos.size();
        list.add(ByteUtils.objToBytes(0, 4));
        //查询标识
        list.add(ByteUtils.objToBytes(0x0005, 1));
        //站号
        list.add(ByteUtils.objToBytes(stationNo, 1));
        //设备数量
        list.add(ByteUtils.objToBytes(stationNum, 1));
        devInfos.stream()
                .filter(baseInfo -> baseInfo.getDevNo().equals("19"))
                .findFirst().ifPresent(baseInfo ->{
            String devNo = baseInfo.getDevNo();
            List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getParasByDevType(baseInfo.getDevType());
            int size = frameParaInfos.size();
            if (size > 0){
                //设备型号
                list.add(ByteUtils.objToBytes(baseInfo.getDevType(), 2));
                //设备编号
                list.add(ByteUtils.objToBytes(devNo, 1));
                //设备参数数量
                list.add(ByteUtils.objToBytes(size, 1));
                frameParaInfos.forEach(frameParaInfo -> {
                    if (frameParaInfo.getParaByteLen() != null && frameParaInfo.getParaVal() != null){
                        //参数编号
                        list.add(ByteUtils.objToBytes(frameParaInfo.getParaNo(), 1));
                        //设备数据长度
                        list.add(ByteUtils.objToBytes(frameParaInfo.getParaByteLen(), 2));
                        //数据内容
                        list.add(ByteUtils.objToBytes(frameParaInfo.getParaVal(), Integer.parseInt(frameParaInfo.getParaByteLen())));
                    }

                });
            }
        });
        byte[] bytes = ByteUtils.listToBytes(list);
        byte[] pack = pack(0x0005, bytes);
        System.out.println(HexUtil.encodeHexStr(pack(0x0005, pack)));
        UdpClientUtil.send(TEST_ADDRESS, TEST_PORT, pack);
    }


    @Test
    public void paramWarning(){
        //获取设备信息
        Collection<BaseInfo> devInfos = BaseInfoContainer.getDevInfos();
        List<byte[]> list = new ArrayList<>();
        String stationNo = sysParamService.getParaRemark1(SysConfigConstant.PUBLIC_PARA_STATION_NO);
        int stationNum = devInfos.size();
        list.add(ByteUtils.objToBytes(0, 4));
        //查询标识
        list.add(ByteUtils.objToBytes(0x0007, 1));
        //站号
        list.add(ByteUtils.objToBytes(stationNo, 1));
        //设备数量
        list.add(ByteUtils.objToBytes(stationNum, 1));
        devInfos.stream()
                .filter(baseInfo -> baseInfo.getDevNo().equals("8"))
                .findFirst().ifPresent(baseInfo ->{
            String devNo = baseInfo.getDevNo();
            List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getParasByDevType(baseInfo.getDevType());
            int size = frameParaInfos.size();
            if (size>0){
                //设备型号
                list.add(ByteUtils.objToBytes(baseInfo.getDevType(), 2));
                //设备编号
                list.add(ByteUtils.objToBytes(devNo, 1));
                //设备参数数量
                list.add(ByteUtils.objToBytes(size, 1));
                frameParaInfos.forEach(frameParaInfo -> {
                    //参数编号
                    list.add(ByteUtils.objToBytes(frameParaInfo.getParaNo(), 1));
                });
            }
        });
        byte[] bytes = ByteUtils.listToBytes(list);
        byte[] pack = pack(0x0007, bytes);
        System.out.println(HexUtil.encodeHexStr(pack));
        UdpClientUtil.send(TEST_ADDRESS, TEST_PORT, pack);
    }

}
