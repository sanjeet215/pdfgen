package com.doc.pdfgen;

import com.doc.pdfgen.persistence.RequestEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RequestTrackingIntegrationTest extends SpringBaseTest {
    @Autowired MockMvc mvc;
    @Autowired RequestEventRepository repository;

    @Test void recordsAnonymousClientJourneyMetadataWithoutDocumentData() throws Exception {
        mvc.perform(post("/api/tracking/event")
                .header("X-Event-Type", "TOOL_USED")
                .header("X-Original-Path", "/private-tools")
                .header("X-Visitor-Id", "visitor_12345678")
                .header("X-Session-Id", "session_12345678")
                .header("X-Screen-Width", "1440")
                .header("X-Screen-Height", "900")
                .header("X-Tool-Name", "merge")
                .header("X-Processing-Mode", "CLIENT")
                .header("X-Tool-Used", "true")
                .header("X-Next-Action", "PROCESS_COMPLETE")
                .header("Referer", "https://www.devpour.com/"))
            .andExpect(status().isNoContent());

        Object[] event=repository.recent().get(0);
        assertThat(event[0]).isInstanceOf(Long.class);
        assertThat(event[4]).isEqualTo("/private-tools");
        assertThat(event[5]).isEqualTo("TOOL_USED");
        assertThat(event[6]).isEqualTo("merge");
        assertThat(event[7]).isEqualTo("CLIENT");
        assertThat(event[8]).isEqualTo(true);
        assertThat(event[11]).isEqualTo("visitor_12345678");
        assertThat(event[12]).isEqualTo("session_12345678");
        assertThat(event[13]).isEqualTo(1440);
        assertThat(event[14]).isEqualTo(900);

        mvc.perform(post("/api/tracking/event").header("X-Event-Type", "FILE_SELECTED").header("X-Original-Path", "/private-tools").header("X-Visitor-Id", "visitor_12345678").header("X-Session-Id", "session_12345678")).andExpect(status().isNoContent());
        mvc.perform(post("/api/tracking/event").header("X-Event-Type", "DOWNLOAD").header("X-Original-Path", "/private-tools").header("X-Visitor-Id", "visitor_12345678").header("X-Session-Id", "session_12345678")).andExpect(status().isNoContent());
        assertThat(repository.visitorCount()).isEqualTo(1);
        assertThat(repository.sessionCount()).isEqualTo(1);
        assertThat(repository.toolUseCount()).isEqualTo(1);
        assertThat(repository.downloadCount()).isEqualTo(1);
        assertThat(repository.usefulVisitCount()).isEqualTo(1);
    }
}
