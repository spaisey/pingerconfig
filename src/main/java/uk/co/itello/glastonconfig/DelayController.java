package uk.co.itello.glastonconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class DelayController {
    private static final Logger LOG = LoggerFactory.getLogger(DelayController.class);

    @RequestMapping(value = "/delay", produces = MediaType.TEXT_HTML_VALUE)
    public String config() throws InterruptedException {
        Thread.sleep(60000);
        return "<html><head><title>delay page</title></head><body><p>This is the news</p></body></html>";
    }
}
