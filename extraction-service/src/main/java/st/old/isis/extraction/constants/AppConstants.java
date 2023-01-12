package st.old.isis.extraction.constants;

public class AppConstants {

    //S3 Buckets
    public static final String AWS_S3_BUCKET_RAW = "${isis.cloud.aws.s3.bucket.warehouse}";

    public static final String AWS_S3_URI = "${isis.cloud.aws.s3.url}";
    public static final String AWS_S3_REGION = "${isis.cloud.aws.s3.region}";
    public static final String AWS_S3_ACCESS_KEY = "${isis.cloud.aws.s3.accessKey}";
    public static final String AWS_S3_SECRET_KEY = "${isis.cloud.aws.s3.secretKey}";

    public static final String AWS_S3_RAW_BUCKET = "${isis.cloud.aws.s3.raw-bucket}";
    public static final String AWS_S3_INGRESS_BUCKET = "${isis.cloud.aws.s3.ingress-bucket}";
    public static final String AWS_S3_PROCESSED_BUCKET = "${isis.cloud.aws.s3.processed-bucket}";

    public static final String AWS_OPENSEARCH_USERNAME = "${isis.cloud.aws.opensearch.username}";

    public static final String AWS_OPENSEARCH_PASSWORD = "${isis.cloud.aws.opensearch.password}";

    public static final String AWS_OPENSEARCH_HOSTNAME = "${isis.cloud.aws.opensearch.hostname}";

    public static final String AWS_OPENSEARCH_INDEX_PATH = "${isis.cloud.aws.opensearch.index-path}";



}
