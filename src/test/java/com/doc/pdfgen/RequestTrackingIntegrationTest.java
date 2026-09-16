package com.doc.pdfgen;

import com.doc.pdfgen.persistence.RequestEventRepository;
import com.doc.pdfgen.security.TenantPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestPropertySource(properties="stats.service-key=test-admin-device-key")
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

        mvc.perform(post("/api/tracking/event").header("X-Event-Type", "PAGE_VIEW").header("X-Original-Path", "/merge-pdf").header("X-Visitor-Id", "visitor_12345678").header("X-Session-Id", "session_87654321")).andExpect(status().isNoContent());
        assertThat(repository.returningVisitorIds()).containsExactly("visitor_12345678");
    }

    @Test void doesNotCountTheProtectedStatsApiAsVisitorTraffic() throws Exception {
        long before = repository.count();
        mvc.perform(get("/api/stats")).andExpect(status().isForbidden());
        assertThat(repository.count()).isEqualTo(before);
    }

    @Test void doesNotCountRequestsFromAnAuthenticatedAdminDevice() throws Exception {
        long before = repository.count();
        mvc.perform(post("/api/tracking/event")
                .header("X-Event-Type", "PAGE_VIEW")
                .header("X-Visitor-Id", "admin_visitor_123")
                .header("X-Session-Id", "admin_session_123")
                .header("X-Admin-Device-Key", "test-admin-device-key"))
            .andExpect(status().isNoContent());
        assertThat(repository.count()).isEqualTo(before);
    }

    @Test void recordsInternalUserIdForAuthenticatedTraffic() throws Exception {
        TenantPrincipal principal = new TenantPrincipal(
                42L, 7L, "Test User", "test@example.com", "password", "TENANT_ADMIN", true);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, principal.getPassword(), principal.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        mvc.perform(post("/api/tracking/event")
                .session(session)
                .header("X-Event-Type", "PAGE_VIEW")
                .header("X-Visitor-Id", "signed_in_visitor")
                .header("X-Session-Id", "signed_in_session"))
            .andExpect(status().isNoContent());

        Object[] event = repository.recent().get(0);
        assertThat(event[19]).isEqualTo(7L);
        assertThat(event[20]).isEqualTo(42L);
    }
}
