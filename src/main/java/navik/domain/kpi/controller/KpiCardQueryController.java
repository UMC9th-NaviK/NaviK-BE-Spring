package navik.domain.kpi.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
import navik.domain.kpi.service.query.KpiCardQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/kpi-cards")
public class KpiCardQueryController {

    private final KpiCardQueryService kpiCardQueryService;

    @GetMapping
    public ApiResponse<List<GridItem>> getKpiCards(@RequestParam Long jobId) {
        List<GridItem> cards = kpiCardQueryService.getAllCardsByJob(jobId);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, cards);
    }

}
