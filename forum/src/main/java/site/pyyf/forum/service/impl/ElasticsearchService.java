package site.pyyf.forum.service.impl;

import org.elasticsearch.index.query.BoolQueryBuilder;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.entity.User;
import site.pyyf.forum.service.IElasticsearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 参考博客： https://blog.csdn.net/tianyaleixiaowu/article/details/77965257
 */
@Service
public class ElasticsearchService extends BaseService implements IElasticsearchService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    @Override
    public void saveDiscussPost(DiscussPost post) {
        discussRepository.save(post);
    }

    @Override
    public void deleteDiscussPost(int id) {
        discussRepository.deleteById(id);
    }


    /**
     * 第一版本，作为参考
     *
     * @param searchKey
     * @param current
     * @param limit
     * @return
     * @Override
     * @Deprecated public Page<DiscussPost> searchDiscussPost(String searchKey, int current, int limit) {
     * SearchQuery searchQuery = new NativeSearchQueryBuilder()
     * .withQuery(QueryBuilders.multiMatchQuery(searchKey, "title", "content"))
     * .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
     * .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
     * .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
     * .withPageable(PageRequest.of(current, limit))
     * .withHighlightFields(
     * new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
     * new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
     * ).build();
     * <p>
     * return elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
     * @Override public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
     * SearchHits hits = response.getHits();
     * if (hits.getTotalHits() <= 0) {
     * return null;
     * }
     * <p>
     * List<DiscussPost> list = new ArrayList<>();
     * for (SearchHit hit : hits) {
     * DiscussPost post = new DiscussPost();
     * <p>
     * String id = hit.getSourceAsMap().get("id").toString();
     * post.setId(Integer.valueOf(id));
     * <p>
     * String userId = hit.getSourceAsMap().get("userId").toString();
     * post.setUserId(Integer.valueOf(userId));
     * <p>
     * String title = hit.getSourceAsMap().get("title").toString();
     * post.setTitle(title);
     * <p>
     * String content = hit.getSourceAsMap().get("content").toString();
     * post.setContent(content);
     * <p>
     * String status = hit.getSourceAsMap().get("status").toString();
     * post.setStatus(Integer.valueOf(status));
     * <p>
     * String createTime = hit.getSourceAsMap().get("createTime").toString();
     * post.setCreateTime(new Date(Long.valueOf(createTime)));
     * <p>
     * String commentCount = hit.getSourceAsMap().get("commentCount").toString();
     * post.setCommentCount(Integer.valueOf(commentCount));
     * <p>
     * // 处理高亮显示的结果
     * HighlightField titleField = hit.getHighlightFields().get("title");
     * if (titleField != null) {
     * post.setTitle(titleField.getFragments()[0].toString());
     * }
     * <p>
     * HighlightField contentField = hit.getHighlightFields().get("content");
     * if (contentField != null) {
     * post.setContent(contentField.getFragments()[0].toString());
     * }
     * <p>
     * list.add(post);
     * }
     * <p>
     * return new AggregatedPageImpl(list, pageable,
     * hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
     * }
     * });
     * }
     */


    @Override
    @Deprecated
    public Page<DiscussPost> searchDiscussPost(String searchKey, int current, int limit) {
        NativeSearchQueryBuilder baseQuery = new NativeSearchQueryBuilder();
        boolean inQuery = false;
        // 1. 用户输入的搜索条件按照空格分为若干部分
        String[] inputFields = searchKey.split(" ");

        // 2. 用户输入的搜索条件被分为两个部分，有:的部分则是条件部分，否则就是关键词部分
        List<String> keyConditions = new ArrayList<>();
        for (String inputField : inputFields) {
            if (!inputField.contains(":")) {
                keyConditions.add(inputField);
            }
        }

        // 3. 对于条件部分，则需要构建搜索条件
        for (String segment : inputFields) {
            if (segment.contains(":")) {
                String[] splits = segment.split(":");
                //如果不是 匹配字符:条件样式 则直接忽略
                if (splits.length != 2) continue;
                String regrex = splits[0];
                String condition = splits[1].trim();
                if (regrex.equals("user")) {
                    // :前是user，则后面传入的一定是用户名，所以根据用户名得到ID,在es中查ID
                    List<User> users = iUserService.queryAll(User.builder().username(condition).build());
                    if (users.size() > 0) {
                        for (User user : users)
                            // 构建条件
                            baseQuery = baseQuery.withQuery(QueryBuilders.multiMatchQuery(user.getId().toString(), "userId"));
                    }
                } else if (regrex.equals("in")) {
                    // :前是in, 则将keyCondition中的所有关键词都在condition中查一遍
                    // 即 git 多线程 in tags  则将帖子的标签与git 多线程分别进行匹配
                    for (String keyCondition : keyConditions)
                        baseQuery = baseQuery.withQuery(QueryBuilders.multiMatchQuery(keyCondition, condition));
                    inQuery = true;
                }
            }
        }

        //4. 只要没有in查询 ,则根据传来的条件进行匹配，这里匹配标题和内容
        if (!inQuery)
            for (String keyCondition : keyConditions)
                baseQuery = baseQuery.withQuery(QueryBuilders.multiMatchQuery(keyCondition, "title", "content"));

        // 5. 继续构建搜索条件，按照类型降序(置顶. 精品. 普通)，按照分数和时间降序，分页，标题匹配到则高亮，内容匹配到则高亮
        SearchQuery searchQuery = baseQuery
                .withSort(SortBuilders.scoreSort())
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        // 6. 将之前构建的搜索条件正式进行搜索，并将结果封装
        return elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                SearchHits hits = response.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    try {
                        DiscussPost post = new DiscussPost();

                        String id = hit.getSourceAsMap().get("id").toString();
                        post.setId(Integer.valueOf(id));

                        String userId = hit.getSourceAsMap().get("userId").toString();
                        post.setUserId(Integer.valueOf(userId));

                        String title = hit.getSourceAsMap().get("title").toString();
                        post.setTitle(title);

                        String content = hit.getSourceAsMap().get("content").toString();
                        int endIndex = content.length() > 200 ? 200 : content.length();
                        post.setContent(content.substring(0, endIndex));

                        String status = hit.getSourceAsMap().get("status").toString();
                        post.setStatus(Integer.valueOf(status));

                        String createTime = hit.getSourceAsMap().get("createTime").toString();
                        post.setCreateTime(new Date(Long.valueOf(createTime)));

                        String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                        post.setCommentCount(Integer.valueOf(commentCount));

                        String tags = hit.getSourceAsMap().get("tags").toString();
                        post.setTags(tags);

                        // 处理高亮显示的结果
                        HighlightField titleField = hit.getHighlightFields().get("title");
                        if (titleField != null) {
                            post.setTitle(titleField.getFragments()[0].toString());
                        }

                        HighlightField contentField = hit.getHighlightFields().get("content");
                        if (contentField != null) {
                            post.setContent(contentField.getFragments()[0].toString());
                        }

                        list.add(post);
                    } catch (NullPointerException e) {
                        logger.error("搜寻到了不存在的帖子：原因是es维护过程中出现了问题");
                    }

                }

                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
            }
        });
    }

}
