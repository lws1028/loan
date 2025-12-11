package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.handler;

import com.apollo.org.vo.OrgUserVo;
import com.zlhj.common.core.domain.R;
import com.zlhj.common.core.utils.StringUtils;
import com.zlhj.infrastructure.po.AuthorizedImage;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.po.CreditAuthorizationFactory;
import com.zlhj.infrastructure.routing.RemoteCluePlatformService;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveDTO;
import com.zlhj.infrastructure.routing.dto.clue.JdJtClueFile;
import com.zlhj.infrastructure.routing.dto.clue.JdJtClueFileType;
import com.zlhj.mq.dto.PreApproveMessage;
import com.zlhj.unifiedInputPlatform.autoCredit.core.strategy.NotificationStrategy;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.FddRASignDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.universal.transform.FddRASignDTOAssembler;
import com.zlhj.unifiedInputPlatform.autoCredit.universal.transform.CreditAuthorizationTransform;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.PreApproveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("jdJrCreditPreApproveHandle")
public class JdJrCreditPreApproveHandle extends BaseCreditPreApproveHandle<CluePreApproveDTO> {

	@Autowired
	private RemoteCluePlatformService remoteCluePlatformService;
	@Autowired
	private CreditAuthorizationFactory creditAuthorizationFactory;

	@Override
	protected CluePreApproveDTO fetchAndValidateRemoteData(PreApproveMessage message) throws Exception {
		if (StringUtils.isBlank(message.getBoId())) {
			throw new PreApproveException("线索不能为空");
		}
		R<CluePreApproveDTO> response = remoteCluePlatformService.queryJdJrApprove(message.getBoId());
		if (200 != response.getCode()) {
			throw new PreApproveException("查询京东金融审批信息失败: " + response.getMsg());
		}
		CluePreApproveDTO preApproveDTO = response.getData();
		if (preApproveDTO == null) {
			throw new PreApproveException("查询京东金融审批信息为空");
		}
		if (StringUtils.isBlank(preApproveDTO.getUserName())) {
			throw new PreApproveException("用户姓名不能为空");
		}
		if (StringUtils.isBlank(preApproveDTO.getCertNo())) {
			throw new PreApproveException("身份证号不能为空");
		}
		if (StringUtils.isBlank(preApproveDTO.getMobile())) {
			throw new PreApproveException("手机号不能为空");
		}
		if (StringUtils.isBlank(preApproveDTO.getCarLicense())) {
			throw new PreApproveException("车牌号不能为空");
		}
		if (CollectionUtils.isEmpty(preApproveDTO.getFileList())) {
			throw new PreApproveException("文件不能为空");
		}
		return response.getData();
	}

	@Override
	protected CreditAuthorization transformToLocalAuth(CluePreApproveDTO data, PreApproveMessage message, OrgUserVo userVo) {
		String applicationNo = creditAuthorizationFactory.splicingApplicationNo();

		CreditAuthorization creditAuthorization = new CreditAuthorizationTransform().byDTO(data, applicationNo, message, userVo);
		creditAuthorization.setVehicleMileage(vehicleMileage(creditAuthorization));
		return creditAuthorization;
	}

	@Override
	protected List<AuthorizedImage> processImages(CreditAuthorization auth, CluePreApproveDTO data) throws Exception {
		List<AuthorizedImage> result = new ArrayList<>();


		List<JdJtClueFile> files = data.getFileList();

		JdJtClueFile sfzzm = files.stream().filter(f -> JdJtClueFileType.sfzzm.getCode().equals(f.getFileType())).findFirst()
				.orElseThrow(() -> new PreApproveException("身份证正面缺失"));
		JdJtClueFile sfzfm = files.stream().filter(f -> JdJtClueFileType.sfzzf.getCode().equals(f.getFileType())).findFirst()
				.orElseThrow(() -> new PreApproveException("身份证反面缺失"));
		JdJtClueFile rxzp = files.stream().filter(f -> JdJtClueFileType.rxzp.getCode().equals(f.getFileType())).findFirst()
				.orElseThrow(() -> new PreApproveException("人像照片缺失"));


		result.add(saveAndStoreImage(auth.getId(), "身份证正面", "sfzzm", fileUrl + sfzzm.getFileCode()));
		result.add(saveAndStoreImage(auth.getId(), "身份证反面", "sfzfm", fileUrl + sfzfm.getFileCode()));
		result.add(saveAndStoreImage(auth.getId(), "人像照片", "rxzp", fileUrl + rxzp.getFileCode()));

		return result;
	}

	@Override
	protected FddRASignDTO buildFddSignDTO(CreditAuthorization auth, CluePreApproveDTO data, List<AuthorizedImage> images) {

		AuthorizedImage sfzzm = images.stream().filter(i -> "sfzzm".equals(i.getFileCodeValue())).findFirst().orElse(null);
		AuthorizedImage sfzfm = images.stream().filter(i -> "sfzfm".equals(i.getFileCodeValue())).findFirst().orElse(null);
		AuthorizedImage rxzp = images.stream().filter(i -> "rxzp".equals(i.getFileCodeValue())).findFirst().orElse(null);

		return new FddRASignDTOAssembler().toDto(auth, data, sfzzm, sfzfm, rxzp);
	}

	@Override
	protected String channelName() {
		return "京东金融";
	}

	@Override
	protected NotificationStrategy notificationStrategy() {
		return creditServiceSupport.getNotificationStrategy("jdJrFinanceNotificationStrategy");
	}
}