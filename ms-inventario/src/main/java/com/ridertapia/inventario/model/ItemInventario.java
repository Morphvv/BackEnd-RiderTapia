package com.ridertapia.inventario.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items_inventario")
@JsonIgnoreProperties(value = {"stockTotal", "estadoStock"}, allowGetters = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItemInventario;

    @Column(nullable = false, unique = true)
    private Long idVariante;

    @Column(nullable = false)
    private Integer stockDisponible;

    @Column(nullable = false)
    private Integer stockReservado;

    @Column(nullable = false)
    private Integer stockMinimo;

    @Column(nullable = false)
    private Integer costoPromedio;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "items_inventario_proveedores", joinColumns = @JoinColumn(name = "id_item_inventario"))
    @Column(name = "id_proveedor")
    private List<Long> idProveedores = new ArrayList<>();

    // Stock total = disponible + reservado (HU-77)
    public int getStockTotal() {
        return (stockDisponible == null ? 0 : stockDisponible) + (stockReservado == null ? 0 : stockReservado);
    }

    // Estado del stock según el disponible y el mínimo (HU-71 y HU-76)
    public EstadoStock getEstadoStock() {
        int disponible = stockDisponible == null ? 0 : stockDisponible;
        int minimo = stockMinimo == null ? 0 : stockMinimo;
        if (disponible == 0) {
            return EstadoStock.SIN_STOCK;
        }
        return disponible < minimo ? EstadoStock.BAJO_MINIMO : EstadoStock.CON_STOCK;
    }
}
