package moe.waifuvault;

import moe.waifuvault.models.GenericSuccess;
import moe.waifuvault.models.WaifuBucket;

import java.util.Map;

class BucketApi extends BaseApi {

    public BucketApi(final HttpService config) {
        super(config);
    }

    public WaifuBucket createBucket() {
        return get("bucket/create", null, WaifuBucket.class);
    }

    public WaifuBucket getBucket(final String bucketToken) {
        return postJson("bucket/get", Map.of("bucket_token", bucketToken), WaifuBucket.class);
    }

    public GenericSuccess deleteBucket(final String bucketToken) {
        return deleteRaw("bucket/" + bucketToken);
    }
}
