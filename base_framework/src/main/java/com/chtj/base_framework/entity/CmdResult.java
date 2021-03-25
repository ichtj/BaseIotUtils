package com.chtj.base_framework.entity;

public class CmdResult {
    private int result;
    private String successMeg;
    private String errMeg;

    public CmdResult() {
    }

    public CmdResult(int result, String successMeg, String errMeg) {
        this.result = result;
        this.successMeg = successMeg;
        this.errMeg = errMeg;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getSuccessMeg() {
        return successMeg;
    }

    public void setSuccessMeg(String successMeg) {
        this.successMeg = successMeg;
    }

    public String getErrMeg() {
        return errMeg;
    }

    public void setErrMeg(String errMeg) {
        this.errMeg = errMeg;
    }
}
