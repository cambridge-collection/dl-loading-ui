package uk.cam.lib.cdl.loading;

import com.amazonaws.services.sns.message.DefaultSnsMessageHandler;
import com.amazonaws.services.sns.message.SnsMessageManager;
import com.amazonaws.services.sns.message.SnsNotification;
import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import uk.cam.lib.cdl.loading.model.logs.LogType;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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

        //List<LogMessage> messages = repository.findTop10ByLogTypeOrderByTimestampDesc(LogType.DATA_PROCESSING_EXCEPTION_FOR_UI_DISPLAY);
        List<LogMessage> messages = repository.findTop20ByOrderByTimestampDesc();
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

                try {
                    repository.save(makeLogMessage(snsNotification));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return "{}";

    }

    private LogMessage makeLogMessage(SnsNotification snsNotification) throws IOException {

        logger.info("making Log Message");
        LogMessage message = new LogMessage();
        message.setMessageId(snsNotification.getMessageId());
        message.setMessage(snsNotification.getMessage());
        message.setTimestamp(
            LocalDateTime.ofInstant(Instant.ofEpochMilli(snsNotification.getTimestamp().getTime()),
                ZoneId.systemDefault()));
        message.setSubject(snsNotification.getSubject());
        message.setTopicArn(snsNotification.getTopicArn());
        message.setUnsubscribeUrl(snsNotification.getUnsubscribeUrl().toString());

        // Notification message format is:
        //    LOG-GROUP-NAME: /aws/lambda/AWSLambda_CUDLPackageData_HTML_to_HTML_Translate_URLS
        //    LOG-STREAM: 2022/01/21/[$LATEST]2baa7f2e2504493dbd895d5cc1840b11
        //    LOG-MESSAGE: ...
        String m = snsNotification.getMessage();
        logger.info("m: "+m);
        message.setLogGroup(StringUtils.substringBetween(m, "LOG-GROUP-NAME: ", "\n"));
        message.setLogStream(StringUtils.substringBetween(m, "LOG-STREAM: ", "\n"));
        logger.info("loggroup: "+message.getLogGroup());
        logger.info("logstream: "+message.getLogStream());

        String error = StringUtils.substringAfter(m,"LOG-MESSAGE: ");

        logger.info("error: "+error);
        // test for LogType.DATA_PROCESSING_EXCEPTION_FOR_UI_DISPLAY
        // This has the format
        // ERROR uk.ac.cam.lib.cudl.awslambda.handlers.AbstractRequestHandler -
        // DATA-PROCESSING-EXCEPTION-FOR-UI-DISPLAY (message,error,stacktrace) || <json_event_message> || <error> || stacktrace
        if (error.contains("DATA-PROCESSING-EXCEPTION-FOR-UI-DISPLAY")) {
            logger.info("found DATA-PROCESSING-EXCEPTION-FOR-UI-DISPLAY");
            String[] parts = error.split(" \\|\\| ");
            logger.info("parts: "+parts.length);
            if (parts.length<3) { throw new IOException("invalid notification found: "+snsNotification.getMessage()); }
            message.setJsonEvent(parts[1]);
            message.setError(parts[2]);
            message.setStacktrace(parts[3]);
            message.setLogType(LogType.DATA_PROCESSING_EXCEPTION_FOR_UI_DISPLAY);
            logger.info("json: "+message.getJsonEvent());
            logger.info("error: "+message.getError());
            logger.info("stacktrace: "+message.getStacktrace());

        } else if (error.contains("ERROR")) {
            message.setLogType(LogType.ERROR);
        } else if (error.contains("WARN")) {
            message.setLogType(LogType.WARNING);
        } else if (error.contains("Task timed out")) {
            message.setLogType(LogType.TIMEOUT);
        } else if (error.matches(".*5\\d{2}.*")) {
            message.setLogType(LogType.CODE5XX);
        }

        return message;
    }


}
