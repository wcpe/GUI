package moe.him188.gui.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author Him188moe @ GUI Project
 */
public class InputTypeDate extends InputType<Date> {
    private final DateFormat format;

    public InputTypeDate(DateFormat dateFormat) {
        format = dateFormat;
    }

    public DateFormat getFormat() {
        return format;
    }

    @Override
    public Date parseResponse(String content) throws InputFormatException {
        try {
            return format.parse(content);
        } catch (ParseException e) {
            throw new InputFormatException(InputFormatException.Reason.DATE_FORMAT, content, e);
        }
    }
}