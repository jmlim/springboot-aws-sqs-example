package io.jmlim.springboot.sqs.etc;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.internal.*;
import com.amazonaws.auth.profile.internal.securitytoken.STSProfileCredentialsServiceLoader;
import com.amazonaws.profile.path.AwsProfileFileLocationProvider;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.amazonaws.auth.profile.internal.AwsProfileNameLoader.AWS_PROFILE_ENVIRONMENT_VARIABLE;

public class EtcTest {

    @Test
    public void test1() {
        System.out.println(StringUtils.trim(System.getenv(AWS_PROFILE_ENVIRONMENT_VARIABLE)));
    }

    @Test
    public void profileAssumeSqsTest() {
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
        AWSCredentialsProvider provider;
        if (profile.isRoleBasedProfile()) {
            provider = new ProfileAssumeRoleCredentialsProvider(STSProfileCredentialsServiceLoader.getInstance(), allProfiles, profile);
        } else {
            provider = new ProfileStaticCredentialsProvider(profile);
        }

        AmazonSQSAsync sqs = AmazonSQSAsyncClientBuilder.standard()//.withRegion(Regions.fromName(region))
                .withCredentials(provider)
                .build();


        String queueUrl = sqs.getQueueUrl("jmlim-sqs").getQueueUrl();
        System.out.println(queueUrl);

        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();

        for (Message m : messages) {
            System.out.println("========");
            System.out.println(m.getBody());
            System.out.println("==========");
        }
    }
}
