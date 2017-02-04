package net.wwwfred.framework.core.web;

import net.wwwfred.framework.po.BasePO;

public class UploadFilePO extends BasePO {

	private static final long serialVersionUID = 1L;
	private byte[] fielData;
	private String fileName;
	public UploadFilePO() {
		// TODO Auto-generated constructor stub
	}
	public UploadFilePO(byte[] fielData, String fileName) {
		super();
		this.fielData = fielData;
		this.fileName = fileName;
	}
	public byte[] getFielData() {
		return fielData;
	}
	public void setFielData(byte[] fielData) {
		this.fielData = fielData;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
