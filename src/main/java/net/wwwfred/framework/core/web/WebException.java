package net.wwwfred.framework.core.web;

@SuppressWarnings("serial")
public class WebException extends RuntimeException{

	public WebException() {
		super();
	}

	public WebException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebException(String message) {
		super(message);
	}

	public WebException(Throwable cause) {
		super(cause);
	}
	
	
}
