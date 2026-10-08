package cl.ridertapia.clienteservice.service;

import cl.ridertapia.clienteservice.client.UsuarioClient;
import cl.ridertapia.clienteservice.dto.ClienteRequest;
import cl.ridertapia.clienteservice.dto.ClienteResponse;
import cl.ridertapia.clienteservice.dto.ClienteUpdateRequest;
import cl.ridertapia.clienteservice.dto.UsuarioDto;
import cl.ridertapia.clienteservice.exception.ApiException;
import cl.ridertapia.clienteservice.model.Cliente;
import cl.ridertapia.clienteservice.repository.ClienteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final ClienteRepository clienteRepository;
    private final UsuarioClient usuarioClient;

    @Transactional
    public ClienteResponse guardarCliente(ClienteRequest request) {
        String rut = normalizar(request.rutUsuario());

        if (clienteRepository.existsById(rut)) {
            throw ApiException.conflicto("Ya existe un perfil de cliente con el RUT " + rut);
        }

        // El perfil solo puede crearse si el RUT tiene una cuenta con rol CLIENTE
        UsuarioDto cuenta = usuarioClient.obtenerPorRut(rut);
        if (!ROL_CLIENTE.equals(cuenta.rol())) {
            throw ApiException.solicitudInvalida("La cuenta con el RUT " + rut + " no tiene rol CLIENTE");
        }

        Cliente cliente = Cliente.builder()
                .rutUsuario(rut)
                .nombre(request.nombre().trim())
                .apellido(request.apellido().trim())
                .telefono(request.telefono())
                .direccion(request.direccion())
                .comuna(request.comuna())
                .build();
        return ClienteResponse.desde(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarClientes() {
        return clienteRepository.findAll().stream().map(ClienteResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse findByRut(String rutUsuario) {
        return ClienteResponse.desde(buscarPorRut(rutUsuario));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorNombre(String texto) {
        String filtro = texto.trim();
        return clienteRepository
                .findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(filtro, filtro)
                .stream()
                .map(ClienteResponse::desde)
                .toList();
    }

    @Transactional
    public ClienteResponse actualizarDatos(String rutUsuario, ClienteUpdateRequest request) {
        Cliente cliente = buscarPorRut(rutUsuario);
        cliente.setNombre(request.nombre().trim());
        cliente.setApellido(request.apellido().trim());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());
        cliente.setComuna(request.comuna());
        return ClienteResponse.desde(clienteRepository.save(cliente));
    }

    private Cliente buscarPorRut(String rutUsuario) {
        String rut = normalizar(rutUsuario);
        return clienteRepository.findById(rut)
                .orElseThrow(() -> ApiException.noEncontrado("No existe un cliente con el RUT " + rut));
    }

    private String normalizar(String rut) {
        return rut.trim().toUpperCase();
    }
}
