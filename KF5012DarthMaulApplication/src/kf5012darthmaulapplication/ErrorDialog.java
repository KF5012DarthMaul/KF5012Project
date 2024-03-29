package kf5012darthmaulapplication;

import javax.swing.JOptionPane;

public class ErrorDialog {
	public ErrorDialog(String errorText, Error error) {
		this.generateJOptionPane(errorText, error);
	}
	public ErrorDialog(String errorText) {
		this.generateJOptionPane(errorText);
	}
	
	void generateJOptionPane(String errorText, Error error) {
		String text = errorText;
		String textError = error.getMessage();
		String textStackTrace = error.getStackTrace().toString();
		
		JOptionPane.showMessageDialog(null, text + "\n" + textError + "\n" + textStackTrace,"An Error has occured", JOptionPane.ERROR_MESSAGE);
	}
	void generateJOptionPane(String errorText) {
		String text = errorText;
		JOptionPane.showMessageDialog(null, text,"An Error has occured", JOptionPane.ERROR_MESSAGE);
	}
}
