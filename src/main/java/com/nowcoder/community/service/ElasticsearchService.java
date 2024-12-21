package com.nowcoder.community.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) throws IOException {
//        Query query = new NativeQueryBuilder()
//                .withQuery()
//                .withSort(s -> s.field(FieldSort.of(f -> f.field("type").order(SortOrder.Desc))))
//                .withSort(s -> s.field(FieldSort.of(f -> f.field("score").order(SortOrder.Desc))))
//                .withSort(s -> s.field(FieldSort.of(f -> f.field("createTime").order(SortOrder.Desc))))
//                .withPageable(PageRequest.of(current, limit))
//                .withHighlightQuery(
//                        new HighlightQuery(new Highlight(Arrays.asList(new HighlightField("title"))), DiscussPost.class).pre,
//                        new HighlightQuery(new Highlight(Arrays.asList(new HighlightField("content"))), DiscussPost.class)
//
//
//                ).build();
        Query query = QueryBuilders.bool()
                .should(QueryBuilders.match().field("title").query(keyword).build()._toQuery())
                .should(QueryBuilders.match().field("content").query(keyword).build()._toQuery())
                .build()
                ._toQuery();

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("discusspost")
                .query(query)
                .highlight(h -> h.fields("title", f -> f.preTags("<em>").postTags("</em>"))
                        .fields("content", f -> f.preTags("<em>").postTags("</em>"))
                )
                .sort(s -> s.field(FieldSort.of(f -> f.field("type").order(SortOrder.Desc))))/**参考代码来源：
                 https://discuss.elastic.co/t/new-elasticsearch-8-java-api-sort-by/311352*/
                .sort(s -> s.field(FieldSort.of(f -> f.field("score").order(SortOrder.Desc))))
                .sort(s -> s.field(FieldSort.of(f -> f.field("createTime").order(SortOrder.Desc))))
                .build();

        SearchResponse<DiscussPost> searchResponse = elasticsearchClient.search(searchRequest, DiscussPost.class);
        HitsMetadata<DiscussPost> hitsMetadata = searchResponse.hits();

        List<DiscussPost> discussPosts = hitsMetadata.hits().stream()
                .map(hit -> {
                    DiscussPost discussPost = hit.source();
                    if (hit.highlight() != null) {
                        if (hit.highlight().get("title") != null) {
                            discussPost.setTitle(String.join(" ", hit.highlight().get("title")));
                        }
                        if (hit.highlight().get("content") != null) {
                            discussPost.setContent(String.join(" ", hit.highlight().get("content")));
                        }
                    }
                    return discussPost;
                })
                .collect(Collectors.toList());

        PageRequest pageable = PageRequest.of(0, 10);
        return new PageImpl<>(discussPosts, pageable, hitsMetadata.total().value());

    }
}
