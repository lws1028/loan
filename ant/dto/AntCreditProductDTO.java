package com.zlhj.unifiedInputPlatform.ant.dto;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AntCreditProductDTO {
	private String prompt;
	private String rate;
	private String term;
	private String expireTime;

	public AntCreditProductDTO(String prompt,String rate, String term) {
		this.prompt = prompt;
		this.rate = rate;
		this.term = term;
	}

	public Long prompt() {
		return StringUtils.isEmpty(prompt) ? 0L : new BigDecimal(prompt).longValue();
	}
}
