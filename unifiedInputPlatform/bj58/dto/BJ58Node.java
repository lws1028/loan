package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

/**
 * 58同城节点状态 对应类
 */
public class BJ58Node {

    private static Node first;

    static {
        Node temp;
        first = new Node(10, "客服更进中", "");
        first.setSuccess(new Node(20, "客户有意愿", ""));
        first.setFailed(new Node(11, "客服跟进失败", "个人资质不符"));
        temp = first.getSuccess();
        temp.setSuccess(new Node(30, "预审通过", ""));
        temp.setFailed(new Node(31, "预审拒绝", "预审批拒绝"));
        temp = temp.getSuccess();
        temp.setSuccess(new Node(40, "终审通过", ""));
        temp.setFailed(new Node(41, "终审拒绝", "审核拒绝"));
        temp = temp.getSuccess();
        temp.setSuccess(new Node(50, "合同已生效", ""));
        temp.setFailed(new Node(51, "放款拒绝", "放款拒绝"));
        temp = temp.getSuccess();
        temp.setSuccess(new Node(60, "合同已放款", ""));
    }

    public static Node start() {
        Node node = first;
        return node;
    }

    public static Node getSuccessNode(Integer status) {
        Node node = first;
        return get(node, status);
    }

    private static Node get(Node node, Integer status) {
        if (node == null) {
            return null;
        }
        if (status.equals(node.getStatus())) {
            return node;
        }
        return get(node.getSuccess(), status);
    }

}
