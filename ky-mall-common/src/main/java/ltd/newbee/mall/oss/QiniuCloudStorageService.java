package ltd.newbee.mall.oss;

import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @ClassName: QiniuCloudStorageService
 * @Description: 七牛云存储
 * @author: <a href="edeis@163.com">edeis</a>
 * @version: V1.0.0
 * @date: 2017-8-1
 */
@Service
public class QiniuCloudStorageService extends CloudStorageService {
    private UploadManager uploadManager;
    private String token;

    public QiniuCloudStorageService(CloudStorageProperties config){
        this.config = config;

        //初始化
        init();
    }

    private void init(){
        uploadManager = new UploadManager(new Configuration(Zone.autoZone()));
        token = Auth.create(config.getAccessKey(), config.getSecretKey()).
                uploadToken(config.getBucketName());
    }

    @Override
    public String upload(byte[] data, String path) {
        try {
            init();
            Response res = uploadManager.put(data, path, token);
            if (!res.isOK()) {
                throw new RuntimeException("上传七牛出错：" + res.toString());
            }
        } catch (Exception e) {
            throw new OssException("上传文件失败，请核对七牛配置信息", e);
        }

        return config.getDomain() + "/" + path;
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        try {
            byte[] data = IOUtils.toByteArray(inputStream);
            return this.upload(data, path);
        } catch (IOException e) {
            throw new OssException("上传文件失败", e);
        }
    }

    @Override
    public String upload(byte[] data) {
        return upload(data, getPath(config.getPrefix()));
    }

    @Override
    public String upload(InputStream inputStream) {
        return upload(inputStream, getPath(config.getPrefix()));
    }
}
