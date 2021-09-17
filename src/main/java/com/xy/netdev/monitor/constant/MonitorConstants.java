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
    /**独立查询*/
    String SINGLE_QUERY = "0027001";
    /**独立控制*/
    String SINGLE_SET = "0027002";
    /**页面查询接口*/
    String PAGE_QUERY = "0027003";
    /**组装查询接口*/
    String PACKAGE_QUERY = "0027004";
    /**组装控制接口*/
    String PACKAGE_SET = "0027005";
    /**子接口*/
    String SUB_QUERY = "0027006";

    /**
     * 设备类型
     */
    /**子调制解调器*/
    String SUB_MODEM = "0020008";
    String SUB_KU_GF = "0020010";

    /**数据类型*/
    String STRING_CODE = "0023004";


    /**参数数据类型*/
    String STR="0023004";
    String INT ="0023002";
    String UNIT="0023003";
    String BYTE="0023001";
    String FLOAT = "0023008";
    String IP_ADDRESS = "0023006";
    String IP_MASK = "0023007";
    String DOUBLE = "0023009";
}
