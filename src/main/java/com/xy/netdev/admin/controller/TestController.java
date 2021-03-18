package com.xy.netdev.admin.controller;

import com.xy.netdev.transit.schedule.ScheduleQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试
 *
 * @author duwenxu
 * @create 2021-03-17 15:27
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ScheduleQuery scheduleQuery;

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public void queryPageList() {
        scheduleQuery.doScheduleQuery();
    }
}
