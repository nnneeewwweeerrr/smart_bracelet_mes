package com.mes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mes.entity.AiDocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiDocumentChunkMapper extends BaseMapper<AiDocumentChunk> {

    List<AiDocumentChunk> searchByKeywords(@Param("keywords") String keywords);

    List<AiDocumentChunk> searchByDocumentId(@Param("documentId") Long documentId);
}
