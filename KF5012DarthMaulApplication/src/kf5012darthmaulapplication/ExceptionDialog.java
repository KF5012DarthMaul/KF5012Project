package kf5012darthmaulapplication;

import javax.swing.JOptionPane;

public class ExceptionDialog {
	public ExceptionDialog(String errorText, Exception error) {
		this.generateJOptionPane(errorText, error);
	}
	public ExceptionDialog(String errorText) {
		this.generateJOptionPane(errorText);
	}
	
	void generateJOptionPane(String exceptionText, Exception exception) {
		String text = exceptionText;
		String textError = exception.getMessage();
		String textStackTrace = exception.getStackTrace().toString();
		
		JOptionPane.showMessageDialog(null, text + "\n" + textError + "\n" + textStackTrace,"An Error has occured", JOptionPane.ERROR_MESSAGE);
	}
	void generateJOptionPane(String exceptionText) {
		String text = exceptionText;
		JOptionPane.showMessageDialog(null, text,"An Error has occured", JOptionPane.ERROR_MESSAGE);
	}
}
