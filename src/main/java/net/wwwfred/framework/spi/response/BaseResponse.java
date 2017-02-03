package net.wwwfred.framework.spi.response;

import net.wwwfred.framework.po.BasePO;

public class BaseResponse<T> extends BasePO
{
    private static final long serialVersionUID = 1167877774707589295L;

    /** 正确处理响应状态码 */
	public static Integer STATUS_SUCCESS_VALUE = 0;
	/** 处理异常响应状态码 */
	public static Integer STATUS_FAILURE_VALUE = -1;
	
	/** 正确处理响应码 */
	public static String CODE_SUCCESS_VALUE = "0";
	/** 正确处理响应消息 */
	public static String MESSAGE_SUCCESS_VALUE = "SUCCESS";
	
	/** 返回状态码   0:成功   -1：失败*/
	protected Integer status;
	/** 返回消息码 */
	protected String code;
	/** 返回消息  */
	protected String message;
	/** 返回结果对象 */
	protected T data;
	
	public BaseResponse() {
	}
	
	public BaseResponse(String code, String message)
	{
		this(STATUS_FAILURE_VALUE,code,message,null);
	}
	
	public BaseResponse(T data)
	{
		this(STATUS_SUCCESS_VALUE,CODE_SUCCESS_VALUE,MESSAGE_SUCCESS_VALUE,data);
	}
	
	public BaseResponse(Integer status, String code, String message, T data) {
		super();
		this.status = status;
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}