package com.drew.accountservice.controller;

import com.drew.accountservice.dto.NetworthOutputDto;
import com.drew.accountservice.service.NetworthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/networth")
public class NetworthController {

    private final NetworthService networthService;

    public NetworthController(NetworthService networthService) {
        this.networthService = networthService;
    }

    @GetMapping
    @Operation(
            summary = "Get Users Networth",
            description = "Retrieves networth value of the User including liabilities, assets and total."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Networth retrieved successfully", content = @Content(schema = @Schema(implementation = NetworthOutputDto.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient Permissions - the requester does not have permission to view the networth"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - an unexpected error occurred while processing the request")
    })
    public ResponseEntity<NetworthOutputDto> getUserNetworth(@RequestHeader("X-User-ID") String keycloakUserId) {
        NetworthOutputDto networth = networthService.getUserNetworth(keycloakUserId);
        return ResponseEntity.ok(networth);
    }
}
