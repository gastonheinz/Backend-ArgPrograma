package com.portafolio.SpringBoot;

import com.portafolio.SpringBoot.security.Entity.Rol;
import com.portafolio.SpringBoot.security.Repository.iRolRepository;
import com.portafolio.SpringBoot.security.Repository.iUsuarioRepository;
import com.portafolio.SpringBoot.security.enums.RolNombre;
import com.portafolio.SpringBoot.security.jwt.JwtProvider;
import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityRegressionTests {
    @Autowired MockMvc mvc;
    @Autowired iRolRepository roles;
    @Autowired iUsuarioRepository users;
    @Autowired JwtProvider jwt;

    @DynamicPropertySource
    static void secrets(DynamicPropertyRegistry properties) {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        String encoded = Base64.getEncoder().encodeToString(key);
        properties.add("jwt.secret", () -> encoded);
    }

    @BeforeEach
    void seedRole() {
        if (roles.findByRolNombre(RolNombre.ROLE_USER).isEmpty()) {
            roles.save(new Rol(RolNombre.ROLE_USER));
        }
    }

    @Test
    void anonymousCannotRegisterOrWrite() throws Exception {
        mvc.perform(post("/auth/nuevo").contentType(MediaType.APPLICATION_JSON).content(registration()))
            .andExpect(status().isUnauthorized());
        mvc.perform(put("/personas/update/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void ordinaryUserCannotRegisterOrWrite() throws Exception {
        mvc.perform(post("/auth/nuevo").with(user("reader").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON).content(registration())).andExpect(status().isForbidden());
        mvc.perform(delete("/educacion/delete/1").with(user("reader").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotAssignAdminRoleThroughRegistrationBody() throws Exception {
        mvc.perform(post("/auth/nuevo").with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content(registration())).andExpect(status().isCreated());
        var created = users.findByNombreUsuario("newuser").orElseThrow();
        assertEquals(1, created.getRoles().size());
        assertEquals(RolNombre.ROLE_USER, created.getRoles().iterator().next().getRolNombre());
        assertNotEquals("Long-password-2026", created.getPassword());
    }

    @Test
    void portfolioReadsRemainPublic() throws Exception {
        mvc.perform(get("/educacion/lista")).andExpect(status().isOk());
        mvc.perform(get("/personas/lista")).andExpect(status().isOk());
    }

    @Test
    void jwtRejectsMalformedAndForeignTokensAndWeakSecrets() {
        String token = jwt.generateToken(new UsernamePasswordAuthenticationToken("admin", "unused"));
        assertTrue(jwt.validateToken(token));
        assertEquals("admin", jwt.getNombreUsuarioFromToken(token));
        assertFalse(jwt.validateToken("not-a-token"));
        assertFalse(jwt.validateToken(null));
        byte[] otherKey = new byte[64];
        new SecureRandom().nextBytes(otherKey);
        var other = new JwtProvider(Base64.getEncoder().encodeToString(otherKey), 3600);
        assertFalse(other.validateToken(token));
        assertThrows(IllegalArgumentException.class, () -> new JwtProvider("c2VjcmV0", 3600));
    }

    private String registration() {
        return """
            {"nombre":"New User","nombreUsuario":"newuser","email":"new@example.com",
             "password":"Long-password-2026","roles":["admin"]}
            """;
    }
}
