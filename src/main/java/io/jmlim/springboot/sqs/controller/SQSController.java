package io.jmlim.springboot.sqs.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmlim.springboot.sqs.dto.PurchaseInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/sqs")
public class SQSController {

    private final QueueMessagingTemplate queueMessagingTemplate;

    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.end-point.uri}")
    private String sqsEndPoint;

    /**
     * 구매완료 시 sqs 에 넣는 부분
     * <p>
     * test.http 안에 테스트 파일 존재
     *
     * @param purchaseInfo
     * @return
     */
    @PostMapping("/complete-purchase")
    public String send(@RequestBody PurchaseInfo purchaseInfo) {
        queueMessagingTemplate.convertAndSend(sqsEndPoint, purchaseInfo);
        return "success";
    }

    /**
     * sqs 에서 받는 부분
     *
     * @param message
     * @throws JsonProcessingException
     */
    @SqsListener("jmlim-sqs")
    public void receive(String message) throws JsonProcessingException {
        log.info("message: {}", message);
        PurchaseInfo completedPurchaseInfo = objectMapper.readValue(message, PurchaseInfo.class);
        log.info("CompletedPurchaseInfo : {} ", completedPurchaseInfo);

    }
}
