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
 * ES????????????
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

    //region ????????????
    /**
     * ??????houseId?????????ES??????
     * @param houseId ??????id
     */
    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    /**
     * ??????houseId?????????ES??????
     * @param houseId ??????id
     */
    @Override
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }

    /**
     * ????????????
     * @param rentSearch
     * @return
     */
    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        // 1. ??????ES????????? QueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // ??????
        boolQueryBuilder.filter(
                QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName())
        );
        // ??????
        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName())
            );
        }
        // ????????????
        if (rentSearch.getDirection() > 0) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection())
            );
        }
        // ????????????
        if (rentSearch.getRentWay() > -1) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay())
            );
        }
        // ????????????
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
        // ????????????
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
        // ?????????
        boolQueryBuilder.should(
                QueryBuilders.multiMatchQuery(
                        rentSearch.getKeywords(),   // ???????????????
                        // HouseIndexKey.TITLE,
                        HouseIndexKey.TRAFFIC,      // ??????????????????????????????????????????
                        HouseIndexKey.DISTRICT,
                        HouseIndexKey.ROUND_SERVICE,
                        HouseIndexKey.SUBWAY_LINE_NAME,
                        HouseIndexKey.SUBWAY_STATION_NAME
                )
        );
        
        boolQueryBuilder.should(
                QueryBuilders.matchQuery(rentSearch.getKeywords(), HouseIndexKey.TITLE).boost(2.0F)  //????????????
        );
        
        // 2. ??????????????? SearchSourceBuilder 
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQueryBuilder)        // ??????Query
                .sort(                          // ??????
                        HouseSort.getSortKey(rentSearch.getOrderBy()),
                        SortOrder.fromString(rentSearch.getOrderDirection())
                )
                .from(rentSearch.getStart())    // ????????????
                .size(rentSearch.getSize())     // ????????????
                .fetchSource(HouseIndexKey.HOUSE_ID, null); // ?????????????????????????????????????????? houseId

        log.debug(sourceBuilder.toString());
        
        // 4. ???????????? SearchRequest
        SearchRequest request = new SearchRequest().indices(INDEX_NAME).source(sourceBuilder);
        
        // 3. ??????????????????
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
     * ???????????????????????????
     * @param prefix
     * @return
     */
    @Override
    public ServiceResult<List<String>> suggest(String prefix) {
        // ??????????????????
        CompletionSuggestionBuilder suggestion = SuggestBuilders
                .completionSuggestion("suggest")   // ?????????????????????
                .prefix(prefix).size(5);   // ?????????????????????????????????
        
        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion("autocomplete", suggestion);  // ???????????????????????????????????????

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().suggest(suggestBuilder);

        // ????????????
        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);
        SearchResponse response;
        try {
            response = esClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error or search suggest word for prefix {}", prefix);
            return ServiceResult.of(new ArrayList<>());
        }

        // ????????????
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return ServiceResult.of(new ArrayList<>());
        }

        // ??????????????????????????????
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
     * ?????????????????????????????????????????????
     * @param cityEnName ?????????
     * @param regionEnName ?????????
     * @param district ?????????
     * @return
     */
    @Override
    public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, regionEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT, district));
        
        // ????????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .aggregation(AggregationBuilders
                        .terms(HouseIndexKey.AGG_DISTRICT)  // ??????????????????????????????
                        .field(HouseIndexKey.DISTRICT)      // ??????????????????
                );
        
        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);
        
        try {
            // ??????
            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
            // ????????????
            Terms terms = response.getAggregations().get(HouseIndexKey.AGG_DISTRICT);
            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
                return ServiceResult.of(terms.getBucketByKey(district).getDocCount()); //??????????????????
            }
        } catch (IOException e) {
            log.error("Failed to Aggregate for  {}", HouseIndexKey.AGG_DISTRICT);
        }
        
        return ServiceResult.of(0L);
    }

    /**
     * ??????????????????
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
     * ??????????????????
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
     * ????????????????????????
     * @param mapSearch
     * @return
     */
    @Override
    public ServiceMultiResult<Long> mapQuery(MapSearch mapSearch) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, mapSearch.getCityEnName()));
        // ??????????????????????????????
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
     * ??????houseId?????????ES??????
     * @param houseId ??????id
     * @param retry ????????????
     */
    private void index(Long houseId, int retry) {
        // ??????????????????
        if (retry > HouseIndexMessage.MAX_RETRY) {
            log.error("Retry index times over 3 for house: {}, Please check it!", houseId);
            return;
        }
        // ????????????????????????
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Json encode error for {}", message);
        }
    }

    /**
     * ??????houseId?????????ES??????
     * @param houseId ??????id
     * @param retry ????????????
     */
    private void remove(Long houseId, int retry) {
        // ??????????????????
        if (retry > HouseIndexMessage.MAX_RETRY) {
            log.error("Retry remove times over 3 for house: {}, Please check it!", houseId);
            return;
        }
        // ????????????????????????
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            this.kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Cannot encode json for " + message, e);
        }
    }

    /**
     * ????????????
     * @param indexTemplate ??????
     */
    private boolean create(HouseIndexTemplate indexTemplate) {
        // ??????????????????
        updateSuggest(indexTemplate);
        
        try {
            // ??????????????????
            IndexRequest request = new IndexRequest(INDEX_NAME)
                    .source(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON);
            // ????????????
            IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
            log.debug("Create index with house: {}", indexTemplate.getHouseId());
            return response.status() == RestStatus.CREATED;
        } catch (IOException e) {
            log.error("Error to index house " + indexTemplate.getHouseId(), e);
            return false;
        }
    }

    /**
     * ????????????
     * @param esId es??????id
     * @param indexTemplate ??????
     */
    private boolean update(String esId, HouseIndexTemplate indexTemplate) {
        // ??????????????????
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
     * ?????????????????????
     * @param totalHit ????????????
     * @param indexTemplate ??????
     */
    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate indexTemplate) {
        try {
            // ??????????????????
            DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX_NAME)
                    .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId()));
            BulkByScrollResponse response = esClient.deleteByQuery(request, RequestOptions.DEFAULT);

            // ????????????????????????
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
     * ????????????????????????????????????????????????
     * @param indexTemplate ??????
     * @return
     */
    private boolean updateSuggest(HouseIndexTemplate indexTemplate) {
        
        // ??????????????????????????????????????????????????????????????????
        AnalyzeRequest request = AnalyzeRequest.withIndexAnalyzer(INDEX_NAME, "ik_smart", 
                indexTemplate.getTitle(), indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
                indexTemplate.getDescription(), indexTemplate.getSubwayLineName(), indexTemplate.getSubwayStationName());

        // ??????????????????
        AnalyzeResponse response;
        try {
            response = esClient.indices().analyze(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error to analyze token for house: {}", indexTemplate.getHouseId());
            return false;
        }

        // ????????????
        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
        if (tokens == null) {
            log.warn("Can not analyze token for house: {}", indexTemplate.getHouseId());
            return false;
        }

        List<HouseSuggest> suggests = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            // ?????????????????? & ??????2????????????????????????
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                continue;
            }

            HouseSuggest suggest = new HouseSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);
        }

        // ???????????????????????????
        HouseSuggest suggest = new HouseSuggest();
        suggest.setInput(indexTemplate.getDistrict());
        suggests.add(suggest);
        // ????????????
        indexTemplate.setSuggest(suggests);

        return true;
    }
    //endregion
    
    
    //region MQ??????
    /**
     * ?????????????????????kafka???????????????????????????
     * @param content kafka??????
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
     * ??????MQ???????????????ES??????
     * @param message mq??????
     */
    private void createOrUpdateIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();

        // ??????ES????????????
        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();

        // ??????????????????????????????
        House house = houseRepository.findById(houseId).orElse(null);
        if (house == null) {
            log.error("Index house {} does not exist!", houseId);
            this.index(houseId, message.getRetry() + 1);
            return;
        }
        modelMapper.map(house, indexTemplate);

        // ????????????????????????
        HouseDetail houseDetail = houseDetailRepository.findByHouseId(houseId);
        if (houseDetail == null) {
            //todo ????????????
        }
        modelMapper.map(houseDetail, indexTemplate);
        
        // ??????????????????????????????
        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(house.getCityEnName(), SupportAddress.Level.CITY.getValue());
        SupportAddress region = supportAddressRepository.findByEnNameAndLevel(house.getRegionEnName(), SupportAddress.Level.REGION.getValue());
        // ????????????????????????
        String address = city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict() + houseDetail.getDetailAddress();
        // ????????????????????????
        ServiceResult<BaiduMapLocation> baiduMapLocation = addressService.getBaiduMapLocation(city.getCnName(), address);
        if (!baiduMapLocation.isSuccess()) {
            this.index(message.getHouseId(), message.getRetry() + 1);
            return;
        }
        indexTemplate.setLocation(baiduMapLocation.getResult());

        // ??????????????????
        List<HouseTag> tags = tagRepository.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagStrings = tags.stream().map(HouseTag::getName).collect(Collectors.toList());
            indexTemplate.setTags(tagStrings);
        }

        // ????????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchQuery(HouseIndexKey.HOUSE_ID, houseId));
        // ????????????
        SearchRequest searchRequest = new SearchRequest().indices(INDEX_NAME).source(sourceBuilder);
        // ????????????
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error to search index house {}", houseId);
            return;
        }

        // ??????ES????????????????????????????????????????????????
        long totalHits = searchResponse.getHits().getTotalHits().value;
        if (totalHits == 0) {           // ES?????????????????????
            this.create(indexTemplate);
        } else if (totalHits == 1) {    // ES????????????????????????
            String esId = searchResponse.getHits().getAt(0).getId();
            this.update(esId, indexTemplate);
        } else {                        // ?????????????????????????????????????????????
            this.deleteAndCreate(totalHits, indexTemplate);
        }
    }

    /**
     * ??????MQ?????????????????????
     * @param message mq??????
     */
    private void removeIndex(HouseIndexMessage message) {
        try {
            // ??????????????????
            DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX_NAME)
                    .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, message.getHouseId()));
            BulkByScrollResponse response =  esClient.deleteByQuery(request, RequestOptions.DEFAULT);

            // ????????????????????????
            long deleted = response.getDeleted();
            log.debug("Deleted {} doc for houseId {}", deleted, message.getHouseId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion
    
}
