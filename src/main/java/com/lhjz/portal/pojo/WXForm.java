package com.lhjz.portal.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class WXForm implements Serializable {

	private static final long serialVersionUID = -8722545481654855375L;
	/**
	 * signature [String]
	 * 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
	 */
	private String signature;
	/** timestamp [String] 时间戳 */
	private String timestamp;
	/** nonce [String] 随机数 */
	private String nonce;
	/** echostr [String] 随机字符串 */
	private String echostr;
}
