package io.jmlim.springboot.sqs.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User: Jeongmuk Lim
 */
@Getter
@ToString
@NoArgsConstructor
public class PurchaseInfo {
    private String uuid = UUID.randomUUID().toString().replace("-", "");
    private String userId;
    private String itemName;
    private Double purchasePrice;

    @Builder
    public PurchaseInfo(String userId, String itemName, Double purchasePrice) {
        this.userId = userId;
        this.itemName = itemName;
        this.purchasePrice = purchasePrice;
    }

    public String toCsvString() {
        return uuid + "," + userId + "," + itemName + "," + purchasePrice + "," + LocalDateTime.now();
    }

}
