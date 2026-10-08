package cl.ridertapia.usuarioservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.usuarioservice.dto.LoginRequest;
import cl.ridertapia.usuarioservice.dto.RecuperarPasswordRequest;
import cl.ridertapia.usuarioservice.dto.UsuarioRequest;
import cl.ridertapia.usuarioservice.dto.UsuarioUpdateRequest;
import cl.ridertapia.usuarioservice.model.Usuario;
import cl.ridertapia.usuarioservice.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase // reemplaza MySQL por H2 solo en esta prueba
class UsuarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiar() {
        usuarioRepository.deleteAll();
    }

    @Test
    void testCrearYObtenerUsuario() throws Exception {
        UsuarioRequest usuario = new UsuarioRequest("12345678-5", "matias@correo.cl", "password123", "CLIENTE");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rutUsuario").value("12345678-5"))
                .andExpect(jsonPath("$.emailCliente").value("matias@correo.cl"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$._links").exists());

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.usuarios", hasSize(1)))
                .andExpect(jsonPath("$._embedded.usuarios[0].rutUsuario").value("12345678-5"))
                .andExpect(jsonPath("$._embedded.usuarios[0].emailCliente").value("matias@correo.cl"))
                .andExpect(jsonPath("$._embedded.usuarios[0].rol").value("CLIENTE"))
                .andExpect(jsonPath("$._embedded.usuarios[0].password").doesNotExist())
                .andExpect(jsonPath("$._links.self").exists());

        // La clave quedó en la BD cifrada con BCrypt, nunca en texto plano
        Usuario enBd = usuarioRepository.findById("12345678-5").orElseThrow();
        assertThat(enBd.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", enBd.getPassword())).isTrue();
    }

    @Test
    void testObtenerPorRutYPorEmail() throws Exception {
        usuarioRepository.save(Usuario.builder().rutUsuario("11111111-1").emailCliente("maria.gomez@example.com")
                .password(passwordEncoder.encode("password123")).rol("ADMIN").build());

        mockMvc.perform(get("/api/v1/usuarios/11111111-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailCliente").value("maria.gomez@example.com"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$._links.self").exists());

        mockMvc.perform(get("/api/v1/usuarios/buscar").param("email", "MARIA.GOMEZ@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rutUsuario").value("11111111-1"));
    }

    @Test
    void testObtenerUsuarioInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/12345678-5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje", containsString("12345678-5")));
    }

    @Test
    void testActualizarUsuario() throws Exception {
        usuarioRepository.save(Usuario.builder().rutUsuario("12342313-5").emailCliente("carlos.lopez@example.com")
                .password(passwordEncoder.encode("password123")).rol("CLIENTE").build());

        UsuarioUpdateRequest actualizado = new UsuarioUpdateRequest("carlos.lopez@example.com", "password423", "ADMIN");

        mockMvc.perform(put("/api/v1/usuarios/12342313-5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$._links").exists());

        Usuario enBd = usuarioRepository.findById("12342313-5").orElseThrow();
        assertThat(enBd.getRol()).isEqualTo("ADMIN");
        assertThat(passwordEncoder.matches("password423", enBd.getPassword())).isTrue();
    }

    @Test
    void testCrearUsuarioDuplicado() throws Exception {
        UsuarioRequest usuario = new UsuarioRequest("12345678-5", "juan.perez@example.com", "password123", null);
        String json = objectMapper.writeValueAsString(usuario);

        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("12345678-5")));

        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    void testCrearUsuarioConRutInvalido() throws Exception {
        UsuarioRequest usuario = new UsuarioRequest("12345678-9", "juan.perez@example.com", "password123", null);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("RUT")));

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void testIniciarSesion() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UsuarioRequest("12345678-5", "juan.perez@example.com", "password123", null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("juan.perez@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rutUsuario").value("12345678-5"))
                .andExpect(jsonPath("$._links").exists());

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("juan.perez@example.com", "incorrecta"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Email o contraseña incorrectos"));
    }

    @Test
    void testRecuperarPassword() throws Exception {
        usuarioRepository.save(Usuario.builder().rutUsuario("12345678-5").emailCliente("juan.perez@example.com")
                .password(passwordEncoder.encode("password123")).rol("CLIENTE").build());

        mockMvc.perform(post("/api/v1/usuarios/recuperar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RecuperarPasswordRequest("juan.perez@example.com", "claveNueva123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada correctamente"));

        // La clave nueva funciona y la anterior ya no
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("juan.perez@example.com", "claveNueva123"))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("juan.perez@example.com", "password123"))))
                .andExpect(status().isUnauthorized());
    }
}