package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.phone.service.impl;

import com.zlhj.infrastructure.adapter.RestException;
import com.zlhj.infrastructure.adapter.impl.CluePhoneSynchronise;
import com.zlhj.unifiedInputPlatform.phone.service.CluePhoneApplicationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class CluePhoneApplicationServiceImpl implements CluePhoneApplicationService {

	private final CluePhoneSynchronise cluePhoneSynchronise;

	@Override
	public void notify(String phone, String channelPartnerId) {
		try {
			this.cluePhoneSynchronise.request(phone, channelPartnerId);
		} catch (RestException e) {
			log.error(e.getMessage(), e);
		}
	}

}
