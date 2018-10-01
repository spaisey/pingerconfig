package uk.co.itello.glastonconfig;

public class GlastoNotify {
    private String ip;

    public GlastoNotify() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "GlastoNotify{" +
                "ip='" + ip + '\'' +
                '}';
    }
}
