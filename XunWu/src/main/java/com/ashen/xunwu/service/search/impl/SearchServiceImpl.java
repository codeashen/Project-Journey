package com.ashen.xunwu.service.search.impl;

import com.ashen.xunwu.base.HouseSort;
import com.ashen.xunwu.config.RentValueBlock;
import com.ashen.xunwu.entity.House;
import com.ashen.xunwu.entity.HouseDetail;
import com.ashen.xunwu.entity.HouseTag;
import com.ashen.xunwu.entity.SupportAddress;
import com.ashen.xunwu.service.ServiceMultiResult;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.service.search.ISearchService;
import com.ashen.xunwu.service.search.common.*;
import com.ashen.xunwu.web.form.MapSearch;
import com.ashen.xunwu.web.form.RentSearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import com.ashen.xunwu.repository.HouseDetailRepository;
import com.ashen.xunwu.repository.HouseRepository;
import com.ashen.xunwu.repository.HouseTagRepository;
import com.ashen.xunwu.repository.SupportAddressRepository;
import com.ashen.xunwu.service.house.IAddressService;
import com.ashen.xunwu.service.search.common.*;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ES检索实现
 */
@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    private static final String INDEX_NAME = "xunwu-house";
    private static final String INDEX_TOPIC = "house_build";

    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private HouseDetailRepository houseDetailRepository;
    @Autowired
    private HouseTagRepository tagRepository;
    @Autowired
    private SupportAddressRepository supportAddressRepository;
    @Autowired
    private IAddressService addressService;

    @Autowired
    private RestHighLevelClient esClient;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ObjectMapper objectMapper;

    //region 用户调用
    /**
     * 根据houseId，更新ES文档
     * @param houseId 房源id
     */
    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    /**
     * 根据houseId，删除ES文档
     * @param houseId 房源id
     */
    @Override
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }

    /**
     * 查询房源
     * @param rentSearch
     * @return
     */
    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        // 1. 构建ES查询的 QueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 城市
        boolQueryBuilder.filter(
                QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName())
        );
        // 地区
        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName())
            );
        }
        // 房屋朝向
        if (rentSearch.getDirection() > 0) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection())
            );
        }
        // 出租方式
        if (rentSearch.getRentWay() > -1) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay())
            );
        }
        // 面积区间
        RentValueBlock areaRange = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(areaRange)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (areaRange.getMax() > 0) {
                rangeQueryBuilder.lte(areaRange.getMax());
            }
            if (areaRange.getMin() > 0) {
                rangeQueryBuilder.gte(areaRange.getMin());
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }
        // 价格区间
        RentValueBlock priceRange = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(priceRange)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (priceRange.getMax() > 0) {
                rangeQueryBuilder.lte(priceRange.getMax());
            }
            if (priceRange.getMin() > 0) {
                rangeQueryBuilder.gte(priceRange.getMin());
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }
        // 关键词
        boolQueryBuilder.should(
                QueryBuilders.multiMatchQuery(
                        rentSearch.getKeywords(),   // 关键词内容
                        // HouseIndexKey.TITLE,
                        HouseIndexKey.TRAFFIC,      // 以下表示用哪些字段匹配关键词
                        HouseIndexKey.DISTRICT,
                        HouseIndexKey.ROUND_SERVICE,
                        HouseIndexKey.SUBWAY_LINE_NAME,
                        HouseIndexKey.SUBWAY_STATION_NAME
                )
        );
        
        boolQueryBuilder.should(
                QueryBuilders.matchQuery(rentSearch.getKeywords(), HouseIndexKey.TITLE).boost(2.0F)  //调整权重
        );
        
        // 2. 构建请求源 SearchSourceBuilder 
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQueryBuilder)        // 请求Query
                .sort(                          // 排序
                        HouseSort.getSortKey(rentSearch.getOrderBy()),
                        SortOrder.fromString(rentSearch.getOrderDirection())
                )
                .from(rentSearch.getStart())    // 开始索引
                .size(rentSearch.getSize())     // 查询数量
                .fetchSource(HouseIndexKey.HOUSE_ID, null); // 指定查询哪些字段，这里只查询 houseId

        log.debug(sourceBuilder.toString());
        
        // 4. 构建请求 SearchRequest
        SearchRequest request = new SearchRequest().indices(INDEX_NAME).source(sourceBuilder);
        
        // 3. 执行搜索请求
        List<Long> houseIds = new ArrayList<>();
        try {
            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
            if (response.status() != RestStatus.OK) {
                log.warn("Search status is no ok for {}", request);
                return new ServiceMultiResult<>(0, houseIds);
            }

            for (SearchHit hit : response.getHits()) {
                HouseIndexTemplate houseIndex = objectMapper.readValue(hit.getSourceAsString(), HouseIndexTemplate.class);
                houseIds.add(houseIndex.getHouseId());
            }
        } catch (IOException e) {
            log.error("Error to query house for " + request, e);
        }
        
        return new ServiceMultiResult<>(houseIds.size(), houseIds);
    }

    /**
     * 获取补全建议关键词
     * @param prefix
     * @return
     */
    @Override
    public ServiceResult<List<String>> suggest(String prefix) {
        // 构建建议请求
        CompletionSuggestionBuilder suggestion = SuggestBuilders
                .completionSuggestion("suggest")   // 指定对应的字段
                .prefix(prefix).size(5);   // 指定前缀和放回结果数量
        
        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion("autocomplete", suggestion);  // 指定一个名称，用于存放结果

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().suggest(suggestBuilder);

        // 执行搜索
        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);
        SearchResponse response;
        try {
            response = esClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error or search suggest word for prefix {}", prefix);
            return ServiceResult.of(new ArrayList<>());
        }

        // 解析结果
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return ServiceResult.of(new ArrayList<>());
        }

        // 根据指定名称获取结果
        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");

        int maxSuggest = 0;
        List<String> suggests = new ArrayList<>();

        for (Object term : result.getEntries()) {
            if (term instanceof CompletionSuggestion.Entry) {
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;

                if (item.getOptions().isEmpty()) {
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                    String tip = option.getText().string();
                    if (suggests.contains(tip)) {
                        continue;
                    }
                    suggests.add(tip);
                    maxSuggest++;
                }
            }

            if (maxSuggest > 5) {
                break;
            }
        }

        return ServiceResult.of(suggests);
    }

    /**
     * 分组查询，聚合特定小区的房间数
     * @param cityEnName 城市名
     * @param regionEnName 地区名
     * @param district 小区名
     * @return
     */
    @Override
    public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, regionEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT, district));
        
        // 分组查询
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .aggregation(AggregationBuilders
                        .terms(HouseIndexKey.AGG_DISTRICT)  // 分组空间名，随意指定
                        .field(HouseIndexKey.DISTRICT)      // 指定分组字段
                );
        
        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);
        
        try {
            // 查询
            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
            // 解析结果
            Terms terms = response.getAggregations().get(HouseIndexKey.AGG_DISTRICT);
            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
                return ServiceResult.of(terms.getBucketByKey(district).getDocCount()); //返回文档数量
            }
        } catch (IOException e) {
            log.error("Failed to Aggregate for  {}", HouseIndexKey.AGG_DISTRICT);
        }
        
        return ServiceResult.of(0L);
    }

    /**
     * 聚合城市数据
     * @param cityEnName
     * @return
     */
    @Override
    public ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));

        AggregationBuilder aggBuilder = AggregationBuilders.terms(HouseIndexKey.AGG_REGION)
                .field(HouseIndexKey.REGION_EN_NAME);
        
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQueryBuilder)
                .aggregation(aggBuilder);
        
        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);
        List<HouseBucketDTO> buckets = new ArrayList<>();

        SearchResponse response;
        try {
            response = esClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Aggregate status is not ok for " + sourceBuilder);
            return new ServiceMultiResult<>(0, buckets);
        }

        Terms terms = response.getAggregations().get(HouseIndexKey.AGG_REGION);
        for (Terms.Bucket bucket : terms.getBuckets()) {
            buckets.add(new HouseBucketDTO(bucket.getKeyAsString(), bucket.getDocCount()));
        }

        return new ServiceMultiResult<>(response.getHits().getTotalHits().value, buckets);
    }

    /**
     * 城市级别查询
     */
    @Override
    public ServiceMultiResult<Long> mapQuery(String cityEnName, String orderBy, String orderDirection, int start, int size) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));
        
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource()
                .query(boolQuery)
                .sort(HouseSort.getSortKey(orderBy), SortOrder.fromString(orderDirection))
                .from(start)
                .size(size);

        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);

        SearchResponse response;
        List<Long> houseIds = new ArrayList<>();
        try {
            response = esClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error to map query : " + sourceBuilder, e);
            return new ServiceMultiResult<>(0, houseIds);
        }

        if (response.status() != RestStatus.OK) {
            log.error("Search status is not ok for " + sourceBuilder);
            return new ServiceMultiResult<>(0, houseIds);
        }

        for (SearchHit hit : response.getHits()) {
            houseIds.add(Longs.tryParse(String.valueOf(hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
        }
        return new ServiceMultiResult<>(response.getHits().getTotalHits().value, houseIds);
    }

    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    @Override
    public ServiceMultiResult<Long> mapQuery(MapSearch mapSearch) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, mapSearch.getCityEnName()));
        // 设置经纬度字段和范围
        boolQuery.filter(
                QueryBuilders.geoBoundingBoxQuery("location").setCorners(
                        new GeoPoint(mapSearch.getLeftLatitude(), mapSearch.getLeftLongitude()),
                        new GeoPoint(mapSearch.getRightLatitude(), mapSearch.getRightLongitude())
                )
        );
        
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource()
                .query(boolQuery)
                .sort(HouseSort.getSortKey(mapSearch.getOrderBy()), SortOrder.fromString(mapSearch.getOrderDirection()))
                .from(mapSearch.getStart())
                .size(mapSearch.getSize());
        log.info(sourceBuilder.toString());
        
        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);
        
        SearchResponse response;
        List<Long> houseIds = new ArrayList<>();
        try {
            response = esClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error to map query : " + sourceBuilder, e);
            return new ServiceMultiResult<>(0, houseIds);
        }

        if (RestStatus.OK != response.status()) {
            log.error("Search status is not ok for " + sourceBuilder);
            return new ServiceMultiResult<>(0, houseIds);
        }

        for (SearchHit hit : response.getHits()) {
            houseIds.add(Longs.tryParse(String.valueOf(hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
        }
        return new ServiceMultiResult<>(response.getHits().getTotalHits().value, houseIds);
    }

    /**
     * 根据houseId，更新ES文档
     * @param houseId 房源id
     * @param retry 重试次数
     */
    private void index(Long houseId, int retry) {
        // 校验重试次数
        if (retry > HouseIndexMessage.MAX_RETRY) {
            log.error("Retry index times over 3 for house: {}, Please check it!", houseId);
            return;
        }
        // 发送更新文档消息
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Json encode error for {}", message);
        }
    }

    /**
     * 根据houseId，删除ES文档
     * @param houseId 房源id
     * @param retry 重试次数
     */
    private void remove(Long houseId, int retry) {
        // 校验重试次数
        if (retry > HouseIndexMessage.MAX_RETRY) {
            log.error("Retry remove times over 3 for house: {}, Please check it!", houseId);
            return;
        }
        // 发送删除文档消息
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            this.kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Cannot encode json for " + message, e);
        }
    }

    /**
     * 创建文档
     * @param indexTemplate 文档
     */
    private boolean create(HouseIndexTemplate indexTemplate) {
        // 填充提示词语
        updateSuggest(indexTemplate);
        
        try {
            // 创建索引请求
            IndexRequest request = new IndexRequest(INDEX_NAME)
                    .source(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON);
            // 创建索引
            IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
            log.debug("Create index with house: {}", indexTemplate.getHouseId());
            return response.status() == RestStatus.CREATED;
        } catch (IOException e) {
            log.error("Error to index house " + indexTemplate.getHouseId(), e);
            return false;
        }
    }

    /**
     * 更新文档
     * @param esId es文档id
     * @param indexTemplate 文档
     */
    private boolean update(String esId, HouseIndexTemplate indexTemplate) {
        // 填充提示词语
        updateSuggest(indexTemplate);
        
        try {
            UpdateRequest request = new UpdateRequest(INDEX_NAME, esId)
                    .doc(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON);

            UpdateResponse response = esClient.update(request, RequestOptions.DEFAULT);
            log.debug("Update index with house: {}", indexTemplate.getHouseId());
            return response.status() == RestStatus.OK;
        } catch (IOException e) {
            log.error("Error to index house " + indexTemplate.getHouseId(), e);
            return false;
        }
    }

    /**
     * 删除后创建文档
     * @param totalHit 文档总数
     * @param indexTemplate 文档
     */
    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate indexTemplate) {
        try {
            // 删除原有文档
            DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX_NAME)
                    .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId()));
            BulkByScrollResponse response = esClient.deleteByQuery(request, RequestOptions.DEFAULT);

            // 根据删除数量判断
            long deleted = response.getDeleted();
            if (deleted != totalHit) {
                log.warn("Need delete {}, but {} was deleted!", totalHit, deleted);
                return false;
            } else {
                return this.create(indexTemplate);
            }
        } catch (IOException e) {
            log.warn("Error to delete and create house " + indexTemplate.getHouseId(), e);
            return false;
        }
    }

    /**
     * 对文档内容进行分词，填充提示词语
     * @param indexTemplate 文档
     * @return
     */
    private boolean updateSuggest(HouseIndexTemplate indexTemplate) {
        
        // 创建分词请求，指定索引、分词器、待分析的文本
        AnalyzeRequest request = AnalyzeRequest.withIndexAnalyzer(INDEX_NAME, "ik_smart", 
                indexTemplate.getTitle(), indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
                indexTemplate.getDescription(), indexTemplate.getSubwayLineName(), indexTemplate.getSubwayStationName());

        // 执行分词操作
        AnalyzeResponse response;
        try {
            response = esClient.indices().analyze(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error to analyze token for house: {}", indexTemplate.getHouseId());
            return false;
        }

        // 处理结果
        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
        if (tokens == null) {
            log.warn("Can not analyze token for house: {}", indexTemplate.getHouseId());
            return false;
        }

        List<HouseSuggest> suggests = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            // 排序数字类型 & 小于2个字符的分词结果
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                continue;
            }

            HouseSuggest suggest = new HouseSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);
        }

        // 定制化小区自动补全
        HouseSuggest suggest = new HouseSuggest();
        suggest.setInput(indexTemplate.getDistrict());
        suggests.add(suggest);
        // 设置建议
        indexTemplate.setSuggest(suggests);

        return true;
    }
    //endregion
    
    
    //region MQ消费
    /**
     * 消费方法，监听kafka，异步创建索引文档
     * @param content kafka消息
     */
    @KafkaListener(topics = INDEX_TOPIC)
    private void handleMessage(String content) {
        try {
            HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);

            switch (message.getOperation()) {
                case HouseIndexMessage.INDEX:
                    this.createOrUpdateIndex(message);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(message);
                    break;
                default:
                    log.warn("Not support operation of message content : {}", content);
                    break;
            }
        } catch (IOException e) {
            log.error("Cannot parse json for " + content, e);
        }
    }

    /**
     * 根据MQ消息，更新ES文档
     * @param message mq消息
     */
    private void createOrUpdateIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();

        // 创建ES文档对象
        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();

        // 查询设置房屋基本信息
        House house = houseRepository.findById(houseId).orElse(null);
        if (house == null) {
            log.error("Index house {} does not exist!", houseId);
            this.index(houseId, message.getRetry() + 1);
            return;
        }
        modelMapper.map(house, indexTemplate);

        // 查询设置房源详情
        HouseDetail houseDetail = houseDetailRepository.findByHouseId(houseId);
        if (houseDetail == null) {
            //todo 异常处理
        }
        modelMapper.map(houseDetail, indexTemplate);
        
        // 查出房源的城市和地区
        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(house.getCityEnName(), SupportAddress.Level.CITY.getValue());
        SupportAddress region = supportAddressRepository.findByEnNameAndLevel(house.getRegionEnName(), SupportAddress.Level.REGION.getValue());
        // 拼出房源详细地址
        String address = city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict() + houseDetail.getDetailAddress();
        // 查询地址的经纬度
        ServiceResult<BaiduMapLocation> baiduMapLocation = addressService.getBaiduMapLocation(city.getCnName(), address);
        if (!baiduMapLocation.isSuccess()) {
            this.index(message.getHouseId(), message.getRetry() + 1);
            return;
        }
        indexTemplate.setLocation(baiduMapLocation.getResult());

        // 查询设置标签
        List<HouseTag> tags = tagRepository.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagStrings = tags.stream().map(HouseTag::getName).collect(Collectors.toList());
            indexTemplate.setTags(tagStrings);
        }

        // 搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchQuery(HouseIndexKey.HOUSE_ID, houseId));
        // 搜索请求
        SearchRequest searchRequest = new SearchRequest().indices(INDEX_NAME).source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error to search index house {}", houseId);
            return;
        }

        // 根据ES中已有数量，分别执行增删改操操作
        long totalHits = searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) {           // ES中没有，则新增
            this.create(indexTemplate);
        } else if (totalHits == 1) {    // ES中有一条，则更新
            String esId = searchResponse.getHits().getAt(0).getId();
            this.update(esId, indexTemplate);
        } else {                        // 多于一条，数据有误，删除再创建
            this.deleteAndCreate(totalHits, indexTemplate);
        }
    }

    /**
     * 根据MQ消息，删除文档
     * @param message mq消息
     */
    private void removeIndex(HouseIndexMessage message) {
        try {
            // 删除原有文档
            DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX_NAME)
                    .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, message.getHouseId()));
            BulkByScrollResponse response =  esClient.deleteByQuery(request, RequestOptions.DEFAULT);

            // 根据删除数量判断
            long deleted = response.getDeleted();
            log.debug("Deleted {} doc for houseId {}", deleted, message.getHouseId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion
    
}
