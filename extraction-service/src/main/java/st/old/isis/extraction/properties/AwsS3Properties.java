package st.old.isis.extraction.properties;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AwsS3Properties {
    private String url;
    private String region;
    private String accessKey;
    private String secretKey;
    private String rawBucket;
    private String ingressBucket;
    private String directory;
}
