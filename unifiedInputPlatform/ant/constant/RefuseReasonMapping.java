package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.constant;

import com.zlhj.unifiedInputPlatform.ant.enums.RefuseReasonType;

import java.util.HashMap;

public class RefuseReasonMapping {
    public static final HashMap<String, RefuseReasonType> refuseReasonMap = new HashMap<>();
     static {
        refuseReasonMap.put("1",RefuseReasonType.RR_SYS_OTHER);
        refuseReasonMap.put("2",RefuseReasonType.RR_USER_NO_INTENTION);
        refuseReasonMap.put("3",RefuseReasonType.RR_USER_NO_ANSWER);
        refuseReasonMap.put("4",RefuseReasonType.RR_SYS_OTHER);
        refuseReasonMap.put("5",RefuseReasonType.RR_BASIC_CHE_NOT_MATCH);
        refuseReasonMap.put("6",RefuseReasonType.RR_USER_NO_ANSWER);
        refuseReasonMap.put("7",RefuseReasonType.RR_USER_NO_WILL);
        refuseReasonMap.put("8",RefuseReasonType.RR_USER_NO_INTENTION);
        refuseReasonMap.put("9",RefuseReasonType.RR_USER_UNWILLING_MORTGAGE);
        refuseReasonMap.put("10",RefuseReasonType.RR_SYS_OVER_INT);
        refuseReasonMap.put("11",RefuseReasonType.RR_SYS_OTHER);
        refuseReasonMap.put("12",RefuseReasonType.RR_BASIC_AGE);
        refuseReasonMap.put("13",RefuseReasonType.RR_OVD_PBOC_NOW);
        refuseReasonMap.put("14",RefuseReasonType.RR_BASIC_CHE_MORTGAGED);
        refuseReasonMap.put("15",RefuseReasonType.RR_SYS_OTHER);
        refuseReasonMap.put("16",RefuseReasonType.RR_SYS_OTHER);
        refuseReasonMap.put("17",RefuseReasonType.RR_SYS_OTHER);
        refuseReasonMap.put("18",RefuseReasonType.RR_SYS_OTHER);
    }
}
