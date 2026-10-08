package cl.ridertapia.usuarioservice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.usuarioservice.dto.LoginRequest;
import cl.ridertapia.usuarioservice.dto.RecuperarPasswordRequest;
import cl.ridertapia.usuarioservice.dto.UsuarioRequest;
import cl.ridertapia.usuarioservice.dto.UsuarioResponse;
import cl.ridertapia.usuarioservice.dto.UsuarioUpdateRequest;
import cl.ridertapia.usuarioservice.exception.ApiException;
import cl.ridertapia.usuarioservice.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Prueba la capa web (rutas, validación, errores y enlaces HATEOAS) con el servicio simulado. */
@WebMvcTest(UsuarioController.class)
@Import(UsuarioModelAssembler.class)
class UsuarioControllerTest {

    private static final String RUT = "12345678-5";
    private static final String EMAIL = "matias@correo.cl";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    private UsuarioResponse respuesta() {
        return new UsuarioResponse(RUT, EMAIL, "CLIENTE");
    }

    @Test
    @DisplayName("POST /api/v1/usuarios → 201, Location y enlace self")
    void guardarUsuario_ok() throws Exception {
        when(usuarioService.guardarUsuario(any(UsuarioRequest.class))).thenReturn(respuesta());
        UsuarioRequest request = new UsuarioRequest(RUT, EMAIL, "clave12345", null);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/usuarios/" + RUT)))
                .andExpect(jsonPath("$.rutUsuario").value(RUT))
                .andExpect(jsonPath("$.emailCliente").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/usuarios/" + RUT)))
                .andExpect(jsonPath("$._links.usuarios.href", endsWith("/api/v1/usuarios")));
    }

    @Test
    @DisplayName("POST /api/v1/usuarios con datos inválidos → 400 con el detalle de cada campo")
    void guardarUsuario_validacion() throws Exception {
        UsuarioRequest request = new UsuarioRequest("123", "no-es-email", "corta", "SUPERADMIN");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje", containsString("rutUsuario")))
                .andExpect(jsonPath("$.mensaje", containsString("emailCliente")))
                .andExpect(jsonPath("$.mensaje", containsString("password")))
                .andExpect(jsonPath("$.mensaje", containsString("rol")));
    }

    @Test
    @DisplayName("POST /api/v1/usuarios con RUT repetido → 409")
    void guardarUsuario_conflicto() throws Exception {
        when(usuarioService.guardarUsuario(any(UsuarioRequest.class)))
                .thenThrow(ApiException.conflicto("Ya existe un usuario con el RUT " + RUT));
        UsuarioRequest request = new UsuarioRequest(RUT, EMAIL, "clave12345", null);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString(RUT)));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios → colección HAL con enlaces por usuario")
    void listarUsuarios() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(List.of(
                respuesta(), new UsuarioResponse("11111111-1", "ana@correo.cl", "ADMIN")));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.usuarios", hasSize(2)))
                .andExpect(jsonPath("$._embedded.usuarios[0].rutUsuario").value(RUT))
                .andExpect(jsonPath("$._embedded.usuarios[0]._links.self.href",
                        endsWith("/api/v1/usuarios/" + RUT)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/usuarios")));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/{rut} → 200 con enlaces")
    void findByRut_ok() throws Exception {
        when(usuarioService.findByRut(RUT)).thenReturn(respuesta());

        mockMvc.perform(get("/api/v1/usuarios/{rut}", RUT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/usuarios/" + RUT)));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/{rut} inexistente → 404")
    void findByRut_noExiste() throws Exception {
        when(usuarioService.findByRut(RUT))
                .thenThrow(ApiException.noEncontrado("No existe un usuario con el RUT " + RUT));

        mockMvc.perform(get("/api/v1/usuarios/{rut}", RUT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje", containsString(RUT)));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/buscar?email= → 200")
    void findByEmail() throws Exception {
        when(usuarioService.findByEmailCliente(EMAIL)).thenReturn(respuesta());

        mockMvc.perform(get("/api/v1/usuarios/buscar").param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailCliente").value(EMAIL));
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/{rut} → 200 con el usuario actualizado")
    void actualizarUsuario_ok() throws Exception {
        when(usuarioService.actualizarUsuario(eq(RUT), any(UsuarioUpdateRequest.class)))
                .thenReturn(new UsuarioResponse(RUT, "nuevo@correo.cl", "ADMIN"));
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("nuevo@correo.cl", null, "ADMIN");

        mockMvc.perform(put("/api/v1/usuarios/{rut}", RUT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailCliente").value("nuevo@correo.cl"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/{rut} con email inválido → 400")
    void actualizarUsuario_validacion() throws Exception {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("malo", null, null);

        mockMvc.perform(put("/api/v1/usuarios/{rut}", RUT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("emailCliente")));
    }

    @Test
    @DisplayName("POST /api/v1/usuarios/login correcto → 200")
    void login_ok() throws Exception {
        when(usuarioService.iniciarSesion(any(LoginRequest.class))).thenReturn(respuesta());

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(EMAIL, "clave12345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rutUsuario").value(RUT))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/usuarios/login con credenciales malas → 401")
    void login_noAutorizado() throws Exception {
        when(usuarioService.iniciarSesion(any(LoginRequest.class)))
                .thenThrow(ApiException.noAutorizado("Email o contraseña incorrectos"));

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(EMAIL, "mala"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Email o contraseña incorrectos"));
    }

    @Test
    @DisplayName("POST /api/v1/usuarios/login sin campos → 400")
    void login_validacion() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/usuarios/recuperar-password → 200 con mensaje")
    void recuperarPassword_ok() throws Exception {
        RecuperarPasswordRequest request = new RecuperarPasswordRequest(EMAIL, "nuevaClave123");

        mockMvc.perform(post("/api/v1/usuarios/recuperar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada correctamente"));

        verify(usuarioService).recuperarPassword(any(RecuperarPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/usuarios/recuperar-password con clave corta → 400")
    void recuperarPassword_validacion() throws Exception {
        RecuperarPasswordRequest request = new RecuperarPasswordRequest(EMAIL, "corta");

        mockMvc.perform(post("/api/v1/usuarios/recuperar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("nuevaPassword")));
    }
}
