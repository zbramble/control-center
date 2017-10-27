package com.wenba.scheduler.jzh;

import com.wenba.scheduler.AbstractResult;

/**
 * @author zhangbo
 *
 */
public class JzhResult extends AbstractResult {

    // 成员变量
    /**
     * img enhance response entity content as bytes
     */
    private byte[] entityContentAsBytes;

    public byte[] getEntityContentAsBytes() {
        return entityContentAsBytes;
    }

    public void setEntityContentAsBytes(byte[] entityContentAsBytes) {
        this.entityContentAsBytes = entityContentAsBytes;
    }

}
