package uk.co.itello.pingerconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Controller
public class ConfigurationController {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationController.class);

    private final List<String> tenants;

    private Map<String, Config> tenantsConfig = new HashMap<>();

    private Map<String, Set<String>> tenantInstances = new HashMap<>();

    public ConfigurationController(@Value("${glasto.tenants:bernie,sp,test}") List<String> tenants) {
        this.tenants = tenants;
    }

    private Map<String, Map<Instant, String>> tenantNotifications = new ConcurrentHashMap<>();

    @GetMapping(value = "{tenant}/config", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Config config(@PathVariable("tenant") String tenant, HttpServletRequest request) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        Set<String> instances = getInstances(tenant);
        instances.add(request.getRemoteAddr());
        tenantInstances.put(tenant, instances);

        return tenantsConfig.get(tenant);
    }

    @PostMapping(value = "{tenant}/clear", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Config clear(@PathVariable("tenant") String tenant) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        return tenantsConfig.remove(tenant);
    }

    @PostMapping(value = "{tenant}/config")
    @ResponseStatus(HttpStatus.CREATED)
    public void config(@PathVariable("tenant") String tenant,
                       @RequestBody Config config) {

        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        LOG.info("*** CONFIG for {} *** : {}", tenant, config);

        tenantsConfig.put(tenant, config);
    }

    @PostMapping(value = "{tenant}/notify", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void notify(@PathVariable("tenant") String tenant,
                       @RequestBody Notify notify) {

        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        LOG.info("*** NOTIFY from {} *** : {}", tenant, notify);

        Map<Instant, String> map = getNotifications(tenant);
        map.put(Instant.now(), notify.getIp());
        tenantNotifications.put(tenant, map);
    }

    @PostMapping(value = "{tenant}/notify/clear", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void notifyClear(@PathVariable("tenant") String tenant) {

        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        LOG.info("*** CLEAR NOTIFY from {} *** : {}", tenant, getNotifications(tenant));

        tenantNotifications.clear();
        tenantInstances.clear();
    }

    @GetMapping(value = "{tenant}/notify", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<Instant, String> notifications(@PathVariable("tenant") String tenant) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        return getNotifications(tenant);
    }

    @GetMapping(value = "{tenant}/notify", produces = TEXT_HTML_VALUE)
    public String notificationsView(@PathVariable("tenant") String tenant, Model model) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        model.addAttribute("instances", getInstances(tenant).size());
        model.addAttribute("notifications", new TreeMap<>(getNotifications(tenant)));
        return "notify";
    }

    private Set<String> getInstances(@PathVariable("tenant") String tenant) {
        return tenantInstances.getOrDefault(tenant, newKeySet());
    }

    private Map<Instant, String> getNotifications(@PathVariable("tenant") String tenant) {
        return tenantNotifications.getOrDefault(tenant, new ConcurrentHashMap<>());
    }
}
