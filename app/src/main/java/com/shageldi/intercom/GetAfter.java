package com.shageldi.intercom;

import java.util.Calendar;
import java.util.Date;

public class GetAfter {
    public Date getBetween(Date startDate,int day){
        Calendar c=Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE,day);
        return c.getTime();
    }
}
