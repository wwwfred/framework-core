package net.wwwfred.framework.po;

import java.util.List;

/**
 * @Author: wangwwy
 * @Description:
 * @Copyright:  2006 Union Operation Support System.
 */

public class ResultSetPO<T> extends BasePO {
	private static final long serialVersionUID = 198307210333l;
	
	private List<T> items;
	private Integer totalCount;
	private Integer beginRow;
	private Integer page;
	private Integer pageSize;
	
	public ResultSetPO(){}
	
    public ResultSetPO(Integer beginRow,Integer totalCount,List<T> items){
        this.beginRow=beginRow;
        this.totalCount=totalCount;
        this.items=items;
    }

	public ResultSetPO(Integer beginRow, Integer totalCount, List<T> items, Integer page, Integer pageSize) {
        this.beginRow=beginRow;
        this.totalCount=totalCount;
        this.items=items;
        this.page=page;
        this.pageSize=pageSize;
    }

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getBeginRow() {
        return beginRow;
    }

    public void setBeginRow(Integer beginRow) {
        this.beginRow = beginRow;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
