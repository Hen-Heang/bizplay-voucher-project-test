package egovframework.com.cmm;

import java.io.Serial;
import java.io.Serializable;

/**
 * 공통 Base VO (practice copy of olv CmmVO)
 * 모든 InVO 의 부모 클래스 — 검색조건, 페이징, 공통 감사(Audit) 컬럼.
 */

public class CmmVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /* ── Search ── */
    private String searchCondition;
    private String searchKeyword;


    /* ── Paging ── */
    private int pageIndex = 1;
    private int recordCountPerPage = 10;

    public int getFirstIndex() {
        return (pageIndex - 1) * recordCountPerPage;
    }

    /* ── Audit ── */
    private String dataRegId;
    private String dataChgId;

    /* ── Getters / Setters ── */

    public String getSearchCondition() { return searchCondition; }
    public void setSearchCondition(String searchCondition) { this.searchCondition = searchCondition; }

    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }

    public int getPageIndex() { return pageIndex; }
    public void setPageIndex(int pageIndex) { this.pageIndex = pageIndex; }

    public int getRecordCountPerPage() { return recordCountPerPage; }
    public void setRecordCountPerPage(int recordCountPerPage) { this.recordCountPerPage = recordCountPerPage; }

    public String getDataRegId() { return dataRegId; }
    public void setDataRegId(String dataRegId) { this.dataRegId = dataRegId; }

    public String getDataChgId() { return dataChgId; }
    public void setDataChgId(String dataChgId) { this.dataChgId = dataChgId; }
}
