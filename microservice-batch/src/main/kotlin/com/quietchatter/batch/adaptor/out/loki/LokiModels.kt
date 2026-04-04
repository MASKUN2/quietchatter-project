package com.quietchatter.batch.adaptor.out.loki

data class LokiResponse(
    val status: String,
    val data: LokiData?
)

data class LokiData(
    val resultType: String,
    val result: List<LokiResult>?
)

data class LokiResult(
    val stream: Map<String, String>,
    val values: List<List<String>>
)

data class LokiStats(
    val rawStatsText: String,
    val bookViews: Map<String, Int>
)
