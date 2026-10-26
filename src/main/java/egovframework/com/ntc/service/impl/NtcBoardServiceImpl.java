package egovframework.com.ntc.service.impl;

import egovframework.com.cmm.exception.CmmBizException;
import egovframework.com.ntc.mapper.NtcBoardMapper;
import egovframework.com.ntc.service.NtcBoardInVO;
import egovframework.com.ntc.service.NtcBoardOutVO;
import egovframework.com.ntc.service.NtcBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Notice Board ServiceImpl — same pattern as SmpBoardServiceImpl in saas-olv.
 * @Service("ntcBoardService") — Spring registers this bean by name.
 * Calls the Mapper. Business validation is handwritten here (saas-olv style),
 * and failures throw CmmBizException (M-code).
 */
@Service("ntcBoardService")
public class NtcBoardServiceImpl implements NtcBoardService {

    /** Same length as the notice_title VARCHAR(200) column */
    private static final int TITLE_MAX_LENGTH = 200;

    @Autowired
    private NtcBoardMapper ntcBoardMapper;

    @Override
    public List<NtcBoardOutVO> selectList(NtcBoardInVO inVO) throws Exception {
        return ntcBoardMapper.selectList(inVO);
    }

    @Override
    public int selectListTotCnt(NtcBoardInVO inVO) throws Exception {
        return ntcBoardMapper.selectListTotCnt(inVO);
    }

    @Override
    public NtcBoardOutVO selectDetail(NtcBoardInVO inVO) throws Exception {
        NtcBoardOutVO outVO = ntcBoardMapper.selectDetail(inVO);
        if (outVO == null) {
            throw new CmmBizException("M0001", "Requested data not found.");
        }
        return outVO;
    }

    @Override
    public void insert(NtcBoardInVO inVO) throws Exception {
        validateTitle(inVO);
        ntcBoardMapper.insert(inVO);
    }

    @Override
    public void update(NtcBoardInVO inVO) throws Exception {
        validateTitle(inVO);
        int cnt = ntcBoardMapper.update(inVO);
        checkAffected(cnt);
    }

    @Override
    public void delete(NtcBoardInVO inVO) throws Exception {
        int cnt = ntcBoardMapper.delete(inVO);
        checkAffected(cnt);
    }

    @Override
    public List<NtcBoardOutVO> selectDeletedList(NtcBoardInVO inVO) throws Exception {
        return ntcBoardMapper.selectDeletedList(inVO);
    }

    @Override
    public void restore(NtcBoardInVO inVO) throws Exception {
        int cnt = ntcBoardMapper.restore(inVO);
        checkAffected(cnt);
    }

    /* ── Exercise 2: TransactionConfig test methods ── */

    /**
     * "insert*" → matches "*" → write transaction, rollback on ANY Exception.
     * Plain Spring @Transactional would NOT roll back a checked Exception — olv's rule does.
     */
    @Override
    public void insertTxRollbackTest(NtcBoardInVO inVO) throws Exception {
        inVO.setNoticeTitle("[TX TEST] row 1 — must be rolled back");
        ntcBoardMapper.insert(inVO);

        inVO.setNoticeTitle("[TX TEST] row 2 — must be rolled back");
        ntcBoardMapper.insert(inVO);

        throw new Exception("TX rollback test: checked exception after 2 inserts");
    }

    /**
     * "select*" → read-only transaction. An INSERT here fails with
     * "cannot execute INSERT in a read-only transaction".
     * Lesson: never name a method that writes data select/get/find/list/count.
     */
    @Override
    public int selectTxReadOnlyTest(NtcBoardInVO inVO) throws Exception {
        inVO.setNoticeTitle("[TX TEST] read-only — must NOT be saved");
        ntcBoardMapper.insert(inVO);
        return ntcBoardMapper.selectListTotCnt(inVO);
    }

    /* ── Validation helpers ─────────────────────── */

    /** Title is required and must fit the DB column */
    private void validateTitle(NtcBoardInVO inVO) {
        String title = inVO.getNoticeTitle();
        if (title == null || title.isBlank()) {
            throw new CmmBizException("M0101", "Title is required.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new CmmBizException("M0102", "Title must be " + TITLE_MAX_LENGTH + " characters or less.");
        }
    }

    /** 0 rows changed = wrong PK, or already deleted/restored by someone else */
    private void checkAffected(int cnt) {
        if (cnt == 0) {
            throw new CmmBizException("M0002", "The data does not exist or was already changed.");
        }
    }
}
