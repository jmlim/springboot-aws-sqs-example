package io.jmlim.springboot.sqs.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmlim.springboot.sqs.dto.PurchaseInfo;
import io.jmlim.springboot.sqs.util.CsvUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
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
        CsvUtils.csvWriter("request-log", purchaseInfo.toCsvString());
        // https://stackoverflow.com/questions/44886026/spring-cloud-aws-send-message-to-fifo-queue
        // https://docs.aws.amazon.com/ko_kr/AWSSimpleQueueService/latest/SQSDeveloperGuide/using-messagegroupid-property.html
        // 메시지 그룹 ID 특정 메시지 그룹에 속한 메시지를 지정하는 태그. 동일한 메시지 그룹에 속한 메시지는 메시지 그룹에 따라 엄격한 순서로 항상 하나씩 처리됨.
        // (단, 서로 다른 그룹에 속한 메시지는 순서를 따르지 않고 처리될 수 있음).
 //       Map<String, Object> headers = new HashMap<>();
//        headers.put("message-group-id", "JMLIM_FIFO_GROUP_ID");
 //       headers.put("message-deduplication-id", purchaseInfo.getUuid());
        queueMessagingTemplate.convertAndSend(sqsEndPoint, purchaseInfo);//, headers);
        return "success";
    }

    /**
     * sqs 에서 받는 부분
     *
     * @param message
     * @throws JsonProcessingException
     */
    @SqsListener(value = "jmlim-sqs.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void receive(String message) throws Exception {

        PurchaseInfo completedPurchaseInfo = objectMapper.readValue(message, PurchaseInfo.class);
        log.info("CompletedPurchaseInfo : {} ", completedPurchaseInfo);
        CsvUtils.csvWriter("receive-log", completedPurchaseInfo.toCsvString());
    }
}
