package net.wwwfred.framework.spi.request;

import net.wwwfred.framework.po.BasePO;

public class BaseRequest extends BasePO {

	private static final long serialVersionUID = 4345356329477640171L;
	
	/** 接口请求版本号 */
	protected String version;
	
	/** 接口请求的客户端类型 */
	protected String clientType;
	
	/** 请求的接口的客户端版本号 */
	protected String clientVersion;
	
	/** 登录后的用户的唯一标识　*/
	protected String token;
	
	/** 接口请求的 业务类型 */
	protected String businessType;
	
	/** 接口请求的时间戳 */
	protected Long timestamp;
	
	/** 接口请求分页数据的页码 */
	protected Integer pageNo;
	
	/** 接口请求分页数据的每页记录数 */
	protected Integer pageSize;
	
	/** 接口请求的密钥（部分安全级别较高的接口需要） */
	protected String signature;
	
	/** 接口调用是否不要缓存数据 */
	protected Boolean nocache;
	
	/** 接口用户请求 的用户标识  */
	protected String requestUserId;
	
	/** 接口调用请求会话的唯一标识符 */
	protected String requestId;
	
	/** 接口用户请求的IP地址 */
	protected String requestIp;
	
	/** 网络状况2G/3G/4G/WIFI */
	protected String network;
	
	/** 显示屏宽度 */
	protected String screenWidth;
	/** 显示屏高度 */
	protected String screenHeight;
	
//	private T data;
	
	public BaseRequest() {
	}

	public BaseRequest(String version, String clientType, String businessType) {
		this(version,clientType,businessType,null);
	}

	public BaseRequest(String version, String clientType, String businessType,
			Long timestamp) {
		this(version,clientType,businessType,timestamp,null,null);
	}

	public BaseRequest(String version, String clientType, String businessType,
			Long timestamp, Integer pageNo, Integer pageSize) {
		this(version,clientType,businessType,timestamp,pageNo,pageSize,null);
	}

	public BaseRequest(String version, String clientType, String businessType,
			Long timestamp, Integer pageNo, Integer pageSize, String signature) {
		super();
		this.version = version;
		this.clientType = clientType;
		this.businessType = businessType;
		this.timestamp = timestamp;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.signature = signature;
	}

//	public BaseRequestPO(String version, String clientType, String businessType,
//			T data) {
//		super();
//		this.version = version;
//		this.clientType = clientType;
//		this.businessType = businessType;
//		this.data = data;
//	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

//	public T getData() {
//		return data;
//	}
//
//	public void setData(T data) {
//		this.data = data;
//	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getNocache() {
		return nocache;
	}

	public void setNocache(Boolean nocache) {
		this.nocache = nocache;
	}
	
	public String getRequestUserId() {
		return requestUserId;
	}

	public void setRequestUserId(String requestUserId) {
		this.requestUserId = requestUserId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getRequestIp() {
		return requestIp;
	}

	public void setRequestIp(String requestIp) {
		this.requestIp = requestIp;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(String screenWidth) {
		this.screenWidth = screenWidth;
	}

	public String getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(String screenHeight) {
		this.screenHeight = screenHeight;
	}
	
	
}
