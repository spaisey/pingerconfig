package uk.co.itello.glastonconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class ConfigurationController {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationController.class);
    @Value("${glasto.url}")
    private String url;
    @Value("${glasto.search.criteria}")
    private List<String> searchCriteria;
    @Value("${glasto.retry.delay}")
    private int retryDelay;

    @RequestMapping("/config")
    public GlastoConfig config() {
        return new GlastoConfig(url, searchCriteria, retryDelay);
    }

    @RequestMapping(value = "/config", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void config(@RequestBody GlastoConfig config) {
        this.url = config.getUrl();
        if (config.getSearchCriteria() != null && !config.getSearchCriteria().isEmpty()) {
            this.searchCriteria = config.getSearchCriteria();
        }
        if (config.getRetryDelay() > 0) {
            this.retryDelay = config.getRetryDelay();
        }
    }

    @RequestMapping(value = "/notify", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void notify(@RequestBody GlastoNotify glastoNotify) {
        LOG.info("*** NOTIFY *** : {}", glastoNotify);
    }

}
