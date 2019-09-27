package uk.co.itello.pingerconfig;

public class Notify {
    private String ip;

    public Notify() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return ip;
    }
}
