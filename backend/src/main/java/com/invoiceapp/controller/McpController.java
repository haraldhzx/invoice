package com.invoiceapp.controller;

import com.invoiceapp.model.dto.McpQueryRequest;
import com.invoiceapp.model.dto.McpQueryResponse;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.repository.UserRepository;
import com.invoiceapp.service.McpQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
@Tag(name = "MCP Query Interface", description = "Natural language query endpoints for spending analysis")
public class McpController {

    private final McpQueryService mcpQueryService;
    private final UserRepository userRepository;

    @PostMapping("/query")
    @Operation(summary = "Process natural language query",
            description = "Ask questions about your spending in natural language")
    public ResponseEntity<McpQueryResponse> processQuery(
            @Valid @RequestBody McpQueryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        McpQueryResponse response = mcpQueryService.processQuery(request.getQuery(), user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/examples")
    @Operation(summary = "Get example queries",
            description = "Get list of example natural language queries")
    public ResponseEntity<List<String>> getExamples() {
        List<String> examples = List.of(
                "How much did I spend on sweets?",
                "How much are house basic costs?",
                "How much was spent for the kids?",
                "What's my total spending this month?",
                "Show me a breakdown of all categories",
                "How much did I spend on Food & Dining last month?",
                "What did I spend on Transportation this year?",
                "Show me my spending for groceries",
                "What are my biggest expenses?",
                "How much did I spend in total?"
        );
        return ResponseEntity.ok(examples);
    }

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
