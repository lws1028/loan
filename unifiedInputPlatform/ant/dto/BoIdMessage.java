package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BoIdMessage {

	private String boId;

	public BoIdMessage(String boId) {
		this.boId = boId;
	}
}
