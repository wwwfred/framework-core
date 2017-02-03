package net.wwwfred.framework.model;

import java.util.Date;

import net.wwwfred.framework.core.dao.ColumnAnnotaion;
import net.wwwfred.framework.core.dao.IdAnnotaion;
import net.wwwfred.framework.po.BasePO;

public class BaseModel extends BasePO {

	private static final long serialVersionUID = 2415224237396701295L;
	
	@IdAnnotaion("ID")
	private Long id;
	@ColumnAnnotaion("CREATE_TIME")
	private Date createTime;
	@ColumnAnnotaion("UPDATE_TIME")
	private Date updateTime;
	@ColumnAnnotaion("STATUS")
	private String status;
	public BaseModel() {
		createTime = new Date();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}
