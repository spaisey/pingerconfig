package uk.co.itello.glastonconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class ConfigurationController {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationController.class);

    private final List<String> tenants;

    private Map<String, GlastoConfig> tenantsConfig = new HashMap<>();

    public ConfigurationController(@Value("${glasto.tenants:bernie,sp,test}") List<String> tenants) {
        this.tenants = tenants;
    }

    private Map<String, Map<Date, String>> tenantNotifications = new HashMap<>();

    @RequestMapping(value = "{tenant}/config", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public GlastoConfig config(@PathVariable("tenant") String tenant) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        return tenantsConfig.get(tenant);
    }

    @RequestMapping(value = "{tenant}/config", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void config(@PathVariable("tenant") String tenant,
                       @RequestBody GlastoConfig config) {

        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        LOG.info("*** CONFIG for {} *** : {}", tenant, config);

        tenantsConfig.put(tenant, config);
    }

    @RequestMapping(value = "{tenant}/notify", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void notify(@PathVariable("tenant") String tenant,
                       @RequestBody GlastoNotify glastoNotify) {

        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        LOG.info("*** NOTIFY from {} *** : {}", tenant, glastoNotify);

        Map<Date, String> notification = new HashMap<>();
        notification.put(new Date(), glastoNotify.getIp());

        tenantNotifications.put(tenant, notification);
    }

    @RequestMapping(value = "{tenant}/notify", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<Date, String> notifications(@PathVariable("tenant") String tenant) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        return tenantNotifications.getOrDefault(tenant, new HashMap<>());
    }

    @RequestMapping(value = "{tenant}/notify", produces = TEXT_HTML_VALUE)
    public String notificationsView(@PathVariable("tenant") String tenant, Model model) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        model.addAttribute("notifications", new TreeMap<>(tenantNotifications.getOrDefault(tenant, new HashMap<>())));
        return "notify";
    }
}
