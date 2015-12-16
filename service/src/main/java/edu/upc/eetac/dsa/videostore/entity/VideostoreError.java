package edu.upc.eetac.dsa.videostore.entity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideostoreError {
    private int status;
    private String reason;

    public VideostoreError() {
    }

    public VideostoreError(int status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

