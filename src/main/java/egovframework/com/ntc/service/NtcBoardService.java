package egovframework.com.ntc.service;

import java.util.List;

/**
 * Notice Board Service interface — same pattern as SmpBoardService in saas-olv.
 * Defines WHAT the service can do. HOW is in NtcBoardServiceImpl.
 */
public interface NtcBoardService {

    List<NtcBoardOutVO> selectList(NtcBoardInVO inVO) throws Exception;

    int selectListTotCnt(NtcBoardInVO inVO) throws Exception;

    NtcBoardOutVO selectDetail(NtcBoardInVO inVO) throws Exception;

    void insert(NtcBoardInVO inVO) throws Exception;

    void update(NtcBoardInVO inVO) throws Exception;

    void delete(NtcBoardInVO inVO) throws Exception;

    List<NtcBoardOutVO> selectDeletedList(NtcBoardInVO inVO) throws Exception;

    void restore(NtcBoardInVO inVO) throws Exception;

    /* ── Exercise 2: TransactionConfig test methods (practice only) ── */

    /** Write method: inserts 2 rows, then throws a checked Exception → both must roll back */
    void insertTxRollbackTest(NtcBoardInVO inVO) throws Exception;

    /** Read-only method (name starts with select): tries to INSERT → PostgreSQL rejects it */
    int selectTxReadOnlyTest(NtcBoardInVO inVO) throws Exception;
}
