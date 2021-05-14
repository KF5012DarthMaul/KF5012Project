package lib;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Adapted from <a href="https://stackoverflow.com/a/25496932">
 * MadProgrammer's Answer</a>.
 * 
 * @author MadProgrammer
 */
@SuppressWarnings("serial")
public class DurationField extends AbstractTimeField {
    private HourDocumentFilter hourDocumentFilter;

    public DurationField() {
        setDuration(0, 0);
    }

    public void setDuration(int hour, int minute) {
        setHour(hour);
        setMinute(minute);
    }

    public long getDuration() throws NumberFormatException {
        int hour = getHour();
        int minute = getMinute();
        
        return (hour * 60 * 60) + (minute * 60);
    }

    @Override
    public void setHour(int hour) {
        setFieldValue(getHourField(), hour, 3);
    }

    @Override
    protected JTextField createHourField() {
        JTextField field = super.createHourField();
        field.setColumns(4);
        return field;
    }

    @Override
    protected int getHourFocusForwardLength() {
        return -1;
    }

    @Override
    protected JComponent[] getTimeFields() {
        return new JComponent[] {
            getHourField(),
            new JLabel(":"),
            getMinuteField()
        };
    }

    @Override
    protected DocumentFilter getHourDocumentFilter() {
        if (hourDocumentFilter == null) {
            hourDocumentFilter = new HourDocumentFilter();
        }
        return hourDocumentFilter;
    }

    protected class HourDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(
        		DocumentFilter.FilterBypass fb, int offset, String text,
        		AttributeSet attr
        ) throws BadLocationException {
            System.out.println("insert: offset = " + offset + "; text = " + text);
            super.insertString(fb, offset, text, attr);
        }

        @Override
        public void replace(
        		DocumentFilter.FilterBypass fb, int offset, int length,
        		String text, AttributeSet attrs
        ) throws BadLocationException {
            try {
                // We convert the value here to make sure it's a number...
                Integer.parseInt(text);
                super.replace(fb, offset, length, text, attrs);
                
            } catch (NumberFormatException exp) {}
        }
    }
}
