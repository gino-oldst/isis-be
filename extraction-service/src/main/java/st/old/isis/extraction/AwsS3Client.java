package st.old.isis.extraction;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import st.old.isis.extraction.properties.AwsS3Properties;

@Component
@RequiredArgsConstructor
public class AwsS3Client {

    private final AwsS3Properties awsS3Properties;

    @Bean
    public AmazonS3 config() {

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsS3Properties.getAccessKey(),
                awsS3Properties.getSecretKey());

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(awsS3Properties.getUrl(), awsS3Properties.getRegion())
                )
                .build();
    }

}
