package com.nowcoder.community;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOptionsBuilders;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.util.ObjectBuilder;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100, 0));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水。");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete() {
        // discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository() throws IOException {
      /**下面代码参考自https://blog.csdn.net/qq_71387716/article/details/140527673*/

        Query query = QueryBuilders.bool()
                .should(QueryBuilders.match().field("title").query("互联网寒冬").build()._toQuery())
                .should(QueryBuilders.match().field("content").query("互联网寒冬").build()._toQuery())
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
        Page<DiscussPost> page = new PageImpl<>(discussPosts, pageable, hitsMetadata.total().value());

        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post: page) {
            System.out.println(post);
        }

        //        Query searchQuery = new NativeQueryBuilder()
//                .withQuery(q -> q.match(m -> m.field("互联网寒冬").query("title")))
//                .withSort(Sort.by("type").descending())
//                .withSort(Sort.by("score").descending())
//                .withSort(Sort.by("createTime").descending())
//                .withPageable(PageRequest.of(0, 10))
//                .build();
//
//        return elasticsearchOperations.search(searchQuery, ElasticsearchTests.class);

    }


}
