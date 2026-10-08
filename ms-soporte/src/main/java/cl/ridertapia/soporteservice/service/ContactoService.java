package cl.ridertapia.soporteservice.service;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.ContactoDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Contacto;
import cl.ridertapia.soporteservice.repository.ContactoRepository;
import cl.ridertapia.soporteservice.util.RutUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactoService {

    private final ContactoRepository contactoRepository;
    private final UsuarioClient usuarioClient;

    @Transactional
    public ContactoDto.Respuesta enviarContacto(ContactoDto.Crear request) {
        // El RUT es opcional: un visitante sin cuenta también puede escribir
        String rut = null;
        if (request.rutUsuario() != null && !request.rutUsuario().isBlank()) {
            rut = RutUtil.normalizar(request.rutUsuario());
            usuarioClient.obtenerPorRut(rut);
        }

        Contacto contacto = Contacto.builder()
                .rutUsuario(rut)
                .nombre(request.nombre().trim())
                .emailContacto(request.emailContacto().trim().toLowerCase())
                .asunto(request.asunto().trim())
                .mensaje(request.mensaje().trim())
                .estado(Contacto.Estado.ABIERTO)
                .fecha(LocalDateTime.now())
                .build();
        return ContactoDto.Respuesta.desde(contactoRepository.save(contacto));
    }

    /** Lista todos los mensajes, o solo los de un estado si se indica (findByEstado). */
    @Transactional(readOnly = true)
    public List<ContactoDto.Respuesta> listarContactos(Contacto.Estado estado) {
        List<Contacto> contactos = estado == null
                ? contactoRepository.findAll()
                : contactoRepository.findByEstado(estado);
        return contactos.stream().map(ContactoDto.Respuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public ContactoDto.Respuesta findById(Long idContacto) {
        return ContactoDto.Respuesta.desde(buscar(idContacto));
    }

    @Transactional
    public ContactoDto.Respuesta responderContacto(Long idContacto, ContactoDto.Responder request) {
        Contacto contacto = buscar(idContacto);
        contacto.setRespuesta(request.respuesta().trim());
        contacto.setEstado(Contacto.Estado.RESPONDIDO);
        return ContactoDto.Respuesta.desde(contactoRepository.save(contacto));
    }

    private Contacto buscar(Long idContacto) {
        return contactoRepository.findById(idContacto)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el mensaje de contacto " + idContacto));
    }
}
