package com.pss.SRAS.controllers;

import com.pss.SRAS.dto.MatchingResultDto;
import com.pss.SRAS.services.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROJECT_MANAGER')")
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * GET /matching/{projectId}?k=10
     * Returns top-K employees ranked by matching score for the given project.
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<List<MatchingResultDto>> getTopK(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "10") int k) {
        return ResponseEntity.ok(matchingService.getTopKForProject(projectId, k));
    }
}
