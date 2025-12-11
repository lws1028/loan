package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zlhj.unifiedInputPlatform.ant.utils.AntCreditSwitchConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class AntFlowSwitcher {

    private static final String SWITCH_KEY = "ant:credit:switch";
    private static final String ANT_SWITCH_COUNT_KEY = "ant:credit:switch:count"  + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private volatile com.zlhj.unifiedInputPlatform.ant.utils.AntCreditSwitchConfig config = new com.zlhj.unifiedInputPlatform.ant.utils.AntCreditSwitchConfig();

	public AntFlowSwitcher(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	@PostConstruct
    public void init() {
        log.info("蚂蚁分流灰度配置AntFlowSwitcher init...");
        refreshConfig();
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::refreshConfig, 0, 30, TimeUnit.SECONDS);
    }

    private void refreshConfig() {
        log.info("蚂蚁分流灰度配置AntFlowSwitcher refreshConfig...");
        try {
            String json = redisTemplate.opsForValue().get(SWITCH_KEY);

            log.info("蚂蚁分流灰度配置AntFlowSwitcher_refreshConfig_redisData  = {}", json);
            if (json != null) {
				this.config = objectMapper.readValue(json, AntCreditSwitchConfig.class);
            }
        } catch (Exception e) {
            log.error("蚂蚁分流灰度配置读取失败", e);
        }
    }

    public boolean useNewFlow() {
        if (!config.isEnabled()){
            return false;
        }
        Long increment = redisTemplate.opsForValue().increment(ANT_SWITCH_COUNT_KEY,1);
        if (increment == null){
            return ThreadLocalRandom.current().nextInt(100) <= config.getPercentage();
        }
        return increment % 100 <= config.getPercentage();
    }

}
