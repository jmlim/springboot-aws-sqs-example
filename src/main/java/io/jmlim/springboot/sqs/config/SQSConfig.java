package io.jmlim.springboot.sqs.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.internal.*;
import com.amazonaws.auth.profile.internal.securitytoken.STSProfileCredentialsServiceLoader;
import com.amazonaws.profile.path.AwsProfileFileLocationProvider;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class SQSConfig {

    /**
     * https://stackoverflow.com/questions/40092518/how-to-configure-custom-spring-cloud-aws-simplemessagelistenercontainerfactory-s
     *
     * @return
     */
    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory() {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSQSAsync());
        factory.setMaxNumberOfMessages(10);
        factory.setWaitTimeOut(20);// Long polling 설정
        return factory;
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate() {
        return new QueueMessagingTemplate(amazonSQSAsync());
    }

    /**
     * @return
     */
    public AmazonSQSAsync amazonSQSAsync() {
        return AmazonSQSAsyncClientBuilder.standard()//.withRegion(Regions.fromName(region))
                .withCredentials(getDefaultCredentials())
                .build();
    }

    /**
     * profile 을 받아 해당 프로파일에 대한 role을 사용
     * <p>
     * environment variable에 AWS_PROFILE=jmlim-sqs;AWS_REGION=ap-northeast-2 셋팅.
     *
     * 설정하지 않으면 default
     *
     * @return
     */
    public AWSCredentialsProvider getDefaultCredentials() {

        final String profileName = AwsProfileNameLoader.INSTANCE.loadProfileName();
        final AllProfiles allProfiles = new AllProfiles(Stream.concat(
                BasicProfileConfigLoader.INSTANCE.loadProfiles(
                        AwsProfileFileLocationProvider.DEFAULT_CONFIG_LOCATION_PROVIDER.getLocation()).getProfiles().values().stream(),
                BasicProfileConfigLoader.INSTANCE.loadProfiles(
                        AwsProfileFileLocationProvider.DEFAULT_CREDENTIALS_LOCATION_PROVIDER.getLocation()).getProfiles().values().stream())
                .map(profile -> new BasicProfile(profile.getProfileName().replaceFirst("^profile ", ""), profile.getProperties()))
                .collect(Collectors.toMap(profile -> profile.getProfileName(), profile -> profile,
                        (left, right) -> {
                            final Map<String, String> properties = new HashMap<>(left.getProperties());
                            properties.putAll(right.getProperties());
                            return new BasicProfile(left.getProfileName(), properties);
                        })));

        final BasicProfile profile = Optional.ofNullable(allProfiles.getProfile(profileName))
                .orElseThrow(() -> new RuntimeException(String.format("Profile '%s' not found in %s",
                        profileName, allProfiles.getProfiles().keySet())));
        if (profile.isRoleBasedProfile()) {
            return new ProfileAssumeRoleCredentialsProvider(STSProfileCredentialsServiceLoader.getInstance(), allProfiles, profile);
        } else {
            return new ProfileStaticCredentialsProvider(profile);
        }
    }

}