package com.disposableemail.rest.controller;

import com.disposableemail.dao.entity.SourceEntity;
import com.disposableemail.service.api.MessageService;
import com.disposableemail.service.api.SourceService;
import io.netty.buffer.ByteBufAllocator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${openapi.disposableEmailProject.base-path:/api/v1}")
public class SourceDownloadController {

    private final SourceService sourceService;
    private final MessageService messageService;
    private final DataBufferFactory dataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/messages/{id}/download", produces = APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Mono<DataBuffer>>> download(@PathVariable String id, ServerWebExchange exchange) {

        return Mono.fromCallable(() -> {
            var body = messageService.getMessage(id, exchange)
                    .flatMap(messageEntity -> {
                        log.info("Retrieved Message: {}", messageEntity.toString());
                        return sourceService.getSourceByMsgId(messageEntity.getMsgid());
                    })
                    .map(SourceEntity::getMsgid)
                    .flatMap(sourceService::downloadSource)
                    .map(dataBufferFactory::wrap);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".eml\"")
                    .body(body);
        });
    }
}
