package uk.cam.lib.cdl.loading.model.packaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Update implements Comparable<Update>{

    private final String author;
    private final Date date;
    private final TimeZone timezone;
    private final List<String> filesChanged;

    public Update(String author, Date date, TimeZone timezone,  List<String> filesChanged) {

        this.author = author;
        this.date = date;
        this.timezone = timezone;
        this.filesChanged = filesChanged;

    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss");
        return sdf.format(date);
    }

    public List<String> getFilesChanged() {
        return filesChanged;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    @Override
    public int compareTo(Update update) {
        return date.compareTo(update.getDate());
    }
}
