package egovframework.com.ntc.mapper;

import egovframework.com.ntc.service.NtcBoardInVO;
import egovframework.com.ntc.service.NtcBoardOutVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Notice Board Mapper — the same pattern as SmpBoardMapper in saas-olv.
 * @Mapper = MyBatis auto-generates the implementation.
 * Method names MUST match the id in NtcBoard_SQL.xml exactly.
 */
@Mapper
public interface NtcBoardMapper {
    List<NtcBoardOutVO> selectList(NtcBoardInVO inVO) throws Exception;
    int selectListTotCnt(NtcBoardInVO inVO) throws Exception;
    NtcBoardOutVO selectDetail(NtcBoardInVO inVO) throws Exception;
    void insert(NtcBoardInVO inVO) throws Exception;
    /** Returns the number of updated rows (0 = not found / already changed) */
    int update(NtcBoardInVO inVO) throws Exception;
    int delete(NtcBoardInVO inVO) throws Exception;

    List<NtcBoardOutVO> selectDeletedList(NtcBoardInVO inVO) throws Exception;
    int restore(NtcBoardInVO inVO) throws Exception;
}
