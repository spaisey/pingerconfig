package uk.co.itello.pingerconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Controller
public class DelayController {
    private static final Logger LOG = LoggerFactory.getLogger(DelayController.class);

    @RequestMapping(value = "/delay", produces = TEXT_HTML_VALUE)
    public String config(@RequestParam(name = "secs", required = false, defaultValue = "60") int seconds) throws InterruptedException {
        LOG.info("Request for a delay page, with secs = {}", seconds);
        Thread.sleep(seconds * 1000);
        return "delay";
    }
}
