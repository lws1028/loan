package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node {
    private Node success;

    private Node failed;

    private Integer status;

    private String msg;

    private String defaultErrorMsg;

    Node(Integer status, String msg, String defaultErrorMsg) {
        this.status = status;
        this.msg = msg;
        this.defaultErrorMsg = defaultErrorMsg;
    }

    private Node() {
    }

}