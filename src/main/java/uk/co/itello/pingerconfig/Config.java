package uk.co.itello.pingerconfig;

import java.util.List;

public class Config {
    private String url;
    private List<String> searchCriteria;
    private int retryDelay;

    public Config() {
    }

    public Config(String url, List<String> searchCriteria, int retryDelay) {
        this.url = url;
        this.searchCriteria = searchCriteria;
        this.retryDelay = retryDelay;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getSearchCriteria() {
        return searchCriteria;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSearchCriteria(List<String> searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    @Override
    public String toString() {
        return "Config{" +
                "url='" + url + '\'' +
                ", searchCriteria=" + searchCriteria +
                ", retryDelay=" + retryDelay +
                '}';
    }
}
