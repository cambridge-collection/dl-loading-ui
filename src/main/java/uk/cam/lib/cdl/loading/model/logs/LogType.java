package uk.cam.lib.cdl.loading.model.logs;

/**
 * These should match each of the different types of log that can match of regular expression used as
 * the filter for these notifications. e.g. ?ERROR ?WARN ?5xx ?"Task timed out" ?"DATA-PROCESSING-EXCEPTION-FOR-UI-DISPLAY"
 */
public enum LogType {

    DATA_PROCESSING_EXCEPTION_FOR_UI_DISPLAY,
    ERROR,
    WARNING,
    TIMEOUT,
    CODE5XX

}
