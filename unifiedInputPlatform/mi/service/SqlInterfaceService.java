package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.service;

public interface SqlInterfaceService {
    /**
     *
     * @param resMap   返回信息
     * @param branchLoanID  @see spl_BUSSINESSBASIC表  branch_loan_id主键
     * @param infoSubmitParam  接收信息
     * @param interfaceName 接口描述
     * @return
     */
    boolean saveInterfaceInfo(String resMap, int branchLoanID, String infoSubmitParam, String interfaceName);

    boolean saveSendInterfaceInfo(String message, Integer branchLoanID, String interfaceName);
}
