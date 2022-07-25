package st.old.isis.extraction.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import st.old.isis.extraction.constants.AppConstants;
import st.old.isis.extraction.properties.AwsS3Properties;

@Configuration
public class AwsS3Configuration {

    @Value(AppConstants.AWS_S3_URI)
    private String url;

    @Value(AppConstants.AWS_S3_REGION)
    private String region;

    @Value(AppConstants.AWS_S3_ACCESS_KEY)
    private String accessKey;

    @Value(AppConstants.AWS_S3_SECRET_KEY)
    private String secretKey;

    @Bean
    public AwsS3Properties awsS3Properties(){
        return AwsS3Properties.builder()
                .url(url)
                .region(region)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .build();
    }



}
