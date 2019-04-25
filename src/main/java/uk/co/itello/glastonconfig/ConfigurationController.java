package uk.co.itello.glastonconfig;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Controller
public class ConfigurationController {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationController.class);

    private final List<String> tenants;

    private Map<String, GlastoConfig> tenantsConfig = new HashMap<>();

    private Map<String, Set<String>> tenantInstances = new HashMap<>();

    public ConfigurationController(@Value("${glasto.tenants:bernie,sp,test}") List<String> tenants) {
        this.tenants = tenants;
    }

    private Map<String, Map<Instant, String>> tenantNotifications = new ConcurrentHashMap<>();

    @GetMapping(value = "{tenant}/config", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public GlastoConfig config(@PathVariable("tenant") String tenant, HttpServletRequest request) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        Set<String> instances = tenantInstances.getOrDefault(tenant, new HashSet<>());
        instances.add(request.getRemoteAddr());
        tenantInstances.put(tenant, instances);

        return tenantsConfig.get(tenant);
    }

    @PostMapping(value = "{tenant}/clear", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public GlastoConfig clear(@PathVariable("tenant") String tenant) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        return tenantsConfig.remove(tenant);
    }

    @PostMapping(value = "{tenant}/config")
    @ResponseStatus(HttpStatus.CREATED)
    public void config(@PathVariable("tenant") String tenant,
                       @RequestBody GlastoConfig config) {

        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        LOG.info("*** CONFIG for {} *** : {}", tenant, config);

        tenantsConfig.put(tenant, config);
    }

    @PostMapping(value = "{tenant}/notify", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public synchronized void notify(@PathVariable("tenant") String tenant,
                       @RequestBody GlastoNotify glastoNotify) {

        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        LOG.info("*** NOTIFY from {} *** : {}", tenant, glastoNotify);

        Map<Instant, String> map = tenantNotifications.getOrDefault(tenant, new HashMap<>());
        map.put(Instant.now(), glastoNotify.getIp());
        tenantNotifications.put(tenant, map);
    }

    @GetMapping(value = "{tenant}/notify", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<Instant, String> notifications(@PathVariable("tenant") String tenant) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        return tenantNotifications.getOrDefault(tenant, new HashMap<>());
    }

    @GetMapping(value = "{tenant}/notify", produces = TEXT_HTML_VALUE)
    public String notificationsView(@PathVariable("tenant") String tenant, Model model) {
        if (!tenants.contains(tenant)) {
            throw new RuntimeException("unauthorized: " + tenant);
        }

        model.addAttribute("instances", tenantInstances.getOrDefault(tenant, new HashSet<>()).size());
        model.addAttribute("notifications", new TreeMap<>(tenantNotifications.getOrDefault(tenant, new HashMap<>())));
        return "notify";
    }
}
