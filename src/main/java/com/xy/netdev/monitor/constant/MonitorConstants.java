package com.xy.netdev.monitor.constant;

/**
 * 监控系统常量类
 *
 * @author duwenxu
 * @create 2021-03-10 14:30
 */
public interface MonitorConstants {
    /**
     * 设备参数状态
     */
    /**只写*/
    String WRITE_ONLY = "0022001";
    /**只读*/
    String READ_ONLY = "0022002";
    /**读写*/
    String READ_WRITE = "0022003";

    /**
     * 接口类型
     */
    /**查询*/
    String QUERY = "0027001";
    /**控制*/
    String SET = "0027002";

    /**
     * 设备类型
     */
    /**子调制解调器*/
    String SUB_MODEM = "0020008";
    String SUB_KU_GF = "0020010";

    /**数据类型*/
    String STRING_CODE = "0023004";
}
