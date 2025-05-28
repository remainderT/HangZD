package org.buaa.project.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class EsConfiguration {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.index-name}")
    private String INDEX_NAME;

    @Bean
    public RestHighLevelClient restHighLevelClient() throws IOException{
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                org.apache.http.auth.AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "ysh030605")
        );
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(elasticsearchHost, 9200, "http"))
                        .setHttpClientConfigCallback(
                                httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                        ));

        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX_NAME);
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (exists) {
            log.info("ES索引库已存在");
        } else {
            log.info("ES索引库不存在，请使用 resources 下的模板文件创建索引库");
        }
        return client;
    }
}