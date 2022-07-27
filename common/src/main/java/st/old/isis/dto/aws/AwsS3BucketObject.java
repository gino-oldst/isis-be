package st.old.isis.dto.aws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwsS3BucketObject {
    private String sourceBucket;
    private String sourceKey;
    private String targetBucket;
    private String targetKey;
}
