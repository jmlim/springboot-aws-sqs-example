package io.jmlim.springboot.sqs.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SQSConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.access-key}")
    private String awsAccessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String awsSecretKey;

    /**
     * https://stackoverflow.com/questions/40092518/how-to-configure-custom-spring-cloud-aws-simplemessagelistenercontainerfactory-s
     * <p>
     * 메세지를 maxNumberOfMessage 수 만큼 불러오면 다음 불러오기까지 기다린다.
     * 재 호출에 대한 시간을 조절하는 것은 AWS SQS 에 들어간 후 다음을 수정한다.(기본값 30초)
     * <p>
     * https://lemontia.tistory.com/871
     *
     * @return
     */
    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory() {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSQSAsync());
        factory.setMaxNumberOfMessages(10); // 2 ~ 10 개 까지 설정 가능.
        factory.setWaitTimeOut(10);// Long polling 설정
        return factory;
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate() {
        return new QueueMessagingTemplate(amazonSQSAsync());
    }

    public AmazonSQSAsync amazonSQSAsync() {
        return AmazonSQSAsyncClientBuilder.standard().withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
                .build();
    }
}