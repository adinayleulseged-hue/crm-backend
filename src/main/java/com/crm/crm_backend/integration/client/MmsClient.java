package com.crm.crm_backend.integration.client;

import com.crm.crm_backend.config.properties.IntegrationProperties;
import com.crm.crm_backend.integration.dto.MmsItemDTO;
import com.crm.crm_backend.integration.dto.MmsStockDTO;
import com.crm.crm_backend.integration.fallback.MmsClientFallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MmsClient {

    private final WebClient.Builder webClientBuilder;
    private final IntegrationProperties integrationProperties;
    private final MmsClientFallback mmsClientFallback;

    public Optional<MmsItemDTO> getItemBySku(String sku) {
        if (!integrationProperties.getMms().isEnabled()) {
            return Optional.ofNullable(mmsClientFallback.fallbackItem(sku));
        }
        try {
            MmsItemDTO item = webClientBuilder.build()
                    .get()
                    .uri(integrationProperties.getMms().getBaseUrl() + "/api/mms/items/sku/{sku}", sku)
                    .retrieve()
                    .bodyToMono(MmsItemDTO.class)
                    .block();
            return Optional.ofNullable(item);
        } catch (Exception ex) {
            log.warn("MMS getItemBySku failed for {}: {}", sku, ex.getMessage());
            return Optional.ofNullable(mmsClientFallback.fallbackItem(sku));
        }
    }

    public Optional<MmsStockDTO> getStock(String sku) {
        if (!integrationProperties.getMms().isEnabled()) {
            return Optional.ofNullable(mmsClientFallback.fallbackStock(sku));
        }
        try {
            MmsStockDTO stock = webClientBuilder.build()
                    .get()
                    .uri(integrationProperties.getMms().getBaseUrl() + "/api/mms/stock/{sku}", sku)
                    .retrieve()
                    .bodyToMono(MmsStockDTO.class)
                    .block();
            return Optional.ofNullable(stock);
        } catch (Exception ex) {
            log.warn("MMS getStock failed for {}: {}", sku, ex.getMessage());
            return Optional.ofNullable(mmsClientFallback.fallbackStock(sku));
        }
    }

    public boolean reserveStock(String sku, BigDecimal quantity, String orderReference) {
        if (!integrationProperties.getMms().isEnabled()) {
            return mmsClientFallback.fallbackReserve(sku, quantity, orderReference);
        }
        try {
            webClientBuilder.build()
                    .post()
                    .uri(integrationProperties.getMms().getBaseUrl() + "/api/mms/stock/reserve")
                    .bodyValue(Map.of(
                            "sku", sku,
                            "quantity", quantity,
                            "orderReference", orderReference
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception ex) {
            log.warn("MMS reserveStock failed for {} qty {}: {}", sku, quantity, ex.getMessage());
            return mmsClientFallback.fallbackReserve(sku, quantity, orderReference);
        }
    }
}
