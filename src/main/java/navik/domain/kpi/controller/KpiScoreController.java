package navik.domain.kpi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO.Initialize;
import navik.domain.kpi.service.command.KpiScoreInitialService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/kpi-scores")
public class KpiScoreController {

    private final KpiScoreInitialService kpiScoreInitialService;

    @PutMapping("/initialize")
    public ApiResponse<Initialize> initialize(
            @AuthUser Long userId,
            @Valid @RequestBody KpiScoreRequestDTO.Initialize request
    ) {
        KpiScoreResponseDTO.Initialize response =
                kpiScoreInitialService.initializeKpiScores(userId, request);

        return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, response);
    }

}
