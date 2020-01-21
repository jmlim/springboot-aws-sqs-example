package io.jmlim.springboot.sqs.dto;

import lombok.*;

/**
 * User: Jeongmuk Lim
 */
@Getter
@ToString
@NoArgsConstructor
public class PurchaseInfo {
    private String userId;
    private String itemName;
    private Double purchasePrice;

    @Builder
    public PurchaseInfo(String userId, String itemName, Double purchasePrice) {
        this.userId = userId;
        this.itemName = itemName;
        this.purchasePrice = purchasePrice;
    }
}
