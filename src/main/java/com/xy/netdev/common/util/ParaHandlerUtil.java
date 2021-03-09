package com.xy.netdev.common.util;
/**
 * <p>
 *参数处理工具类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public class ParaHandlerUtil {

    /**
     * 链接符号
     */
    private static final  String  LINK_MARK ="&";
    /**
     * @功能：链接两个字段
     * @param mark1   标识1
     * @param mark2   标识2
     * @return
     */
    public static String genLinkKey(String mark1,String mark2){
        return mark1 + LINK_MARK + mark2;
    }

}
