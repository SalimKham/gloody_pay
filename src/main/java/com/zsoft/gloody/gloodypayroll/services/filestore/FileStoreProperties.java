package com.zsoft.gloody.gloodypayroll.services.filestore;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file-store")
@Setter
@Getter
public class FileStoreProperties {
    private String path;
    private String payRecordPrefix;

    public String getPayRecordPathPrefix() {
        return String.format("%s/%s", this.path, this.payRecordPrefix);
    }

    public String getAffiliateLogoPrefix() {
        return String.format("%s/%s", this.path, "affiliate-logo");
    }
}
