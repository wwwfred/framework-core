package net.wwwfred.framework.core.exception;
/**
 * 系统运行期异常
 * @author wangwwy
 * createdDatetime 2014年8月13日 下午3:37:23
 */

@SuppressWarnings("serial")
public class TeshehuiRuntimeException extends RuntimeException{
    private static final String _code = "500";
	private static final String _message="后台系统处理异常";
	private String message;
	private String code;
	public TeshehuiRuntimeException() {
		this(null,null,null);
	}
	
	public TeshehuiRuntimeException(String message, Throwable cause) {
		this(null,message,cause);
	}

	
	public TeshehuiRuntimeException(String message) {
		this(null,message,null);
	}

	public TeshehuiRuntimeException(Throwable cause) {
		this(null,null,cause);
	}
	
	public TeshehuiRuntimeException(String code,String message)
    {
	    this(code,message,null);
    }
	
	public TeshehuiRuntimeException(String code,String message,Throwable cause)
	{
	    super(message,cause);
	    this.code = (code==null||"".equals(code.trim()))?_code:code;
	    this.message = (message==null||"".equals(message.trim()))?_message:message;
	}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
	
}
