package uk.cam.lib.cdl.loading;

import com.amazonaws.services.sns.message.DefaultSnsMessageHandler;
import com.amazonaws.services.sns.message.SnsMessageManager;
import com.amazonaws.services.sns.message.SnsNotification;
import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.cam.lib.cdl.loading.dao.LogMessageRepository;
import uk.cam.lib.cdl.loading.model.logs.LogMessage;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping ("/logs")
public class ProcessingLogController {
    private static final Logger logger = LoggerFactory.getLogger(ProcessingLogController.class);

    private final LogMessageRepository repository;
    private final Path pathForDataDisplay;
    private SnsMessageManager snsManager;

    @Autowired
    public ProcessingLogController(
        LogMessageRepository repository,
        @Value("${data.url.display}") String pathForDataDisplay
    ) {
        this.repository = repository;
        this.pathForDataDisplay = Path.of("/logs", pathForDataDisplay);
        Preconditions.checkArgument(
            this.pathForDataDisplay.isAbsolute() &&
                this.pathForDataDisplay.normalize().equals(this.pathForDataDisplay),
            "pathForDataDisplay must start with / and not contain relative segments");

        snsManager = new SnsMessageManager("eu-west-1");
    }

    @PreAuthorize("@roleService.canViewWorkspaces(authentication)")
    @GetMapping("/view-logs.html")
    public String viewLogs(Model model, HttpServletRequest request) {

        List<LogMessage> messages = repository.findTop10ByOrderByTimestampDesc();
        for (LogMessage message: messages) {
            logger.info(String.valueOf(message));
        }
        model.addAttribute("messages", messages);

        return "view-logs";
    }

    @RequestMapping(value = "/handleErrorLog", method = { RequestMethod.GET, RequestMethod.POST },
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String handleErrorLog(HttpServletRequest request) throws IOException {

        logger.info("Received Message");

        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement();
            String header = request.getHeader(name);
            logger.info("header: "+name+" : "+header);
        }

        Map<String, String[]> map = request.getParameterMap();
        for (String name: map.keySet()) {
            String[] values = map.get(name);
            logger.info("parameter: "+name+" : "+ Arrays.toString(values));
        }

        String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        logger.info("content: "+content);

        // take is a request from the SNS service of a failure that has occurred.
        snsManager.handleMessage(IOUtils.toInputStream(content, "UTF-8"), new DefaultSnsMessageHandler() {
            @Override
            public void handle(SnsNotification snsNotification) {
                logger.info("snsNotification: "+snsNotification);
                logger.info("Received message \n"
                        + "Subject= {} \n"
                        + "Message = {} \n",
                    snsNotification.getSubject(), snsNotification.getMessage());

                LogMessage message = new LogMessage();
                message.setMessageId(snsNotification.getMessageId());
                message.setMessage(snsNotification.getMessage());
                message.setTimestamp(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(snsNotification.getTimestamp().getTime()),
                        ZoneId.systemDefault()));
                message.setSubject(snsNotification.getSubject());
                message.setTopic_arn(snsNotification.getTopicArn());
                message.setUnsubscribe_url(snsNotification.getUnsubscribeUrl().toString());

                repository.save(message);
            }
        });

        return "{}";

    }
}
